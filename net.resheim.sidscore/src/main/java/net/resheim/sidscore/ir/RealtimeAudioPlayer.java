/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Torkild Ulvøy Resheim <torkildr@gmail.com> - initial API and implementation
 */
package net.resheim.sidscore.ir;

import net.resheim.sidscore.sid.SidModel;
import net.resheim.sidscore.sid.SidWaveforms;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Realtime mock player for TimedScore via javax.sound.sampled.
 *
 * Emulation model (digital SID 6581/8580, approximate; no filter/analog):
 * <ul>
 * <li>16-bit frequency register added each clock to a 24-bit phase accumulator
 * (frequency formula uses 2^24 scaling). We derive waveforms from the
 * accumulator.</li>
 * <li>Pulse width is 12-bit (PW11-0); $0800 is 50% duty.</li>
 * <li>Noise uses a 23-bit LFSR; bit0 = bit22 XOR bit17. Output taps are
 * {20,18,14,11,9,5,2,0} widened to 12-bit.</li>
 * <li>ADSR rates use the measured MOS 6581 timing table (2 ms..8 s attack, 6 ms..24 s
 * decay/release).</li>
 * <li>Sustain levels are 16 steps (0..15).</li>
 * <li>Combined waveforms use clean-room tables (or optional external tables).</li>
 * <li>SYNC resets the oscillator when the modulator MSB rises (V1<-V3, V2<-V1,
 * V3<-V2).</li>
 * <li>RING mod flips the triangle sign by modulator MSB (approximate XOR of MSB).</li>
 * </ul>
 * <p>
 * Not emulated: analog distortion/nonlinearity and exact filter response.
 * The programmable filter is approximated with a simple digital SVF.
 * </p>
 *
 * References:
 * <ul>
 * <li>Oxyron SID register reference (frequency formula, pulse width, noise
 * LFSR/taps): https://www.oxyron.de/html/registers_sid.html - MOS 6581</li>
 * <li>SID datasheet (ADSR timing table, sustain levels, sync/ring-mod
 * features): https://www.cpcwiki.eu/imgs/9/9d/Mos_6581_sid.pdf</li>
 * </ul>
 */
public final class RealtimeAudioPlayer {

