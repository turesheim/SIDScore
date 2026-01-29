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
		TITLE=1, AUTHOR=2, RELEASED=3, TEMPO=4, TIME=5, SYSTEM=6, TABLE=7, LOOP=8, 
		HOLD=9, INSTR=10, VOICE=11, SWING=12, SYNC=13, RING=14, FILTER=15, CUTOFF=16, 
		RES=17, NOTEK=18, RESET=19, GATE=20, GATEMIN=21, RETRIGGER=22, LEGATO=23, 
		LP=24, BP=25, HP=26, OFF=27, ON=28, PAL=29, NTSC=30, WAVE=31, ADSR=32, 
		PW=33, WAVESEQ=34, PWMIN=35, PWMAX=36, PWSWEEP=37, PWSEQ=38, FILTERSEQ=39, 
		GATESEQ=40, PITCHSEQ=41, WAVEVAL=42, LEG_START=43, LEG_END=44, TUPLET_START=45, 
		RBRACE=46, LBRACE=47, AT=48, LPAREN=49, AMP=50, GT=51, LT=52, COLON=53, 
		EQ=54, COMMA=55, SLASH=56, PLUS=57, MINUS=58, REPEAT_END=59, OCTAVE=60, 
		LENGTH=61, NOTE=62, REST=63, HIT=64, ID=65, INT=66, HEX=67, STRING=68, 
		COMMENT=69, WS=70;
	public static final int
		RULE_file = 0, RULE_stmt = 1, RULE_titleStmt = 2, RULE_authorStmt = 3, 
		RULE_releasedStmt = 4, RULE_tempoStmt = 5, RULE_timeStmt = 6, RULE_swingStmt = 7, 
		RULE_systemStmt = 8, RULE_instrStmt = 9, RULE_instrParam = 10, RULE_signedInt = 11, 
		RULE_waveList = 12, RULE_onOff = 13, RULE_gateMode = 14, RULE_filterSpec = 15, 
		RULE_filterList = 16, RULE_filterMode = 17, RULE_tableStmt = 18, RULE_tableStep = 19, 
		RULE_tableValue = 20, RULE_tableDuration = 21, RULE_tableCtrl = 22, RULE_tableCtrlItem = 23, 
		RULE_noteSpec = 24, RULE_voiceBlock = 25, RULE_voiceItem = 26, RULE_noteOrRestOrHit = 27, 
		RULE_legatoScope = 28, RULE_tuplet = 29, RULE_repeat = 30;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "stmt", "titleStmt", "authorStmt", "releasedStmt", "tempoStmt", 
			"timeStmt", "swingStmt", "systemStmt", "instrStmt", "instrParam", "signedInt", 
			"waveList", "onOff", "gateMode", "filterSpec", "filterList", "filterMode", 
			"tableStmt", "tableStep", "tableValue", "tableDuration", "tableCtrl", 
			"tableCtrlItem", "noteSpec", "voiceBlock", "voiceItem", "noteOrRestOrHit", 
			"legatoScope", "tuplet", "repeat"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'TITLE'", "'AUTHOR'", "'RELEASED'", "'TEMPO'", "'TIME'", "'SYSTEM'", 
			"'TABLE'", "'LOOP'", "'HOLD'", "'INSTR'", "'VOICE'", "'SWING'", "'SYNC'", 
			"'RING'", "'FILTER'", "'CUTOFF'", "'RES'", "'NOTE'", "'RESET'", "'GATE'", 
			"'GATEMIN'", "'RETRIGGER'", "'LEGATO'", "'LP'", "'BP'", "'HP'", "'OFF'", 
			"'ON'", "'PAL'", "'NTSC'", "'WAVE'", "'ADSR'", "'PW'", "'WAVESEQ'", "'PWMIN'", 
			"'PWMAX'", "'PWSWEEP'", "'PWSEQ'", "'FILTERSEQ'", "'GATESEQ'", "'PITCHSEQ'", 
			null, "'(leg)'", "'(end)'", "'T{'", "'}'", "'{'", "'@'", "'('", "'&'", 
			"'>'", "'<'", "':'", "'='", "','", "'/'", "'+'", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TITLE", "AUTHOR", "RELEASED", "TEMPO", "TIME", "SYSTEM", "TABLE", 
			"LOOP", "HOLD", "INSTR", "VOICE", "SWING", "SYNC", "RING", "FILTER", 
			"CUTOFF", "RES", "NOTEK", "RESET", "GATE", "GATEMIN", "RETRIGGER", "LEGATO", 
			"LP", "BP", "HP", "OFF", "ON", "PAL", "NTSC", "WAVE", "ADSR", "PW", "WAVESEQ", 
			"PWMIN", "PWMAX", "PWSWEEP", "PWSEQ", "FILTERSEQ", "GATESEQ", "PITCHSEQ", 
			"WAVEVAL", "LEG_START", "LEG_END", "TUPLET_START", "RBRACE", "LBRACE", 
			"AT", "LPAREN", "AMP", "GT", "LT", "COLON", "EQ", "COMMA", "SLASH", "PLUS", 
			"MINUS", "REPEAT_END", "OCTAVE", "LENGTH", "NOTE", "REST", "HIT", "ID", 
			"INT", "HEX", "STRING", "COMMENT", "WS"
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
			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7422L) != 0)) {
				{
				{
				setState(62);
				stmt();
				}
				}
				setState(67);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(68);
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
			setState(80);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TITLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(70);
				titleStmt();
				}
				break;
			case AUTHOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(71);
				authorStmt();
				}
				break;
			case RELEASED:
				enterOuterAlt(_localctx, 3);
				{
				setState(72);
				releasedStmt();
				}
				break;
			case TEMPO:
				enterOuterAlt(_localctx, 4);
				{
				setState(73);
				tempoStmt();
				}
				break;
			case TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(74);
				timeStmt();
				}
				break;
			case SYSTEM:
				enterOuterAlt(_localctx, 6);
				{
				setState(75);
				systemStmt();
				}
				break;
			case TABLE:
				enterOuterAlt(_localctx, 7);
				{
				setState(76);
				tableStmt();
				}
				break;
			case INSTR:
				enterOuterAlt(_localctx, 8);
				{
				setState(77);
				instrStmt();
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 9);
				{
				setState(78);
				swingStmt();
				}
				break;
			case VOICE:
				enterOuterAlt(_localctx, 10);
				{
				setState(79);
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
			setState(82);
			match(TITLE);
			setState(83);
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
			setState(85);
			match(AUTHOR);
			setState(86);
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
			setState(88);
			match(RELEASED);
			setState(89);
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
			setState(91);
			match(TEMPO);
			setState(92);
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
			setState(94);
			match(TIME);
			setState(95);
			match(INT);
			setState(96);
			match(SLASH);
			setState(97);
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
			setState(99);
			match(SWING);
			setState(100);
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
			setState(102);
			match(SYSTEM);
			setState(103);
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
		enterRule(_localctx, 18, RULE_instrStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(INSTR);
			setState(106);
			match(ID);
			setState(113);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				{
				setState(107);
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
			case WAVESEQ:
			case PWMIN:
			case PWMAX:
			case PWSWEEP:
			case PWSEQ:
			case FILTERSEQ:
			case GATESEQ:
			case PITCHSEQ:
				{
				setState(109); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(108);
					instrParam();
					}
					}
					setState(111); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 4395902427136L) != 0) );
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
		enterRule(_localctx, 20, RULE_instrParam);
		int _la;
		try {
			setState(175);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				match(WAVE);
				setState(116);
				match(EQ);
				setState(117);
				waveList();
				}
				break;
			case ADSR:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				match(ADSR);
				setState(119);
				match(EQ);
				setState(120);
				match(INT);
				setState(121);
				match(COMMA);
				setState(122);
				match(INT);
				setState(123);
				match(COMMA);
				setState(124);
				match(INT);
				setState(125);
				match(COMMA);
				setState(126);
				match(INT);
				}
				break;
			case PW:
				enterOuterAlt(_localctx, 3);
				{
				setState(127);
				match(PW);
				setState(128);
				match(EQ);
				setState(129);
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
				enterOuterAlt(_localctx, 4);
				{
				setState(130);
				match(FILTER);
				setState(131);
				match(EQ);
				setState(132);
				filterSpec();
				}
				break;
			case CUTOFF:
				enterOuterAlt(_localctx, 5);
				{
				setState(133);
				match(CUTOFF);
				setState(134);
				match(EQ);
				setState(135);
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
				enterOuterAlt(_localctx, 6);
				{
				setState(136);
				match(RES);
				setState(137);
				match(EQ);
				setState(138);
				match(INT);
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 7);
				{
				setState(139);
				match(GATE);
				setState(140);
				match(EQ);
				setState(141);
				gateMode();
				}
				break;
			case GATEMIN:
				enterOuterAlt(_localctx, 8);
				{
				setState(142);
				match(GATEMIN);
				setState(143);
				match(EQ);
				setState(144);
				match(INT);
				}
				break;
			case WAVESEQ:
				enterOuterAlt(_localctx, 9);
				{
				setState(145);
				match(WAVESEQ);
				setState(146);
				match(EQ);
				setState(147);
				match(ID);
				}
				break;
			case PWMIN:
				enterOuterAlt(_localctx, 10);
				{
				setState(148);
				match(PWMIN);
				setState(149);
				match(EQ);
				setState(150);
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
				enterOuterAlt(_localctx, 11);
				{
				setState(151);
				match(PWMAX);
				setState(152);
				match(EQ);
				setState(153);
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
				enterOuterAlt(_localctx, 12);
				{
				setState(154);
				match(PWSWEEP);
				setState(155);
				match(EQ);
				setState(156);
				signedInt();
				}
				break;
			case PWSEQ:
				enterOuterAlt(_localctx, 13);
				{
				setState(157);
				match(PWSEQ);
				setState(158);
				match(EQ);
				setState(159);
				match(ID);
				}
				break;
			case FILTERSEQ:
				enterOuterAlt(_localctx, 14);
				{
				setState(160);
				match(FILTERSEQ);
				setState(161);
				match(EQ);
				setState(162);
				match(ID);
				}
				break;
			case GATESEQ:
				enterOuterAlt(_localctx, 15);
				{
				setState(163);
				match(GATESEQ);
				setState(164);
				match(EQ);
				setState(165);
				match(ID);
				}
				break;
			case PITCHSEQ:
				enterOuterAlt(_localctx, 16);
				{
				setState(166);
				match(PITCHSEQ);
				setState(167);
				match(EQ);
				setState(168);
				match(ID);
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 17);
				{
				setState(169);
				match(SYNC);
				setState(170);
				match(EQ);
				setState(171);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 18);
				{
				setState(172);
				match(RING);
				setState(173);
				match(EQ);
				setState(174);
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
		enterRule(_localctx, 22, RULE_signedInt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(177);
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

			setState(180);
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
		enterRule(_localctx, 24, RULE_waveList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(WAVEVAL);
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(183);
				match(PLUS);
				setState(184);
				match(WAVEVAL);
				}
				}
				setState(189);
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
		enterRule(_localctx, 26, RULE_onOff);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
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
		enterRule(_localctx, 28, RULE_gateMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
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
		enterRule(_localctx, 30, RULE_filterSpec);
		try {
			setState(196);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OFF:
				enterOuterAlt(_localctx, 1);
				{
				setState(194);
				match(OFF);
				}
				break;
			case LP:
			case BP:
			case HP:
				enterOuterAlt(_localctx, 2);
				{
				setState(195);
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
		enterRule(_localctx, 32, RULE_filterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			filterMode();
			setState(203);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(199);
				match(PLUS);
				setState(200);
				filterMode();
				}
				}
				setState(205);
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
		enterRule(_localctx, 34, RULE_filterMode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 117440512L) != 0)) ) {
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
		enterRule(_localctx, 36, RULE_tableStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(TABLE);
			setState(209);
			match(ID);
			setState(210);
			match(ID);
			setState(211);
			match(LBRACE);
			setState(215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 8)) & ~0x3f) == 0 && ((1L << (_la - 8)) & 866379995505237089L) != 0)) {
				{
				{
				setState(212);
				tableStep();
				}
				}
				setState(217);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(218);
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
		enterRule(_localctx, 38, RULE_tableStep);
		try {
			setState(235);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(220);
				tableValue();
				setState(221);
				match(AT);
				setState(222);
				tableDuration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				tableValue();
				setState(225);
				match(HOLD);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(227);
				tableCtrl();
				setState(228);
				match(AT);
				setState(229);
				tableDuration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(231);
				tableCtrl();
				setState(232);
				match(HOLD);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(234);
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
		enterRule(_localctx, 40, RULE_tableValue);
		try {
			setState(243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				waveList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(238);
				match(ON);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(239);
				match(OFF);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(240);
				match(HEX);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(241);
				signedInt();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(242);
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
		enterRule(_localctx, 42, RULE_tableDuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
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
		enterRule(_localctx, 44, RULE_tableCtrl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(247);
				tableCtrlItem();
				}
				}
				setState(250); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 2149343232L) != 0) );
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
		enterRule(_localctx, 46, RULE_tableCtrlItem);
		try {
			setState(268);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(252);
				match(WAVE);
				setState(253);
				match(EQ);
				setState(254);
				waveList();
				}
				break;
			case NOTEK:
				enterOuterAlt(_localctx, 2);
				{
				setState(255);
				match(NOTEK);
				setState(256);
				match(EQ);
				setState(257);
				noteSpec();
				}
				break;
			case GATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(258);
				match(GATE);
				setState(259);
				match(EQ);
				setState(260);
				onOff();
				}
				break;
			case RING:
				enterOuterAlt(_localctx, 4);
				{
				setState(261);
				match(RING);
				setState(262);
				match(EQ);
				setState(263);
				onOff();
				}
				break;
			case SYNC:
				enterOuterAlt(_localctx, 5);
				{
				setState(264);
				match(SYNC);
				setState(265);
				match(EQ);
				setState(266);
				onOff();
				}
				break;
			case RESET:
				enterOuterAlt(_localctx, 6);
				{
				setState(267);
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
		enterRule(_localctx, 48, RULE_noteSpec);
		try {
			setState(273);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(270);
				match(NOTE);
				}
				break;
			case PLUS:
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(271);
				signedInt();
				}
				break;
			case OFF:
				enterOuterAlt(_localctx, 3);
				{
				setState(272);
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
		enterRule(_localctx, 50, RULE_voiceBlock);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			match(VOICE);
			setState(276);
			match(INT);
			setState(277);
			match(ID);
			setState(278);
			match(COLON);
			setState(282);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(279);
					voiceItem();
					}
					} 
				}
				setState(284);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		enterRule(_localctx, 52, RULE_voiceItem);
		try {
			setState(295);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OCTAVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(285);
				match(OCTAVE);
				}
				break;
			case LENGTH:
				enterOuterAlt(_localctx, 2);
				{
				setState(286);
				match(LENGTH);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 3);
				{
				setState(287);
				match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 4);
				{
				setState(288);
				match(LT);
				}
				break;
			case SWING:
				enterOuterAlt(_localctx, 5);
				{
				setState(289);
				swingStmt();
				}
				break;
			case NOTE:
			case REST:
			case HIT:
				enterOuterAlt(_localctx, 6);
				{
				setState(290);
				noteOrRestOrHit();
				}
				break;
			case AMP:
				enterOuterAlt(_localctx, 7);
				{
				setState(291);
				match(AMP);
				}
				break;
			case LEG_START:
				enterOuterAlt(_localctx, 8);
				{
				setState(292);
				legatoScope();
				}
				break;
			case TUPLET_START:
				enterOuterAlt(_localctx, 9);
				{
				setState(293);
				tuplet();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 10);
				{
				setState(294);
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
		enterRule(_localctx, 54, RULE_noteOrRestOrHit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			_la = _input.LA(1);
			if ( !(((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & 7L) != 0)) ) {
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
		enterRule(_localctx, 56, RULE_legatoScope);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(LEG_START);
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 12)) & ~0x3f) == 0 && ((1L << (_la - 12)) & 8727796599750657L) != 0)) {
				{
				{
				setState(300);
				voiceItem();
				}
				}
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(306);
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
		enterRule(_localctx, 58, RULE_tuplet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			match(TUPLET_START);
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 12)) & ~0x3f) == 0 && ((1L << (_la - 12)) & 8727796599750657L) != 0)) {
				{
				{
				setState(309);
				voiceItem();
				}
				}
				setState(314);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(315);
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
		enterRule(_localctx, 60, RULE_repeat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			match(LPAREN);
			setState(321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 12)) & ~0x3f) == 0 && ((1L << (_la - 12)) & 8727796599750657L) != 0)) {
				{
				{
				setState(318);
				voiceItem();
				}
				}
				setState(323);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(324);
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
		"\u0004\u0001F\u0147\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0001\u0000\u0005\u0000@\b\u0000\n\u0000\f\u0000C\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"Q\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0004\tn\b\t\u000b\t\f\to\u0003\tr\b\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00b0\b\n\u0001\u000b\u0003"+
		"\u000b\u00b3\b\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u00ba\b\f\n\f\f\f\u00bd\t\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0003\u000f\u00c5\b\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0005\u0010\u00ca\b\u0010\n\u0010\f\u0010\u00cd\t\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0005\u0012\u00d6\b\u0012\n\u0012\f\u0012\u00d9\t\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00ec\b\u0013"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0003\u0014\u00f4\b\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0004\u0016"+
		"\u00f9\b\u0016\u000b\u0016\f\u0016\u00fa\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0003\u0017\u010d\b\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u0112\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0005\u0019\u0119\b\u0019\n\u0019\f\u0019\u011c\t\u0019"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0128\b\u001a"+
		"\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0005\u001c\u012e\b\u001c"+
		"\n\u001c\f\u001c\u0131\t\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001"+
		"\u001d\u0005\u001d\u0137\b\u001d\n\u001d\f\u001d\u013a\t\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001e\u0001\u001e\u0005\u001e\u0140\b\u001e\n\u001e"+
		"\f\u001e\u0143\t\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0000\u0000"+
		"\u001f\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.02468:<\u0000\t\u0002\u0000\u001b\u001bBB\u0001"+
		"\u0000\u001d\u001e\u0001\u0000BC\u0001\u00009:\u0001\u0000\u001b\u001c"+
		"\u0001\u0000\u0016\u0017\u0001\u0000\u0018\u001a\u0002\u0000\t\tBB\u0001"+
		"\u0000>@\u0167\u0000A\u0001\u0000\u0000\u0000\u0002P\u0001\u0000\u0000"+
		"\u0000\u0004R\u0001\u0000\u0000\u0000\u0006U\u0001\u0000\u0000\u0000\b"+
		"X\u0001\u0000\u0000\u0000\n[\u0001\u0000\u0000\u0000\f^\u0001\u0000\u0000"+
		"\u0000\u000ec\u0001\u0000\u0000\u0000\u0010f\u0001\u0000\u0000\u0000\u0012"+
		"i\u0001\u0000\u0000\u0000\u0014\u00af\u0001\u0000\u0000\u0000\u0016\u00b2"+
		"\u0001\u0000\u0000\u0000\u0018\u00b6\u0001\u0000\u0000\u0000\u001a\u00be"+
		"\u0001\u0000\u0000\u0000\u001c\u00c0\u0001\u0000\u0000\u0000\u001e\u00c4"+
		"\u0001\u0000\u0000\u0000 \u00c6\u0001\u0000\u0000\u0000\"\u00ce\u0001"+
		"\u0000\u0000\u0000$\u00d0\u0001\u0000\u0000\u0000&\u00eb\u0001\u0000\u0000"+
		"\u0000(\u00f3\u0001\u0000\u0000\u0000*\u00f5\u0001\u0000\u0000\u0000,"+
		"\u00f8\u0001\u0000\u0000\u0000.\u010c\u0001\u0000\u0000\u00000\u0111\u0001"+
		"\u0000\u0000\u00002\u0113\u0001\u0000\u0000\u00004\u0127\u0001\u0000\u0000"+
		"\u00006\u0129\u0001\u0000\u0000\u00008\u012b\u0001\u0000\u0000\u0000:"+
		"\u0134\u0001\u0000\u0000\u0000<\u013d\u0001\u0000\u0000\u0000>@\u0003"+
		"\u0002\u0001\u0000?>\u0001\u0000\u0000\u0000@C\u0001\u0000\u0000\u0000"+
		"A?\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000BD\u0001\u0000\u0000"+
		"\u0000CA\u0001\u0000\u0000\u0000DE\u0005\u0000\u0000\u0001E\u0001\u0001"+
		"\u0000\u0000\u0000FQ\u0003\u0004\u0002\u0000GQ\u0003\u0006\u0003\u0000"+
		"HQ\u0003\b\u0004\u0000IQ\u0003\n\u0005\u0000JQ\u0003\f\u0006\u0000KQ\u0003"+
		"\u0010\b\u0000LQ\u0003$\u0012\u0000MQ\u0003\u0012\t\u0000NQ\u0003\u000e"+
		"\u0007\u0000OQ\u00032\u0019\u0000PF\u0001\u0000\u0000\u0000PG\u0001\u0000"+
		"\u0000\u0000PH\u0001\u0000\u0000\u0000PI\u0001\u0000\u0000\u0000PJ\u0001"+
		"\u0000\u0000\u0000PK\u0001\u0000\u0000\u0000PL\u0001\u0000\u0000\u0000"+
		"PM\u0001\u0000\u0000\u0000PN\u0001\u0000\u0000\u0000PO\u0001\u0000\u0000"+
		"\u0000Q\u0003\u0001\u0000\u0000\u0000RS\u0005\u0001\u0000\u0000ST\u0005"+
		"D\u0000\u0000T\u0005\u0001\u0000\u0000\u0000UV\u0005\u0002\u0000\u0000"+
		"VW\u0005D\u0000\u0000W\u0007\u0001\u0000\u0000\u0000XY\u0005\u0003\u0000"+
		"\u0000YZ\u0005D\u0000\u0000Z\t\u0001\u0000\u0000\u0000[\\\u0005\u0004"+
		"\u0000\u0000\\]\u0005B\u0000\u0000]\u000b\u0001\u0000\u0000\u0000^_\u0005"+
		"\u0005\u0000\u0000_`\u0005B\u0000\u0000`a\u00058\u0000\u0000ab\u0005B"+
		"\u0000\u0000b\r\u0001\u0000\u0000\u0000cd\u0005\f\u0000\u0000de\u0007"+
		"\u0000\u0000\u0000e\u000f\u0001\u0000\u0000\u0000fg\u0005\u0006\u0000"+
		"\u0000gh\u0007\u0001\u0000\u0000h\u0011\u0001\u0000\u0000\u0000ij\u0005"+
		"\n\u0000\u0000jq\u0005A\u0000\u0000kr\u0005D\u0000\u0000ln\u0003\u0014"+
		"\n\u0000ml\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000om\u0001\u0000"+
		"\u0000\u0000op\u0001\u0000\u0000\u0000pr\u0001\u0000\u0000\u0000qk\u0001"+
		"\u0000\u0000\u0000qm\u0001\u0000\u0000\u0000r\u0013\u0001\u0000\u0000"+
		"\u0000st\u0005\u001f\u0000\u0000tu\u00056\u0000\u0000u\u00b0\u0003\u0018"+
		"\f\u0000vw\u0005 \u0000\u0000wx\u00056\u0000\u0000xy\u0005B\u0000\u0000"+
		"yz\u00057\u0000\u0000z{\u0005B\u0000\u0000{|\u00057\u0000\u0000|}\u0005"+
		"B\u0000\u0000}~\u00057\u0000\u0000~\u00b0\u0005B\u0000\u0000\u007f\u0080"+
		"\u0005!\u0000\u0000\u0080\u0081\u00056\u0000\u0000\u0081\u00b0\u0007\u0002"+
		"\u0000\u0000\u0082\u0083\u0005\u000f\u0000\u0000\u0083\u0084\u00056\u0000"+
		"\u0000\u0084\u00b0\u0003\u001e\u000f\u0000\u0085\u0086\u0005\u0010\u0000"+
		"\u0000\u0086\u0087\u00056\u0000\u0000\u0087\u00b0\u0007\u0002\u0000\u0000"+
		"\u0088\u0089\u0005\u0011\u0000\u0000\u0089\u008a\u00056\u0000\u0000\u008a"+
		"\u00b0\u0005B\u0000\u0000\u008b\u008c\u0005\u0014\u0000\u0000\u008c\u008d"+
		"\u00056\u0000\u0000\u008d\u00b0\u0003\u001c\u000e\u0000\u008e\u008f\u0005"+
		"\u0015\u0000\u0000\u008f\u0090\u00056\u0000\u0000\u0090\u00b0\u0005B\u0000"+
		"\u0000\u0091\u0092\u0005\"\u0000\u0000\u0092\u0093\u00056\u0000\u0000"+
		"\u0093\u00b0\u0005A\u0000\u0000\u0094\u0095\u0005#\u0000\u0000\u0095\u0096"+
		"\u00056\u0000\u0000\u0096\u00b0\u0007\u0002\u0000\u0000\u0097\u0098\u0005"+
		"$\u0000\u0000\u0098\u0099\u00056\u0000\u0000\u0099\u00b0\u0007\u0002\u0000"+
		"\u0000\u009a\u009b\u0005%\u0000\u0000\u009b\u009c\u00056\u0000\u0000\u009c"+
		"\u00b0\u0003\u0016\u000b\u0000\u009d\u009e\u0005&\u0000\u0000\u009e\u009f"+
		"\u00056\u0000\u0000\u009f\u00b0\u0005A\u0000\u0000\u00a0\u00a1\u0005\'"+
		"\u0000\u0000\u00a1\u00a2\u00056\u0000\u0000\u00a2\u00b0\u0005A\u0000\u0000"+
		"\u00a3\u00a4\u0005(\u0000\u0000\u00a4\u00a5\u00056\u0000\u0000\u00a5\u00b0"+
		"\u0005A\u0000\u0000\u00a6\u00a7\u0005)\u0000\u0000\u00a7\u00a8\u00056"+
		"\u0000\u0000\u00a8\u00b0\u0005A\u0000\u0000\u00a9\u00aa\u0005\r\u0000"+
		"\u0000\u00aa\u00ab\u00056\u0000\u0000\u00ab\u00b0\u0003\u001a\r\u0000"+
		"\u00ac\u00ad\u0005\u000e\u0000\u0000\u00ad\u00ae\u00056\u0000\u0000\u00ae"+
		"\u00b0\u0003\u001a\r\u0000\u00afs\u0001\u0000\u0000\u0000\u00afv\u0001"+
		"\u0000\u0000\u0000\u00af\u007f\u0001\u0000\u0000\u0000\u00af\u0082\u0001"+
		"\u0000\u0000\u0000\u00af\u0085\u0001\u0000\u0000\u0000\u00af\u0088\u0001"+
		"\u0000\u0000\u0000\u00af\u008b\u0001\u0000\u0000\u0000\u00af\u008e\u0001"+
		"\u0000\u0000\u0000\u00af\u0091\u0001\u0000\u0000\u0000\u00af\u0094\u0001"+
		"\u0000\u0000\u0000\u00af\u0097\u0001\u0000\u0000\u0000\u00af\u009a\u0001"+
		"\u0000\u0000\u0000\u00af\u009d\u0001\u0000\u0000\u0000\u00af\u00a0\u0001"+
		"\u0000\u0000\u0000\u00af\u00a3\u0001\u0000\u0000\u0000\u00af\u00a6\u0001"+
		"\u0000\u0000\u0000\u00af\u00a9\u0001\u0000\u0000\u0000\u00af\u00ac\u0001"+
		"\u0000\u0000\u0000\u00b0\u0015\u0001\u0000\u0000\u0000\u00b1\u00b3\u0007"+
		"\u0003\u0000\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001"+
		"\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b5\u0005"+
		"B\u0000\u0000\u00b5\u0017\u0001\u0000\u0000\u0000\u00b6\u00bb\u0005*\u0000"+
		"\u0000\u00b7\u00b8\u00059\u0000\u0000\u00b8\u00ba\u0005*\u0000\u0000\u00b9"+
		"\u00b7\u0001\u0000\u0000\u0000\u00ba\u00bd\u0001\u0000\u0000\u0000\u00bb"+
		"\u00b9\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc"+
		"\u0019\u0001\u0000\u0000\u0000\u00bd\u00bb\u0001\u0000\u0000\u0000\u00be"+
		"\u00bf\u0007\u0004\u0000\u0000\u00bf\u001b\u0001\u0000\u0000\u0000\u00c0"+
		"\u00c1\u0007\u0005\u0000\u0000\u00c1\u001d\u0001\u0000\u0000\u0000\u00c2"+
		"\u00c5\u0005\u001b\u0000\u0000\u00c3\u00c5\u0003 \u0010\u0000\u00c4\u00c2"+
		"\u0001\u0000\u0000\u0000\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c5\u001f"+
		"\u0001\u0000\u0000\u0000\u00c6\u00cb\u0003\"\u0011\u0000\u00c7\u00c8\u0005"+
		"9\u0000\u0000\u00c8\u00ca\u0003\"\u0011\u0000\u00c9\u00c7\u0001\u0000"+
		"\u0000\u0000\u00ca\u00cd\u0001\u0000\u0000\u0000\u00cb\u00c9\u0001\u0000"+
		"\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc!\u0001\u0000\u0000"+
		"\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00ce\u00cf\u0007\u0006\u0000"+
		"\u0000\u00cf#\u0001\u0000\u0000\u0000\u00d0\u00d1\u0005\u0007\u0000\u0000"+
		"\u00d1\u00d2\u0005A\u0000\u0000\u00d2\u00d3\u0005A\u0000\u0000\u00d3\u00d7"+
		"\u0005/\u0000\u0000\u00d4\u00d6\u0003&\u0013\u0000\u00d5\u00d4\u0001\u0000"+
		"\u0000\u0000\u00d6\u00d9\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001\u0000"+
		"\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8\u00da\u0001\u0000"+
		"\u0000\u0000\u00d9\u00d7\u0001\u0000\u0000\u0000\u00da\u00db\u0005.\u0000"+
		"\u0000\u00db%\u0001\u0000\u0000\u0000\u00dc\u00dd\u0003(\u0014\u0000\u00dd"+
		"\u00de\u00050\u0000\u0000\u00de\u00df\u0003*\u0015\u0000\u00df\u00ec\u0001"+
		"\u0000\u0000\u0000\u00e0\u00e1\u0003(\u0014\u0000\u00e1\u00e2\u0005\t"+
		"\u0000\u0000\u00e2\u00ec\u0001\u0000\u0000\u0000\u00e3\u00e4\u0003,\u0016"+
		"\u0000\u00e4\u00e5\u00050\u0000\u0000\u00e5\u00e6\u0003*\u0015\u0000\u00e6"+
		"\u00ec\u0001\u0000\u0000\u0000\u00e7\u00e8\u0003,\u0016\u0000\u00e8\u00e9"+
		"\u0005\t\u0000\u0000\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea\u00ec\u0005"+
		"\b\u0000\u0000\u00eb\u00dc\u0001\u0000\u0000\u0000\u00eb\u00e0\u0001\u0000"+
		"\u0000\u0000\u00eb\u00e3\u0001\u0000\u0000\u0000\u00eb\u00e7\u0001\u0000"+
		"\u0000\u0000\u00eb\u00ea\u0001\u0000\u0000\u0000\u00ec\'\u0001\u0000\u0000"+
		"\u0000\u00ed\u00f4\u0003\u0018\f\u0000\u00ee\u00f4\u0005\u001c\u0000\u0000"+
		"\u00ef\u00f4\u0005\u001b\u0000\u0000\u00f0\u00f4\u0005C\u0000\u0000\u00f1"+
		"\u00f4\u0003\u0016\u000b\u0000\u00f2\u00f4\u0005B\u0000\u0000\u00f3\u00ed"+
		"\u0001\u0000\u0000\u0000\u00f3\u00ee\u0001\u0000\u0000\u0000\u00f3\u00ef"+
		"\u0001\u0000\u0000\u0000\u00f3\u00f0\u0001\u0000\u0000\u0000\u00f3\u00f1"+
		"\u0001\u0000\u0000\u0000\u00f3\u00f2\u0001\u0000\u0000\u0000\u00f4)\u0001"+
		"\u0000\u0000\u0000\u00f5\u00f6\u0007\u0007\u0000\u0000\u00f6+\u0001\u0000"+
		"\u0000\u0000\u00f7\u00f9\u0003.\u0017\u0000\u00f8\u00f7\u0001\u0000\u0000"+
		"\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u00f8\u0001\u0000\u0000"+
		"\u0000\u00fa\u00fb\u0001\u0000\u0000\u0000\u00fb-\u0001\u0000\u0000\u0000"+
		"\u00fc\u00fd\u0005\u001f\u0000\u0000\u00fd\u00fe\u00056\u0000\u0000\u00fe"+
		"\u010d\u0003\u0018\f\u0000\u00ff\u0100\u0005\u0012\u0000\u0000\u0100\u0101"+
		"\u00056\u0000\u0000\u0101\u010d\u00030\u0018\u0000\u0102\u0103\u0005\u0014"+
		"\u0000\u0000\u0103\u0104\u00056\u0000\u0000\u0104\u010d\u0003\u001a\r"+
		"\u0000\u0105\u0106\u0005\u000e\u0000\u0000\u0106\u0107\u00056\u0000\u0000"+
		"\u0107\u010d\u0003\u001a\r\u0000\u0108\u0109\u0005\r\u0000\u0000\u0109"+
		"\u010a\u00056\u0000\u0000\u010a\u010d\u0003\u001a\r\u0000\u010b\u010d"+
		"\u0005\u0013\u0000\u0000\u010c\u00fc\u0001\u0000\u0000\u0000\u010c\u00ff"+
		"\u0001\u0000\u0000\u0000\u010c\u0102\u0001\u0000\u0000\u0000\u010c\u0105"+
		"\u0001\u0000\u0000\u0000\u010c\u0108\u0001\u0000\u0000\u0000\u010c\u010b"+
		"\u0001\u0000\u0000\u0000\u010d/\u0001\u0000\u0000\u0000\u010e\u0112\u0005"+
		">\u0000\u0000\u010f\u0112\u0003\u0016\u000b\u0000\u0110\u0112\u0005\u001b"+
		"\u0000\u0000\u0111\u010e\u0001\u0000\u0000\u0000\u0111\u010f\u0001\u0000"+
		"\u0000\u0000\u0111\u0110\u0001\u0000\u0000\u0000\u01121\u0001\u0000\u0000"+
		"\u0000\u0113\u0114\u0005\u000b\u0000\u0000\u0114\u0115\u0005B\u0000\u0000"+
		"\u0115\u0116\u0005A\u0000\u0000\u0116\u011a\u00055\u0000\u0000\u0117\u0119"+
		"\u00034\u001a\u0000\u0118\u0117\u0001\u0000\u0000\u0000\u0119\u011c\u0001"+
		"\u0000\u0000\u0000\u011a\u0118\u0001\u0000\u0000\u0000\u011a\u011b\u0001"+
		"\u0000\u0000\u0000\u011b3\u0001\u0000\u0000\u0000\u011c\u011a\u0001\u0000"+
		"\u0000\u0000\u011d\u0128\u0005<\u0000\u0000\u011e\u0128\u0005=\u0000\u0000"+
		"\u011f\u0128\u00053\u0000\u0000\u0120\u0128\u00054\u0000\u0000\u0121\u0128"+
		"\u0003\u000e\u0007\u0000\u0122\u0128\u00036\u001b\u0000\u0123\u0128\u0005"+
		"2\u0000\u0000\u0124\u0128\u00038\u001c\u0000\u0125\u0128\u0003:\u001d"+
		"\u0000\u0126\u0128\u0003<\u001e\u0000\u0127\u011d\u0001\u0000\u0000\u0000"+
		"\u0127\u011e\u0001\u0000\u0000\u0000\u0127\u011f\u0001\u0000\u0000\u0000"+
		"\u0127\u0120\u0001\u0000\u0000\u0000\u0127\u0121\u0001\u0000\u0000\u0000"+
		"\u0127\u0122\u0001\u0000\u0000\u0000\u0127\u0123\u0001\u0000\u0000\u0000"+
		"\u0127\u0124\u0001\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000\u0000"+
		"\u0127\u0126\u0001\u0000\u0000\u0000\u01285\u0001\u0000\u0000\u0000\u0129"+
		"\u012a\u0007\b\u0000\u0000\u012a7\u0001\u0000\u0000\u0000\u012b\u012f"+
		"\u0005+\u0000\u0000\u012c\u012e\u00034\u001a\u0000\u012d\u012c\u0001\u0000"+
		"\u0000\u0000\u012e\u0131\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000"+
		"\u0000\u0000\u012f\u0130\u0001\u0000\u0000\u0000\u0130\u0132\u0001\u0000"+
		"\u0000\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0132\u0133\u0005,\u0000"+
		"\u0000\u01339\u0001\u0000\u0000\u0000\u0134\u0138\u0005-\u0000\u0000\u0135"+
		"\u0137\u00034\u001a\u0000\u0136\u0135\u0001\u0000\u0000\u0000\u0137\u013a"+
		"\u0001\u0000\u0000\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0138\u0139"+
		"\u0001\u0000\u0000\u0000\u0139\u013b\u0001\u0000\u0000\u0000\u013a\u0138"+
		"\u0001\u0000\u0000\u0000\u013b\u013c\u0005.\u0000\u0000\u013c;\u0001\u0000"+
		"\u0000\u0000\u013d\u0141\u00051\u0000\u0000\u013e\u0140\u00034\u001a\u0000"+
		"\u013f\u013e\u0001\u0000\u0000\u0000\u0140\u0143\u0001\u0000\u0000\u0000"+
		"\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000"+
		"\u0142\u0144\u0001\u0000\u0000\u0000\u0143\u0141\u0001\u0000\u0000\u0000"+
		"\u0144\u0145\u0005;\u0000\u0000\u0145=\u0001\u0000\u0000\u0000\u0014A"+
		"Poq\u00af\u00b2\u00bb\u00c4\u00cb\u00d7\u00eb\u00f3\u00fa\u010c\u0111"+
		"\u011a\u0127\u012f\u0138\u0141";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}