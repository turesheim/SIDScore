parser grammar SIDScoreParser;

options { tokenVocab=SIDScoreLexer; }

// --------------------
// Entry point
// --------------------
file
  : stmt* EOF
  ;

// --------------------
// Statements
// --------------------
stmt
  : titleStmt
  | authorStmt
  | releasedStmt
  | tempoStmt
  | timeStmt
  | systemStmt
  | tableStmt
  | instrStmt
  | swingStmt
  | voiceBlock
  ;

titleStmt
  : TITLE STRING
  ;

authorStmt
  : AUTHOR STRING
  ;

releasedStmt
  : RELEASED STRING
  ;

tempoStmt
  : TEMPO INT
  ;

timeStmt
  : TIME INT SLASH INT
  ;

swingStmt
  : SWING (INT | OFF)
  ;

systemStmt
  : SYSTEM (PAL | NTSC)
  ;

// --------------------
// Instruments
// --------------------
instrStmt
  : INSTR ID (STRING | instrParam+)
  ;

instrParam
  : WAVE EQ waveList
  | ADSR EQ INT COMMA INT COMMA INT COMMA INT
  | PW   EQ (HEX | INT)
  | FILTER EQ filterSpec
  | CUTOFF EQ (HEX | INT)
  | RES EQ INT
  | GATE EQ gateMode
  | GATEMIN EQ INT
  | WAVESEQ EQ ID
  | PWMIN EQ (HEX | INT)
  | PWMAX EQ (HEX | INT)
  | PWSWEEP EQ signedInt
  | PWSEQ EQ ID
  | FILTERSEQ EQ ID
  | GATESEQ EQ ID
  | PITCHSEQ EQ ID
  | SYNC EQ onOff
  | RING EQ onOff
  ;

signedInt
  : (PLUS | MINUS)? INT
  ;

waveList
  : WAVEVAL (PLUS WAVEVAL)*
  ;

onOff
  : ON
  | OFF
  ;

gateMode
  : RETRIGGER
  | LEGATO
  ;

filterSpec
  : OFF
  | filterList
  ;

filterList
  : filterMode (PLUS filterMode)*
  ;

filterMode
  : LP
  | BP
  | HP
  ;


// --------------------
// Tables (wave/pw/gate/pitch/filter)
// --------------------
tableStmt
  : TABLE ID ID LBRACE tableStep* RBRACE
  ;

tableStep
  : tableValue AT tableDuration
  | tableValue HOLD
  | tableCtrl AT tableDuration
  | tableCtrl HOLD
  | LOOP
  ;

tableValue
  : waveList
  | ON
  | OFF
  | HEX
  | signedInt
  | INT
  ;

tableDuration
  : INT
  | HOLD
  ;

tableCtrl
  : tableCtrlItem+
  ;

tableCtrlItem
  : WAVE EQ waveList
  | NOTEK EQ noteSpec
  | GATE EQ onOff
  | RING EQ onOff
  | SYNC EQ onOff
  | RESET
  ;

noteSpec
  : NOTE
  | signedInt
  | OFF
  ;

// --------------------
// Voices
// --------------------
voiceBlock
  : VOICE INT ID COLON voiceItem*
  ;

// Voice items are linear tokens plus scoped constructs.
voiceItem
  : OCTAVE
  | LENGTH
  | GT
  | LT
  | swingStmt
  | noteOrRestOrHit
  | AMP
  | legatoScope
  | tuplet
  | repeat
  ;

noteOrRestOrHit
  : NOTE
  | REST
  | HIT
  ;

// --------------------
// Scopes
// --------------------
legatoScope
  : LEG_START voiceItem* LEG_END
  ;

tuplet
  : TUPLET_START voiceItem* RBRACE
  ;

// Repeat: "( ... )xN" where ")xN" is token REPEAT_END
repeat
  : LPAREN voiceItem* REPEAT_END
  ;
  
