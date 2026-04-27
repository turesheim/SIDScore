// Generated from SIDScoreParser.g4 by ANTLR 4.13.1
package net.resheim.sidscore.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SIDScoreParser}.
 */
public interface SIDScoreParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(SIDScoreParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(SIDScoreParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(SIDScoreParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(SIDScoreParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#titleStmt}.
	 * @param ctx the parse tree
	 */
	void enterTitleStmt(SIDScoreParser.TitleStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#titleStmt}.
	 * @param ctx the parse tree
	 */
	void exitTitleStmt(SIDScoreParser.TitleStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#authorStmt}.
	 * @param ctx the parse tree
	 */
	void enterAuthorStmt(SIDScoreParser.AuthorStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#authorStmt}.
	 * @param ctx the parse tree
	 */
	void exitAuthorStmt(SIDScoreParser.AuthorStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#releasedStmt}.
	 * @param ctx the parse tree
	 */
	void enterReleasedStmt(SIDScoreParser.ReleasedStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#releasedStmt}.
	 * @param ctx the parse tree
	 */
	void exitReleasedStmt(SIDScoreParser.ReleasedStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tempoStmt}.
	 * @param ctx the parse tree
	 */
	void enterTempoStmt(SIDScoreParser.TempoStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tempoStmt}.
	 * @param ctx the parse tree
	 */
	void exitTempoStmt(SIDScoreParser.TempoStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#timeStmt}.
	 * @param ctx the parse tree
	 */
	void enterTimeStmt(SIDScoreParser.TimeStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#timeStmt}.
	 * @param ctx the parse tree
	 */
	void exitTimeStmt(SIDScoreParser.TimeStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#swingStmt}.
	 * @param ctx the parse tree
	 */
	void enterSwingStmt(SIDScoreParser.SwingStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#swingStmt}.
	 * @param ctx the parse tree
	 */
	void exitSwingStmt(SIDScoreParser.SwingStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#systemStmt}.
	 * @param ctx the parse tree
	 */
	void enterSystemStmt(SIDScoreParser.SystemStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#systemStmt}.
	 * @param ctx the parse tree
	 */
	void exitSystemStmt(SIDScoreParser.SystemStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#importStmt}.
	 * @param ctx the parse tree
	 */
	void enterImportStmt(SIDScoreParser.ImportStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#importStmt}.
	 * @param ctx the parse tree
	 */
	void exitImportStmt(SIDScoreParser.ImportStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#songBlock}.
	 * @param ctx the parse tree
	 */
	void enterSongBlock(SIDScoreParser.SongBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#songBlock}.
	 * @param ctx the parse tree
	 */
	void exitSongBlock(SIDScoreParser.SongBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#songStmt}.
	 * @param ctx the parse tree
	 */
	void enterSongStmt(SIDScoreParser.SongStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#songStmt}.
	 * @param ctx the parse tree
	 */
	void exitSongStmt(SIDScoreParser.SongStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectStmt(SIDScoreParser.EffectStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectStmt(SIDScoreParser.EffectStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectBodyStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectBodyStmt(SIDScoreParser.EffectBodyStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectBodyStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectBodyStmt(SIDScoreParser.EffectBodyStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectVoiceStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectVoiceStmt(SIDScoreParser.EffectVoiceStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectVoiceStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectVoiceStmt(SIDScoreParser.EffectVoiceStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectVoice}.
	 * @param ctx the parse tree
	 */
	void enterEffectVoice(SIDScoreParser.EffectVoiceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectVoice}.
	 * @param ctx the parse tree
	 */
	void exitEffectVoice(SIDScoreParser.EffectVoiceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectLengthStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectLengthStmt(SIDScoreParser.EffectLengthStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectLengthStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectLengthStmt(SIDScoreParser.EffectLengthStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectPriorityStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectPriorityStmt(SIDScoreParser.EffectPriorityStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectPriorityStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectPriorityStmt(SIDScoreParser.EffectPriorityStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectRetriggerStmt}.
	 * @param ctx the parse tree
	 */
	void enterEffectRetriggerStmt(SIDScoreParser.EffectRetriggerStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectRetriggerStmt}.
	 * @param ctx the parse tree
	 */
	void exitEffectRetriggerStmt(SIDScoreParser.EffectRetriggerStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectRetriggerMode}.
	 * @param ctx the parse tree
	 */
	void enterEffectRetriggerMode(SIDScoreParser.EffectRetriggerModeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectRetriggerMode}.
	 * @param ctx the parse tree
	 */
	void exitEffectRetriggerMode(SIDScoreParser.EffectRetriggerModeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectStep}.
	 * @param ctx the parse tree
	 */
	void enterEffectStep(SIDScoreParser.EffectStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectStep}.
	 * @param ctx the parse tree
	 */
	void exitEffectStep(SIDScoreParser.EffectStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectTick}.
	 * @param ctx the parse tree
	 */
	void enterEffectTick(SIDScoreParser.EffectTickContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectTick}.
	 * @param ctx the parse tree
	 */
	void exitEffectTick(SIDScoreParser.EffectTickContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectGroup}.
	 * @param ctx the parse tree
	 */
	void enterEffectGroup(SIDScoreParser.EffectGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectGroup}.
	 * @param ctx the parse tree
	 */
	void exitEffectGroup(SIDScoreParser.EffectGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectAssignment}.
	 * @param ctx the parse tree
	 */
	void enterEffectAssignment(SIDScoreParser.EffectAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectAssignment}.
	 * @param ctx the parse tree
	 */
	void exitEffectAssignment(SIDScoreParser.EffectAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectSweep}.
	 * @param ctx the parse tree
	 */
	void enterEffectSweep(SIDScoreParser.EffectSweepContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectSweep}.
	 * @param ctx the parse tree
	 */
	void exitEffectSweep(SIDScoreParser.EffectSweepContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectSweepParam}.
	 * @param ctx the parse tree
	 */
	void enterEffectSweepParam(SIDScoreParser.EffectSweepParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectSweepParam}.
	 * @param ctx the parse tree
	 */
	void exitEffectSweepParam(SIDScoreParser.EffectSweepParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectSweepValue}.
	 * @param ctx the parse tree
	 */
	void enterEffectSweepValue(SIDScoreParser.EffectSweepValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectSweepValue}.
	 * @param ctx the parse tree
	 */
	void exitEffectSweepValue(SIDScoreParser.EffectSweepValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#effectSweepCurve}.
	 * @param ctx the parse tree
	 */
	void enterEffectSweepCurve(SIDScoreParser.EffectSweepCurveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#effectSweepCurve}.
	 * @param ctx the parse tree
	 */
	void exitEffectSweepCurve(SIDScoreParser.EffectSweepCurveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#numericValue}.
	 * @param ctx the parse tree
	 */
	void enterNumericValue(SIDScoreParser.NumericValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#numericValue}.
	 * @param ctx the parse tree
	 */
	void exitNumericValue(SIDScoreParser.NumericValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#instrStmt}.
	 * @param ctx the parse tree
	 */
	void enterInstrStmt(SIDScoreParser.InstrStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#instrStmt}.
	 * @param ctx the parse tree
	 */
	void exitInstrStmt(SIDScoreParser.InstrStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#instrParam}.
	 * @param ctx the parse tree
	 */
	void enterInstrParam(SIDScoreParser.InstrParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#instrParam}.
	 * @param ctx the parse tree
	 */
	void exitInstrParam(SIDScoreParser.InstrParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#signedInt}.
	 * @param ctx the parse tree
	 */
	void enterSignedInt(SIDScoreParser.SignedIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#signedInt}.
	 * @param ctx the parse tree
	 */
	void exitSignedInt(SIDScoreParser.SignedIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#waveList}.
	 * @param ctx the parse tree
	 */
	void enterWaveList(SIDScoreParser.WaveListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#waveList}.
	 * @param ctx the parse tree
	 */
	void exitWaveList(SIDScoreParser.WaveListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#onOff}.
	 * @param ctx the parse tree
	 */
	void enterOnOff(SIDScoreParser.OnOffContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#onOff}.
	 * @param ctx the parse tree
	 */
	void exitOnOff(SIDScoreParser.OnOffContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#gateMode}.
	 * @param ctx the parse tree
	 */
	void enterGateMode(SIDScoreParser.GateModeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#gateMode}.
	 * @param ctx the parse tree
	 */
	void exitGateMode(SIDScoreParser.GateModeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#filterSpec}.
	 * @param ctx the parse tree
	 */
	void enterFilterSpec(SIDScoreParser.FilterSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#filterSpec}.
	 * @param ctx the parse tree
	 */
	void exitFilterSpec(SIDScoreParser.FilterSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#filterList}.
	 * @param ctx the parse tree
	 */
	void enterFilterList(SIDScoreParser.FilterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#filterList}.
	 * @param ctx the parse tree
	 */
	void exitFilterList(SIDScoreParser.FilterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#filterMode}.
	 * @param ctx the parse tree
	 */
	void enterFilterMode(SIDScoreParser.FilterModeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#filterMode}.
	 * @param ctx the parse tree
	 */
	void exitFilterMode(SIDScoreParser.FilterModeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableStmt}.
	 * @param ctx the parse tree
	 */
	void enterTableStmt(SIDScoreParser.TableStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableStmt}.
	 * @param ctx the parse tree
	 */
	void exitTableStmt(SIDScoreParser.TableStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableStep}.
	 * @param ctx the parse tree
	 */
	void enterTableStep(SIDScoreParser.TableStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableStep}.
	 * @param ctx the parse tree
	 */
	void exitTableStep(SIDScoreParser.TableStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableValue}.
	 * @param ctx the parse tree
	 */
	void enterTableValue(SIDScoreParser.TableValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableValue}.
	 * @param ctx the parse tree
	 */
	void exitTableValue(SIDScoreParser.TableValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableDuration}.
	 * @param ctx the parse tree
	 */
	void enterTableDuration(SIDScoreParser.TableDurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableDuration}.
	 * @param ctx the parse tree
	 */
	void exitTableDuration(SIDScoreParser.TableDurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableCtrl}.
	 * @param ctx the parse tree
	 */
	void enterTableCtrl(SIDScoreParser.TableCtrlContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableCtrl}.
	 * @param ctx the parse tree
	 */
	void exitTableCtrl(SIDScoreParser.TableCtrlContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tableCtrlItem}.
	 * @param ctx the parse tree
	 */
	void enterTableCtrlItem(SIDScoreParser.TableCtrlItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tableCtrlItem}.
	 * @param ctx the parse tree
	 */
	void exitTableCtrlItem(SIDScoreParser.TableCtrlItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#noteSpec}.
	 * @param ctx the parse tree
	 */
	void enterNoteSpec(SIDScoreParser.NoteSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#noteSpec}.
	 * @param ctx the parse tree
	 */
	void exitNoteSpec(SIDScoreParser.NoteSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#voiceBlock}.
	 * @param ctx the parse tree
	 */
	void enterVoiceBlock(SIDScoreParser.VoiceBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#voiceBlock}.
	 * @param ctx the parse tree
	 */
	void exitVoiceBlock(SIDScoreParser.VoiceBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#voiceItem}.
	 * @param ctx the parse tree
	 */
	void enterVoiceItem(SIDScoreParser.VoiceItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#voiceItem}.
	 * @param ctx the parse tree
	 */
	void exitVoiceItem(SIDScoreParser.VoiceItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#noteOrRestOrHit}.
	 * @param ctx the parse tree
	 */
	void enterNoteOrRestOrHit(SIDScoreParser.NoteOrRestOrHitContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#noteOrRestOrHit}.
	 * @param ctx the parse tree
	 */
	void exitNoteOrRestOrHit(SIDScoreParser.NoteOrRestOrHitContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#legatoScope}.
	 * @param ctx the parse tree
	 */
	void enterLegatoScope(SIDScoreParser.LegatoScopeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#legatoScope}.
	 * @param ctx the parse tree
	 */
	void exitLegatoScope(SIDScoreParser.LegatoScopeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#tuplet}.
	 * @param ctx the parse tree
	 */
	void enterTuplet(SIDScoreParser.TupletContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#tuplet}.
	 * @param ctx the parse tree
	 */
	void exitTuplet(SIDScoreParser.TupletContext ctx);
	/**
	 * Enter a parse tree produced by {@link SIDScoreParser#repeat}.
	 * @param ctx the parse tree
	 */
	void enterRepeat(SIDScoreParser.RepeatContext ctx);
	/**
	 * Exit a parse tree produced by {@link SIDScoreParser#repeat}.
	 * @param ctx the parse tree
	 */
	void exitRepeat(SIDScoreParser.RepeatContext ctx);
}