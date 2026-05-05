/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic Java Sound input device used by MIDI tests.
 * <p>
 * The production router talks to {@link javax.sound.midi.MidiSystem}, which
 * makes automated tests depend on whatever keyboard or virtual MIDI bus is
 * attached to the machine. This fixture is deliberately small and visible:
 * reviewers can validate each test by following {@link #send(MidiMessage)} to
 * the receiver installed by {@link MidiInputRouter#open(String, Map,
 * MidiInputRouter.EventListener)}.
 */
final class MockMidiInstrument implements MidiDevice {

	private final MockInfo info;
	private final int maxTransmitters;
	private final boolean failTransmitterOpen;
	private final List<MockTransmitter> transmitters = new ArrayList<>();
	private boolean open;
	private int openCount;
	private int closeCount;

	MockMidiInstrument(String name) {
		this(name, "SIDScore Test", "Mock MIDI input instrument", "1.0", 1, false);
	}

	MockMidiInstrument(String name, String vendor, String description, String version,
			int maxTransmitters, boolean failTransmitterOpen) {
		this.info = new MockInfo(name, vendor, description, version);
		this.maxTransmitters = maxTransmitters;
		this.failTransmitterOpen = failTransmitterOpen;
	}

	static AutoCloseable install(MockMidiInstrument... instruments) {
		return MidiInputRouter.useDeviceProviderForTesting(new MockDeviceProvider(instruments));
	}

	@Override
	public Info getDeviceInfo() {
		return info;
	}

	@Override
	public void open() throws MidiUnavailableException {
		open = true;
		openCount++;
	}

	@Override
	public void close() {
		for (MockTransmitter transmitter : transmitters) {
			transmitter.close();
		}
		open = false;
		closeCount++;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public long getMicrosecondPosition() {
		return 0;
	}

	@Override
	public int getMaxReceivers() {
		return 0;
	}

	@Override
	public int getMaxTransmitters() {
		return maxTransmitters;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		throw new MidiUnavailableException("Mock input instruments do not accept receiver output.");
	}

	@Override
	public List<Receiver> getReceivers() {
		return List.of();
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		if (failTransmitterOpen) {
			throw new MidiUnavailableException("Mock transmitter open failure");
		}
		MockTransmitter transmitter = new MockTransmitter();
		transmitters.add(transmitter);
		return transmitter;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		return List.copyOf(transmitters);
	}

	int openCount() {
		return openCount;
	}

	int closeCount() {
		return closeCount;
	}

	boolean hasReceiver() {
		return transmitters.stream().anyMatch(MockTransmitter::hasReceiver);
	}

	int transmitterCount() {
		return transmitters.size();
	}

	void send(MidiMessage message) {
		for (MockTransmitter transmitter : transmitters) {
			transmitter.send(message);
		}
	}

	static ShortMessage shortMessage(int command, int oneBasedChannel, int data1, int data2)
			throws Exception {
		ShortMessage message = new ShortMessage();
		message.setMessage(command, oneBasedChannel - 1, data1, data2);
		return message;
	}

	static RawMidiMessage raw(int... bytes) {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			data[i] = (byte) bytes[i];
		}
		return new RawMidiMessage(data);
	}

	private static final class MockInfo extends Info {
		MockInfo(String name, String vendor, String description, String version) {
			super(name, vendor, description, version);
		}
	}

	private static final class MockTransmitter implements Transmitter {
		private Receiver receiver;
		private boolean closed;

		@Override
		public void setReceiver(Receiver receiver) {
			this.receiver = receiver;
		}

		@Override
		public Receiver getReceiver() {
			return receiver;
		}

		@Override
		public void close() {
			closed = true;
			receiver = null;
		}

		boolean hasReceiver() {
			return receiver != null;
		}

		void send(MidiMessage message) {
			if (!closed && receiver != null) {
				receiver.send(message, -1);
			}
		}
	}

	private static final class MockDeviceProvider implements MidiInputRouter.DeviceProvider {
		private final MockMidiInstrument[] instruments;

		MockDeviceProvider(MockMidiInstrument[] instruments) {
			this.instruments = instruments.clone();
		}

		@Override
		public Info[] getMidiDeviceInfo() {
			Info[] infos = new Info[instruments.length];
			for (int i = 0; i < instruments.length; i++) {
				infos[i] = instruments[i].getDeviceInfo();
			}
			return infos;
		}

		@Override
		public MidiDevice getMidiDevice(Info info) throws MidiUnavailableException {
			for (MockMidiInstrument instrument : instruments) {
				if (instrument.getDeviceInfo() == info) {
					return instrument;
				}
			}
			throw new MidiUnavailableException("Unknown mock MIDI instrument: " + info.getName());
		}
	}

	static final class RawMidiMessage extends MidiMessage {
		RawMidiMessage(byte[] data) {
			super(data);
		}

		@Override
		public Object clone() {
			return new RawMidiMessage(getMessage());
		}
	}
}
