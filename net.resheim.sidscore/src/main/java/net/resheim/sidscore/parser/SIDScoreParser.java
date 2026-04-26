// Generated from SIDScoreParser.g4 by ANTLR 4.13.1
package net.resheim.sidscore.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SIDScoreParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TITLE=1, AUTHOR=2, RELEASED=3, TEMPO=4, TIME=5, SYSTEM=6, TUNE=7, IMPORT=8,
		AS=9, TABLE=10, LOOP=11, HOLD=12, INSTR=13, VOICE=14, SWING=15, SYNC=16,
		RING=17, FILTER=18, CUTOFF=19, RES=20, NOTEK=21, RESET=22, GATE=23, GATEMIN=24,
		RETRIGGER=25, LEGATO=26, LP=27, BP=28, HP=29, OFF=30, ON=31, PAL=32, NTSC=33,
		WAVE=34, ADSR=35, PW=36, HIPULSE=37, LOWPULSE=38, WAVESEQ=39, PWMIN=40,
		PWMAX=41, PWSWEEP=42, PWSEQ=43, FILTERSEQ=44, GATESEQ=45, PITCHSEQ=46,
		WAVEVAL=47, LEG_START=48, LEG_END=49, TUPLET_START=50, RBRACE=51, LBRACE=52,
		AT=53, LPAREN=54, AMP=55, GT=56, LT=57, COLON=58, EQ=59, COMMA=60, SLASH=61,
		PLUS=62, MINUS=63, REPEAT_END=64, OCTAVE=65, LENGTH=66, NOTE=67, REST=68,
		HIT=69, ID=70, INT=71, HEX=72, STRING=73, COMMENT=74, WS=75;
	public static final int
		RULE_file = 0, RULE_stmt = 1, RULE_titleStmt = 2, RULE_authorStmt = 3,
		RULE_releasedStmt = 4, RULE_tempoStmt = 5, RULE_timeStmt = 6, RULE_swingStmt = 7,
		RULE_systemStmt = 8, RULE_importStmt = 9, RULE_songBlock = 10, RULE_songStmt = 11,
		RULE_instrStmt = 12, RULE_instrParam = 13, RULE_signedInt = 14, RULE_waveList = 15,
		RULE_onOff = 16, RULE_gateMode = 17, RULE_filterSpec = 18, RULE_filterList = 19,
		RULE_filterMode = 20, RULE_tableStmt = 21, RULE_tableStep = 22, RULE_tableValue = 23,
		RULE_tableDuration = 24, RULE_tableCtrl = 25, RULE_tableCtrlItem = 26,
		RULE_noteSpec = 27, RULE_voiceBlock = 28, RULE_voiceItem = 29, RULE_noteOrRestOrHit = 30,
		RULE_legatoScope = 31, RULE_tuplet = 32, RULE_repeat = 33;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "stmt", "titleStmt", "authorStmt", "releasedStmt", "tempoStmt",
			"timeStmt", "swingStmt", "systemStmt", "importStmt", "songBlock", "songStmt",
			"instrStmt", "instrParam", "signedInt", "waveList", "onOff", "gateMode",
			"filterSpec", "filterList", "filterMode", "tableStmt", "tableStep", "tableValue",
			"tableDuration", "tableCtrl", "tableCtrlItem", "noteSpec", "voiceBlock",
			"voiceItem", "noteOrRestOrHit", "legatoScope", "tuplet", "repeat"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'TITLE'", "'AUTHOR'", "'RELEASED'", "'TEMPO'", "'TIME'", "'SYSTEM'",
			"'TUNE'", "'IMPORT'", "'AS'", "'TABLE'", "'LOOP'", "'HOLD'", "'INSTR'",
			"'VOICE'", "'SWING'", "'SYNC'", "'RING'", "'FILTER'", "'CUTOFF'", "'RES'",
			"'NOTE'", "'RESET'", "'GATE'", "'GATEMIN'", "'RETRIGGER'", "'LEGATO'",
			"'LP'", "'BP'", "'HP'", "'OFF'", "'ON'", "'PAL'", "'NTSC'", "'WAVE'",
			"'ADSR'", "'PW'", "'HIPULSE'", "'LOWPULSE'", "'WAVESEQ'", "'PWMIN'",
			"'PWMAX'", "'PWSWEEP'", "'PWSEQ'", "'FILTERSEQ'", "'GATESEQ'", "'PITCHSEQ'",
			null, "'(leg)'", "'(end)'", "'T{'", "'}'", "'{'", "'@'", "'('", "'&'",
			"'>'", "'<'", "':'", "'='", "','", "'/'", "'+'", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TITLE", "AUTHOR", "RELEASED", "TEMPO", "TIME", "SYSTEM", "TUNE",
			"IMPORT", "AS", "TABLE", "LOOP", "HOLD", "INSTR", "VOICE", "SWING", "SYNC",
			"RING", "FILTER", "CUTOFF", "RES", "NOTEK", "RESET", "GATE", "GATEMIN",
			"RETRIGGER", "LEGATO", "LP", "BP", "HP", "OFF", "ON", "PAL", "NTSC",
			"WAVE", "ADSR", "PW", "HIPULSE", "LOWPULSE", "WAVESEQ", "PWMIN", "PWMAX",
			"PWSWEEP", "PWSEQ", "FILTERSEQ", "GATESEQ", "PITCHSEQ", "WAVEVAL", "LEG_START",
			"LEG_END", "TUPLET_START", "RBRACE", "LBRACE", "AT", "LPAREN", "AMP",
			"GT", "LT", "COLON", "EQ", "COMMA", "SLASH", "PLUS", "MINUS", "REPEAT_END",
			"OCTAVE", "LENGTH", "NOTE", "REST", "HIT", "ID", "INT", "HEX", "STRING",
			"COMMENT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SIDScoreParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SIDScoreParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(SIDScoreParser.EOF, 0); }
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitFile(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 58878L) != 0)) {
				{
				{
				setState(68);
				stmt();
				}
				}
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(74);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StmtContext extends ParserRuleContext {
		public TitleStmtContext titleStmt() {
			return getRuleContext(TitleStmtContext.class,0);
		}
		public AuthorStmtContext authorStmt() {
			return getRuleContext(AuthorStmtContext.class,0);
		}
		public ReleasedStmtContext releasedStmt() {
			return getRuleContext(ReleasedStmtContext.class,0);
		}
		public TempoStmtContext tempoStmt() {
			return getRuleContext(TempoStmtContext.class,0);
		}
		public TimeStmtContext timeStmt() {
			return getRuleContext(TimeStmtContext.class,0);
		}
		public SystemStmtContext systemStmt() {
			return getRuleContext(SystemStmtContext.class,0);
		}
		public ImportStmtContext importStmt() {
			return getRuleContext(ImportStmtContext.class,0);
		}
		public SongBlockContext songBlock() {
			return getRuleContext(SongBlockContext.class,0);
		}
		public TableStmtContext tableStmt() {
			return getRuleContext(TableStmtContext.class,0);
		}
		public InstrStmtContext instrStmt() {
			return getRuleContext(InstrStmtContext.class,0);
		}
		public SwingStmtContext swingStmt() {
			return getRuleContext(SwingStmtContext.class,0);
		}
		public VoiceBlockContext voiceBlock() {
			return getRuleContext(VoiceBlockContext.class,0);
		}
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitStmt(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_stmt);
		try {
			setState(88);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TITLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				titleStmt();
				}
				break;
			case AUTHOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(77);
				authorStmt();
				}
				break;
			case RELEASED:
				enterOuterAlt(_localctx, 3);
				{
				setState(78);
				releasedStmt();
				}
				break;
			case TEMPO:
				enterOuterAlt(_localctx, 4);
				{
				setState(79);
				tempoStmt();
				}
				break;
			case TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(80);
				timeStmt();
				}
				break;
			case SYSTEM:
				enterOuterAlt(_localctx, 6);
				{
				setState(81);
				systemStmt();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 7);
				{
				setState(82);
				importStmt();
				}
				break;
			case TUNE:
				enterOuterAlt(_localctx, 8);
				{
				setState(83);
				songBlock();
				}
				break;
			case TABLE:
				enterOuterAlt(_localctx, 9);
				{
				setState(84);
				tableStmt();
				}
				break;
			case INSTR:
				enterOuterAlt(_localctx, 10);
				{
				setState(85);
				instrStmt();
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 11);
				{
				setState(86);
				swingStmt();
				}
				break;
			case VOICE:
				enterOuterAlt(_localctx, 12);
				{
				setState(87);
				voiceBlock();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TitleStmtContext extends ParserRuleContext {
		public TerminalNode TITLE() { return getToken(SIDScoreParser.TITLE, 0); }
		public TerminalNode STRING() { return getToken(SIDScoreParser.STRING, 0); }
		public TitleStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_titleStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTitleStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTitleStmt(this);
		}
	}

	public final TitleStmtContext titleStmt() throws RecognitionException {
		TitleStmtContext _localctx = new TitleStmtContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_titleStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			match(TITLE);
			setState(91);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AuthorStmtContext extends ParserRuleContext {
		public TerminalNode AUTHOR() { return getToken(SIDScoreParser.AUTHOR, 0); }
		public TerminalNode STRING() { return getToken(SIDScoreParser.STRING, 0); }
		public AuthorStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_authorStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterAuthorStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitAuthorStmt(this);
		}
	}

	public final AuthorStmtContext authorStmt() throws RecognitionException {
		AuthorStmtContext _localctx = new AuthorStmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_authorStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			match(AUTHOR);
			setState(94);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReleasedStmtContext extends ParserRuleContext {
		public TerminalNode RELEASED() { return getToken(SIDScoreParser.RELEASED, 0); }
		public TerminalNode STRING() { return getToken(SIDScoreParser.STRING, 0); }
		public ReleasedStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_releasedStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterReleasedStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitReleasedStmt(this);
		}
	}

	public final ReleasedStmtContext releasedStmt() throws RecognitionException {
		ReleasedStmtContext _localctx = new ReleasedStmtContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_releasedStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(RELEASED);
			setState(97);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TempoStmtContext extends ParserRuleContext {
		public TerminalNode TEMPO() { return getToken(SIDScoreParser.TEMPO, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TempoStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tempoStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTempoStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTempoStmt(this);
		}
	}

	public final TempoStmtContext tempoStmt() throws RecognitionException {
		TempoStmtContext _localctx = new TempoStmtContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tempoStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			match(TEMPO);
			setState(100);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TimeStmtContext extends ParserRuleContext {
		public TerminalNode TIME() { return getToken(SIDScoreParser.TIME, 0); }
		public List<TerminalNode> INT() { return getTokens(SIDScoreParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(SIDScoreParser.INT, i);
		}
		public TerminalNode SLASH() { return getToken(SIDScoreParser.SLASH, 0); }
		public TimeStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTimeStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTimeStmt(this);
		}
	}

	public final TimeStmtContext timeStmt() throws RecognitionException {
		TimeStmtContext _localctx = new TimeStmtContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_timeStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(TIME);
			setState(103);
			match(INT);
			setState(104);
			match(SLASH);
			setState(105);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwingStmtContext extends ParserRuleContext {
		public TerminalNode SWING() { return getToken(SIDScoreParser.SWING, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode OFF() { return getToken(SIDScoreParser.OFF, 0); }
		public SwingStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_swingStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterSwingStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitSwingStmt(this);
		}
	}

	public final SwingStmtContext swingStmt() throws RecognitionException {
		SwingStmtContext _localctx = new SwingStmtContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_swingStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(SWING);
			setState(108);
			_la = _input.LA(1);
			if ( !(_la==OFF || _la==INT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SystemStmtContext extends ParserRuleContext {
		public TerminalNode SYSTEM() { return getToken(SIDScoreParser.SYSTEM, 0); }
		public TerminalNode PAL() { return getToken(SIDScoreParser.PAL, 0); }
		public TerminalNode NTSC() { return getToken(SIDScoreParser.NTSC, 0); }
		public SystemStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_systemStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterSystemStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitSystemStmt(this);
		}
	}

	public final SystemStmtContext systemStmt() throws RecognitionException {
		SystemStmtContext _localctx = new SystemStmtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_systemStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(SYSTEM);
			setState(111);
			_la = _input.LA(1);
			if ( !(_la==PAL || _la==NTSC) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportStmtContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(SIDScoreParser.IMPORT, 0); }
		public TerminalNode STRING() { return getToken(SIDScoreParser.STRING, 0); }
		public TerminalNode AS() { return getToken(SIDScoreParser.AS, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public ImportStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterImportStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitImportStmt(this);
		}
	}

	public final ImportStmtContext importStmt() throws RecognitionException {
		ImportStmtContext _localctx = new ImportStmtContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_importStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(IMPORT);
			setState(114);
			match(STRING);
			setState(115);
			match(AS);
			setState(116);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SongBlockContext extends ParserRuleContext {
		public TerminalNode TUNE() { return getToken(SIDScoreParser.TUNE, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode LBRACE() { return getToken(SIDScoreParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(SIDScoreParser.RBRACE, 0); }
		public List<SongStmtContext> songStmt() {
			return getRuleContexts(SongStmtContext.class);
		}
		public SongStmtContext songStmt(int i) {
			return getRuleContext(SongStmtContext.class,i);
		}
		public SongBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_songBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterSongBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitSongBlock(this);
		}
	}

	public final SongBlockContext songBlock() throws RecognitionException {
		SongBlockContext _localctx = new SongBlockContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_songBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			match(TUNE);
			setState(119);
			match(INT);
			setState(120);
			match(LBRACE);
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 49278L) != 0)) {
				{
				{
				setState(121);
				songStmt();
				}
				}
				setState(126);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(127);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SongStmtContext extends ParserRuleContext {
		public TitleStmtContext titleStmt() {
			return getRuleContext(TitleStmtContext.class,0);
		}
		public AuthorStmtContext authorStmt() {
			return getRuleContext(AuthorStmtContext.class,0);
		}
		public ReleasedStmtContext releasedStmt() {
			return getRuleContext(ReleasedStmtContext.class,0);
		}
		public TempoStmtContext tempoStmt() {
			return getRuleContext(TempoStmtContext.class,0);
		}
		public TimeStmtContext timeStmt() {
			return getRuleContext(TimeStmtContext.class,0);
		}
		public SystemStmtContext systemStmt() {
			return getRuleContext(SystemStmtContext.class,0);
		}
		public SwingStmtContext swingStmt() {
			return getRuleContext(SwingStmtContext.class,0);
		}
		public VoiceBlockContext voiceBlock() {
			return getRuleContext(VoiceBlockContext.class,0);
		}
		public SongStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_songStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterSongStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitSongStmt(this);
		}
	}

	public final SongStmtContext songStmt() throws RecognitionException {
		SongStmtContext _localctx = new SongStmtContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_songStmt);
		try {
			setState(137);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TITLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(129);
				titleStmt();
				}
				break;
			case AUTHOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(130);
				authorStmt();
				}
				break;
			case RELEASED:
				enterOuterAlt(_localctx, 3);
				{
				setState(131);
				releasedStmt();
				}
				break;
			case TEMPO:
				enterOuterAlt(_localctx, 4);
				{
				setState(132);
				tempoStmt();
				}
				break;
			case TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(133);
				timeStmt();
				}
				break;
			case SYSTEM:
				enterOuterAlt(_localctx, 6);
				{
				setState(134);
				systemStmt();
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 7);
				{
				setState(135);
				swingStmt();
				}
				break;
			case VOICE:
				enterOuterAlt(_localctx, 8);
				{
				setState(136);
				voiceBlock();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InstrStmtContext extends ParserRuleContext {
		public TerminalNode INSTR() { return getToken(SIDScoreParser.INSTR, 0); }
		public TerminalNode ID() { return getToken(SIDScoreParser.ID, 0); }
		public TerminalNode STRING() { return getToken(SIDScoreParser.STRING, 0); }
		public List<InstrParamContext> instrParam() {
			return getRuleContexts(InstrParamContext.class);
		}
		public InstrParamContext instrParam(int i) {
			return getRuleContext(InstrParamContext.class,i);
		}
		public InstrStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instrStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterInstrStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitInstrStmt(this);
		}
	}

	public final InstrStmtContext instrStmt() throws RecognitionException {
		InstrStmtContext _localctx = new InstrStmtContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_instrStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(INSTR);
			setState(140);
			match(ID);
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				{
				setState(141);
				match(STRING);
				}
				break;
			case SYNC:
			case RING:
			case FILTER:
			case CUTOFF:
			case RES:
			case GATE:
			case GATEMIN:
			case WAVE:
			case ADSR:
			case PW:
			case HIPULSE:
			case LOWPULSE:
			case WAVESEQ:
			case PWMIN:
			case PWMAX:
			case PWSWEEP:
			case PWSEQ:
			case FILTERSEQ:
			case GATESEQ:
			case PITCHSEQ:
				{
				setState(143);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(142);
					instrParam();
					}
					}
					setState(145);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 140720335683584L) != 0) );
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InstrParamContext extends ParserRuleContext {
		public TerminalNode WAVE() { return getToken(SIDScoreParser.WAVE, 0); }
		public TerminalNode EQ() { return getToken(SIDScoreParser.EQ, 0); }
		public WaveListContext waveList() {
			return getRuleContext(WaveListContext.class,0);
		}
		public TerminalNode ADSR() { return getToken(SIDScoreParser.ADSR, 0); }
		public List<TerminalNode> INT() { return getTokens(SIDScoreParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(SIDScoreParser.INT, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SIDScoreParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SIDScoreParser.COMMA, i);
		}
		public TerminalNode PW() { return getToken(SIDScoreParser.PW, 0); }
		public TerminalNode HEX() { return getToken(SIDScoreParser.HEX, 0); }
		public TerminalNode HIPULSE() { return getToken(SIDScoreParser.HIPULSE, 0); }
		public TerminalNode LOWPULSE() { return getToken(SIDScoreParser.LOWPULSE, 0); }
		public TerminalNode FILTER() { return getToken(SIDScoreParser.FILTER, 0); }
		public FilterSpecContext filterSpec() {
			return getRuleContext(FilterSpecContext.class,0);
		}
		public TerminalNode CUTOFF() { return getToken(SIDScoreParser.CUTOFF, 0); }
		public TerminalNode RES() { return getToken(SIDScoreParser.RES, 0); }
		public TerminalNode GATE() { return getToken(SIDScoreParser.GATE, 0); }
		public GateModeContext gateMode() {
			return getRuleContext(GateModeContext.class,0);
		}
		public TerminalNode GATEMIN() { return getToken(SIDScoreParser.GATEMIN, 0); }
		public TerminalNode WAVESEQ() { return getToken(SIDScoreParser.WAVESEQ, 0); }
		public TerminalNode ID() { return getToken(SIDScoreParser.ID, 0); }
		public TerminalNode PWMIN() { return getToken(SIDScoreParser.PWMIN, 0); }
		public TerminalNode PWMAX() { return getToken(SIDScoreParser.PWMAX, 0); }
		public TerminalNode PWSWEEP() { return getToken(SIDScoreParser.PWSWEEP, 0); }
		public SignedIntContext signedInt() {
			return getRuleContext(SignedIntContext.class,0);
		}
		public TerminalNode PWSEQ() { return getToken(SIDScoreParser.PWSEQ, 0); }
		public TerminalNode FILTERSEQ() { return getToken(SIDScoreParser.FILTERSEQ, 0); }
		public TerminalNode GATESEQ() { return getToken(SIDScoreParser.GATESEQ, 0); }
		public TerminalNode PITCHSEQ() { return getToken(SIDScoreParser.PITCHSEQ, 0); }
		public TerminalNode SYNC() { return getToken(SIDScoreParser.SYNC, 0); }
		public OnOffContext onOff() {
			return getRuleContext(OnOffContext.class,0);
		}
		public TerminalNode RING() { return getToken(SIDScoreParser.RING, 0); }
		public InstrParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instrParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterInstrParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitInstrParam(this);
		}
	}

	public final InstrParamContext instrParam() throws RecognitionException {
		InstrParamContext _localctx = new InstrParamContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_instrParam);
		int _la;
		try {
			setState(215);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(149);
				match(WAVE);
				setState(150);
				match(EQ);
				setState(151);
				waveList();
				}
				break;
			case ADSR:
				enterOuterAlt(_localctx, 2);
				{
				setState(152);
				match(ADSR);
				setState(153);
				match(EQ);
				setState(154);
				match(INT);
				setState(155);
				match(COMMA);
				setState(156);
				match(INT);
				setState(157);
				match(COMMA);
				setState(158);
				match(INT);
				setState(159);
				match(COMMA);
				setState(160);
				match(INT);
				}
				break;
			case PW:
				enterOuterAlt(_localctx, 3);
				{
				setState(161);
				match(PW);
				setState(162);
				match(EQ);
				setState(163);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case HIPULSE:
				enterOuterAlt(_localctx, 4);
				{
				setState(164);
				match(HIPULSE);
				setState(165);
				match(EQ);
				setState(166);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case LOWPULSE:
				enterOuterAlt(_localctx, 5);
				{
				setState(167);
				match(LOWPULSE);
				setState(168);
				match(EQ);
				setState(169);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case FILTER:
				enterOuterAlt(_localctx, 6);
				{
				setState(170);
				match(FILTER);
				setState(171);
				match(EQ);
				setState(172);
				filterSpec();
				}
				break;
			case CUTOFF:
				enterOuterAlt(_localctx, 7);
				{
				setState(173);
				match(CUTOFF);
				setState(174);
				match(EQ);
				setState(175);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case RES:
				enterOuterAlt(_localctx, 8);
				{
				setState(176);
				match(RES);
				setState(177);
				match(EQ);
				setState(178);
				match(INT);
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 9);
				{
				setState(179);
				match(GATE);
				setState(180);
				match(EQ);
				setState(181);
				gateMode();
				}
				break;
			case GATEMIN:
				enterOuterAlt(_localctx, 10);
				{
				setState(182);
				match(GATEMIN);
				setState(183);
				match(EQ);
				setState(184);
				match(INT);
				}
				break;
			case WAVESEQ:
				enterOuterAlt(_localctx, 11);
				{
				setState(185);
				match(WAVESEQ);
				setState(186);
				match(EQ);
				setState(187);
				match(ID);
				}
				break;
			case PWMIN:
				enterOuterAlt(_localctx, 12);
				{
				setState(188);
				match(PWMIN);
				setState(189);
				match(EQ);
				setState(190);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case PWMAX:
				enterOuterAlt(_localctx, 13);
				{
				setState(191);
				match(PWMAX);
				setState(192);
				match(EQ);
				setState(193);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==HEX) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case PWSWEEP:
				enterOuterAlt(_localctx, 14);
				{
				setState(194);
				match(PWSWEEP);
				setState(195);
				match(EQ);
				setState(196);
				signedInt();
				}
				break;
			case PWSEQ:
				enterOuterAlt(_localctx, 15);
				{
				setState(197);
				match(PWSEQ);
				setState(198);
				match(EQ);
				setState(199);
				match(ID);
				}
				break;
			case FILTERSEQ:
				enterOuterAlt(_localctx, 16);
				{
				setState(200);
				match(FILTERSEQ);
				setState(201);
				match(EQ);
				setState(202);
				match(ID);
				}
				break;
			case GATESEQ:
				enterOuterAlt(_localctx, 17);
				{
				setState(203);
				match(GATESEQ);
				setState(204);
				match(EQ);
				setState(205);
				match(ID);
				}
				break;
			case PITCHSEQ:
				enterOuterAlt(_localctx, 18);
				{
				setState(206);
				match(PITCHSEQ);
				setState(207);
				match(EQ);
				setState(208);
				match(ID);
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 19);
				{
				setState(209);
				match(SYNC);
				setState(210);
				match(EQ);
				setState(211);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 20);
				{
				setState(212);
				match(RING);
				setState(213);
				match(EQ);
				setState(214);
				onOff();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignedIntContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode PLUS() { return getToken(SIDScoreParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SIDScoreParser.MINUS, 0); }
		public SignedIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterSignedInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitSignedInt(this);
		}
	}

	public final SignedIntContext signedInt() throws RecognitionException {
		SignedIntContext _localctx = new SignedIntContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_signedInt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(217);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(220);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WaveListContext extends ParserRuleContext {
		public List<TerminalNode> WAVEVAL() { return getTokens(SIDScoreParser.WAVEVAL); }
		public TerminalNode WAVEVAL(int i) {
			return getToken(SIDScoreParser.WAVEVAL, i);
		}
		public List<TerminalNode> PLUS() { return getTokens(SIDScoreParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(SIDScoreParser.PLUS, i);
		}
		public WaveListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_waveList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterWaveList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitWaveList(this);
		}
	}

	public final WaveListContext waveList() throws RecognitionException {
		WaveListContext _localctx = new WaveListContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_waveList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(222);
			match(WAVEVAL);
			setState(227);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(223);
				match(PLUS);
				setState(224);
				match(WAVEVAL);
				}
				}
				setState(229);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OnOffContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(SIDScoreParser.ON, 0); }
		public TerminalNode OFF() { return getToken(SIDScoreParser.OFF, 0); }
		public OnOffContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_onOff; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterOnOff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitOnOff(this);
		}
	}

	public final OnOffContext onOff() throws RecognitionException {
		OnOffContext _localctx = new OnOffContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_onOff);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			_la = _input.LA(1);
			if ( !(_la==OFF || _la==ON) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GateModeContext extends ParserRuleContext {
		public TerminalNode RETRIGGER() { return getToken(SIDScoreParser.RETRIGGER, 0); }
		public TerminalNode LEGATO() { return getToken(SIDScoreParser.LEGATO, 0); }
		public GateModeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gateMode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterGateMode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitGateMode(this);
		}
	}

	public final GateModeContext gateMode() throws RecognitionException {
		GateModeContext _localctx = new GateModeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_gateMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			_la = _input.LA(1);
			if ( !(_la==RETRIGGER || _la==LEGATO) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterSpecContext extends ParserRuleContext {
		public TerminalNode OFF() { return getToken(SIDScoreParser.OFF, 0); }
		public FilterListContext filterList() {
			return getRuleContext(FilterListContext.class,0);
		}
		public FilterSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterFilterSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitFilterSpec(this);
		}
	}

	public final FilterSpecContext filterSpec() throws RecognitionException {
		FilterSpecContext _localctx = new FilterSpecContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_filterSpec);
		try {
			setState(236);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OFF:
				enterOuterAlt(_localctx, 1);
				{
				setState(234);
				match(OFF);
				}
				break;
			case LP:
			case BP:
			case HP:
				enterOuterAlt(_localctx, 2);
				{
				setState(235);
				filterList();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterListContext extends ParserRuleContext {
		public List<FilterModeContext> filterMode() {
			return getRuleContexts(FilterModeContext.class);
		}
		public FilterModeContext filterMode(int i) {
			return getRuleContext(FilterModeContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(SIDScoreParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(SIDScoreParser.PLUS, i);
		}
		public FilterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterFilterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitFilterList(this);
		}
	}

	public final FilterListContext filterList() throws RecognitionException {
		FilterListContext _localctx = new FilterListContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_filterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			filterMode();
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(239);
				match(PLUS);
				setState(240);
				filterMode();
				}
				}
				setState(245);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterModeContext extends ParserRuleContext {
		public TerminalNode LP() { return getToken(SIDScoreParser.LP, 0); }
		public TerminalNode BP() { return getToken(SIDScoreParser.BP, 0); }
		public TerminalNode HP() { return getToken(SIDScoreParser.HP, 0); }
		public FilterModeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterMode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterFilterMode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitFilterMode(this);
		}
	}

	public final FilterModeContext filterMode() throws RecognitionException {
		FilterModeContext _localctx = new FilterModeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_filterMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(246);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 939524096L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableStmtContext extends ParserRuleContext {
		public TerminalNode TABLE() { return getToken(SIDScoreParser.TABLE, 0); }
		public List<TerminalNode> ID() { return getTokens(SIDScoreParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(SIDScoreParser.ID, i);
		}
		public TerminalNode LBRACE() { return getToken(SIDScoreParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(SIDScoreParser.RBRACE, 0); }
		public List<TableStepContext> tableStep() {
			return getRuleContexts(TableStepContext.class);
		}
		public TableStepContext tableStep(int i) {
			return getRuleContext(TableStepContext.class,i);
		}
		public TableStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableStmt(this);
		}
	}

	public final TableStmtContext tableStmt() throws RecognitionException {
		TableStmtContext _localctx = new TableStmtContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_tableStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(TABLE);
			setState(249);
			match(ID);
			setState(250);
			match(ID);
			setState(251);
			match(LBRACE);
			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 11)) & ~0x3f) == 0 && ((1L << (_la - 11)) & 3465519981991042145L) != 0)) {
				{
				{
				setState(252);
				tableStep();
				}
				}
				setState(257);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(258);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableStepContext extends ParserRuleContext {
		public TableValueContext tableValue() {
			return getRuleContext(TableValueContext.class,0);
		}
		public TerminalNode AT() { return getToken(SIDScoreParser.AT, 0); }
		public TableDurationContext tableDuration() {
			return getRuleContext(TableDurationContext.class,0);
		}
		public TerminalNode HOLD() { return getToken(SIDScoreParser.HOLD, 0); }
		public TableCtrlContext tableCtrl() {
			return getRuleContext(TableCtrlContext.class,0);
		}
		public TerminalNode LOOP() { return getToken(SIDScoreParser.LOOP, 0); }
		public TableStepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableStep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableStep(this);
		}
	}

	public final TableStepContext tableStep() throws RecognitionException {
		TableStepContext _localctx = new TableStepContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_tableStep);
		try {
			setState(275);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(260);
				tableValue();
				setState(261);
				match(AT);
				setState(262);
				tableDuration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(264);
				tableValue();
				setState(265);
				match(HOLD);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(267);
				tableCtrl();
				setState(268);
				match(AT);
				setState(269);
				tableDuration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(271);
				tableCtrl();
				setState(272);
				match(HOLD);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(274);
				match(LOOP);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableValueContext extends ParserRuleContext {
		public WaveListContext waveList() {
			return getRuleContext(WaveListContext.class,0);
		}
		public TerminalNode ON() { return getToken(SIDScoreParser.ON, 0); }
		public TerminalNode OFF() { return getToken(SIDScoreParser.OFF, 0); }
		public TerminalNode HEX() { return getToken(SIDScoreParser.HEX, 0); }
		public SignedIntContext signedInt() {
			return getRuleContext(SignedIntContext.class,0);
		}
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TableValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableValue(this);
		}
	}

	public final TableValueContext tableValue() throws RecognitionException {
		TableValueContext _localctx = new TableValueContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_tableValue);
		try {
			setState(283);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(277);
				waveList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(278);
				match(ON);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(279);
				match(OFF);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(280);
				match(HEX);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(281);
				signedInt();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(282);
				match(INT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableDurationContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode HOLD() { return getToken(SIDScoreParser.HOLD, 0); }
		public TableDurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableDuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableDuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableDuration(this);
		}
	}

	public final TableDurationContext tableDuration() throws RecognitionException {
		TableDurationContext _localctx = new TableDurationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_tableDuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285);
			_la = _input.LA(1);
			if ( !(_la==HOLD || _la==INT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableCtrlContext extends ParserRuleContext {
		public List<TableCtrlItemContext> tableCtrlItem() {
			return getRuleContexts(TableCtrlItemContext.class);
		}
		public TableCtrlItemContext tableCtrlItem(int i) {
			return getRuleContext(TableCtrlItemContext.class,i);
		}
		public TableCtrlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableCtrl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableCtrl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableCtrl(this);
		}
	}

	public final TableCtrlContext tableCtrl() throws RecognitionException {
		TableCtrlContext _localctx = new TableCtrlContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_tableCtrl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(287);
				tableCtrlItem();
				}
				}
				setState(290);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 17194745856L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableCtrlItemContext extends ParserRuleContext {
		public TerminalNode WAVE() { return getToken(SIDScoreParser.WAVE, 0); }
		public TerminalNode EQ() { return getToken(SIDScoreParser.EQ, 0); }
		public WaveListContext waveList() {
			return getRuleContext(WaveListContext.class,0);
		}
		public TerminalNode NOTEK() { return getToken(SIDScoreParser.NOTEK, 0); }
		public NoteSpecContext noteSpec() {
			return getRuleContext(NoteSpecContext.class,0);
		}
		public TerminalNode GATE() { return getToken(SIDScoreParser.GATE, 0); }
		public OnOffContext onOff() {
			return getRuleContext(OnOffContext.class,0);
		}
		public TerminalNode RING() { return getToken(SIDScoreParser.RING, 0); }
		public TerminalNode SYNC() { return getToken(SIDScoreParser.SYNC, 0); }
		public TerminalNode RESET() { return getToken(SIDScoreParser.RESET, 0); }
		public TableCtrlItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableCtrlItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTableCtrlItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTableCtrlItem(this);
		}
	}

	public final TableCtrlItemContext tableCtrlItem() throws RecognitionException {
		TableCtrlItemContext _localctx = new TableCtrlItemContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_tableCtrlItem);
		try {
			setState(308);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(292);
				match(WAVE);
				setState(293);
				match(EQ);
				setState(294);
				waveList();
				}
				break;
			case NOTEK:
				enterOuterAlt(_localctx, 2);
				{
				setState(295);
				match(NOTEK);
				setState(296);
				match(EQ);
				setState(297);
				noteSpec();
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(298);
				match(GATE);
				setState(299);
				match(EQ);
				setState(300);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 4);
				{
				setState(301);
				match(RING);
				setState(302);
				match(EQ);
				setState(303);
				onOff();
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 5);
				{
				setState(304);
				match(SYNC);
				setState(305);
				match(EQ);
				setState(306);
				onOff();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 6);
				{
				setState(307);
				match(RESET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NoteSpecContext extends ParserRuleContext {
		public TerminalNode NOTE() { return getToken(SIDScoreParser.NOTE, 0); }
		public SignedIntContext signedInt() {
			return getRuleContext(SignedIntContext.class,0);
		}
		public TerminalNode OFF() { return getToken(SIDScoreParser.OFF, 0); }
		public NoteSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_noteSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterNoteSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitNoteSpec(this);
		}
	}

	public final NoteSpecContext noteSpec() throws RecognitionException {
		NoteSpecContext _localctx = new NoteSpecContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_noteSpec);
		try {
			setState(313);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				match(NOTE);
				}
				break;
			case PLUS:
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				signedInt();
				}
				break;
			case OFF:
				enterOuterAlt(_localctx, 3);
				{
				setState(312);
				match(OFF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VoiceBlockContext extends ParserRuleContext {
		public TerminalNode VOICE() { return getToken(SIDScoreParser.VOICE, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode ID() { return getToken(SIDScoreParser.ID, 0); }
		public TerminalNode COLON() { return getToken(SIDScoreParser.COLON, 0); }
		public List<VoiceItemContext> voiceItem() {
			return getRuleContexts(VoiceItemContext.class);
		}
		public VoiceItemContext voiceItem(int i) {
			return getRuleContext(VoiceItemContext.class,i);
		}
		public VoiceBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_voiceBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterVoiceBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitVoiceBlock(this);
		}
	}

	public final VoiceBlockContext voiceBlock() throws RecognitionException {
		VoiceBlockContext _localctx = new VoiceBlockContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_voiceBlock);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(315);
			match(VOICE);
			setState(316);
			match(INT);
			setState(317);
			match(ID);
			setState(318);
			match(COLON);
			setState(322);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(319);
					voiceItem();
					}
					}
				}
				setState(324);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VoiceItemContext extends ParserRuleContext {
		public TerminalNode OCTAVE() { return getToken(SIDScoreParser.OCTAVE, 0); }
		public TerminalNode LENGTH() { return getToken(SIDScoreParser.LENGTH, 0); }
		public TerminalNode GT() { return getToken(SIDScoreParser.GT, 0); }
		public TerminalNode LT() { return getToken(SIDScoreParser.LT, 0); }
		public SwingStmtContext swingStmt() {
			return getRuleContext(SwingStmtContext.class,0);
		}
		public NoteOrRestOrHitContext noteOrRestOrHit() {
			return getRuleContext(NoteOrRestOrHitContext.class,0);
		}
		public TerminalNode AMP() { return getToken(SIDScoreParser.AMP, 0); }
		public LegatoScopeContext legatoScope() {
			return getRuleContext(LegatoScopeContext.class,0);
		}
		public TupletContext tuplet() {
			return getRuleContext(TupletContext.class,0);
		}
		public RepeatContext repeat() {
			return getRuleContext(RepeatContext.class,0);
		}
		public VoiceItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_voiceItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterVoiceItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitVoiceItem(this);
		}
	}

	public final VoiceItemContext voiceItem() throws RecognitionException {
		VoiceItemContext _localctx = new VoiceItemContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_voiceItem);
		try {
			setState(335);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OCTAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(325);
				match(OCTAVE);
				}
				break;
			case LENGTH:
				enterOuterAlt(_localctx, 2);
				{
				setState(326);
				match(LENGTH);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 3);
				{
				setState(327);
				match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 4);
				{
				setState(328);
				match(LT);
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 5);
				{
				setState(329);
				swingStmt();
				}
				break;
			case NOTE:
			case REST:
			case HIT:
				enterOuterAlt(_localctx, 6);
				{
				setState(330);
				noteOrRestOrHit();
				}
				break;
			case AMP:
				enterOuterAlt(_localctx, 7);
				{
				setState(331);
				match(AMP);
				}
				break;
			case LEG_START:
				enterOuterAlt(_localctx, 8);
				{
				setState(332);
				legatoScope();
				}
				break;
			case TUPLET_START:
				enterOuterAlt(_localctx, 9);
				{
				setState(333);
				tuplet();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 10);
				{
				setState(334);
				repeat();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NoteOrRestOrHitContext extends ParserRuleContext {
		public TerminalNode NOTE() { return getToken(SIDScoreParser.NOTE, 0); }
		public TerminalNode REST() { return getToken(SIDScoreParser.REST, 0); }
		public TerminalNode HIT() { return getToken(SIDScoreParser.HIT, 0); }
		public NoteOrRestOrHitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_noteOrRestOrHit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterNoteOrRestOrHit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitNoteOrRestOrHit(this);
		}
	}

	public final NoteOrRestOrHitContext noteOrRestOrHit() throws RecognitionException {
		NoteOrRestOrHitContext _localctx = new NoteOrRestOrHitContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_noteOrRestOrHit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			_la = _input.LA(1);
			if ( !(((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 7L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LegatoScopeContext extends ParserRuleContext {
		public TerminalNode LEG_START() { return getToken(SIDScoreParser.LEG_START, 0); }
		public TerminalNode LEG_END() { return getToken(SIDScoreParser.LEG_END, 0); }
		public List<VoiceItemContext> voiceItem() {
			return getRuleContexts(VoiceItemContext.class);
		}
		public VoiceItemContext voiceItem(int i) {
			return getRuleContext(VoiceItemContext.class,i);
		}
		public LegatoScopeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_legatoScope; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterLegatoScope(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitLegatoScope(this);
		}
	}

	public final LegatoScopeContext legatoScope() throws RecognitionException {
		LegatoScopeContext _localctx = new LegatoScopeContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_legatoScope);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(339);
			match(LEG_START);
			setState(343);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 15)) & ~0x3f) == 0 && ((1L << (_la - 15)) & 34911186399002625L) != 0)) {
				{
				{
				setState(340);
				voiceItem();
				}
				}
				setState(345);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(346);
			match(LEG_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TupletContext extends ParserRuleContext {
		public TerminalNode TUPLET_START() { return getToken(SIDScoreParser.TUPLET_START, 0); }
		public TerminalNode RBRACE() { return getToken(SIDScoreParser.RBRACE, 0); }
		public List<VoiceItemContext> voiceItem() {
			return getRuleContexts(VoiceItemContext.class);
		}
		public VoiceItemContext voiceItem(int i) {
			return getRuleContext(VoiceItemContext.class,i);
		}
		public TupletContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tuplet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterTuplet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitTuplet(this);
		}
	}

	public final TupletContext tuplet() throws RecognitionException {
		TupletContext _localctx = new TupletContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_tuplet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			match(TUPLET_START);
			setState(352);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 15)) & ~0x3f) == 0 && ((1L << (_la - 15)) & 34911186399002625L) != 0)) {
				{
				{
				setState(349);
				voiceItem();
				}
				}
				setState(354);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(355);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RepeatContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SIDScoreParser.LPAREN, 0); }
		public TerminalNode REPEAT_END() { return getToken(SIDScoreParser.REPEAT_END, 0); }
		public List<VoiceItemContext> voiceItem() {
			return getRuleContexts(VoiceItemContext.class);
		}
		public VoiceItemContext voiceItem(int i) {
			return getRuleContext(VoiceItemContext.class,i);
		}
		public RepeatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repeat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterRepeat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitRepeat(this);
		}
	}

	public final RepeatContext repeat() throws RecognitionException {
		RepeatContext _localctx = new RepeatContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_repeat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(357);
			match(LPAREN);
			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 15)) & ~0x3f) == 0 && ((1L << (_la - 15)) & 34911186399002625L) != 0)) {
				{
				{
				setState(358);
				voiceItem();
				}
				}
				setState(363);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(364);
			match(REPEAT_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001K\u016f\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0005"+
		"\u0000F\b\u0000\n\u0000\f\u0000I\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001Y\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0005"+
		"\n{\b\n\n\n\f\n~\t\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u008a\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0004\f\u0090\b\f\u000b"+
		"\f\f\f\u0091\u0003\f\u0094\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00d8"+
		"\b\r\u0001\u000e\u0003\u000e\u00db\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u00e2\b\u000f\n\u000f\f\u000f"+
		"\u00e5\t\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u00ed\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0005\u0013\u00f2\b\u0013\n\u0013\f\u0013\u00f5\t\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u00fe\b\u0015\n\u0015\f\u0015\u0101\t\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0114\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017"+
		"\u011c\b\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0004\u0019\u0121\b"+
		"\u0019\u000b\u0019\f\u0019\u0122\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0003\u001a\u0135\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u013a\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0005\u001c\u0141\b\u001c\n\u001c\f\u001c\u0144\t\u001c\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u0150\b\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001f\u0001\u001f\u0005\u001f\u0156\b\u001f\n\u001f"+
		"\f\u001f\u0159\t\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0005 \u015f"+
		"\b \n \f \u0162\t \u0001 \u0001 \u0001!\u0001!\u0005!\u0168\b!\n!\f!\u016b"+
		"\t!\u0001!\u0001!\u0001!\u0000\u0000\"\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B\u0000"+
		"\t\u0002\u0000\u001e\u001eGG\u0001\u0000 !\u0001\u0000GH\u0001\u0000>"+
		"?\u0001\u0000\u001e\u001f\u0001\u0000\u0019\u001a\u0001\u0000\u001b\u001d"+
		"\u0002\u0000\f\fGG\u0001\u0000CE\u0198\u0000G\u0001\u0000\u0000\u0000"+
		"\u0002X\u0001\u0000\u0000\u0000\u0004Z\u0001\u0000\u0000\u0000\u0006]"+
		"\u0001\u0000\u0000\u0000\b`\u0001\u0000\u0000\u0000\nc\u0001\u0000\u0000"+
		"\u0000\ff\u0001\u0000\u0000\u0000\u000ek\u0001\u0000\u0000\u0000\u0010"+
		"n\u0001\u0000\u0000\u0000\u0012q\u0001\u0000\u0000\u0000\u0014v\u0001"+
		"\u0000\u0000\u0000\u0016\u0089\u0001\u0000\u0000\u0000\u0018\u008b\u0001"+
		"\u0000\u0000\u0000\u001a\u00d7\u0001\u0000\u0000\u0000\u001c\u00da\u0001"+
		"\u0000\u0000\u0000\u001e\u00de\u0001\u0000\u0000\u0000 \u00e6\u0001\u0000"+
		"\u0000\u0000\"\u00e8\u0001\u0000\u0000\u0000$\u00ec\u0001\u0000\u0000"+
		"\u0000&\u00ee\u0001\u0000\u0000\u0000(\u00f6\u0001\u0000\u0000\u0000*"+
		"\u00f8\u0001\u0000\u0000\u0000,\u0113\u0001\u0000\u0000\u0000.\u011b\u0001"+
		"\u0000\u0000\u00000\u011d\u0001\u0000\u0000\u00002\u0120\u0001\u0000\u0000"+
		"\u00004\u0134\u0001\u0000\u0000\u00006\u0139\u0001\u0000\u0000\u00008"+
		"\u013b\u0001\u0000\u0000\u0000:\u014f\u0001\u0000\u0000\u0000<\u0151\u0001"+
		"\u0000\u0000\u0000>\u0153\u0001\u0000\u0000\u0000@\u015c\u0001\u0000\u0000"+
		"\u0000B\u0165\u0001\u0000\u0000\u0000DF\u0003\u0002\u0001\u0000ED\u0001"+
		"\u0000\u0000\u0000FI\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000"+
		"GH\u0001\u0000\u0000\u0000HJ\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000"+
		"\u0000JK\u0005\u0000\u0000\u0001K\u0001\u0001\u0000\u0000\u0000LY\u0003"+
		"\u0004\u0002\u0000MY\u0003\u0006\u0003\u0000NY\u0003\b\u0004\u0000OY\u0003"+
		"\n\u0005\u0000PY\u0003\f\u0006\u0000QY\u0003\u0010\b\u0000RY\u0003\u0012"+
		"\t\u0000SY\u0003\u0014\n\u0000TY\u0003*\u0015\u0000UY\u0003\u0018\f\u0000"+
		"VY\u0003\u000e\u0007\u0000WY\u00038\u001c\u0000XL\u0001\u0000\u0000\u0000"+
		"XM\u0001\u0000\u0000\u0000XN\u0001\u0000\u0000\u0000XO\u0001\u0000\u0000"+
		"\u0000XP\u0001\u0000\u0000\u0000XQ\u0001\u0000\u0000\u0000XR\u0001\u0000"+
		"\u0000\u0000XS\u0001\u0000\u0000\u0000XT\u0001\u0000\u0000\u0000XU\u0001"+
		"\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000"+
		"Y\u0003\u0001\u0000\u0000\u0000Z[\u0005\u0001\u0000\u0000[\\\u0005I\u0000"+
		"\u0000\\\u0005\u0001\u0000\u0000\u0000]^\u0005\u0002\u0000\u0000^_\u0005"+
		"I\u0000\u0000_\u0007\u0001\u0000\u0000\u0000`a\u0005\u0003\u0000\u0000"+
		"ab\u0005I\u0000\u0000b\t\u0001\u0000\u0000\u0000cd\u0005\u0004\u0000\u0000"+
		"de\u0005G\u0000\u0000e\u000b\u0001\u0000\u0000\u0000fg\u0005\u0005\u0000"+
		"\u0000gh\u0005G\u0000\u0000hi\u0005=\u0000\u0000ij\u0005G\u0000\u0000"+
		"j\r\u0001\u0000\u0000\u0000kl\u0005\u000f\u0000\u0000lm\u0007\u0000\u0000"+
		"\u0000m\u000f\u0001\u0000\u0000\u0000no\u0005\u0006\u0000\u0000op\u0007"+
		"\u0001\u0000\u0000p\u0011\u0001\u0000\u0000\u0000qr\u0005\b\u0000\u0000"+
		"rs\u0005I\u0000\u0000st\u0005\t\u0000\u0000tu\u0005G\u0000\u0000u\u0013"+
		"\u0001\u0000\u0000\u0000vw\u0005\u0007\u0000\u0000wx\u0005G\u0000\u0000"+
		"x|\u00054\u0000\u0000y{\u0003\u0016\u000b\u0000zy\u0001\u0000\u0000\u0000"+
		"{~\u0001\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000"+
		"\u0000}\u007f\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000\u007f"+
		"\u0080\u00053\u0000\u0000\u0080\u0015\u0001\u0000\u0000\u0000\u0081\u008a"+
		"\u0003\u0004\u0002\u0000\u0082\u008a\u0003\u0006\u0003\u0000\u0083\u008a"+
		"\u0003\b\u0004\u0000\u0084\u008a\u0003\n\u0005\u0000\u0085\u008a\u0003"+
		"\f\u0006\u0000\u0086\u008a\u0003\u0010\b\u0000\u0087\u008a\u0003\u000e"+
		"\u0007\u0000\u0088\u008a\u00038\u001c\u0000\u0089\u0081\u0001\u0000\u0000"+
		"\u0000\u0089\u0082\u0001\u0000\u0000\u0000\u0089\u0083\u0001\u0000\u0000"+
		"\u0000\u0089\u0084\u0001\u0000\u0000\u0000\u0089\u0085\u0001\u0000\u0000"+
		"\u0000\u0089\u0086\u0001\u0000\u0000\u0000\u0089\u0087\u0001\u0000\u0000"+
		"\u0000\u0089\u0088\u0001\u0000\u0000\u0000\u008a\u0017\u0001\u0000\u0000"+
		"\u0000\u008b\u008c\u0005\r\u0000\u0000\u008c\u0093\u0005F\u0000\u0000"+
		"\u008d\u0094\u0005I\u0000\u0000\u008e\u0090\u0003\u001a\r\u0000\u008f"+
		"\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091"+
		"\u008f\u0001\u0000\u0000\u0000\u0091\u0092\u0001\u0000\u0000\u0000\u0092"+
		"\u0094\u0001\u0000\u0000\u0000\u0093\u008d\u0001\u0000\u0000\u0000\u0093"+
		"\u008f\u0001\u0000\u0000\u0000\u0094\u0019\u0001\u0000\u0000\u0000\u0095"+
		"\u0096\u0005\"\u0000\u0000\u0096\u0097\u0005;\u0000\u0000\u0097\u00d8"+
		"\u0003\u001e\u000f\u0000\u0098\u0099\u0005#\u0000\u0000\u0099\u009a\u0005"+
		";\u0000\u0000\u009a\u009b\u0005G\u0000\u0000\u009b\u009c\u0005<\u0000"+
		"\u0000\u009c\u009d\u0005G\u0000\u0000\u009d\u009e\u0005<\u0000\u0000\u009e"+
		"\u009f\u0005G\u0000\u0000\u009f\u00a0\u0005<\u0000\u0000\u00a0\u00d8\u0005"+
		"G\u0000\u0000\u00a1\u00a2\u0005$\u0000\u0000\u00a2\u00a3\u0005;\u0000"+
		"\u0000\u00a3\u00d8\u0007\u0002\u0000\u0000\u00a4\u00a5\u0005%\u0000\u0000"+
		"\u00a5\u00a6\u0005;\u0000\u0000\u00a6\u00d8\u0007\u0002\u0000\u0000\u00a7"+
		"\u00a8\u0005&\u0000\u0000\u00a8\u00a9\u0005;\u0000\u0000\u00a9\u00d8\u0007"+
		"\u0002\u0000\u0000\u00aa\u00ab\u0005\u0012\u0000\u0000\u00ab\u00ac\u0005"+
		";\u0000\u0000\u00ac\u00d8\u0003$\u0012\u0000\u00ad\u00ae\u0005\u0013\u0000"+
		"\u0000\u00ae\u00af\u0005;\u0000\u0000\u00af\u00d8\u0007\u0002\u0000\u0000"+
		"\u00b0\u00b1\u0005\u0014\u0000\u0000\u00b1\u00b2\u0005;\u0000\u0000\u00b2"+
		"\u00d8\u0005G\u0000\u0000\u00b3\u00b4\u0005\u0017\u0000\u0000\u00b4\u00b5"+
		"\u0005;\u0000\u0000\u00b5\u00d8\u0003\"\u0011\u0000\u00b6\u00b7\u0005"+
		"\u0018\u0000\u0000\u00b7\u00b8\u0005;\u0000\u0000\u00b8\u00d8\u0005G\u0000"+
		"\u0000\u00b9\u00ba\u0005\'\u0000\u0000\u00ba\u00bb\u0005;\u0000\u0000"+
		"\u00bb\u00d8\u0005F\u0000\u0000\u00bc\u00bd\u0005(\u0000\u0000\u00bd\u00be"+
		"\u0005;\u0000\u0000\u00be\u00d8\u0007\u0002\u0000\u0000\u00bf\u00c0\u0005"+
		")\u0000\u0000\u00c0\u00c1\u0005;\u0000\u0000\u00c1\u00d8\u0007\u0002\u0000"+
		"\u0000\u00c2\u00c3\u0005*\u0000\u0000\u00c3\u00c4\u0005;\u0000\u0000\u00c4"+
		"\u00d8\u0003\u001c\u000e\u0000\u00c5\u00c6\u0005+\u0000\u0000\u00c6\u00c7"+
		"\u0005;\u0000\u0000\u00c7\u00d8\u0005F\u0000\u0000\u00c8\u00c9\u0005,"+
		"\u0000\u0000\u00c9\u00ca\u0005;\u0000\u0000\u00ca\u00d8\u0005F\u0000\u0000"+
		"\u00cb\u00cc\u0005-\u0000\u0000\u00cc\u00cd\u0005;\u0000\u0000\u00cd\u00d8"+
		"\u0005F\u0000\u0000\u00ce\u00cf\u0005.\u0000\u0000\u00cf\u00d0\u0005;"+
		"\u0000\u0000\u00d0\u00d8\u0005F\u0000\u0000\u00d1\u00d2\u0005\u0010\u0000"+
		"\u0000\u00d2\u00d3\u0005;\u0000\u0000\u00d3\u00d8\u0003 \u0010\u0000\u00d4"+
		"\u00d5\u0005\u0011\u0000\u0000\u00d5\u00d6\u0005;\u0000\u0000\u00d6\u00d8"+
		"\u0003 \u0010\u0000\u00d7\u0095\u0001\u0000\u0000\u0000\u00d7\u0098\u0001"+
		"\u0000\u0000\u0000\u00d7\u00a1\u0001\u0000\u0000\u0000\u00d7\u00a4\u0001"+
		"\u0000\u0000\u0000\u00d7\u00a7\u0001\u0000\u0000\u0000\u00d7\u00aa\u0001"+
		"\u0000\u0000\u0000\u00d7\u00ad\u0001\u0000\u0000\u0000\u00d7\u00b0\u0001"+
		"\u0000\u0000\u0000\u00d7\u00b3\u0001\u0000\u0000\u0000\u00d7\u00b6\u0001"+
		"\u0000\u0000\u0000\u00d7\u00b9\u0001\u0000\u0000\u0000\u00d7\u00bc\u0001"+
		"\u0000\u0000\u0000\u00d7\u00bf\u0001\u0000\u0000\u0000\u00d7\u00c2\u0001"+
		"\u0000\u0000\u0000\u00d7\u00c5\u0001\u0000\u0000\u0000\u00d7\u00c8\u0001"+
		"\u0000\u0000\u0000\u00d7\u00cb\u0001\u0000\u0000\u0000\u00d7\u00ce\u0001"+
		"\u0000\u0000\u0000\u00d7\u00d1\u0001\u0000\u0000\u0000\u00d7\u00d4\u0001"+
		"\u0000\u0000\u0000\u00d8\u001b\u0001\u0000\u0000\u0000\u00d9\u00db\u0007"+
		"\u0003\u0000\u0000\u00da\u00d9\u0001\u0000\u0000\u0000\u00da\u00db\u0001"+
		"\u0000\u0000\u0000\u00db\u00dc\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005"+
		"G\u0000\u0000\u00dd\u001d\u0001\u0000\u0000\u0000\u00de\u00e3\u0005/\u0000"+
		"\u0000\u00df\u00e0\u0005>\u0000\u0000\u00e0\u00e2\u0005/\u0000\u0000\u00e1"+
		"\u00df\u0001\u0000\u0000\u0000\u00e2\u00e5\u0001\u0000\u0000\u0000\u00e3"+
		"\u00e1\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000\u00e4"+
		"\u001f\u0001\u0000\u0000\u0000\u00e5\u00e3\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e7\u0007\u0004\u0000\u0000\u00e7!\u0001\u0000\u0000\u0000\u00e8\u00e9"+
		"\u0007\u0005\u0000\u0000\u00e9#\u0001\u0000\u0000\u0000\u00ea\u00ed\u0005"+
		"\u001e\u0000\u0000\u00eb\u00ed\u0003&\u0013\u0000\u00ec\u00ea\u0001\u0000"+
		"\u0000\u0000\u00ec\u00eb\u0001\u0000\u0000\u0000\u00ed%\u0001\u0000\u0000"+
		"\u0000\u00ee\u00f3\u0003(\u0014\u0000\u00ef\u00f0\u0005>\u0000\u0000\u00f0"+
		"\u00f2\u0003(\u0014\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f5"+
		"\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4"+
		"\u0001\u0000\u0000\u0000\u00f4\'\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f7\u0007\u0006\u0000\u0000\u00f7)\u0001\u0000"+
		"\u0000\u0000\u00f8\u00f9\u0005\n\u0000\u0000\u00f9\u00fa\u0005F\u0000"+
		"\u0000\u00fa\u00fb\u0005F\u0000\u0000\u00fb\u00ff\u00054\u0000\u0000\u00fc"+
		"\u00fe\u0003,\u0016\u0000\u00fd\u00fc\u0001\u0000\u0000\u0000\u00fe\u0101"+
		"\u0001\u0000\u0000\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100"+
		"\u0001\u0000\u0000\u0000\u0100\u0102\u0001\u0000\u0000\u0000\u0101\u00ff"+
		"\u0001\u0000\u0000\u0000\u0102\u0103\u00053\u0000\u0000\u0103+\u0001\u0000"+
		"\u0000\u0000\u0104\u0105\u0003.\u0017\u0000\u0105\u0106\u00055\u0000\u0000"+
		"\u0106\u0107\u00030\u0018\u0000\u0107\u0114\u0001\u0000\u0000\u0000\u0108"+
		"\u0109\u0003.\u0017\u0000\u0109\u010a\u0005\f\u0000\u0000\u010a\u0114"+
		"\u0001\u0000\u0000\u0000\u010b\u010c\u00032\u0019\u0000\u010c\u010d\u0005"+
		"5\u0000\u0000\u010d\u010e\u00030\u0018\u0000\u010e\u0114\u0001\u0000\u0000"+
		"\u0000\u010f\u0110\u00032\u0019\u0000\u0110\u0111\u0005\f\u0000\u0000"+
		"\u0111\u0114\u0001\u0000\u0000\u0000\u0112\u0114\u0005\u000b\u0000\u0000"+
		"\u0113\u0104\u0001\u0000\u0000\u0000\u0113\u0108\u0001\u0000\u0000\u0000"+
		"\u0113\u010b\u0001\u0000\u0000\u0000\u0113\u010f\u0001\u0000\u0000\u0000"+
		"\u0113\u0112\u0001\u0000\u0000\u0000\u0114-\u0001\u0000\u0000\u0000\u0115"+
		"\u011c\u0003\u001e\u000f\u0000\u0116\u011c\u0005\u001f\u0000\u0000\u0117"+
		"\u011c\u0005\u001e\u0000\u0000\u0118\u011c\u0005H\u0000\u0000\u0119\u011c"+
		"\u0003\u001c\u000e\u0000\u011a\u011c\u0005G\u0000\u0000\u011b\u0115\u0001"+
		"\u0000\u0000\u0000\u011b\u0116\u0001\u0000\u0000\u0000\u011b\u0117\u0001"+
		"\u0000\u0000\u0000\u011b\u0118\u0001\u0000\u0000\u0000\u011b\u0119\u0001"+
		"\u0000\u0000\u0000\u011b\u011a\u0001\u0000\u0000\u0000\u011c/\u0001\u0000"+
		"\u0000\u0000\u011d\u011e\u0007\u0007\u0000\u0000\u011e1\u0001\u0000\u0000"+
		"\u0000\u011f\u0121\u00034\u001a\u0000\u0120\u011f\u0001\u0000\u0000\u0000"+
		"\u0121\u0122\u0001\u0000\u0000\u0000\u0122\u0120\u0001\u0000\u0000\u0000"+
		"\u0122\u0123\u0001\u0000\u0000\u0000\u01233\u0001\u0000\u0000\u0000\u0124"+
		"\u0125\u0005\"\u0000\u0000\u0125\u0126\u0005;\u0000\u0000\u0126\u0135"+
		"\u0003\u001e\u000f\u0000\u0127\u0128\u0005\u0015\u0000\u0000\u0128\u0129"+
		"\u0005;\u0000\u0000\u0129\u0135\u00036\u001b\u0000\u012a\u012b\u0005\u0017"+
		"\u0000\u0000\u012b\u012c\u0005;\u0000\u0000\u012c\u0135\u0003 \u0010\u0000"+
		"\u012d\u012e\u0005\u0011\u0000\u0000\u012e\u012f\u0005;\u0000\u0000\u012f"+
		"\u0135\u0003 \u0010\u0000\u0130\u0131\u0005\u0010\u0000\u0000\u0131\u0132"+
		"\u0005;\u0000\u0000\u0132\u0135\u0003 \u0010\u0000\u0133\u0135\u0005\u0016"+
		"\u0000\u0000\u0134\u0124\u0001\u0000\u0000\u0000\u0134\u0127\u0001\u0000"+
		"\u0000\u0000\u0134\u012a\u0001\u0000\u0000\u0000\u0134\u012d\u0001\u0000"+
		"\u0000\u0000\u0134\u0130\u0001\u0000\u0000\u0000\u0134\u0133\u0001\u0000"+
		"\u0000\u0000\u01355\u0001\u0000\u0000\u0000\u0136\u013a\u0005C\u0000\u0000"+
		"\u0137\u013a\u0003\u001c\u000e\u0000\u0138\u013a\u0005\u001e\u0000\u0000"+
		"\u0139\u0136\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000\u0000\u0000"+
		"\u0139\u0138\u0001\u0000\u0000\u0000\u013a7\u0001\u0000\u0000\u0000\u013b"+
		"\u013c\u0005\u000e\u0000\u0000\u013c\u013d\u0005G\u0000\u0000\u013d\u013e"+
		"\u0005F\u0000\u0000\u013e\u0142\u0005:\u0000\u0000\u013f\u0141\u0003:"+
		"\u001d\u0000\u0140\u013f\u0001\u0000\u0000\u0000\u0141\u0144\u0001\u0000"+
		"\u0000\u0000\u0142\u0140\u0001\u0000\u0000\u0000\u0142\u0143\u0001\u0000"+
		"\u0000\u0000\u01439\u0001\u0000\u0000\u0000\u0144\u0142\u0001\u0000\u0000"+
		"\u0000\u0145\u0150\u0005A\u0000\u0000\u0146\u0150\u0005B\u0000\u0000\u0147"+
		"\u0150\u00058\u0000\u0000\u0148\u0150\u00059\u0000\u0000\u0149\u0150\u0003"+
		"\u000e\u0007\u0000\u014a\u0150\u0003<\u001e\u0000\u014b\u0150\u00057\u0000"+
		"\u0000\u014c\u0150\u0003>\u001f\u0000\u014d\u0150\u0003@ \u0000\u014e"+
		"\u0150\u0003B!\u0000\u014f\u0145\u0001\u0000\u0000\u0000\u014f\u0146\u0001"+
		"\u0000\u0000\u0000\u014f\u0147\u0001\u0000\u0000\u0000\u014f\u0148\u0001"+
		"\u0000\u0000\u0000\u014f\u0149\u0001\u0000\u0000\u0000\u014f\u014a\u0001"+
		"\u0000\u0000\u0000\u014f\u014b\u0001\u0000\u0000\u0000\u014f\u014c\u0001"+
		"\u0000\u0000\u0000\u014f\u014d\u0001\u0000\u0000\u0000\u014f\u014e\u0001"+
		"\u0000\u0000\u0000\u0150;\u0001\u0000\u0000\u0000\u0151\u0152\u0007\b"+
		"\u0000\u0000\u0152=\u0001\u0000\u0000\u0000\u0153\u0157\u00050\u0000\u0000"+
		"\u0154\u0156\u0003:\u001d\u0000\u0155\u0154\u0001\u0000\u0000\u0000\u0156"+
		"\u0159\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0157"+
		"\u0158\u0001\u0000\u0000\u0000\u0158\u015a\u0001\u0000\u0000\u0000\u0159"+
		"\u0157\u0001\u0000\u0000\u0000\u015a\u015b\u00051\u0000\u0000\u015b?\u0001"+
		"\u0000\u0000\u0000\u015c\u0160\u00052\u0000\u0000\u015d\u015f\u0003:\u001d"+
		"\u0000\u015e\u015d\u0001\u0000\u0000\u0000\u015f\u0162\u0001\u0000\u0000"+
		"\u0000\u0160\u015e\u0001\u0000\u0000\u0000\u0160\u0161\u0001\u0000\u0000"+
		"\u0000\u0161\u0163\u0001\u0000\u0000\u0000\u0162\u0160\u0001\u0000\u0000"+
		"\u0000\u0163\u0164\u00053\u0000\u0000\u0164A\u0001\u0000\u0000\u0000\u0165"+
		"\u0169\u00056\u0000\u0000\u0166\u0168\u0003:\u001d\u0000\u0167\u0166\u0001"+
		"\u0000\u0000\u0000\u0168\u016b\u0001\u0000\u0000\u0000\u0169\u0167\u0001"+
		"\u0000\u0000\u0000\u0169\u016a\u0001\u0000\u0000\u0000\u016a\u016c\u0001"+
		"\u0000\u0000\u0000\u016b\u0169\u0001\u0000\u0000\u0000\u016c\u016d\u0005"+
		"@\u0000\u0000\u016dC\u0001\u0000\u0000\u0000\u0016GX|\u0089\u0091\u0093"+
		"\u00d7\u00da\u00e3\u00ec\u00f3\u00ff\u0113\u011b\u0122\u0134\u0139\u0142"+
		"\u014f\u0157\u0160\u0169";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}