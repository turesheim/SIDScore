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
		AS=9, TABLE=10, LOOP=11, HOLD=12, EFFECT=13, ANY=14, EFFECT_LENGTH=15, 
		TICKS=16, PRIORITY=17, RESTART=18, IGNORE=19, STEAL=20, ATK=21, FRAME=22, 
		TO=23, LINEAR=24, EXP=25, LOG=26, STEP=27, INSTR=28, VOICE=29, SWING=30, 
		SYNC=31, RING=32, FILTER=33, FILTERROUTE=34, CUTOFF=35, RES=36, NOTEK=37, 
		RESET=38, GATE=39, GATEMIN=40, RETRIGGER=41, LEGATO=42, LP=43, BP=44, 
		HP=45, OFF=46, ON=47, PAL=48, NTSC=49, WAVE=50, ADSR=51, PW=52, HIPULSE=53, 
		LOWPULSE=54, WAVESEQ=55, PWMIN=56, PWMAX=57, PWSWEEP=58, PWSEQ=59, FILTERSEQ=60, 
		GATESEQ=61, PITCHSEQ=62, PITCH=63, FREQ=64, ATTACK=65, DECAY=66, SUSTAIN=67, 
		RELEASE=68, VOLUME=69, WAVEVAL=70, LEG_START=71, LEG_END=72, TUPLET_START=73, 
		RBRACE=74, LBRACE=75, AT=76, LPAREN=77, AMP=78, GT=79, LT=80, COLON=81, 
		EQ=82, COMMA=83, SLASH=84, PLUS=85, MINUS=86, REPEAT_END=87, OCTAVE=88, 
		LENGTH=89, NOTE=90, REST=91, HIT=92, ID=93, INT=94, HEX=95, STRING=96, 
		COMMENT=97, WS=98;
	public static final int
		RULE_file = 0, RULE_stmt = 1, RULE_titleStmt = 2, RULE_authorStmt = 3, 
		RULE_releasedStmt = 4, RULE_tempoStmt = 5, RULE_timeStmt = 6, RULE_swingStmt = 7, 
		RULE_systemStmt = 8, RULE_importStmt = 9, RULE_songBlock = 10, RULE_songStmt = 11, 
		RULE_effectStmt = 12, RULE_effectBodyStmt = 13, RULE_effectVoiceStmt = 14, 
		RULE_effectVoice = 15, RULE_effectLengthStmt = 16, RULE_effectPriorityStmt = 17, 
		RULE_effectRetriggerStmt = 18, RULE_effectRetriggerMode = 19, RULE_effectStep = 20, 
		RULE_effectTick = 21, RULE_effectGroup = 22, RULE_effectAssignment = 23, 
		RULE_effectSweep = 24, RULE_effectSweepParam = 25, RULE_effectSweepValue = 26, 
		RULE_effectSweepCurve = 27, RULE_numericValue = 28, RULE_instrStmt = 29, 
		RULE_instrParam = 30, RULE_signedInt = 31, RULE_waveList = 32, RULE_onOff = 33, 
		RULE_gateMode = 34, RULE_filterSpec = 35, RULE_filterList = 36, RULE_filterMode = 37, 
		RULE_tableStmt = 38, RULE_tableStep = 39, RULE_tableValue = 40, RULE_tableDuration = 41, 
		RULE_tableCtrl = 42, RULE_tableCtrlItem = 43, RULE_noteSpec = 44, RULE_voiceBlock = 45, 
		RULE_voiceItem = 46, RULE_noteOrRestOrHit = 47, RULE_legatoScope = 48, 
		RULE_tuplet = 49, RULE_repeat = 50;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "stmt", "titleStmt", "authorStmt", "releasedStmt", "tempoStmt", 
			"timeStmt", "swingStmt", "systemStmt", "importStmt", "songBlock", "songStmt", 
			"effectStmt", "effectBodyStmt", "effectVoiceStmt", "effectVoice", "effectLengthStmt", 
			"effectPriorityStmt", "effectRetriggerStmt", "effectRetriggerMode", "effectStep", 
			"effectTick", "effectGroup", "effectAssignment", "effectSweep", "effectSweepParam", 
			"effectSweepValue", "effectSweepCurve", "numericValue", "instrStmt", 
			"instrParam", "signedInt", "waveList", "onOff", "gateMode", "filterSpec", 
			"filterList", "filterMode", "tableStmt", "tableStep", "tableValue", "tableDuration", 
			"tableCtrl", "tableCtrlItem", "noteSpec", "voiceBlock", "voiceItem", 
			"noteOrRestOrHit", "legatoScope", "tuplet", "repeat"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'TITLE'", "'AUTHOR'", "'RELEASED'", "'TEMPO'", "'TIME'", "'SYSTEM'", 
			"'TUNE'", "'IMPORT'", "'AS'", "'TABLE'", "'LOOP'", "'HOLD'", "'EFFECT'", 
			"'ANY'", "'LENGTH'", "'TICKS'", "'PRIORITY'", "'RESTART'", "'IGNORE'", 
			"'STEAL'", "'AT'", "'FRAME'", "'TO'", "'LINEAR'", "'EXP'", "'LOG'", "'STEP'", 
			"'INSTR'", "'VOICE'", "'SWING'", "'SYNC'", "'RING'", "'FILTER'", "'FILTERROUTE'", 
			"'CUTOFF'", "'RES'", "'NOTE'", "'RESET'", "'GATE'", "'GATEMIN'", "'RETRIGGER'", 
			"'LEGATO'", "'LP'", "'BP'", "'HP'", "'OFF'", "'ON'", "'PAL'", "'NTSC'", 
			"'WAVE'", "'ADSR'", "'PW'", "'HIPULSE'", "'LOWPULSE'", "'WAVESEQ'", "'PWMIN'", 
			"'PWMAX'", "'PWSWEEP'", "'PWSEQ'", "'FILTERSEQ'", "'GATESEQ'", "'PITCHSEQ'", 
			"'PITCH'", "'FREQ'", "'ATTACK'", "'DECAY'", "'SUSTAIN'", "'RELEASE'", 
			"'VOLUME'", null, "'(leg)'", "'(end)'", "'T{'", "'}'", "'{'", "'@'", 
			"'('", "'&'", "'>'", "'<'", "':'", "'='", "','", "'/'", "'+'", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TITLE", "AUTHOR", "RELEASED", "TEMPO", "TIME", "SYSTEM", "TUNE", 
			"IMPORT", "AS", "TABLE", "LOOP", "HOLD", "EFFECT", "ANY", "EFFECT_LENGTH", 
			"TICKS", "PRIORITY", "RESTART", "IGNORE", "STEAL", "ATK", "FRAME", "TO", 
			"LINEAR", "EXP", "LOG", "STEP", "INSTR", "VOICE", "SWING", "SYNC", "RING", 
			"FILTER", "FILTERROUTE", "CUTOFF", "RES", "NOTEK", "RESET", "GATE", "GATEMIN", 
			"RETRIGGER", "LEGATO", "LP", "BP", "HP", "OFF", "ON", "PAL", "NTSC", 
			"WAVE", "ADSR", "PW", "HIPULSE", "LOWPULSE", "WAVESEQ", "PWMIN", "PWMAX", 
			"PWSWEEP", "PWSEQ", "FILTERSEQ", "GATESEQ", "PITCHSEQ", "PITCH", "FREQ", 
			"ATTACK", "DECAY", "SUSTAIN", "RELEASE", "VOLUME", "WAVEVAL", "LEG_START", 
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
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1879057918L) != 0)) {
				{
				{
				setState(102);
				stmt();
				}
				}
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(108);
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
		public EffectStmtContext effectStmt() {
			return getRuleContext(EffectStmtContext.class,0);
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
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TITLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				titleStmt();
				}
				break;
			case AUTHOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				authorStmt();
				}
				break;
			case RELEASED:
				enterOuterAlt(_localctx, 3);
				{
				setState(112);
				releasedStmt();
				}
				break;
			case TEMPO:
				enterOuterAlt(_localctx, 4);
				{
				setState(113);
				tempoStmt();
				}
				break;
			case TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(114);
				timeStmt();
				}
				break;
			case SYSTEM:
				enterOuterAlt(_localctx, 6);
				{
				setState(115);
				systemStmt();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 7);
				{
				setState(116);
				importStmt();
				}
				break;
			case TUNE:
				enterOuterAlt(_localctx, 8);
				{
				setState(117);
				songBlock();
				}
				break;
			case TABLE:
				enterOuterAlt(_localctx, 9);
				{
				setState(118);
				tableStmt();
				}
				break;
			case EFFECT:
				enterOuterAlt(_localctx, 10);
				{
				setState(119);
				effectStmt();
				}
				break;
			case INSTR:
				enterOuterAlt(_localctx, 11);
				{
				setState(120);
				instrStmt();
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 12);
				{
				setState(121);
				swingStmt();
				}
				break;
			case VOICE:
				enterOuterAlt(_localctx, 13);
				{
				setState(122);
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
			setState(125);
			match(TITLE);
			setState(126);
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
			setState(128);
			match(AUTHOR);
			setState(129);
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
			setState(131);
			match(RELEASED);
			setState(132);
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
			setState(134);
			match(TEMPO);
			setState(135);
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
			setState(137);
			match(TIME);
			setState(138);
			match(INT);
			setState(139);
			match(SLASH);
			setState(140);
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
			setState(142);
			match(SWING);
			setState(143);
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
			setState(145);
			match(SYSTEM);
			setState(146);
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
			setState(148);
			match(IMPORT);
			setState(149);
			match(STRING);
			setState(150);
			match(AS);
			setState(151);
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
			setState(153);
			match(TUNE);
			setState(154);
			match(INT);
			setState(155);
			match(LBRACE);
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1610621054L) != 0)) {
				{
				{
				setState(156);
				songStmt();
				}
				}
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(162);
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
		public EffectStmtContext effectStmt() {
			return getRuleContext(EffectStmtContext.class,0);
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
			setState(173);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TITLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(164);
				titleStmt();
				}
				break;
			case AUTHOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(165);
				authorStmt();
				}
				break;
			case RELEASED:
				enterOuterAlt(_localctx, 3);
				{
				setState(166);
				releasedStmt();
				}
				break;
			case TEMPO:
				enterOuterAlt(_localctx, 4);
				{
				setState(167);
				tempoStmt();
				}
				break;
			case TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(168);
				timeStmt();
				}
				break;
			case SYSTEM:
				enterOuterAlt(_localctx, 6);
				{
				setState(169);
				systemStmt();
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 7);
				{
				setState(170);
				swingStmt();
				}
				break;
			case EFFECT:
				enterOuterAlt(_localctx, 8);
				{
				setState(171);
				effectStmt();
				}
				break;
			case VOICE:
				enterOuterAlt(_localctx, 9);
				{
				setState(172);
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
	public static class EffectStmtContext extends ParserRuleContext {
		public TerminalNode EFFECT() { return getToken(SIDScoreParser.EFFECT, 0); }
		public TerminalNode ID() { return getToken(SIDScoreParser.ID, 0); }
		public TerminalNode LBRACE() { return getToken(SIDScoreParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(SIDScoreParser.RBRACE, 0); }
		public List<EffectBodyStmtContext> effectBodyStmt() {
			return getRuleContexts(EffectBodyStmtContext.class);
		}
		public EffectBodyStmtContext effectBodyStmt(int i) {
			return getRuleContext(EffectBodyStmtContext.class,i);
		}
		public EffectStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectStmt(this);
		}
	}

	public final EffectStmtContext effectStmt() throws RecognitionException {
		EffectStmtContext _localctx = new EffectStmtContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_effectStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(EFFECT);
			setState(176);
			match(ID);
			setState(177);
			match(LBRACE);
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 15)) & ~0x3f) == 0 && ((1L << (_la - 15)) & 35748387290562757L) != 0)) {
				{
				{
				setState(178);
				effectBodyStmt();
				}
				}
				setState(183);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(184);
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
	public static class EffectBodyStmtContext extends ParserRuleContext {
		public EffectVoiceStmtContext effectVoiceStmt() {
			return getRuleContext(EffectVoiceStmtContext.class,0);
		}
		public EffectLengthStmtContext effectLengthStmt() {
			return getRuleContext(EffectLengthStmtContext.class,0);
		}
		public EffectPriorityStmtContext effectPriorityStmt() {
			return getRuleContext(EffectPriorityStmtContext.class,0);
		}
		public EffectRetriggerStmtContext effectRetriggerStmt() {
			return getRuleContext(EffectRetriggerStmtContext.class,0);
		}
		public EffectStepContext effectStep() {
			return getRuleContext(EffectStepContext.class,0);
		}
		public EffectBodyStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectBodyStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectBodyStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectBodyStmt(this);
		}
	}

	public final EffectBodyStmtContext effectBodyStmt() throws RecognitionException {
		EffectBodyStmtContext _localctx = new EffectBodyStmtContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_effectBodyStmt);
		try {
			setState(191);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VOICE:
				enterOuterAlt(_localctx, 1);
				{
				setState(186);
				effectVoiceStmt();
				}
				break;
			case EFFECT_LENGTH:
				enterOuterAlt(_localctx, 2);
				{
				setState(187);
				effectLengthStmt();
				}
				break;
			case PRIORITY:
				enterOuterAlt(_localctx, 3);
				{
				setState(188);
				effectPriorityStmt();
				}
				break;
			case RETRIGGER:
				enterOuterAlt(_localctx, 4);
				{
				setState(189);
				effectRetriggerStmt();
				}
				break;
			case ATK:
			case FRAME:
			case SYNC:
			case RING:
			case FILTER:
			case FILTERROUTE:
			case CUTOFF:
			case RES:
			case RESET:
			case GATE:
			case WAVE:
			case ADSR:
			case PW:
			case HIPULSE:
			case LOWPULSE:
			case PITCH:
			case FREQ:
			case ATTACK:
			case DECAY:
			case SUSTAIN:
			case RELEASE:
			case VOLUME:
				enterOuterAlt(_localctx, 5);
				{
				setState(190);
				effectStep();
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
	public static class EffectVoiceStmtContext extends ParserRuleContext {
		public TerminalNode VOICE() { return getToken(SIDScoreParser.VOICE, 0); }
		public EffectVoiceContext effectVoice() {
			return getRuleContext(EffectVoiceContext.class,0);
		}
		public EffectVoiceStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectVoiceStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectVoiceStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectVoiceStmt(this);
		}
	}

	public final EffectVoiceStmtContext effectVoiceStmt() throws RecognitionException {
		EffectVoiceStmtContext _localctx = new EffectVoiceStmtContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_effectVoiceStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(VOICE);
			setState(194);
			effectVoice();
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
	public static class EffectVoiceContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode ANY() { return getToken(SIDScoreParser.ANY, 0); }
		public EffectVoiceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectVoice; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectVoice(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectVoice(this);
		}
	}

	public final EffectVoiceContext effectVoice() throws RecognitionException {
		EffectVoiceContext _localctx = new EffectVoiceContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_effectVoice);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_la = _input.LA(1);
			if ( !(_la==ANY || _la==INT) ) {
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
	public static class EffectLengthStmtContext extends ParserRuleContext {
		public TerminalNode EFFECT_LENGTH() { return getToken(SIDScoreParser.EFFECT_LENGTH, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode TICKS() { return getToken(SIDScoreParser.TICKS, 0); }
		public EffectLengthStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectLengthStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectLengthStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectLengthStmt(this);
		}
	}

	public final EffectLengthStmtContext effectLengthStmt() throws RecognitionException {
		EffectLengthStmtContext _localctx = new EffectLengthStmtContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_effectLengthStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(EFFECT_LENGTH);
			setState(199);
			match(INT);
			setState(200);
			match(TICKS);
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
	public static class EffectPriorityStmtContext extends ParserRuleContext {
		public TerminalNode PRIORITY() { return getToken(SIDScoreParser.PRIORITY, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public EffectPriorityStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectPriorityStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectPriorityStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectPriorityStmt(this);
		}
	}

	public final EffectPriorityStmtContext effectPriorityStmt() throws RecognitionException {
		EffectPriorityStmtContext _localctx = new EffectPriorityStmtContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_effectPriorityStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			match(PRIORITY);
			setState(203);
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
	public static class EffectRetriggerStmtContext extends ParserRuleContext {
		public TerminalNode RETRIGGER() { return getToken(SIDScoreParser.RETRIGGER, 0); }
		public EffectRetriggerModeContext effectRetriggerMode() {
			return getRuleContext(EffectRetriggerModeContext.class,0);
		}
		public EffectRetriggerStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectRetriggerStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectRetriggerStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectRetriggerStmt(this);
		}
	}

	public final EffectRetriggerStmtContext effectRetriggerStmt() throws RecognitionException {
		EffectRetriggerStmtContext _localctx = new EffectRetriggerStmtContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_effectRetriggerStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			match(RETRIGGER);
			setState(206);
			effectRetriggerMode();
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
	public static class EffectRetriggerModeContext extends ParserRuleContext {
		public TerminalNode RESTART() { return getToken(SIDScoreParser.RESTART, 0); }
		public TerminalNode IGNORE() { return getToken(SIDScoreParser.IGNORE, 0); }
		public TerminalNode STEAL() { return getToken(SIDScoreParser.STEAL, 0); }
		public EffectRetriggerModeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectRetriggerMode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectRetriggerMode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectRetriggerMode(this);
		}
	}

	public final EffectRetriggerModeContext effectRetriggerMode() throws RecognitionException {
		EffectRetriggerModeContext _localctx = new EffectRetriggerModeContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_effectRetriggerMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835008L) != 0)) ) {
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
	public static class EffectStepContext extends ParserRuleContext {
		public EffectAssignmentContext effectAssignment() {
			return getRuleContext(EffectAssignmentContext.class,0);
		}
		public EffectTickContext effectTick() {
			return getRuleContext(EffectTickContext.class,0);
		}
		public EffectSweepContext effectSweep() {
			return getRuleContext(EffectSweepContext.class,0);
		}
		public EffectGroupContext effectGroup() {
			return getRuleContext(EffectGroupContext.class,0);
		}
		public EffectStepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectStep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectStep(this);
		}
	}

	public final EffectStepContext effectStep() throws RecognitionException {
		EffectStepContext _localctx = new EffectStepContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_effectStep);
		int _la;
		try {
			setState(216);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(210);
				effectAssignment();
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AT) {
					{
					setState(211);
					effectTick();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(214);
				effectSweep();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(215);
				effectGroup();
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
	public static class EffectTickContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(SIDScoreParser.AT, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public EffectTickContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectTick; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectTick(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectTick(this);
		}
	}

	public final EffectTickContext effectTick() throws RecognitionException {
		EffectTickContext _localctx = new EffectTickContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_effectTick);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(AT);
			setState(219);
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
	public static class EffectGroupContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public TerminalNode LBRACE() { return getToken(SIDScoreParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(SIDScoreParser.RBRACE, 0); }
		public TerminalNode ATK() { return getToken(SIDScoreParser.ATK, 0); }
		public TerminalNode FRAME() { return getToken(SIDScoreParser.FRAME, 0); }
		public List<EffectAssignmentContext> effectAssignment() {
			return getRuleContexts(EffectAssignmentContext.class);
		}
		public EffectAssignmentContext effectAssignment(int i) {
			return getRuleContext(EffectAssignmentContext.class,i);
		}
		public EffectGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectGroup(this);
		}
	}

	public final EffectGroupContext effectGroup() throws RecognitionException {
		EffectGroupContext _localctx = new EffectGroupContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_effectGroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			_la = _input.LA(1);
			if ( !(_la==ATK || _la==FRAME) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(222);
			match(INT);
			setState(223);
			match(LBRACE);
			setState(227);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 31)) & ~0x3f) == 0 && ((1L << (_la - 31)) & 545477099967L) != 0)) {
				{
				{
				setState(224);
				effectAssignment();
				}
				}
				setState(229);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(230);
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
	public static class EffectAssignmentContext extends ParserRuleContext {
		public TerminalNode WAVE() { return getToken(SIDScoreParser.WAVE, 0); }
		public TerminalNode EQ() { return getToken(SIDScoreParser.EQ, 0); }
		public WaveListContext waveList() {
			return getRuleContext(WaveListContext.class,0);
		}
		public TerminalNode GATE() { return getToken(SIDScoreParser.GATE, 0); }
		public OnOffContext onOff() {
			return getRuleContext(OnOffContext.class,0);
		}
		public TerminalNode SYNC() { return getToken(SIDScoreParser.SYNC, 0); }
		public TerminalNode RING() { return getToken(SIDScoreParser.RING, 0); }
		public TerminalNode RESET() { return getToken(SIDScoreParser.RESET, 0); }
		public TerminalNode PITCH() { return getToken(SIDScoreParser.PITCH, 0); }
		public TerminalNode NOTE() { return getToken(SIDScoreParser.NOTE, 0); }
		public TerminalNode FREQ() { return getToken(SIDScoreParser.FREQ, 0); }
		public NumericValueContext numericValue() {
			return getRuleContext(NumericValueContext.class,0);
		}
		public TerminalNode PW() { return getToken(SIDScoreParser.PW, 0); }
		public TerminalNode HIPULSE() { return getToken(SIDScoreParser.HIPULSE, 0); }
		public TerminalNode LOWPULSE() { return getToken(SIDScoreParser.LOWPULSE, 0); }
		public TerminalNode ADSR() { return getToken(SIDScoreParser.ADSR, 0); }
		public List<TerminalNode> INT() { return getTokens(SIDScoreParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(SIDScoreParser.INT, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SIDScoreParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SIDScoreParser.COMMA, i);
		}
		public TerminalNode ATTACK() { return getToken(SIDScoreParser.ATTACK, 0); }
		public TerminalNode DECAY() { return getToken(SIDScoreParser.DECAY, 0); }
		public TerminalNode SUSTAIN() { return getToken(SIDScoreParser.SUSTAIN, 0); }
		public TerminalNode RELEASE() { return getToken(SIDScoreParser.RELEASE, 0); }
		public TerminalNode FILTER() { return getToken(SIDScoreParser.FILTER, 0); }
		public FilterSpecContext filterSpec() {
			return getRuleContext(FilterSpecContext.class,0);
		}
		public TerminalNode FILTERROUTE() { return getToken(SIDScoreParser.FILTERROUTE, 0); }
		public TerminalNode CUTOFF() { return getToken(SIDScoreParser.CUTOFF, 0); }
		public TerminalNode RES() { return getToken(SIDScoreParser.RES, 0); }
		public TerminalNode VOLUME() { return getToken(SIDScoreParser.VOLUME, 0); }
		public EffectAssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectAssignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectAssignment(this);
		}
	}

	public final EffectAssignmentContext effectAssignment() throws RecognitionException {
		EffectAssignmentContext _localctx = new EffectAssignmentContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_effectAssignment);
		try {
			setState(296);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(232);
				match(WAVE);
				setState(233);
				match(EQ);
				setState(234);
				waveList();
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(235);
				match(GATE);
				setState(236);
				match(EQ);
				setState(237);
				onOff();
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 3);
				{
				setState(238);
				match(SYNC);
				setState(239);
				match(EQ);
				setState(240);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 4);
				{
				setState(241);
				match(RING);
				setState(242);
				match(EQ);
				setState(243);
				onOff();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 5);
				{
				setState(244);
				match(RESET);
				}
				break;
			case PITCH:
				enterOuterAlt(_localctx, 6);
				{
				setState(245);
				match(PITCH);
				setState(246);
				match(EQ);
				setState(247);
				match(NOTE);
				}
				break;
			case FREQ:
				enterOuterAlt(_localctx, 7);
				{
				setState(248);
				match(FREQ);
				setState(249);
				match(EQ);
				setState(250);
				numericValue();
				}
				break;
			case PW:
				enterOuterAlt(_localctx, 8);
				{
				setState(251);
				match(PW);
				setState(252);
				match(EQ);
				setState(253);
				numericValue();
				}
				break;
			case HIPULSE:
				enterOuterAlt(_localctx, 9);
				{
				setState(254);
				match(HIPULSE);
				setState(255);
				match(EQ);
				setState(256);
				numericValue();
				}
				break;
			case LOWPULSE:
				enterOuterAlt(_localctx, 10);
				{
				setState(257);
				match(LOWPULSE);
				setState(258);
				match(EQ);
				setState(259);
				numericValue();
				}
				break;
			case ADSR:
				enterOuterAlt(_localctx, 11);
				{
				setState(260);
				match(ADSR);
				setState(261);
				match(EQ);
				setState(262);
				match(INT);
				setState(263);
				match(COMMA);
				setState(264);
				match(INT);
				setState(265);
				match(COMMA);
				setState(266);
				match(INT);
				setState(267);
				match(COMMA);
				setState(268);
				match(INT);
				}
				break;
			case ATTACK:
				enterOuterAlt(_localctx, 12);
				{
				setState(269);
				match(ATTACK);
				setState(270);
				match(EQ);
				setState(271);
				match(INT);
				}
				break;
			case DECAY:
				enterOuterAlt(_localctx, 13);
				{
				setState(272);
				match(DECAY);
				setState(273);
				match(EQ);
				setState(274);
				match(INT);
				}
				break;
			case SUSTAIN:
				enterOuterAlt(_localctx, 14);
				{
				setState(275);
				match(SUSTAIN);
				setState(276);
				match(EQ);
				setState(277);
				match(INT);
				}
				break;
			case RELEASE:
				enterOuterAlt(_localctx, 15);
				{
				setState(278);
				match(RELEASE);
				setState(279);
				match(EQ);
				setState(280);
				match(INT);
				}
				break;
			case FILTER:
				enterOuterAlt(_localctx, 16);
				{
				setState(281);
				match(FILTER);
				setState(282);
				match(EQ);
				setState(283);
				filterSpec();
				}
				break;
			case FILTERROUTE:
				enterOuterAlt(_localctx, 17);
				{
				setState(284);
				match(FILTERROUTE);
				setState(285);
				match(EQ);
				setState(286);
				numericValue();
				}
				break;
			case CUTOFF:
				enterOuterAlt(_localctx, 18);
				{
				setState(287);
				match(CUTOFF);
				setState(288);
				match(EQ);
				setState(289);
				numericValue();
				}
				break;
			case RES:
				enterOuterAlt(_localctx, 19);
				{
				setState(290);
				match(RES);
				setState(291);
				match(EQ);
				setState(292);
				match(INT);
				}
				break;
			case VOLUME:
				enterOuterAlt(_localctx, 20);
				{
				setState(293);
				match(VOLUME);
				setState(294);
				match(EQ);
				setState(295);
				match(INT);
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
	public static class EffectSweepContext extends ParserRuleContext {
		public EffectSweepParamContext effectSweepParam() {
			return getRuleContext(EffectSweepParamContext.class,0);
		}
		public List<EffectSweepValueContext> effectSweepValue() {
			return getRuleContexts(EffectSweepValueContext.class);
		}
		public EffectSweepValueContext effectSweepValue(int i) {
			return getRuleContext(EffectSweepValueContext.class,i);
		}
		public TerminalNode TO() { return getToken(SIDScoreParser.TO, 0); }
		public TerminalNode AT() { return getToken(SIDScoreParser.AT, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public EffectSweepCurveContext effectSweepCurve() {
			return getRuleContext(EffectSweepCurveContext.class,0);
		}
		public EffectSweepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectSweep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectSweep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectSweep(this);
		}
	}

	public final EffectSweepContext effectSweep() throws RecognitionException {
		EffectSweepContext _localctx = new EffectSweepContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_effectSweep);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			effectSweepParam();
			setState(299);
			effectSweepValue();
			setState(300);
			match(TO);
			setState(301);
			effectSweepValue();
			setState(302);
			match(AT);
			setState(303);
			match(INT);
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 251658240L) != 0)) {
				{
				setState(304);
				effectSweepCurve();
				}
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
	public static class EffectSweepParamContext extends ParserRuleContext {
		public TerminalNode PITCH() { return getToken(SIDScoreParser.PITCH, 0); }
		public TerminalNode FREQ() { return getToken(SIDScoreParser.FREQ, 0); }
		public TerminalNode PW() { return getToken(SIDScoreParser.PW, 0); }
		public TerminalNode CUTOFF() { return getToken(SIDScoreParser.CUTOFF, 0); }
		public TerminalNode VOLUME() { return getToken(SIDScoreParser.VOLUME, 0); }
		public EffectSweepParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectSweepParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectSweepParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectSweepParam(this);
		}
	}

	public final EffectSweepParamContext effectSweepParam() throws RecognitionException {
		EffectSweepParamContext _localctx = new EffectSweepParamContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_effectSweepParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			_la = _input.LA(1);
			if ( !(((((_la - 35)) & ~0x3f) == 0 && ((1L << (_la - 35)) & 17985306625L) != 0)) ) {
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
	public static class EffectSweepValueContext extends ParserRuleContext {
		public TerminalNode NOTE() { return getToken(SIDScoreParser.NOTE, 0); }
		public NumericValueContext numericValue() {
			return getRuleContext(NumericValueContext.class,0);
		}
		public EffectSweepValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectSweepValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectSweepValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectSweepValue(this);
		}
	}

	public final EffectSweepValueContext effectSweepValue() throws RecognitionException {
		EffectSweepValueContext _localctx = new EffectSweepValueContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_effectSweepValue);
		try {
			setState(311);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(309);
				match(NOTE);
				}
				break;
			case INT:
			case HEX:
				enterOuterAlt(_localctx, 2);
				{
				setState(310);
				numericValue();
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
	public static class EffectSweepCurveContext extends ParserRuleContext {
		public TerminalNode LINEAR() { return getToken(SIDScoreParser.LINEAR, 0); }
		public TerminalNode EXP() { return getToken(SIDScoreParser.EXP, 0); }
		public TerminalNode LOG() { return getToken(SIDScoreParser.LOG, 0); }
		public TerminalNode STEP() { return getToken(SIDScoreParser.STEP, 0); }
		public EffectSweepCurveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectSweepCurve; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterEffectSweepCurve(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitEffectSweepCurve(this);
		}
	}

	public final EffectSweepCurveContext effectSweepCurve() throws RecognitionException {
		EffectSweepCurveContext _localctx = new EffectSweepCurveContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_effectSweepCurve);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 251658240L) != 0)) ) {
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
	public static class NumericValueContext extends ParserRuleContext {
		public TerminalNode HEX() { return getToken(SIDScoreParser.HEX, 0); }
		public TerminalNode INT() { return getToken(SIDScoreParser.INT, 0); }
		public NumericValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).enterNumericValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SIDScoreParserListener ) ((SIDScoreParserListener)listener).exitNumericValue(this);
		}
	}

	public final NumericValueContext numericValue() throws RecognitionException {
		NumericValueContext _localctx = new NumericValueContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_numericValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(315);
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
		enterRule(_localctx, 58, RULE_instrStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			match(INSTR);
			setState(318);
			match(ID);
			setState(325);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				{
				setState(319);
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
				setState(321); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(320);
					instrParam();
					}
					}
					setState(323); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 9222247904326975488L) != 0) );
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
		enterRule(_localctx, 60, RULE_instrParam);
		int _la;
		try {
			setState(393);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(327);
				match(WAVE);
				setState(328);
				match(EQ);
				setState(329);
				waveList();
				}
				break;
			case ADSR:
				enterOuterAlt(_localctx, 2);
				{
				setState(330);
				match(ADSR);
				setState(331);
				match(EQ);
				setState(332);
				match(INT);
				setState(333);
				match(COMMA);
				setState(334);
				match(INT);
				setState(335);
				match(COMMA);
				setState(336);
				match(INT);
				setState(337);
				match(COMMA);
				setState(338);
				match(INT);
				}
				break;
			case PW:
				enterOuterAlt(_localctx, 3);
				{
				setState(339);
				match(PW);
				setState(340);
				match(EQ);
				setState(341);
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
				setState(342);
				match(HIPULSE);
				setState(343);
				match(EQ);
				setState(344);
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
				setState(345);
				match(LOWPULSE);
				setState(346);
				match(EQ);
				setState(347);
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
				setState(348);
				match(FILTER);
				setState(349);
				match(EQ);
				setState(350);
				filterSpec();
				}
				break;
			case CUTOFF:
				enterOuterAlt(_localctx, 7);
				{
				setState(351);
				match(CUTOFF);
				setState(352);
				match(EQ);
				setState(353);
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
				setState(354);
				match(RES);
				setState(355);
				match(EQ);
				setState(356);
				match(INT);
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 9);
				{
				setState(357);
				match(GATE);
				setState(358);
				match(EQ);
				setState(359);
				gateMode();
				}
				break;
			case GATEMIN:
				enterOuterAlt(_localctx, 10);
				{
				setState(360);
				match(GATEMIN);
				setState(361);
				match(EQ);
				setState(362);
				match(INT);
				}
				break;
			case WAVESEQ:
				enterOuterAlt(_localctx, 11);
				{
				setState(363);
				match(WAVESEQ);
				setState(364);
				match(EQ);
				setState(365);
				match(ID);
				}
				break;
			case PWMIN:
				enterOuterAlt(_localctx, 12);
				{
				setState(366);
				match(PWMIN);
				setState(367);
				match(EQ);
				setState(368);
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
				setState(369);
				match(PWMAX);
				setState(370);
				match(EQ);
				setState(371);
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
				setState(372);
				match(PWSWEEP);
				setState(373);
				match(EQ);
				setState(374);
				signedInt();
				}
				break;
			case PWSEQ:
				enterOuterAlt(_localctx, 15);
				{
				setState(375);
				match(PWSEQ);
				setState(376);
				match(EQ);
				setState(377);
				match(ID);
				}
				break;
			case FILTERSEQ:
				enterOuterAlt(_localctx, 16);
				{
				setState(378);
				match(FILTERSEQ);
				setState(379);
				match(EQ);
				setState(380);
				match(ID);
				}
				break;
			case GATESEQ:
				enterOuterAlt(_localctx, 17);
				{
				setState(381);
				match(GATESEQ);
				setState(382);
				match(EQ);
				setState(383);
				match(ID);
				}
				break;
			case PITCHSEQ:
				enterOuterAlt(_localctx, 18);
				{
				setState(384);
				match(PITCHSEQ);
				setState(385);
				match(EQ);
				setState(386);
				match(ID);
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 19);
				{
				setState(387);
				match(SYNC);
				setState(388);
				match(EQ);
				setState(389);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 20);
				{
				setState(390);
				match(RING);
				setState(391);
				match(EQ);
				setState(392);
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
		enterRule(_localctx, 62, RULE_signedInt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(395);
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

			setState(398);
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
		enterRule(_localctx, 64, RULE_waveList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			match(WAVEVAL);
			setState(405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(401);
				match(PLUS);
				setState(402);
				match(WAVEVAL);
				}
				}
				setState(407);
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
		enterRule(_localctx, 66, RULE_onOff);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(408);
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
		enterRule(_localctx, 68, RULE_gateMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
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
		enterRule(_localctx, 70, RULE_filterSpec);
		try {
			setState(414);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OFF:
				enterOuterAlt(_localctx, 1);
				{
				setState(412);
				match(OFF);
				}
				break;
			case LP:
			case BP:
			case HP:
				enterOuterAlt(_localctx, 2);
				{
				setState(413);
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
		enterRule(_localctx, 72, RULE_filterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			filterMode();
			setState(421);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(417);
				match(PLUS);
				setState(418);
				filterMode();
				}
				}
				setState(423);
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
		enterRule(_localctx, 74, RULE_filterMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(424);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 61572651155456L) != 0)) ) {
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
		enterRule(_localctx, 76, RULE_tableStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(426);
			match(TABLE);
			setState(427);
			match(ID);
			setState(428);
			match(ID);
			setState(429);
			match(LBRACE);
			setState(433);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1337974654502912L) != 0) || ((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 50429953L) != 0)) {
				{
				{
				setState(430);
				tableStep();
				}
				}
				setState(435);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(436);
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
		enterRule(_localctx, 78, RULE_tableStep);
		try {
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(438);
				tableValue();
				setState(439);
				match(AT);
				setState(440);
				tableDuration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(442);
				tableValue();
				setState(443);
				match(HOLD);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(445);
				tableCtrl();
				setState(446);
				match(AT);
				setState(447);
				tableDuration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(449);
				tableCtrl();
				setState(450);
				match(HOLD);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(452);
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
		enterRule(_localctx, 80, RULE_tableValue);
		try {
			setState(461);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(455);
				waveList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(456);
				match(ON);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(457);
				match(OFF);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(458);
				match(HEX);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(459);
				signedInt();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(460);
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
		enterRule(_localctx, 82, RULE_tableDuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
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
		enterRule(_localctx, 84, RULE_tableCtrl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(466); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(465);
				tableCtrlItem();
				}
				}
				setState(468); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 1126868421967872L) != 0) );
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
		enterRule(_localctx, 86, RULE_tableCtrlItem);
		try {
			setState(486);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(470);
				match(WAVE);
				setState(471);
				match(EQ);
				setState(472);
				waveList();
				}
				break;
			case NOTEK:
				enterOuterAlt(_localctx, 2);
				{
				setState(473);
				match(NOTEK);
				setState(474);
				match(EQ);
				setState(475);
				noteSpec();
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(476);
				match(GATE);
				setState(477);
				match(EQ);
				setState(478);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 4);
				{
				setState(479);
				match(RING);
				setState(480);
				match(EQ);
				setState(481);
				onOff();
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 5);
				{
				setState(482);
				match(SYNC);
				setState(483);
				match(EQ);
				setState(484);
				onOff();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 6);
				{
				setState(485);
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
		enterRule(_localctx, 88, RULE_noteSpec);
		try {
			setState(491);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(488);
				match(NOTE);
				}
				break;
			case PLUS:
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(489);
				signedInt();
				}
				break;
			case OFF:
				enterOuterAlt(_localctx, 3);
				{
				setState(490);
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
		enterRule(_localctx, 90, RULE_voiceBlock);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(493);
			match(VOICE);
			setState(494);
			match(INT);
			setState(495);
			match(ID);
			setState(496);
			match(COLON);
			setState(500);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(497);
					voiceItem();
					}
					} 
				}
				setState(502);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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
		enterRule(_localctx, 92, RULE_voiceItem);
		try {
			setState(513);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OCTAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(503);
				match(OCTAVE);
				}
				break;
			case LENGTH:
				enterOuterAlt(_localctx, 2);
				{
				setState(504);
				match(LENGTH);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 3);
				{
				setState(505);
				match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 4);
				{
				setState(506);
				match(LT);
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 5);
				{
				setState(507);
				swingStmt();
				}
				break;
			case NOTE:
			case REST:
			case HIT:
				enterOuterAlt(_localctx, 6);
				{
				setState(508);
				noteOrRestOrHit();
				}
				break;
			case AMP:
				enterOuterAlt(_localctx, 7);
				{
				setState(509);
				match(AMP);
				}
				break;
			case LEG_START:
				enterOuterAlt(_localctx, 8);
				{
				setState(510);
				legatoScope();
				}
				break;
			case TUPLET_START:
				enterOuterAlt(_localctx, 9);
				{
				setState(511);
				tuplet();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 10);
				{
				setState(512);
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
		enterRule(_localctx, 94, RULE_noteOrRestOrHit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(515);
			_la = _input.LA(1);
			if ( !(((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 7L) != 0)) ) {
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
		enterRule(_localctx, 96, RULE_legatoScope);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(517);
			match(LEG_START);
			setState(521);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 8937263718144671745L) != 0)) {
				{
				{
				setState(518);
				voiceItem();
				}
				}
				setState(523);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(524);
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
		enterRule(_localctx, 98, RULE_tuplet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(526);
			match(TUPLET_START);
			setState(530);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 8937263718144671745L) != 0)) {
				{
				{
				setState(527);
				voiceItem();
				}
				}
				setState(532);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(533);
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
		enterRule(_localctx, 100, RULE_repeat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			match(LPAREN);
			setState(539);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 8937263718144671745L) != 0)) {
				{
				{
				setState(536);
				voiceItem();
				}
				}
				setState(541);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(542);
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
		"\u0004\u0001b\u0221\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u0001\u0000\u0005\u0000h\b\u0000\n\u0000\f\u0000k\t\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001|\b\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\n\u0001\n\u0001\n\u0001\n\u0005\n\u009e\b\n\n\n\f\n\u00a1\t\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00ae\b\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00b4\b\f\n\f\f\f\u00b7\t\f\u0001"+
		"\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00c0\b\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u00d5\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u00d9"+
		"\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0005\u0016\u00e2\b\u0016\n\u0016\f\u0016\u00e5\t\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u0129\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u0132\b\u0018\u0001\u0019"+
		"\u0001\u0019\u0001\u001a\u0001\u001a\u0003\u001a\u0138\b\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0004\u001d\u0142\b\u001d\u000b\u001d\f\u001d\u0143\u0003"+
		"\u001d\u0146\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0003\u001e\u018a\b\u001e\u0001\u001f\u0003\u001f\u018d"+
		"\b\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0005 \u0194\b "+
		"\n \f \u0197\t \u0001!\u0001!\u0001\"\u0001\"\u0001#\u0001#\u0003#\u019f"+
		"\b#\u0001$\u0001$\u0001$\u0005$\u01a4\b$\n$\f$\u01a7\t$\u0001%\u0001%"+
		"\u0001&\u0001&\u0001&\u0001&\u0001&\u0005&\u01b0\b&\n&\f&\u01b3\t&\u0001"+
		"&\u0001&\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u01c6"+
		"\b\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u01ce\b(\u0001)"+
		"\u0001)\u0001*\u0004*\u01d3\b*\u000b*\f*\u01d4\u0001+\u0001+\u0001+\u0001"+
		"+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0001+\u0001+\u0003+\u01e7\b+\u0001,\u0001,\u0001,\u0003,\u01ec\b,\u0001"+
		"-\u0001-\u0001-\u0001-\u0001-\u0005-\u01f3\b-\n-\f-\u01f6\t-\u0001.\u0001"+
		".\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0003.\u0202"+
		"\b.\u0001/\u0001/\u00010\u00010\u00050\u0208\b0\n0\f0\u020b\t0\u00010"+
		"\u00010\u00011\u00011\u00051\u0211\b1\n1\f1\u0214\t1\u00011\u00011\u0001"+
		"2\u00012\u00052\u021a\b2\n2\f2\u021d\t2\u00012\u00012\u00012\u0000\u0000"+
		"3\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bd\u0000\u000e\u0002\u0000"+
		"..^^\u0001\u000001\u0002\u0000\u000e\u000e^^\u0001\u0000\u0012\u0014\u0001"+
		"\u0000\u0015\u0016\u0004\u0000##44?@EE\u0001\u0000\u0018\u001b\u0001\u0000"+
		"^_\u0001\u0000UV\u0001\u0000./\u0001\u0000)*\u0001\u0000+-\u0002\u0000"+
		"\f\f^^\u0001\u0000Z\\\u0259\u0000i\u0001\u0000\u0000\u0000\u0002{\u0001"+
		"\u0000\u0000\u0000\u0004}\u0001\u0000\u0000\u0000\u0006\u0080\u0001\u0000"+
		"\u0000\u0000\b\u0083\u0001\u0000\u0000\u0000\n\u0086\u0001\u0000\u0000"+
		"\u0000\f\u0089\u0001\u0000\u0000\u0000\u000e\u008e\u0001\u0000\u0000\u0000"+
		"\u0010\u0091\u0001\u0000\u0000\u0000\u0012\u0094\u0001\u0000\u0000\u0000"+
		"\u0014\u0099\u0001\u0000\u0000\u0000\u0016\u00ad\u0001\u0000\u0000\u0000"+
		"\u0018\u00af\u0001\u0000\u0000\u0000\u001a\u00bf\u0001\u0000\u0000\u0000"+
		"\u001c\u00c1\u0001\u0000\u0000\u0000\u001e\u00c4\u0001\u0000\u0000\u0000"+
		" \u00c6\u0001\u0000\u0000\u0000\"\u00ca\u0001\u0000\u0000\u0000$\u00cd"+
		"\u0001\u0000\u0000\u0000&\u00d0\u0001\u0000\u0000\u0000(\u00d8\u0001\u0000"+
		"\u0000\u0000*\u00da\u0001\u0000\u0000\u0000,\u00dd\u0001\u0000\u0000\u0000"+
		".\u0128\u0001\u0000\u0000\u00000\u012a\u0001\u0000\u0000\u00002\u0133"+
		"\u0001\u0000\u0000\u00004\u0137\u0001\u0000\u0000\u00006\u0139\u0001\u0000"+
		"\u0000\u00008\u013b\u0001\u0000\u0000\u0000:\u013d\u0001\u0000\u0000\u0000"+
		"<\u0189\u0001\u0000\u0000\u0000>\u018c\u0001\u0000\u0000\u0000@\u0190"+
		"\u0001\u0000\u0000\u0000B\u0198\u0001\u0000\u0000\u0000D\u019a\u0001\u0000"+
		"\u0000\u0000F\u019e\u0001\u0000\u0000\u0000H\u01a0\u0001\u0000\u0000\u0000"+
		"J\u01a8\u0001\u0000\u0000\u0000L\u01aa\u0001\u0000\u0000\u0000N\u01c5"+
		"\u0001\u0000\u0000\u0000P\u01cd\u0001\u0000\u0000\u0000R\u01cf\u0001\u0000"+
		"\u0000\u0000T\u01d2\u0001\u0000\u0000\u0000V\u01e6\u0001\u0000\u0000\u0000"+
		"X\u01eb\u0001\u0000\u0000\u0000Z\u01ed\u0001\u0000\u0000\u0000\\\u0201"+
		"\u0001\u0000\u0000\u0000^\u0203\u0001\u0000\u0000\u0000`\u0205\u0001\u0000"+
		"\u0000\u0000b\u020e\u0001\u0000\u0000\u0000d\u0217\u0001\u0000\u0000\u0000"+
		"fh\u0003\u0002\u0001\u0000gf\u0001\u0000\u0000\u0000hk\u0001\u0000\u0000"+
		"\u0000ig\u0001\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000jl\u0001\u0000"+
		"\u0000\u0000ki\u0001\u0000\u0000\u0000lm\u0005\u0000\u0000\u0001m\u0001"+
		"\u0001\u0000\u0000\u0000n|\u0003\u0004\u0002\u0000o|\u0003\u0006\u0003"+
		"\u0000p|\u0003\b\u0004\u0000q|\u0003\n\u0005\u0000r|\u0003\f\u0006\u0000"+
		"s|\u0003\u0010\b\u0000t|\u0003\u0012\t\u0000u|\u0003\u0014\n\u0000v|\u0003"+
		"L&\u0000w|\u0003\u0018\f\u0000x|\u0003:\u001d\u0000y|\u0003\u000e\u0007"+
		"\u0000z|\u0003Z-\u0000{n\u0001\u0000\u0000\u0000{o\u0001\u0000\u0000\u0000"+
		"{p\u0001\u0000\u0000\u0000{q\u0001\u0000\u0000\u0000{r\u0001\u0000\u0000"+
		"\u0000{s\u0001\u0000\u0000\u0000{t\u0001\u0000\u0000\u0000{u\u0001\u0000"+
		"\u0000\u0000{v\u0001\u0000\u0000\u0000{w\u0001\u0000\u0000\u0000{x\u0001"+
		"\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000{z\u0001\u0000\u0000\u0000"+
		"|\u0003\u0001\u0000\u0000\u0000}~\u0005\u0001\u0000\u0000~\u007f\u0005"+
		"`\u0000\u0000\u007f\u0005\u0001\u0000\u0000\u0000\u0080\u0081\u0005\u0002"+
		"\u0000\u0000\u0081\u0082\u0005`\u0000\u0000\u0082\u0007\u0001\u0000\u0000"+
		"\u0000\u0083\u0084\u0005\u0003\u0000\u0000\u0084\u0085\u0005`\u0000\u0000"+
		"\u0085\t\u0001\u0000\u0000\u0000\u0086\u0087\u0005\u0004\u0000\u0000\u0087"+
		"\u0088\u0005^\u0000\u0000\u0088\u000b\u0001\u0000\u0000\u0000\u0089\u008a"+
		"\u0005\u0005\u0000\u0000\u008a\u008b\u0005^\u0000\u0000\u008b\u008c\u0005"+
		"T\u0000\u0000\u008c\u008d\u0005^\u0000\u0000\u008d\r\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0005\u001e\u0000\u0000\u008f\u0090\u0007\u0000\u0000"+
		"\u0000\u0090\u000f\u0001\u0000\u0000\u0000\u0091\u0092\u0005\u0006\u0000"+
		"\u0000\u0092\u0093\u0007\u0001\u0000\u0000\u0093\u0011\u0001\u0000\u0000"+
		"\u0000\u0094\u0095\u0005\b\u0000\u0000\u0095\u0096\u0005`\u0000\u0000"+
		"\u0096\u0097\u0005\t\u0000\u0000\u0097\u0098\u0005^\u0000\u0000\u0098"+
		"\u0013\u0001\u0000\u0000\u0000\u0099\u009a\u0005\u0007\u0000\u0000\u009a"+
		"\u009b\u0005^\u0000\u0000\u009b\u009f\u0005K\u0000\u0000\u009c\u009e\u0003"+
		"\u0016\u000b\u0000\u009d\u009c\u0001\u0000\u0000\u0000\u009e\u00a1\u0001"+
		"\u0000\u0000\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u009f\u00a0\u0001"+
		"\u0000\u0000\u0000\u00a0\u00a2\u0001\u0000\u0000\u0000\u00a1\u009f\u0001"+
		"\u0000\u0000\u0000\u00a2\u00a3\u0005J\u0000\u0000\u00a3\u0015\u0001\u0000"+
		"\u0000\u0000\u00a4\u00ae\u0003\u0004\u0002\u0000\u00a5\u00ae\u0003\u0006"+
		"\u0003\u0000\u00a6\u00ae\u0003\b\u0004\u0000\u00a7\u00ae\u0003\n\u0005"+
		"\u0000\u00a8\u00ae\u0003\f\u0006\u0000\u00a9\u00ae\u0003\u0010\b\u0000"+
		"\u00aa\u00ae\u0003\u000e\u0007\u0000\u00ab\u00ae\u0003\u0018\f\u0000\u00ac"+
		"\u00ae\u0003Z-\u0000\u00ad\u00a4\u0001\u0000\u0000\u0000\u00ad\u00a5\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a6\u0001\u0000\u0000\u0000\u00ad\u00a7\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a8\u0001\u0000\u0000\u0000\u00ad\u00a9\u0001"+
		"\u0000\u0000\u0000\u00ad\u00aa\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ae\u0017\u0001"+
		"\u0000\u0000\u0000\u00af\u00b0\u0005\r\u0000\u0000\u00b0\u00b1\u0005]"+
		"\u0000\u0000\u00b1\u00b5\u0005K\u0000\u0000\u00b2\u00b4\u0003\u001a\r"+
		"\u0000\u00b3\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000"+
		"\u0000\u00b6\u00b8\u0001\u0000\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b9\u0005J\u0000\u0000\u00b9\u0019\u0001\u0000\u0000\u0000"+
		"\u00ba\u00c0\u0003\u001c\u000e\u0000\u00bb\u00c0\u0003 \u0010\u0000\u00bc"+
		"\u00c0\u0003\"\u0011\u0000\u00bd\u00c0\u0003$\u0012\u0000\u00be\u00c0"+
		"\u0003(\u0014\u0000\u00bf\u00ba\u0001\u0000\u0000\u0000\u00bf\u00bb\u0001"+
		"\u0000\u0000\u0000\u00bf\u00bc\u0001\u0000\u0000\u0000\u00bf\u00bd\u0001"+
		"\u0000\u0000\u0000\u00bf\u00be\u0001\u0000\u0000\u0000\u00c0\u001b\u0001"+
		"\u0000\u0000\u0000\u00c1\u00c2\u0005\u001d\u0000\u0000\u00c2\u00c3\u0003"+
		"\u001e\u000f\u0000\u00c3\u001d\u0001\u0000\u0000\u0000\u00c4\u00c5\u0007"+
		"\u0002\u0000\u0000\u00c5\u001f\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005"+
		"\u000f\u0000\u0000\u00c7\u00c8\u0005^\u0000\u0000\u00c8\u00c9\u0005\u0010"+
		"\u0000\u0000\u00c9!\u0001\u0000\u0000\u0000\u00ca\u00cb\u0005\u0011\u0000"+
		"\u0000\u00cb\u00cc\u0005^\u0000\u0000\u00cc#\u0001\u0000\u0000\u0000\u00cd"+
		"\u00ce\u0005)\u0000\u0000\u00ce\u00cf\u0003&\u0013\u0000\u00cf%\u0001"+
		"\u0000\u0000\u0000\u00d0\u00d1\u0007\u0003\u0000\u0000\u00d1\'\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d4\u0003.\u0017\u0000\u00d3\u00d5\u0003*\u0015\u0000"+
		"\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d4\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d5\u00d9\u0001\u0000\u0000\u0000\u00d6\u00d9\u00030\u0018\u0000\u00d7"+
		"\u00d9\u0003,\u0016\u0000\u00d8\u00d2\u0001\u0000\u0000\u0000\u00d8\u00d6"+
		"\u0001\u0000\u0000\u0000\u00d8\u00d7\u0001\u0000\u0000\u0000\u00d9)\u0001"+
		"\u0000\u0000\u0000\u00da\u00db\u0005L\u0000\u0000\u00db\u00dc\u0005^\u0000"+
		"\u0000\u00dc+\u0001\u0000\u0000\u0000\u00dd\u00de\u0007\u0004\u0000\u0000"+
		"\u00de\u00df\u0005^\u0000\u0000\u00df\u00e3\u0005K\u0000\u0000\u00e0\u00e2"+
		"\u0003.\u0017\u0000\u00e1\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e5\u0001"+
		"\u0000\u0000\u0000\u00e3\u00e1\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001"+
		"\u0000\u0000\u0000\u00e4\u00e6\u0001\u0000\u0000\u0000\u00e5\u00e3\u0001"+
		"\u0000\u0000\u0000\u00e6\u00e7\u0005J\u0000\u0000\u00e7-\u0001\u0000\u0000"+
		"\u0000\u00e8\u00e9\u00052\u0000\u0000\u00e9\u00ea\u0005R\u0000\u0000\u00ea"+
		"\u0129\u0003@ \u0000\u00eb\u00ec\u0005\'\u0000\u0000\u00ec\u00ed\u0005"+
		"R\u0000\u0000\u00ed\u0129\u0003B!\u0000\u00ee\u00ef\u0005\u001f\u0000"+
		"\u0000\u00ef\u00f0\u0005R\u0000\u0000\u00f0\u0129\u0003B!\u0000\u00f1"+
		"\u00f2\u0005 \u0000\u0000\u00f2\u00f3\u0005R\u0000\u0000\u00f3\u0129\u0003"+
		"B!\u0000\u00f4\u0129\u0005&\u0000\u0000\u00f5\u00f6\u0005?\u0000\u0000"+
		"\u00f6\u00f7\u0005R\u0000\u0000\u00f7\u0129\u0005Z\u0000\u0000\u00f8\u00f9"+
		"\u0005@\u0000\u0000\u00f9\u00fa\u0005R\u0000\u0000\u00fa\u0129\u00038"+
		"\u001c\u0000\u00fb\u00fc\u00054\u0000\u0000\u00fc\u00fd\u0005R\u0000\u0000"+
		"\u00fd\u0129\u00038\u001c\u0000\u00fe\u00ff\u00055\u0000\u0000\u00ff\u0100"+
		"\u0005R\u0000\u0000\u0100\u0129\u00038\u001c\u0000\u0101\u0102\u00056"+
		"\u0000\u0000\u0102\u0103\u0005R\u0000\u0000\u0103\u0129\u00038\u001c\u0000"+
		"\u0104\u0105\u00053\u0000\u0000\u0105\u0106\u0005R\u0000\u0000\u0106\u0107"+
		"\u0005^\u0000\u0000\u0107\u0108\u0005S\u0000\u0000\u0108\u0109\u0005^"+
		"\u0000\u0000\u0109\u010a\u0005S\u0000\u0000\u010a\u010b\u0005^\u0000\u0000"+
		"\u010b\u010c\u0005S\u0000\u0000\u010c\u0129\u0005^\u0000\u0000\u010d\u010e"+
		"\u0005A\u0000\u0000\u010e\u010f\u0005R\u0000\u0000\u010f\u0129\u0005^"+
		"\u0000\u0000\u0110\u0111\u0005B\u0000\u0000\u0111\u0112\u0005R\u0000\u0000"+
		"\u0112\u0129\u0005^\u0000\u0000\u0113\u0114\u0005C\u0000\u0000\u0114\u0115"+
		"\u0005R\u0000\u0000\u0115\u0129\u0005^\u0000\u0000\u0116\u0117\u0005D"+
		"\u0000\u0000\u0117\u0118\u0005R\u0000\u0000\u0118\u0129\u0005^\u0000\u0000"+
		"\u0119\u011a\u0005!\u0000\u0000\u011a\u011b\u0005R\u0000\u0000\u011b\u0129"+
		"\u0003F#\u0000\u011c\u011d\u0005\"\u0000\u0000\u011d\u011e\u0005R\u0000"+
		"\u0000\u011e\u0129\u00038\u001c\u0000\u011f\u0120\u0005#\u0000\u0000\u0120"+
		"\u0121\u0005R\u0000\u0000\u0121\u0129\u00038\u001c\u0000\u0122\u0123\u0005"+
		"$\u0000\u0000\u0123\u0124\u0005R\u0000\u0000\u0124\u0129\u0005^\u0000"+
		"\u0000\u0125\u0126\u0005E\u0000\u0000\u0126\u0127\u0005R\u0000\u0000\u0127"+
		"\u0129\u0005^\u0000\u0000\u0128\u00e8\u0001\u0000\u0000\u0000\u0128\u00eb"+
		"\u0001\u0000\u0000\u0000\u0128\u00ee\u0001\u0000\u0000\u0000\u0128\u00f1"+
		"\u0001\u0000\u0000\u0000\u0128\u00f4\u0001\u0000\u0000\u0000\u0128\u00f5"+
		"\u0001\u0000\u0000\u0000\u0128\u00f8\u0001\u0000\u0000\u0000\u0128\u00fb"+
		"\u0001\u0000\u0000\u0000\u0128\u00fe\u0001\u0000\u0000\u0000\u0128\u0101"+
		"\u0001\u0000\u0000\u0000\u0128\u0104\u0001\u0000\u0000\u0000\u0128\u010d"+
		"\u0001\u0000\u0000\u0000\u0128\u0110\u0001\u0000\u0000\u0000\u0128\u0113"+
		"\u0001\u0000\u0000\u0000\u0128\u0116\u0001\u0000\u0000\u0000\u0128\u0119"+
		"\u0001\u0000\u0000\u0000\u0128\u011c\u0001\u0000\u0000\u0000\u0128\u011f"+
		"\u0001\u0000\u0000\u0000\u0128\u0122\u0001\u0000\u0000\u0000\u0128\u0125"+
		"\u0001\u0000\u0000\u0000\u0129/\u0001\u0000\u0000\u0000\u012a\u012b\u0003"+
		"2\u0019\u0000\u012b\u012c\u00034\u001a\u0000\u012c\u012d\u0005\u0017\u0000"+
		"\u0000\u012d\u012e\u00034\u001a\u0000\u012e\u012f\u0005L\u0000\u0000\u012f"+
		"\u0131\u0005^\u0000\u0000\u0130\u0132\u00036\u001b\u0000\u0131\u0130\u0001"+
		"\u0000\u0000\u0000\u0131\u0132\u0001\u0000\u0000\u0000\u01321\u0001\u0000"+
		"\u0000\u0000\u0133\u0134\u0007\u0005\u0000\u0000\u01343\u0001\u0000\u0000"+
		"\u0000\u0135\u0138\u0005Z\u0000\u0000\u0136\u0138\u00038\u001c\u0000\u0137"+
		"\u0135\u0001\u0000\u0000\u0000\u0137\u0136\u0001\u0000\u0000\u0000\u0138"+
		"5\u0001\u0000\u0000\u0000\u0139\u013a\u0007\u0006\u0000\u0000\u013a7\u0001"+
		"\u0000\u0000\u0000\u013b\u013c\u0007\u0007\u0000\u0000\u013c9\u0001\u0000"+
		"\u0000\u0000\u013d\u013e\u0005\u001c\u0000\u0000\u013e\u0145\u0005]\u0000"+
		"\u0000\u013f\u0146\u0005`\u0000\u0000\u0140\u0142\u0003<\u001e\u0000\u0141"+
		"\u0140\u0001\u0000\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u0143"+
		"\u0141\u0001\u0000\u0000\u0000\u0143\u0144\u0001\u0000\u0000\u0000\u0144"+
		"\u0146\u0001\u0000\u0000\u0000\u0145\u013f\u0001\u0000\u0000\u0000\u0145"+
		"\u0141\u0001\u0000\u0000\u0000\u0146;\u0001\u0000\u0000\u0000\u0147\u0148"+
		"\u00052\u0000\u0000\u0148\u0149\u0005R\u0000\u0000\u0149\u018a\u0003@"+
		" \u0000\u014a\u014b\u00053\u0000\u0000\u014b\u014c\u0005R\u0000\u0000"+
		"\u014c\u014d\u0005^\u0000\u0000\u014d\u014e\u0005S\u0000\u0000\u014e\u014f"+
		"\u0005^\u0000\u0000\u014f\u0150\u0005S\u0000\u0000\u0150\u0151\u0005^"+
		"\u0000\u0000\u0151\u0152\u0005S\u0000\u0000\u0152\u018a\u0005^\u0000\u0000"+
		"\u0153\u0154\u00054\u0000\u0000\u0154\u0155\u0005R\u0000\u0000\u0155\u018a"+
		"\u0007\u0007\u0000\u0000\u0156\u0157\u00055\u0000\u0000\u0157\u0158\u0005"+
		"R\u0000\u0000\u0158\u018a\u0007\u0007\u0000\u0000\u0159\u015a\u00056\u0000"+
		"\u0000\u015a\u015b\u0005R\u0000\u0000\u015b\u018a\u0007\u0007\u0000\u0000"+
		"\u015c\u015d\u0005!\u0000\u0000\u015d\u015e\u0005R\u0000\u0000\u015e\u018a"+
		"\u0003F#\u0000\u015f\u0160\u0005#\u0000\u0000\u0160\u0161\u0005R\u0000"+
		"\u0000\u0161\u018a\u0007\u0007\u0000\u0000\u0162\u0163\u0005$\u0000\u0000"+
		"\u0163\u0164\u0005R\u0000\u0000\u0164\u018a\u0005^\u0000\u0000\u0165\u0166"+
		"\u0005\'\u0000\u0000\u0166\u0167\u0005R\u0000\u0000\u0167\u018a\u0003"+
		"D\"\u0000\u0168\u0169\u0005(\u0000\u0000\u0169\u016a\u0005R\u0000\u0000"+
		"\u016a\u018a\u0005^\u0000\u0000\u016b\u016c\u00057\u0000\u0000\u016c\u016d"+
		"\u0005R\u0000\u0000\u016d\u018a\u0005]\u0000\u0000\u016e\u016f\u00058"+
		"\u0000\u0000\u016f\u0170\u0005R\u0000\u0000\u0170\u018a\u0007\u0007\u0000"+
		"\u0000\u0171\u0172\u00059\u0000\u0000\u0172\u0173\u0005R\u0000\u0000\u0173"+
		"\u018a\u0007\u0007\u0000\u0000\u0174\u0175\u0005:\u0000\u0000\u0175\u0176"+
		"\u0005R\u0000\u0000\u0176\u018a\u0003>\u001f\u0000\u0177\u0178\u0005;"+
		"\u0000\u0000\u0178\u0179\u0005R\u0000\u0000\u0179\u018a\u0005]\u0000\u0000"+
		"\u017a\u017b\u0005<\u0000\u0000\u017b\u017c\u0005R\u0000\u0000\u017c\u018a"+
		"\u0005]\u0000\u0000\u017d\u017e\u0005=\u0000\u0000\u017e\u017f\u0005R"+
		"\u0000\u0000\u017f\u018a\u0005]\u0000\u0000\u0180\u0181\u0005>\u0000\u0000"+
		"\u0181\u0182\u0005R\u0000\u0000\u0182\u018a\u0005]\u0000\u0000\u0183\u0184"+
		"\u0005\u001f\u0000\u0000\u0184\u0185\u0005R\u0000\u0000\u0185\u018a\u0003"+
		"B!\u0000\u0186\u0187\u0005 \u0000\u0000\u0187\u0188\u0005R\u0000\u0000"+
		"\u0188\u018a\u0003B!\u0000\u0189\u0147\u0001\u0000\u0000\u0000\u0189\u014a"+
		"\u0001\u0000\u0000\u0000\u0189\u0153\u0001\u0000\u0000\u0000\u0189\u0156"+
		"\u0001\u0000\u0000\u0000\u0189\u0159\u0001\u0000\u0000\u0000\u0189\u015c"+
		"\u0001\u0000\u0000\u0000\u0189\u015f\u0001\u0000\u0000\u0000\u0189\u0162"+
		"\u0001\u0000\u0000\u0000\u0189\u0165\u0001\u0000\u0000\u0000\u0189\u0168"+
		"\u0001\u0000\u0000\u0000\u0189\u016b\u0001\u0000\u0000\u0000\u0189\u016e"+
		"\u0001\u0000\u0000\u0000\u0189\u0171\u0001\u0000\u0000\u0000\u0189\u0174"+
		"\u0001\u0000\u0000\u0000\u0189\u0177\u0001\u0000\u0000\u0000\u0189\u017a"+
		"\u0001\u0000\u0000\u0000\u0189\u017d\u0001\u0000\u0000\u0000\u0189\u0180"+
		"\u0001\u0000\u0000\u0000\u0189\u0183\u0001\u0000\u0000\u0000\u0189\u0186"+
		"\u0001\u0000\u0000\u0000\u018a=\u0001\u0000\u0000\u0000\u018b\u018d\u0007"+
		"\b\u0000\u0000\u018c\u018b\u0001\u0000\u0000\u0000\u018c\u018d\u0001\u0000"+
		"\u0000\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u018f\u0005^\u0000"+
		"\u0000\u018f?\u0001\u0000\u0000\u0000\u0190\u0195\u0005F\u0000\u0000\u0191"+
		"\u0192\u0005U\u0000\u0000\u0192\u0194\u0005F\u0000\u0000\u0193\u0191\u0001"+
		"\u0000\u0000\u0000\u0194\u0197\u0001\u0000\u0000\u0000\u0195\u0193\u0001"+
		"\u0000\u0000\u0000\u0195\u0196\u0001\u0000\u0000\u0000\u0196A\u0001\u0000"+
		"\u0000\u0000\u0197\u0195\u0001\u0000\u0000\u0000\u0198\u0199\u0007\t\u0000"+
		"\u0000\u0199C\u0001\u0000\u0000\u0000\u019a\u019b\u0007\n\u0000\u0000"+
		"\u019bE\u0001\u0000\u0000\u0000\u019c\u019f\u0005.\u0000\u0000\u019d\u019f"+
		"\u0003H$\u0000\u019e\u019c\u0001\u0000\u0000\u0000\u019e\u019d\u0001\u0000"+
		"\u0000\u0000\u019fG\u0001\u0000\u0000\u0000\u01a0\u01a5\u0003J%\u0000"+
		"\u01a1\u01a2\u0005U\u0000\u0000\u01a2\u01a4\u0003J%\u0000\u01a3\u01a1"+
		"\u0001\u0000\u0000\u0000\u01a4\u01a7\u0001\u0000\u0000\u0000\u01a5\u01a3"+
		"\u0001\u0000\u0000\u0000\u01a5\u01a6\u0001\u0000\u0000\u0000\u01a6I\u0001"+
		"\u0000\u0000\u0000\u01a7\u01a5\u0001\u0000\u0000\u0000\u01a8\u01a9\u0007"+
		"\u000b\u0000\u0000\u01a9K\u0001\u0000\u0000\u0000\u01aa\u01ab\u0005\n"+
		"\u0000\u0000\u01ab\u01ac\u0005]\u0000\u0000\u01ac\u01ad\u0005]\u0000\u0000"+
		"\u01ad\u01b1\u0005K\u0000\u0000\u01ae\u01b0\u0003N\'\u0000\u01af\u01ae"+
		"\u0001\u0000\u0000\u0000\u01b0\u01b3\u0001\u0000\u0000\u0000\u01b1\u01af"+
		"\u0001\u0000\u0000\u0000\u01b1\u01b2\u0001\u0000\u0000\u0000\u01b2\u01b4"+
		"\u0001\u0000\u0000\u0000\u01b3\u01b1\u0001\u0000\u0000\u0000\u01b4\u01b5"+
		"\u0005J\u0000\u0000\u01b5M\u0001\u0000\u0000\u0000\u01b6\u01b7\u0003P"+
		"(\u0000\u01b7\u01b8\u0005L\u0000\u0000\u01b8\u01b9\u0003R)\u0000\u01b9"+
		"\u01c6\u0001\u0000\u0000\u0000\u01ba\u01bb\u0003P(\u0000\u01bb\u01bc\u0005"+
		"\f\u0000\u0000\u01bc\u01c6\u0001\u0000\u0000\u0000\u01bd\u01be\u0003T"+
		"*\u0000\u01be\u01bf\u0005L\u0000\u0000\u01bf\u01c0\u0003R)\u0000\u01c0"+
		"\u01c6\u0001\u0000\u0000\u0000\u01c1\u01c2\u0003T*\u0000\u01c2\u01c3\u0005"+
		"\f\u0000\u0000\u01c3\u01c6\u0001\u0000\u0000\u0000\u01c4\u01c6\u0005\u000b"+
		"\u0000\u0000\u01c5\u01b6\u0001\u0000\u0000\u0000\u01c5\u01ba\u0001\u0000"+
		"\u0000\u0000\u01c5\u01bd\u0001\u0000\u0000\u0000\u01c5\u01c1\u0001\u0000"+
		"\u0000\u0000\u01c5\u01c4\u0001\u0000\u0000\u0000\u01c6O\u0001\u0000\u0000"+
		"\u0000\u01c7\u01ce\u0003@ \u0000\u01c8\u01ce\u0005/\u0000\u0000\u01c9"+
		"\u01ce\u0005.\u0000\u0000\u01ca\u01ce\u0005_\u0000\u0000\u01cb\u01ce\u0003"+
		">\u001f\u0000\u01cc\u01ce\u0005^\u0000\u0000\u01cd\u01c7\u0001\u0000\u0000"+
		"\u0000\u01cd\u01c8\u0001\u0000\u0000\u0000\u01cd\u01c9\u0001\u0000\u0000"+
		"\u0000\u01cd\u01ca\u0001\u0000\u0000\u0000\u01cd\u01cb\u0001\u0000\u0000"+
		"\u0000\u01cd\u01cc\u0001\u0000\u0000\u0000\u01ceQ\u0001\u0000\u0000\u0000"+
		"\u01cf\u01d0\u0007\f\u0000\u0000\u01d0S\u0001\u0000\u0000\u0000\u01d1"+
		"\u01d3\u0003V+\u0000\u01d2\u01d1\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001"+
		"\u0000\u0000\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001"+
		"\u0000\u0000\u0000\u01d5U\u0001\u0000\u0000\u0000\u01d6\u01d7\u00052\u0000"+
		"\u0000\u01d7\u01d8\u0005R\u0000\u0000\u01d8\u01e7\u0003@ \u0000\u01d9"+
		"\u01da\u0005%\u0000\u0000\u01da\u01db\u0005R\u0000\u0000\u01db\u01e7\u0003"+
		"X,\u0000\u01dc\u01dd\u0005\'\u0000\u0000\u01dd\u01de\u0005R\u0000\u0000"+
		"\u01de\u01e7\u0003B!\u0000\u01df\u01e0\u0005 \u0000\u0000\u01e0\u01e1"+
		"\u0005R\u0000\u0000\u01e1\u01e7\u0003B!\u0000\u01e2\u01e3\u0005\u001f"+
		"\u0000\u0000\u01e3\u01e4\u0005R\u0000\u0000\u01e4\u01e7\u0003B!\u0000"+
		"\u01e5\u01e7\u0005&\u0000\u0000\u01e6\u01d6\u0001\u0000\u0000\u0000\u01e6"+
		"\u01d9\u0001\u0000\u0000\u0000\u01e6\u01dc\u0001\u0000\u0000\u0000\u01e6"+
		"\u01df\u0001\u0000\u0000\u0000\u01e6\u01e2\u0001\u0000\u0000\u0000\u01e6"+
		"\u01e5\u0001\u0000\u0000\u0000\u01e7W\u0001\u0000\u0000\u0000\u01e8\u01ec"+
		"\u0005Z\u0000\u0000\u01e9\u01ec\u0003>\u001f\u0000\u01ea\u01ec\u0005."+
		"\u0000\u0000\u01eb\u01e8\u0001\u0000\u0000\u0000\u01eb\u01e9\u0001\u0000"+
		"\u0000\u0000\u01eb\u01ea\u0001\u0000\u0000\u0000\u01ecY\u0001\u0000\u0000"+
		"\u0000\u01ed\u01ee\u0005\u001d\u0000\u0000\u01ee\u01ef\u0005^\u0000\u0000"+
		"\u01ef\u01f0\u0005]\u0000\u0000\u01f0\u01f4\u0005Q\u0000\u0000\u01f1\u01f3"+
		"\u0003\\.\u0000\u01f2\u01f1\u0001\u0000\u0000\u0000\u01f3\u01f6\u0001"+
		"\u0000\u0000\u0000\u01f4\u01f2\u0001\u0000\u0000\u0000\u01f4\u01f5\u0001"+
		"\u0000\u0000\u0000\u01f5[\u0001\u0000\u0000\u0000\u01f6\u01f4\u0001\u0000"+
		"\u0000\u0000\u01f7\u0202\u0005X\u0000\u0000\u01f8\u0202\u0005Y\u0000\u0000"+
		"\u01f9\u0202\u0005O\u0000\u0000\u01fa\u0202\u0005P\u0000\u0000\u01fb\u0202"+
		"\u0003\u000e\u0007\u0000\u01fc\u0202\u0003^/\u0000\u01fd\u0202\u0005N"+
		"\u0000\u0000\u01fe\u0202\u0003`0\u0000\u01ff\u0202\u0003b1\u0000\u0200"+
		"\u0202\u0003d2\u0000\u0201\u01f7\u0001\u0000\u0000\u0000\u0201\u01f8\u0001"+
		"\u0000\u0000\u0000\u0201\u01f9\u0001\u0000\u0000\u0000\u0201\u01fa\u0001"+
		"\u0000\u0000\u0000\u0201\u01fb\u0001\u0000\u0000\u0000\u0201\u01fc\u0001"+
		"\u0000\u0000\u0000\u0201\u01fd\u0001\u0000\u0000\u0000\u0201\u01fe\u0001"+
		"\u0000\u0000\u0000\u0201\u01ff\u0001\u0000\u0000\u0000\u0201\u0200\u0001"+
		"\u0000\u0000\u0000\u0202]\u0001\u0000\u0000\u0000\u0203\u0204\u0007\r"+
		"\u0000\u0000\u0204_\u0001\u0000\u0000\u0000\u0205\u0209\u0005G\u0000\u0000"+
		"\u0206\u0208\u0003\\.\u0000\u0207\u0206\u0001\u0000\u0000\u0000\u0208"+
		"\u020b\u0001\u0000\u0000\u0000\u0209\u0207\u0001\u0000\u0000\u0000\u0209"+
		"\u020a\u0001\u0000\u0000\u0000\u020a\u020c\u0001\u0000\u0000\u0000\u020b"+
		"\u0209\u0001\u0000\u0000\u0000\u020c\u020d\u0005H\u0000\u0000\u020da\u0001"+
		"\u0000\u0000\u0000\u020e\u0212\u0005I\u0000\u0000\u020f\u0211\u0003\\"+
		".\u0000\u0210\u020f\u0001\u0000\u0000\u0000\u0211\u0214\u0001\u0000\u0000"+
		"\u0000\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000\u0000"+
		"\u0000\u0213\u0215\u0001\u0000\u0000\u0000\u0214\u0212\u0001\u0000\u0000"+
		"\u0000\u0215\u0216\u0005J\u0000\u0000\u0216c\u0001\u0000\u0000\u0000\u0217"+
		"\u021b\u0005M\u0000\u0000\u0218\u021a\u0003\\.\u0000\u0219\u0218\u0001"+
		"\u0000\u0000\u0000\u021a\u021d\u0001\u0000\u0000\u0000\u021b\u0219\u0001"+
		"\u0000\u0000\u0000\u021b\u021c\u0001\u0000\u0000\u0000\u021c\u021e\u0001"+
		"\u0000\u0000\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021e\u021f\u0005"+
		"W\u0000\u0000\u021fe\u0001\u0000\u0000\u0000\u001ei{\u009f\u00ad\u00b5"+
		"\u00bf\u00d4\u00d8\u00e3\u0128\u0131\u0137\u0143\u0145\u0189\u018c\u0195"+
		"\u019e\u01a5\u01b1\u01c5\u01cd\u01d4\u01e6\u01eb\u01f4\u0201\u0209\u0212"+
		"\u021b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}