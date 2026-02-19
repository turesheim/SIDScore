lexer grammar SIDScoreLexer;

// --------------------
// Keywords
// --------------------
TITLE : 'TITLE';
AUTHOR : 'AUTHOR';
RELEASED : 'RELEASED';
TEMPO : 'TEMPO';
TIME  : 'TIME';
SYSTEM : 'SYSTEM';
TABLE : 'TABLE';
LOOP  : 'LOOP';
HOLD  : 'HOLD';
INSTR : 'INSTR';
VOICE : 'VOICE';
SWING : 'SWING';
SYNC  : 'SYNC';
RING  : 'RING';
FILTER : 'FILTER';
CUTOFF : 'CUTOFF';
RES : 'RES';
NOTEK : 'NOTE';
RESET : 'RESET';
GATE  : 'GATE';
GATEMIN : 'GATEMIN';
RETRIGGER : 'RETRIGGER';
LEGATO : 'LEGATO';
LP : 'LP';
BP : 'BP';
HP : 'HP';

OFF   : 'OFF';
ON    : 'ON';
PAL   : 'PAL';
NTSC  : 'NTSC';

// Instrument parameter keys
WAVE  : 'WAVE';
ADSR  : 'ADSR';
PW    : 'PW';
WAVESEQ : 'WAVESEQ';
PWMIN : 'PWMIN';
PWMAX : 'PWMAX';
PWSWEEP : 'PWSWEEP';
PWSEQ : 'PWSEQ';
FILTERSEQ : 'FILTERSEQ';
GATESEQ : 'GATESEQ';
PITCHSEQ : 'PITCHSEQ';

// Waveform values
WAVEVAL : 'PULSE' | 'SAW' | 'TRI' | 'NOISE';

// --------------------
// Fixed tokens / punctuation
// --------------------
LEG_START     : '(leg)';
LEG_END       : '(end)';

TUPLET_START  : 'T{';
RBRACE        : '}';
LBRACE        : '{';
AT            : '@';

LPAREN        : '(';
AMP           : '&';
GT            : '>';
LT            : '<';
COLON         : ':';
EQ            : '=';
COMMA         : ',';
SLASH         : '/';
PLUS          : '+';
MINUS         : '-';

// Repeat close token: ")x4" (digits are part of the token text)
REPEAT_END    : ')x' DIGITS;

// --------------------
// Musical tokens
// --------------------
// Octave / default length (MML-like)
OCTAVE : 'O' DIGITS;     // e.g. O4
LENGTH : 'L' DIGITS;     // e.g. L8

// Note/rest/hit tokens include optional length and optional dotted suffix.
// Examples:
//   NOTE: C  C#  Db8  F16.
//   REST: R  R4  R8.
//   HIT : X  X8  X16.
NOTE  : NOTE_LETTER ACCIDENTAL? DIGITS? DOT?;
REST  : 'R' DIGITS? DOT?;
HIT   : 'X' DIGITS? DOT?;

// --------------------
// General tokens
// --------------------
ID     : [A-Za-z_] [A-Za-z0-9_]*;
INT    : DIGITS;                 // for general integers (e.g. TEMPO 120)
HEX    : '$' [0-9A-Fa-f]+;       // for PW=$0800

STRING : '"' (~["\r\n])* '"';

// Comments and whitespace
COMMENT : ';' ~[\r\n]* -> skip;
WS      : [ \t\r\n]+   -> skip;

// --------------------
// Fragments
// --------------------
fragment NOTE_LETTER : [A-G];
fragment ACCIDENTAL : [#b];
fragment DOT        : '.';
fragment DIGITS     : [0-9]+;