	private static final float SAMPLE_RATE = 44100f;
	private static final int BUFFER_SAMPLES = 512;
	private static final double SID_CLOCK_NTSC = 1022727.0;
	private static final double SID_CLOCK_PAL = 985248.0;
	private static final double RASTER_RATE_PAL = 50.124542;
	private static final double RASTER_RATE_NTSC = 60.098814;
	private static final double MIX_GAIN = 0.25;
	private static final int OVERSAMPLE_BASE = 2;
	private static final int OVERSAMPLE_RING = 4;
	// Output stage reconstruction (not the SID programmable filter).
	private static final double OUTPUT_LP_HZ = 12000.0;
	private static final SIDScoreIR.InstrumentIR SILENT_INSTR = new SIDScoreIR.InstrumentIR("silence", 0,
			new SIDScoreIR.AdsrIR(0, 0, 0, 0), OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), 0,
			Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), 0, OptionalInt.empty(),
			OptionalInt.empty(), Optional.empty(), SIDScoreIR.InstrumentGateMode.RETRIGGER, 0, false, false);
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);
	private final SidModel sidModel;
	private final SidWaveforms.TableSet waveTables;

	public RealtimeAudioPlayer() {
		this(SidModel.MOS6581, null);
	}

	public RealtimeAudioPlayer(SidModel model) {
		this(model, null);
	}

	public RealtimeAudioPlayer(SidModel model, Path waveformsPath) {
		this.sidModel = model != null ? model : SidModel.MOS6581;
		this.waveTables = SidWaveforms.loadTables(this.sidModel, waveformsPath);
	}

	public interface SampleListener {
		void onSamples(float[] voice1, float[] voice2, float[] voice3, int length, float sampleRate);
	}

	public void play(SIDScoreIR.TimedScore score) throws LineUnavailableException {
		play(score, null, null);
	}

	public void play(SIDScoreIR.TimedScore score, Path wavOut) throws LineUnavailableException {
		render(score, wavOut, true, null);
	}

	public void play(SIDScoreIR.TimedScore score, SampleListener listener) throws LineUnavailableException {
		play(score, null, listener);
	}

	public void play(SIDScoreIR.TimedScore score, Path wavOut, SampleListener listener) throws LineUnavailableException {
		render(score, wavOut, true, listener);
	}

	public void renderToWav(SIDScoreIR.TimedScore score, Path wavOut) {
		renderToWav(score, wavOut, null);
	}

	public void renderToWav(SIDScoreIR.TimedScore score, Path wavOut, SampleListener listener) {
		if (wavOut == null) {
			throw new IllegalArgumentException("wavOut is required");
		}
		try {
			render(score, wavOut, false, listener);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		stopRequested.set(true);
	}

	private void render(SIDScoreIR.TimedScore score, Path wavOut, boolean playAudio, SampleListener listener)
			throws LineUnavailableException {
		stopRequested.set(false);
		AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
		SourceDataLine line = null;
		if (playAudio) {
			line = AudioSystem.getSourceDataLine(fmt);
			line.open(fmt, BUFFER_SAMPLES * 2);
			line.start();
		}

		double sidClockHz = score.system() == SIDScoreIR.VideoSystem.NTSC ? SID_CLOCK_NTSC : SID_CLOCK_PAL;
		double frameRate = score.system() == SIDScoreIR.VideoSystem.NTSC ? RASTER_RATE_NTSC : RASTER_RATE_PAL;

		FilterRuntime filter = new FilterRuntime(frameRate);
		VoiceRuntime[] vr = new VoiceRuntime[3];
		for (int i = 0; i < 3; i++) {
			var tv = score.voices().get(i + 1);
			if (tv == null) {
				vr[i] = new VoiceRuntime(SILENT_INSTR, FrameEventCompiler.compileVoice(null, score), sidClockHz,
						frameRate, score.tables(), filter, waveTables);
			} else {
				vr[i] = new VoiceRuntime(tv.instrument(), FrameEventCompiler.compileVoice(tv, score), sidClockHz,
						frameRate, score.tables(), filter, waveTables);
			}
		}

		byte[] buf = new byte[BUFFER_SAMPLES * 2];
		ByteArrayOutputStream wavBuffer = wavOut != null ? new ByteArrayOutputStream() : null;
		float[][] voiceBuf = listener != null ? new float[3][BUFFER_SAMPLES] : null;
		double[] voiceMix = listener != null ? new double[3] : null;

		boolean[] msb = new boolean[3];
		boolean[] rise = new boolean[3];
		int[] modIndex = new int[] { 2, 0, 1 };

		int oversample = OVERSAMPLE_BASE;
		for (int i = 0; i < 3; i++) {
			var tv = score.voices().get(i + 1);
			if (tv != null && tv.instrument().ring()) {
				oversample = Math.max(oversample, OVERSAMPLE_RING);
			}
		}

		double outLP = 0.0;
		double srOS = SAMPLE_RATE * oversample;
		double outLPAlpha = onePoleAlpha(OUTPUT_LP_HZ, srOS);

		boolean done;
		do {
			done = true;
			int samplesWritten = 0;
			for (int s = 0; s < BUFFER_SAMPLES; s++) {
				if (stopRequested.get()) {
					break;
				}
				if (voiceMix != null) {
					voiceMix[0] = 0.0;
					voiceMix[1] = 0.0;
					voiceMix[2] = 0.0;
				}
				for (int os = 0; os < oversample; os++) {
					for (int v = 0; v < 3; v++) {
						vr[v].prepareSample((float) srOS);
					}
					for (int v = 0; v < 3; v++) {
						OscState st = vr[v].advanceOsc((float) srOS);
						msb[v] = st.msb;
						rise[v] = st.msbRise;
					}
					for (int v = 0; v < 3; v++) {
						vr[v].applySync(rise[modIndex[v]]);
					}

					double dry = 0.0;
					double wet = 0.0;
					for (int v = 0; v < 3; v++) {
						double voiceSample = vr[v].renderSample((float) srOS, msb[modIndex[v]]);
						if (vr[v].filterRoute()) {
							wet += voiceSample;
						} else {
							dry += voiceSample;
						}
						if (voiceMix != null) {
							voiceMix[v] += voiceSample;
						}
						done &= vr[v].done();
					}

					double filtered = filter.apply(wet, (float) srOS);
					double mix = dry + filtered;

					// Output stage smoothing (simple RC) at oversampled rate.
					outLP += outLPAlpha * (mix - outLP);
				}

				// Safety clamp at output sample rate.
				double mix = Math.max(-1.0, Math.min(1.0, outLP));

				short pcm = (short) (mix * 32767);
				buf[s * 2] = (byte) (pcm & 0xFF);
				buf[s * 2 + 1] = (byte) ((pcm >>> 8) & 0xFF);

				if (voiceBuf != null) {
					voiceBuf[0][s] = (float) (voiceMix[0] / oversample);
					voiceBuf[1][s] = (float) (voiceMix[1] / oversample);
					voiceBuf[2][s] = (float) (voiceMix[2] / oversample);
				}
				samplesWritten++;
			}
			if (playAudio && line != null) {
				line.write(buf, 0, samplesWritten * 2);
			}
			if (wavBuffer != null) {
				wavBuffer.write(buf, 0, samplesWritten * 2);
			}
			if (listener != null && samplesWritten > 0) {
				listener.onSamples(voiceBuf[0], voiceBuf[1], voiceBuf[2], samplesWritten, SAMPLE_RATE);
			}
		} while (!done && !stopRequested.get());

		if (playAudio && line != null) {
			if (stopRequested.get()) {
				line.stop();
				line.flush();
			} else {
				line.drain();
				line.stop();
			}
			line.close();
		}

		if (wavBuffer != null) {
			writeWav(wavBuffer.toByteArray(), fmt, wavOut);
		}
	}

	// -------- Voice runtime --------
	static final class VoiceRuntime {
		private final SIDScoreIR.InstrumentIR instr;
		private final List<FrameEventCompiler.FrameEvent> events;
		private final double sidClockHz;
		private final double frameRate;
		private final int waveMask;
		private boolean sync;
		private boolean ring;
		private final SIDScoreIR.TableIR pwTable;
		private final SIDScoreIR.TableIR waveTable;
		private final SIDScoreIR.TableIR gateTable;
		private final SIDScoreIR.TableIR pitchTable;
		private final FilterRuntime filter;
		private final boolean filterRoute;
		private final int filterModeMask;
		private final int filterCutoff;
		private final int filterRes;
		private final SIDScoreIR.TableIR filterTable;

		private int ev = 0;
		private int samplesLeft = 0;
		private double eventSampleRemainder = 0.0;
		private final Osc osc;
		private final Env env = new Env();
		private boolean done = false;
		private boolean active = false;
		private boolean noise = false;
		private int activeWaveMask = 0;
		private int pw = 0x0800;
		private int pwMin = 0x0000;
		private int pwMax = 0x0FFF;
		private int pwSweep = 0;
		private int pwStep = 0;
		private int pwSamplesLeft = 0;
		private double pwSampleRemainder = 0.0;
		private boolean pwHolding = false;
		private int waveStep = 0;
		private int waveSamplesLeft = 0;
		private double waveSampleRemainder = 0.0;
		private boolean waveHolding = false;
		private int gateStep = 0;
		private int gateSamplesLeft = 0;
		private double gateSampleRemainder = 0.0;
		private boolean gateHolding = false;
		private boolean gateOn = false;
		private int pitchStep = 0;
		private int pitchSamplesLeft = 0;
		private double pitchSampleRemainder = 0.0;
		private boolean pitchHolding = false;
		private int baseMidi = -1;
		private int noteBaseMidi = -1;
		private int pitchOffset = 0;

		VoiceRuntime(SIDScoreIR.InstrumentIR instr, List<FrameEventCompiler.FrameEvent> events,
				double sidClockHz, double frameRate, java.util.Map<String, SIDScoreIR.TableIR> tables,
				FilterRuntime filter, SidWaveforms.TableSet waveTables) {
			this.instr = instr;
			this.events = events;
			this.sidClockHz = sidClockHz;
			this.frameRate = frameRate;
			this.filter = filter;
			this.osc = new Osc(waveTables);
			this.waveMask = instr.waveMask();
			this.sync = instr.sync();
			this.ring = instr.ring();
			this.done = events.isEmpty();
			this.pwTable = instr.pwSeq().isPresent() ? tables.get(instr.pwSeq().get()) : null;
			this.waveTable = instr.waveSeq().isPresent() ? tables.get(instr.waveSeq().get()) : null;
			this.gateTable = instr.gateSeq().isPresent() ? tables.get(instr.gateSeq().get()) : null;
			this.pitchTable = instr.pitchSeq().isPresent() ? tables.get(instr.pitchSeq().get()) : null;
			this.filterModeMask = instr.filterModeMask();
			this.filterRoute = filterModeMask != 0;
			this.filterCutoff = instr.filterCutoff().orElse(0);
			this.filterRes = instr.filterRes().orElse(0);
			this.filterTable = instr.filterSeq().isPresent() ? tables.get(instr.filterSeq().get()) : null;
			env.setAdsr(instr.adsr().a(), instr.adsr().d(), instr.adsr().s(), instr.adsr().r());
		}

		boolean done() {
			return done;
		}

		boolean filterRoute() {
			return filterRoute;
		}

		void prepareSample(float sr) {
			if (done)
				return;
			if (samplesLeft > 0)
				return;
			if (ev >= events.size()) {
				done = true;
				env.noteOff();
				activeWaveMask = 0;
				return;
			}
			start(events.get(ev++), sr);
		}

		OscState advanceOsc(float sr) {
			if (done)
				return OscState.OFF;
			return osc.advance(sr);
		}

		void applySync(boolean modRise) {
			if (done || !sync)
				return;
			if (modRise)
				osc.syncReset();
		}

		double renderSample(float sr, boolean modMsb) {
			if (done)
				return 0.0;
			if (samplesLeft > 0) {
				samplesLeft--;
			}

			advanceGateSeq(sr);
			advanceWaveSeq(sr);
			advancePwm(sr);
			advancePitchSeq(sr);
			osc.setPulseWidth(pw);

			double e = env.next(sr);
			double o = osc.output(activeWaveMask, ring, modMsb);

			double raw = e * o;

			// simple per-voice gain
			return MIX_GAIN * raw;
		}

		private void start(FrameEventCompiler.FrameEvent e, float sr) {
			if (e.frames() <= 0) {
				done = true;
				active = false;
				noise = false;
				gateOn = false;
				env.noteOff();
				activeWaveMask = 0;
				return;
			}

			double samplesExact = e.frames() * (sr / frameRate) + eventSampleRemainder;
			samplesLeft = Math.max(1, (int) Math.round(samplesExact));
			eventSampleRemainder = samplesExact - samplesLeft;

			int ctrl = e.ctrl() & 0xF7;
			boolean gateBit = (ctrl & 0x01) != 0;
			sync = (ctrl & 0x02) != 0;
			ring = (ctrl & 0x04) != 0;

			noise = (e.baseNote() & 0x80) != 0;
			baseMidi = noise ? -1 : (e.baseNote() & 0x7f);
			noteBaseMidi = baseMidi;
			pitchOffset = 0;

			active = gateBit || e.freq() != 0;

			int waveBits = ctrl & 0xF0;
			if (gateBit) {
				resetPwm(sr);
				resetWaveSeq(sr);
				resetGateSeq(sr);
				resetPitchSeq(sr);
				primeGateSeq(sr);
				if (filterRoute) {
					filter.activate(filterModeMask, filterCutoff, filterRes, filterTable, sr);
				}
			} else {
				activeWaveMask = ctrlToWaveMask(waveBits);
				if (!active) {
					resetPitchSeq(sr);
				}
			}

			if (noise) {
				activeWaveMask = SIDScoreIR.Wave.NOISE.mask;
			}

			osc.setWaveMask(activeWaveMask, OptionalInt.of(pw));

			if (!active) {
				applyGateValue(false, sr);
			} else if (gateTable == null || gateTable.steps().isEmpty()) {
				applyGateValue(gateBit, sr);
			}

			osc.setFreq(freqRegToHz(e.freq(), sidClockHz), sr);
			if (!noise && gateOn) {
				applyPitchOffset(sr, pitchOffset);
			}
		}

		private void resetPwm(float sr) {
			pw = instr.pw().orElse(0x0800) & 0x0FFF;
			pwSweep = instr.pwSweep();
			pwStep = 0;
			pwSamplesLeft = 0;
			pwSampleRemainder = 0.0;
			pwHolding = false;

			if (pwTable != null && !pwTable.steps().isEmpty()) {
				pwMin = 0x0000;
				pwMax = 0x0FFF;
				loadPwTableStep(sr);
				return;
			}

			pwMin = instr.pwMin().isPresent() ? (instr.pwMin().getAsInt() & 0x0FFF) : 0x0000;
			pwMax = instr.pwMax().isPresent() ? (instr.pwMax().getAsInt() & 0x0FFF) : 0x0FFF;
			if (pwMin > pwMax) {
				int tmp = pwMin;
				pwMin = pwMax;
				pwMax = tmp;
			}
			pw = clampPw(pw);
			if (pwSweep != 0) {
				setPwSamplesLeft(1, sr);
			}
		}

		private void resetWaveSeq(float sr) {
			waveStep = 0;
			waveSamplesLeft = 0;
			waveSampleRemainder = 0.0;
			waveHolding = false;
			activeWaveMask = waveMask;

			if (waveTable != null && !waveTable.steps().isEmpty()) {
				loadWaveTableStep(sr);
			}
		}

		private void resetGateSeq(float sr) {
			gateStep = 0;
			gateSamplesLeft = 0;
			gateSampleRemainder = 0.0;
			gateHolding = false;
		}

		private void primeGateSeq(float sr) {
			if (gateTable == null || gateTable.steps().isEmpty()) {
				return;
			}
			SIDScoreIR.TableStepIR step = gateTable.steps().get(0);
			applyGateValue(step.value() != 0, sr);
			if (step.hold() || step.durationFrames() <= 0) {
				gateHolding = true;
				gateSamplesLeft = 0;
			} else {
				setGateSamplesLeft(step.durationFrames(), sr);
			}
			gateStep = 1;
		}

		private void resetPitchSeq(float sr) {
			pitchStep = 0;
			pitchSamplesLeft = 0;
			pitchSampleRemainder = 0.0;
			pitchHolding = false;
			pitchOffset = 0;
			noteBaseMidi = baseMidi;

			if (baseMidi < 0)
				return;

			if (pitchTable != null && !pitchTable.steps().isEmpty()) {
				loadPitchTableStep(sr);
			} else {
				applyPitchOffset(sr, 0);
			}
		}

		private void advanceWaveSeq(float sr) {
			if (!active)
				return;
			if (waveTable == null || waveTable.steps().isEmpty())
				return;
			if (waveHolding)
				return;
			if (waveSamplesLeft > 0) {
				waveSamplesLeft--;
				if (waveSamplesLeft > 0)
					return;
			}
			loadWaveTableStep(sr);
			osc.setWaveMask(activeWaveMask, OptionalInt.of(pw));
		}

		private void advanceGateSeq(float sr) {
			if (!active)
				return;
			if (gateTable == null || gateTable.steps().isEmpty())
				return;
			if (gateHolding)
				return;
			if (gateSamplesLeft > 0) {
				gateSamplesLeft--;
				if (gateSamplesLeft > 0)
					return;
			}
			loadGateTableStep(sr);
		}

		private void loadWaveTableStep(float sr) {
			if (waveTable == null)
				return;
			List<SIDScoreIR.TableStepIR> steps = waveTable.steps();
			if (steps.isEmpty()) {
				waveHolding = true;
				return;
			}
			if (waveStep >= steps.size()) {
				if (waveTable.loop()) {
					waveStep = 0;
				} else {
					waveHolding = true;
					return;
				}
			}
			SIDScoreIR.TableStepIR step = steps.get(waveStep);
			if (step.waveSet()) {
				activeWaveMask = step.value();
			}
			applyWaveControls(step, sr);
			if (step.hold() || step.durationFrames() <= 0) {
				waveHolding = true;
				waveSamplesLeft = 0;
				return;
			}
			setWaveSamplesLeft(step.durationFrames(), sr);
			waveStep++;
		}

		private void applyWaveControls(SIDScoreIR.TableStepIR step, float sr) {
			if (step.noteMode() != SIDScoreIR.NoteMode.NONE && baseMidi >= 0) {
				if (step.noteMode() == SIDScoreIR.NoteMode.ABS) {
					noteBaseMidi = clampMidi(step.noteValue());
				} else if (step.noteMode() == SIDScoreIR.NoteMode.REL) {
					noteBaseMidi = clampMidi(noteBaseMidi + step.noteValue());
				}
				applyPitchOffset(sr, pitchOffset);
			}
			if (step.gate() != SIDScoreIR.TriState.UNSET) {
				boolean on = step.gate() == SIDScoreIR.TriState.ON;
				applyGateValue(on, sr);
			}
			if (step.ring() != SIDScoreIR.TriState.UNSET) {
				ring = step.ring() == SIDScoreIR.TriState.ON;
			}
			if (step.sync() != SIDScoreIR.TriState.UNSET) {
				sync = step.sync() == SIDScoreIR.TriState.ON;
			}
			if (step.reset()) {
				osc.hardReset();
			}
		}

		private void loadGateTableStep(float sr) {
			if (gateTable == null)
				return;
			List<SIDScoreIR.TableStepIR> steps = gateTable.steps();
			if (steps.isEmpty()) {
				gateHolding = true;
				return;
			}
			if (gateStep >= steps.size()) {
				if (gateTable.loop()) {
					gateStep = 0;
				} else {
					gateHolding = true;
					return;
				}
			}
			SIDScoreIR.TableStepIR step = steps.get(gateStep);
			applyGateValue(step.value() != 0, sr);
			if (step.hold() || step.durationFrames() <= 0) {
				gateHolding = true;
				gateSamplesLeft = 0;
				return;
			}
			setGateSamplesLeft(step.durationFrames(), sr);
			gateStep++;
		}

		private void setWaveSamplesLeft(int frames, float sr) {
			double samplesExact = frames * (sr / frameRate) + waveSampleRemainder;
			waveSamplesLeft = Math.max(1, (int) Math.round(samplesExact));
			waveSampleRemainder = samplesExact - waveSamplesLeft;
		}

		private void setGateSamplesLeft(int frames, float sr) {
			double samplesExact = frames * (sr / frameRate) + gateSampleRemainder;
			gateSamplesLeft = Math.max(1, (int) Math.round(samplesExact));
			gateSampleRemainder = samplesExact - gateSamplesLeft;
		}

		private void applyGateValue(boolean on, float sr) {
			if (on) {
				if (!gateOn) {
					gateOn = true;
					boolean shortAttack = env.isActive() && !env.isReleasing();
					env.noteOn(true, shortAttack);
				}
				return;
			}
			if (gateOn) {
				env.noteOff();
				gateOn = false;
			}
		}

		private void advancePwm(float sr) {
			if (!active)
				return;
			if (pwTable != null && !pwTable.steps().isEmpty()) {
				if (pwHolding)
					return;
				if (pwSamplesLeft > 0) {
					pwSamplesLeft--;
					if (pwSamplesLeft > 0)
						return;
				}
				loadPwTableStep(sr);
				return;
			}

			if (pwSweep == 0)
				return;
			if (pwSamplesLeft > 0) {
				pwSamplesLeft--;
				if (pwSamplesLeft > 0)
					return;
			}
			pw = clampPw(pw + pwSweep);
			setPwSamplesLeft(1, sr);
		}

		private void advancePitchSeq(float sr) {
			if (!active)
				return;
			if (baseMidi < 0 || noise)
				return;
			if (pitchTable == null || pitchTable.steps().isEmpty())
				return;
			if (pitchHolding)
				return;
			if (pitchSamplesLeft > 0) {
				pitchSamplesLeft--;
				if (pitchSamplesLeft > 0)
					return;
			}
			loadPitchTableStep(sr);
		}

		private void loadPitchTableStep(float sr) {
			if (pitchTable == null || baseMidi < 0)
				return;
			List<SIDScoreIR.TableStepIR> steps = pitchTable.steps();
			if (steps.isEmpty()) {
				pitchHolding = true;
				return;
			}
			if (pitchStep >= steps.size()) {
				if (pitchTable.loop()) {
					pitchStep = 0;
				} else {
					pitchHolding = true;
					return;
				}
			}
			SIDScoreIR.TableStepIR step = steps.get(pitchStep);
			pitchOffset = step.value();
			applyPitchOffset(sr, pitchOffset);
			if (step.hold() || step.durationFrames() <= 0) {
				pitchHolding = true;
				pitchSamplesLeft = 0;
				return;
			}
			setPitchSamplesLeft(step.durationFrames(), sr);
			pitchStep++;
		}

		private void setPitchSamplesLeft(int frames, float sr) {
			double samplesExact = frames * (sr / frameRate) + pitchSampleRemainder;
			pitchSamplesLeft = Math.max(1, (int) Math.round(samplesExact));
			pitchSampleRemainder = samplesExact - pitchSamplesLeft;
		}

		private void applyPitchOffset(float sr, int offset) {
			if (noteBaseMidi < 0)
				return;
			int midi = noteBaseMidi + offset;
			if (midi < 0)
				midi = 0;
			if (midi > 127)
				midi = 127;
			double hz = midiToHz(midi, sidClockHz);
			osc.setFreq(hz, sr);
		}

		private static int clampMidi(int midi) {
			if (midi < 0)
				return 0;
			if (midi > 127)
				return 127;
			return midi;
		}

		private void loadPwTableStep(float sr) {
			if (pwTable == null)
				return;
			List<SIDScoreIR.TableStepIR> steps = pwTable.steps();
			if (steps.isEmpty()) {
				pwHolding = true;
				return;
			}
			if (pwStep >= steps.size()) {
				if (pwTable.loop()) {
					pwStep = 0;
				} else {
					pwHolding = true;
					return;
				}
			}
			SIDScoreIR.TableStepIR step = steps.get(pwStep);
			pw = clampPw(step.value() & 0x0FFF);
			if (step.hold() || step.durationFrames() <= 0) {
				pwHolding = true;
				pwSamplesLeft = 0;
				return;
			}
			setPwSamplesLeft(step.durationFrames(), sr);
			pwStep++;
		}

		private void setPwSamplesLeft(int frames, float sr) {
			double samplesExact = frames * (sr / frameRate) + pwSampleRemainder;
			pwSamplesLeft = Math.max(1, (int) Math.round(samplesExact));
			pwSampleRemainder = samplesExact - pwSamplesLeft;
		}

		private int clampPw(int v) {
			if (v < pwMin)
				return pwMin;
			if (v > pwMax)
				return pwMax;
			return v;
		}

		private static double midiToHz(int m, double sidClockHz) {
			double hz = 440.0 * Math.pow(2.0, (m - 69) / 12.0);
			return quantizeToSidHz(hz, sidClockHz);
		}

		private static double freqRegToHz(int reg, double sidClockHz) {
			if (reg <= 0)
				return 0.0;
			return (reg & 0xFFFF) * sidClockHz / 16777216.0;
		}

		private static int ctrlToWaveMask(int waveBits) {
			int mask = 0;
			if ((waveBits & 0x10) != 0)
				mask |= SIDScoreIR.Wave.TRI.mask;
			if ((waveBits & 0x20) != 0)
				mask |= SIDScoreIR.Wave.SAW.mask;
			if ((waveBits & 0x40) != 0)
				mask |= SIDScoreIR.Wave.PULSE.mask;
			if ((waveBits & 0x80) != 0)
				mask |= SIDScoreIR.Wave.NOISE.mask;
			return mask;
		}

	}

	// -------- Global filter runtime (approximate SID filter) --------
	static final class FilterRuntime {
		private final double frameRate;

		private int modeMask = 0;
		private int cutoff = 0;
		private int resonance = 0;
		private SIDScoreIR.TableIR table = null;

		private int step = 0;
		private int samplesLeft = 0;
		private double sampleRemainder = 0.0;
		private boolean holding = false;

		private double low = 0.0;
		private double band = 0.0;
		private double f = 0.0;
		private double q = 1.0;

		FilterRuntime(double frameRate) {
			this.frameRate = frameRate;
		}

		void activate(int modeMask, int cutoff, int resonance, SIDScoreIR.TableIR table, float sr) {
			this.modeMask = modeMask;
			this.cutoff = clampCutoff(cutoff);
			this.resonance = clampRes(resonance);
			this.table = table;
			this.step = 0;
			this.samplesLeft = 0;
			this.sampleRemainder = 0.0;
			this.holding = false;
			updateCoeffs(sr);
			if (table != null && !table.steps().isEmpty()) {
				loadTableStep(sr);
			}
		}

		double apply(double input, float sr) {
			if (modeMask == 0) {
				return input;
			}
			advanceSeq(sr);

			// State-variable filter (Chamberlin)
			low += f * band;
			double high = input - low - q * band;
			band += f * high;

			double out = 0.0;
			int count = 0;
			if ((modeMask & SIDScoreIR.FilterMode.LP.mask) != 0) {
				out += low;
				count++;
			}
			if ((modeMask & SIDScoreIR.FilterMode.BP.mask) != 0) {
				out += band;
				count++;
			}
			if ((modeMask & SIDScoreIR.FilterMode.HP.mask) != 0) {
				out += high;
				count++;
			}
			return count == 0 ? input : out / count;
		}

		private void advanceSeq(float sr) {
			if (table == null || table.steps().isEmpty())
				return;
			if (holding)
				return;
			if (samplesLeft > 0) {
				samplesLeft--;
				if (samplesLeft > 0)
					return;
			}
			loadTableStep(sr);
		}

		private void loadTableStep(float sr) {
			if (table == null)
				return;
			List<SIDScoreIR.TableStepIR> steps = table.steps();
			if (steps.isEmpty()) {
				holding = true;
				return;
			}
			if (step >= steps.size()) {
				if (table.loop()) {
					step = 0;
				} else {
					holding = true;
					return;
				}
			}
			SIDScoreIR.TableStepIR s = steps.get(step);
			cutoff = clampCutoff(s.value());
			updateCoeffs(sr);
			if (s.hold() || s.durationFrames() <= 0) {
				holding = true;
				samplesLeft = 0;
				return;
			}
			setSamplesLeft(s.durationFrames(), sr);
			step++;
		}

		private void setSamplesLeft(int frames, float sr) {
			double samplesExact = frames * (sr / frameRate) + sampleRemainder;
			samplesLeft = Math.max(1, (int) Math.round(samplesExact));
			sampleRemainder = samplesExact - samplesLeft;
		}

		private void updateCoeffs(float sr) {
			double cutoffHz = cutoffToHz(cutoff);
			cutoffHz = Math.min(cutoffHz, sr * 0.45);
			if (cutoffHz <= 0.0) {
				f = 0.0;
				return;
			}
			f = 2.0 * Math.sin(Math.PI * cutoffHz / sr);
			double resNorm = clampRes(resonance) / 15.0;
			q = 2.0 - 1.5 * resNorm; // 0.5..2.0 (higher resonance -> lower damping)
		}

		private static int clampCutoff(int v) {
			return Math.max(0, Math.min(0x07FF, v));
		}

		private static int clampRes(int v) {
			return Math.max(0, Math.min(15, v));
		}

		private static double cutoffToHz(int cutoff) {
			double t = clampCutoff(cutoff) / 2047.0;
			double minHz = 30.0;
			double maxHz = 12000.0;
			return minHz + (maxHz - minHz) * t;
		}
	}

	static final class OscState {
		static final OscState OFF = new OscState(false, false);
		final boolean msb;
		final boolean msbRise;

		OscState(boolean msb, boolean msbRise) {
			this.msb = msb;
			this.msbRise = msbRise;
		}
	}

	// -------- Oscillator (improved mock) --------
	static final class Osc {
		private static final double PHASE_SCALE = 16777216.0; // 2^24
		private static final byte[] TRI_TABLE = new byte[SidWaveforms.WAVE_LEN];
		private static final byte[] SAW_TABLE = new byte[SidWaveforms.WAVE_LEN];

		private double phaseAcc = 0.0;
		private double phaseInc = 0.0;
		private int pulseWidth = 0x0800;
		private final SidWaveforms.TableSet tables;

		// Noise
		private int lfsr = 0x7FFFFF;
		private int noiseOut8 = 0;
		private int lastBit19 = 0;
		private int lastMsb = 0;

		static {
			for (int i = 0; i < SidWaveforms.WAVE_LEN; i++) {
				int tri = (i < 2048) ? (i << 1) : ((0x0FFF - i) << 1);
				TRI_TABLE[i] = (byte) ((tri >> 4) & 0xFF);
				SAW_TABLE[i] = (byte) ((i >> 4) & 0xFF);
			}
		}

		Osc(SidWaveforms.TableSet tables) {
			this.tables = tables;
		}

		void setWaveMask(int waveMask, java.util.OptionalInt pw) {
			if (pw.isPresent()) {
				// PW is 12-bit (PW11-0); $0800 is a square wave.
				pulseWidth = Math.max(0, Math.min(0x0FFF, pw.getAsInt() & 0x0FFF));
			} else {
				pulseWidth = 0x0800;
			}

			if ((waveMask & SIDScoreIR.Wave.NOISE.mask) == 0) {
				noiseOut8 = 0;
				lastBit19 = 0;
			}
		}

		void setPulseWidth(int pw) {
			pulseWidth = Math.max(0, Math.min(0x0FFF, pw & 0x0FFF));
		}

		void setFreq(double hz, float sr) {
			double f = Math.max(0.0, hz);
			phaseInc = f * PHASE_SCALE / sr;
		}

		OscState advance(float sr) {
			if (phaseInc <= 0.0)
				return OscState.OFF;

			phaseAcc += phaseInc;
			if (phaseAcc >= PHASE_SCALE) {
				phaseAcc -= PHASE_SCALE * Math.floor(phaseAcc / PHASE_SCALE);
			}

			int phaseInt = (int) phaseAcc;
			int bit19 = (phaseInt >> 19) & 1;
			if (bit19 == 1 && lastBit19 == 0) {
				stepNoise();
			}
			lastBit19 = bit19;

			int msb = (phaseInt >> 23) & 1;
			boolean rise = msb == 1 && lastMsb == 0;
			lastMsb = msb;
			return new OscState(msb == 1, rise);
		}

		void syncReset() {
			phaseAcc = 0.0;
			lastBit19 = 0;
			lastMsb = 0;
		}

		void hardReset() {
			phaseAcc = 0.0;
			lastBit19 = 0;
			lastMsb = 0;
			lfsr = 0x7FFFFF;
			noiseOut8 = 0;
		}

		double output(int waveMask, boolean ring, boolean modMsb) {
			if (phaseInc <= 0.0 || waveMask == 0)
				return 0.0;

			int phaseInt = (int) phaseAcc;
			int phase12 = (phaseInt >> 12) & 0x0FFF;

			boolean hasTri = (waveMask & SIDScoreIR.Wave.TRI.mask) != 0;
			boolean hasSaw = (waveMask & SIDScoreIR.Wave.SAW.mask) != 0;
			boolean hasPulse = (waveMask & SIDScoreIR.Wave.PULSE.mask) != 0;
			boolean hasNoise = (waveMask & SIDScoreIR.Wave.NOISE.mask) != 0;

			int value;
			if (hasNoise) {
				value = noiseOut8;
			} else if (hasTri && hasSaw && hasPulse) {
				value = tables.wave70[phase12 + pulseWidth] & 0xFF;
			} else if (hasTri && hasSaw) {
				value = tables.wave30[phase12] & 0xFF;
			} else if (hasTri && hasPulse) {
				value = tables.wave50[phase12 + pulseWidth] & 0xFF;
			} else if (hasSaw && hasPulse) {
				value = tables.wave60[phase12 + pulseWidth] & 0xFF;
			} else if (hasTri) {
				value = TRI_TABLE[phase12] & 0xFF;
			} else if (hasSaw) {
				value = SAW_TABLE[phase12] & 0xFF;
			} else if (hasPulse) {
				value = (phase12 < pulseWidth) ? 255 : 0;
			} else {
				value = 0;
			}

			if (ring && modMsb && hasTri && !hasNoise) {
				value = 255 - value;
			}

			return ((value - 128) / 128.0);
		}

		private void stepNoise() {
			// 23-bit LFSR with bit0 = bit22 XOR bit17.
			int bit = ((lfsr >> 22) ^ (lfsr >> 17)) & 1;
			lfsr = ((lfsr << 1) | bit) & 0x7FFFFF;
			noiseOut8 = noiseFromLfsr(lfsr);
		}

		private static int noiseFromLfsr(int lfsr) {
			// Noise output taps: {20,18,14,11,9,5,2,0} -> 8-bit value, widened to 12-bit.
			int v = 0;
			v |= ((lfsr >> 20) & 1) << 7;
			v |= ((lfsr >> 18) & 1) << 6;
			v |= ((lfsr >> 14) & 1) << 5;
			v |= ((lfsr >> 11) & 1) << 4;
			v |= ((lfsr >> 9) & 1) << 3;
			v |= ((lfsr >> 5) & 1) << 2;
			v |= ((lfsr >> 2) & 1) << 1;
			v |= (lfsr & 1);
			return (v & 0xFF);
		}
	}

	// -------- Envelope (MAME-style ADSR, BSD-3-Clause data tables) --------
	static final class Env {
		private static final int ENVE_STARTATTACK = 0;
		private static final int ENVE_STARTRELEASE = 2;
		private static final int ENVE_ATTACK = 4;
		private static final int ENVE_DECAY = 6;
		private static final int ENVE_SUSTAIN = 8;
		private static final int ENVE_RELEASE = 10;
		private static final int ENVE_SUSTAINDECAY = 12;
		private static final int ENVE_MUTE = 14;
		private static final int ENVE_SHORTATTACK = 16;
		private static final int ENVE_ALTER = 32;

		private static final int[] MASTER_LEVELS = {
				0, 17, 34, 51, 68, 85, 102, 119,
				136, 153, 170, 187, 204, 221, 238, 255
		};

		private static final double[] ATTACK_TIMES = {
				2.2528606, 8.0099577, 15.7696042, 23.7795619,
				37.2963655, 55.0684591, 66.8330845, 78.3473987,
				98.1219818, 244.554021, 489.108042, 782.472742,
				977.715461, 2933.64701, 4889.07793, 7822.72493
		};

		private static final double[] DECAY_RELEASE_TIMES = {
				8.91777693, 24.594051, 48.4185907, 73.0116639,
				114.512475, 169.078356, 205.199432, 240.551975,
				301.266125, 750.858245, 1501.71551, 2402.43682,
				3001.89298, 9007.21405, 15010.998, 24018.2111
		};

		private static final int ATTACK_TAB_LEN = 255;
		private static final int[] RELEASE_TAB = {
				255, 255, 254, 254, 253, 253, 252, 252, 251, 251, 250, 250, 249, 249, 248,
				248, 247, 247, 246, 246, 245, 245, 244, 244, 243, 243, 242, 242, 241, 241,
				240, 240, 239, 239, 238, 238, 237, 237, 236, 236, 235, 235, 234, 234, 233,
				233, 232, 232, 231, 231, 230, 230, 229, 229, 228, 228, 227, 227, 226, 226,
				225, 225, 224, 224, 223, 223, 222, 222, 221, 221, 220, 220, 219, 219, 218,
				218, 217, 217, 216, 216, 215, 215, 214, 214, 213, 213, 212, 212, 211, 211,
				210, 210, 209, 209, 208, 208, 207, 207, 206, 206, 205, 205, 204, 204, 203,
				203, 202, 202, 201, 201, 200, 200, 199, 199, 198, 198, 197, 197, 196, 196,
				195, 195, 194, 194, 193, 193, 192, 192, 191, 191, 190, 190, 189, 189, 188,
				188, 187, 187, 186, 186, 185, 185, 184, 184, 183, 183, 182, 182, 181, 181,
				180, 180, 179, 179, 178, 178, 177, 177, 176, 176, 175, 175, 174, 174, 173,
				173, 172, 172, 171, 171, 170, 170, 169, 169, 168, 168, 167, 167, 166, 166,
				165, 165, 164, 164, 163, 163, 162, 162, 161, 161, 160, 160, 159, 159, 158,
				158, 157, 157, 156, 156, 155, 155, 154, 154, 153, 153, 152, 152, 151, 151,
				150, 150, 149, 149, 148, 148, 147, 147, 146, 146, 145, 145, 144, 144, 143,
				143, 142, 142, 141, 141, 140, 140, 139, 139, 138, 138, 137, 137, 136, 136,
				135, 135, 134, 134, 133, 133, 132, 132, 131, 131, 130, 130, 129, 129, 128,
				128, 127, 127, 126, 126, 125, 125, 124, 124, 123, 123, 122, 122, 121, 121,
				120, 120, 119, 119, 118, 118, 117, 117, 116, 116, 115, 115, 114, 114, 113,
				113, 112, 112, 111, 111, 110, 110, 109, 109, 108, 108, 107, 107, 106, 106,
				105, 105, 104, 104, 103, 103, 102, 102, 101, 101, 100, 100, 99, 99, 98,
				98, 97, 97, 96, 96, 95, 95, 94, 94, 94, 94, 93, 93, 93, 93,
				92, 92, 92, 92, 91, 91, 91, 91, 90, 90, 90, 90, 89, 89, 89,
				89, 88, 88, 88, 88, 87, 87, 87, 87, 86, 86, 86, 86, 85, 85,
				85, 85, 84, 84, 84, 84, 83, 83, 83, 83, 82, 82, 82, 82, 81,
				81, 81, 81, 80, 80, 80, 80, 79, 79, 79, 79, 78, 78, 78, 78,
				77, 77, 77, 77, 76, 76, 76, 76, 75, 75, 75, 75, 74, 74, 74,
				74, 73, 73, 73, 73, 72, 72, 72, 72, 71, 71, 71, 71, 70, 70,
				70, 70, 69, 69, 69, 69, 68, 68, 68, 68, 67, 67, 67, 67, 66,
				66, 66, 66, 65, 65, 65, 65, 64, 64, 64, 64, 63, 63, 63, 63,
				62, 62, 62, 62, 61, 61, 61, 61, 60, 60, 60, 60, 59, 59, 59,
				59, 58, 58, 58, 58, 57, 57, 57, 57, 56, 56, 56, 56, 55, 55,
				55, 55, 55, 55, 55, 55, 54, 54, 54, 54, 54, 54, 54, 54, 53,
				53, 53, 53, 53, 53, 53, 53, 52, 52, 52, 52, 52, 52, 52, 52,
				51, 51, 51, 51, 51, 51, 51, 51, 50, 50, 50, 50, 50, 50, 50,
				50, 49, 49, 49, 49, 49, 49, 49, 49, 48, 48, 48, 48, 48, 48,
				48, 48, 47, 47, 47, 47, 47, 47, 47, 47, 46, 46, 46, 46, 46,
				46, 46, 46, 45, 45, 45, 45, 45, 45, 45, 45, 44, 44, 44, 44,
				44, 44, 44, 44, 43, 43, 43, 43, 43, 43, 43, 43, 42, 42, 42,
				42, 42, 42, 42, 42, 41, 41, 41, 41, 41, 41, 41, 41, 40, 40,
				40, 40, 40, 40, 40, 40, 39, 39, 39, 39, 39, 39, 39, 39, 38,
				38, 38, 38, 38, 38, 38, 38, 37, 37, 37, 37, 37, 37, 37, 37,
				36, 36, 36, 36, 36, 36, 36, 36, 35, 35, 35, 35, 35, 35, 35,
				35, 34, 34, 34, 34, 34, 34, 34, 34, 33, 33, 33, 33, 33, 33,
				33, 33, 32, 32, 32, 32, 32, 32, 32, 32, 31, 31, 31, 31, 31,
				31, 31, 31, 30, 30, 30, 30, 30, 30, 30, 30, 29, 29, 29, 29,
				29, 29, 29, 29, 28, 28, 28, 28, 28, 28, 28, 28, 27, 27, 27,
				27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 26, 26,
				26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 25,
				25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25,
				24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
				24, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
				23, 23, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
				22, 22, 22, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
				21, 21, 21, 21, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
				20, 20, 20, 20, 20, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
				19, 19, 19, 19, 19, 19, 18, 18, 18, 18, 18, 18, 18, 18, 18,
				18, 18, 18, 18, 18, 18, 18, 17, 17, 17, 17, 17, 17, 17, 17,
				17, 17, 17, 17, 17, 17, 17, 17, 16, 16, 16, 16, 16, 16, 16,
				16, 16, 16, 16, 16, 16, 16, 16, 16, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 14, 14, 14, 14,
				14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
				14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 13, 13,
				13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
				13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
				12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
				12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
				12, 12, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
				11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
				11, 11, 11, 11, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
				10, 10, 10, 10, 10, 10, 9, 9, 9, 9, 9, 9, 9, 9, 9,
				9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
				9, 9, 9, 9, 9, 9, 9, 9, 8, 8, 8, 8, 8, 8, 8,
				8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
				8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 7,
				7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
				7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
				7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
				7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6,
				6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
				6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
				6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
				6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5,
				5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
				5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
				5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
				5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4,
				4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
				4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
				4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
				4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0
		};

		private static final int[] RELEASE_POS = new int[256];
		private static final int[] MEASURED_VOL = new int[256];
		private static final int[] ATTACK_RATES = new int[16];
		private static final int[] ATTACK_RATES_P = new int[16];
		private static final int[] DECAY_RATES = new int[16];
		private static final int[] DECAY_RATES_P = new int[16];
		private static volatile float ratesSr = -1f;

		static {
			for (int i = 0; i < 256; i++) {
				int j = 0;
				while (j < RELEASE_TAB.length && RELEASE_TAB[j] > i) {
					j++;
				}
				RELEASE_POS[i] = (j < RELEASE_TAB.length) ? j : RELEASE_TAB.length - 1;
			}
			for (int i = 0; i < 256; i++) {
				double tmp = (293.0 * (1.0 - Math.exp(i / -130.0))) + 4.0;
				int v = (i == 0) ? 0 : (int) Math.round(tmp);
				if (v < 0) {
					v = 0;
				} else if (v > 255) {
					v = 255;
				}
				MEASURED_VOL[i] = v;
			}
		}

		private int ADSRctrl = ENVE_MUTE;
		private int SIDAD = 0;
		private int SIDSR = 0;
		private int enveStep = 0;
		private int enveStepPnt = 0;
		private int enveStepAdd = 0;
		private int enveStepAddPnt = 0;
		private int enveVol = 0;
		private int enveSusVol = 0;
		private int enveShortAttackCount = 0;
		private boolean shortAttackInit = false;

		void setAdsr(int A, int D, int S, int R) {
			int newAD = ((clampNibble(A) & 0x0F) << 4) | (clampNibble(D) & 0x0F);
			int newSR = ((clampNibble(S) & 0x0F) << 4) | (clampNibble(R) & 0x0F);
			boolean changed = (newAD != SIDAD) || (newSR != SIDSR);
			SIDAD = newAD;
			SIDSR = newSR;
			enveSusVol = MASTER_LEVELS[clampNibble(S)];
			if (changed && ADSRctrl != ENVE_MUTE) {
				ADSRctrl |= ENVE_ALTER;
			}
		}

		void noteOn(boolean retrig, boolean shortAttack) {
			if (retrig || ADSRctrl == ENVE_RELEASE || ADSRctrl == ENVE_MUTE) {
				ADSRctrl = shortAttack ? ENVE_SHORTATTACK : ENVE_STARTATTACK;
			}
		}

		void noteOff() {
			if (ADSRctrl != ENVE_MUTE) {
				ADSRctrl = ENVE_STARTRELEASE;
			}
		}

		boolean isActive() {
			return (ADSRctrl & ~ENVE_ALTER) != ENVE_MUTE;
		}

		boolean isReleasing() {
			int state = ADSRctrl & ~ENVE_ALTER;
			return state == ENVE_RELEASE || state == ENVE_STARTRELEASE;
		}

		double next(float sr) {
			ensureRates(sr);
			if (ADSRctrl != ENVE_SHORTATTACK) {
				shortAttackInit = false;
			}
			return switch (ADSRctrl) {
			case ENVE_MUTE -> 0.0;
			case ENVE_STARTATTACK -> startAttack();
			case ENVE_ATTACK -> attack();
			case ENVE_DECAY -> decay();
			case ENVE_SUSTAIN -> sustain();
			case ENVE_SUSTAINDECAY -> sustainDecay();
			case ENVE_RELEASE -> release();
			case ENVE_STARTRELEASE -> startRelease();
			case ENVE_SHORTATTACK -> {
				if (!shortAttackInit) {
					shortAttackInit = true;
					yield startShortAttack();
				}
				yield shortAttack();
			}
			case (ENVE_ATTACK | ENVE_ALTER) -> alterAttack();
			case (ENVE_DECAY | ENVE_ALTER) -> alterDecay();
			case (ENVE_SUSTAIN | ENVE_ALTER) -> alterSustain();
			case (ENVE_SUSTAINDECAY | ENVE_ALTER) -> alterSustainDecay();
			case (ENVE_RELEASE | ENVE_ALTER) -> alterRelease();
			default -> attack();
			};
		}

		private double startAttack() {
			ADSRctrl = ENVE_ATTACK;
			enveStep = enveVol;
			enveStepPnt = 0;
			return alterAttack();
		}

		private double alterAttack() {
			int attack = (SIDAD >> 4) & 0x0F;
			enveStepAdd = ATTACK_RATES[attack];
			enveStepAddPnt = ATTACK_RATES_P[attack];
			ADSRctrl = ENVE_ATTACK;
			return attack();
		}

		private double attack() {
			if (enveStep >= ATTACK_TAB_LEN) {
				return startDecay();
			}
			enveVol = enveStep;
			advance();
			return output();
		}

		private double startDecay() {
			ADSRctrl = ENVE_DECAY;
			enveStep = 0;
			enveStepPnt = 0;
			return alterDecay();
		}

		private double alterDecay() {
			int decay = SIDAD & 0x0F;
			enveStepAdd = DECAY_RATES[decay];
			enveStepAddPnt = DECAY_RATES_P[decay];
			ADSRctrl = ENVE_DECAY;
			return decay();
		}

		private double decay() {
			if (enveStep >= RELEASE_TAB.length) {
				enveVol = enveSusVol;
				return alterSustain();
			}
			enveVol = RELEASE_TAB[enveStep];
			if (enveVol <= enveSusVol) {
				enveVol = enveSusVol;
				return alterSustain();
			}
			advance();
			return output();
		}

		private double alterSustain() {
			if (enveVol > enveSusVol) {
				ADSRctrl = ENVE_SUSTAINDECAY;
				return alterSustainDecay();
			}
			ADSRctrl = ENVE_SUSTAIN;
			return sustain();
		}

		private double sustain() {
			return output();
		}

		private double alterSustainDecay() {
			int decay = SIDAD & 0x0F;
			enveStepAdd = DECAY_RATES[decay];
			enveStepAddPnt = DECAY_RATES_P[decay];
			ADSRctrl = ENVE_SUSTAINDECAY;
			return sustainDecay();
		}

		private double sustainDecay() {
			if (enveStep >= RELEASE_TAB.length) {
				enveVol = RELEASE_TAB[RELEASE_TAB.length - 1];
				return alterSustain();
			}
			enveVol = RELEASE_TAB[enveStep];
			if (enveVol <= enveSusVol) {
				enveVol = enveSusVol;
				return alterSustain();
			}
			advance();
			return output();
		}

		private double startRelease() {
			ADSRctrl = ENVE_RELEASE;
			enveStep = RELEASE_POS[enveVol];
			enveStepPnt = 0;
			return alterRelease();
		}

		private double alterRelease() {
			int rel = SIDSR & 0x0F;
			enveStepAdd = DECAY_RATES[rel];
			enveStepAddPnt = DECAY_RATES_P[rel];
			ADSRctrl = ENVE_RELEASE;
			return release();
		}

		private double release() {
			if (enveStep >= RELEASE_TAB.length) {
				enveVol = RELEASE_TAB[RELEASE_TAB.length - 1];
			} else {
				enveVol = RELEASE_TAB[enveStep];
				advance();
			}
			return output();
		}

		private double startShortAttack() {
			ADSRctrl = ENVE_SHORTATTACK;
			enveStep = enveVol;
			enveStepPnt = 0;
			enveShortAttackCount = 65535;
			return alterShortAttack();
		}

		private double alterShortAttack() {
			int attack = (SIDAD >> 4) & 0x0F;
			enveStepAdd = ATTACK_RATES[attack];
			enveStepAddPnt = ATTACK_RATES_P[attack];
			ADSRctrl = ENVE_SHORTATTACK;
			return shortAttack();
		}

		private double shortAttack() {
			if (enveStep >= ATTACK_TAB_LEN || enveShortAttackCount == 0) {
				return startDecay();
			}
			enveVol = enveStep;
			enveShortAttackCount--;
			advance();
			return output();
		}

		private void advance() {
			enveStepPnt += enveStepAddPnt;
			enveStep += enveStepAdd + (enveStepPnt > 65535 ? 1 : 0);
			enveStepPnt &= 0xFFFF;
		}

		private double output() {
			int v = MEASURED_VOL[Math.max(0, Math.min(255, enveVol))];
			return v / 255.0;
		}

		private static void ensureRates(float sr) {
			if (Math.abs(ratesSr - sr) < 0.01f) {
				return;
			}
			synchronized (Env.class) {
				if (Math.abs(ratesSr - sr) < 0.01f) {
					return;
				}
				for (int i = 0; i < 16; i++) {
					double scaledenvelen = (ATTACK_TIMES[i] * sr) / 1000.0;
					if (scaledenvelen <= 0.0) {
						scaledenvelen = 1.0;
					}
					int scl = (int) Math.floor(scaledenvelen);
					if (scl < 1) {
						scl = 1;
					}
					ATTACK_RATES[i] = ATTACK_TAB_LEN / scl;
					ATTACK_RATES_P[i] = (int) (((ATTACK_TAB_LEN % scl) * 65536L) / scl);

					double scaledRelease = (DECAY_RELEASE_TIMES[i] * sr) / 1000.0;
					if (scaledRelease <= 0.0) {
						scaledRelease = 1.0;
					}
					int sclRel = (int) Math.floor(scaledRelease);
					if (sclRel < 1) {
						sclRel = 1;
					}
					DECAY_RATES[i] = RELEASE_TAB.length / sclRel;
					DECAY_RATES_P[i] = (int) (((RELEASE_TAB.length % sclRel) * 65536L) / sclRel);
				}
				ratesSr = sr;
			}
		}

		private static int clampNibble(int n) {
			return Math.max(0, Math.min(15, n));
		}
	}

	private static double quantizeToSidHz(double hz, double sidClockHz) {
		if (hz <= 0.0)
			return 0.0;
		// SID uses a 24-bit phase accumulator with a 16-bit frequency register.
		// f_out = f_reg * f_clk / 2^24
		double scale = 16777216.0; // 2^24
		int reg = (int) Math.round(hz * scale / sidClockHz);
		reg = Math.max(1, Math.min(0xFFFF, reg));
		return reg * sidClockHz / scale;
	}

	private static double onePoleAlpha(double cutoffHz, double sr) {
		if (cutoffHz <= 0.0)
			return 1.0;
		return 1.0 - Math.exp(-2.0 * Math.PI * cutoffHz / sr);
	}

	private static void writeWav(byte[] pcm, AudioFormat fmt, Path out) {
		long frames = pcm.length / fmt.getFrameSize();
		try (ByteArrayInputStream bais = new ByteArrayInputStream(pcm);
				AudioInputStream ais = new AudioInputStream(bais, fmt, frames)) {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out.toFile());
		} catch (IOException e) {
			throw new RuntimeException("Failed to write WAV: " + out, e);
		}
	}

}
