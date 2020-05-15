grammar HqlFilter;

options {
    // antlr will generate java lexer and parser
    language = Java;
}

@header {
package com.wavemaker.runtime.data.filter.parser.antlr4;
}

/*
 * Parser Rules
 */
whereClause : logicalExpression EOF;

logicalExpression : BRAC_OPEN logicalExpression BRAC_CLOSE | logicalExpression (AND | OR) logicalExpression | expression ( (AND | OR) logicalExpression )* ;

expression : key condition;

condition : (comparison | between | in | notIn | like | notLike | isNull | isNotNull);

comparison :  OPERATOR (string | BOOLEAN_VALUE | number | NULL | function);
between : BETWEEN ((number AND number) | (string AND string));
in : IN BRAC_OPEN (commaSeparatedStrings |  commaSeparatedNumbers) BRAC_CLOSE;
notIn : NOT IN BRAC_OPEN (commaSeparatedStrings |  commaSeparatedNumbers) BRAC_CLOSE;
like : LIKE string ;
notLike : NOT LIKE string ;
isNull : IS NULL;
isNotNull : IS NOT NULL;

//Comma saperated values
commaSeparatedStrings : string (COMMA string)*;
commaSeparatedNumbers : NUMBER_VALUE (COMMA NUMBER_VALUE)*;


//Handling hql functions
key : FUNCTION BRAC_OPEN KEY BRAC_CLOSE |  KEY;
string : FUNCTION BRAC_OPEN STRING_VALUE BRAC_CLOSE |  STRING_VALUE;
number : FUNCTION BRAC_OPEN NUMBER_VALUE BRAC_CLOSE |  NUMBER_VALUE;
function : FUNCTION BRAC_OPEN KEY? BRAC_CLOSE;

/*
 * Lexer Rules
 */
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

fragment DOT : '.';
fragment COLON : ':';
fragment ESCAPE_QUOTE : ( '\'' | '\\' )  '\'' ; // \' or '' (escape quote)
fragment ALL_BUT_QUOTE : ~('\''); // all the charecters except '
fragment NUMBER : ('0'..'9');
fragment ALPHABET : ('_'|'a'..'z'|'A'..'Z');
fragment ALPHA_NUMERIC : ( ALPHABET | NUMBER );
fragment STRING : (ESCAPE_QUOTE|ALL_BUT_QUOTE)* ; // all the content which can be inside singlequotes, this includes escaped singlequotes.
fragment EQ : '=';
fragment NE : '!=' | '<>';
fragment LT : '<';
fragment LE : '<=';
fragment GT : '>';
fragment GE : '>=';
fragment US : '_';

WHITESPACE : ' ' -> skip ;


COMMA : ',';
BRAC_OPEN : '(';
BRAC_CLOSE : ')';
SINGLE_QUOTE : '\'';
HYPHEN : '-';

//Case insensitive words
BETWEEN : B E T W E E N ;
IN : I N ;
AND : A N D ;
LIKE : L I K E ;
OR : O R ;
IS : I S ;
NOT : N O T ;
NULL : N U L L ;

//Hql Functions
FUNCTION :  C U R R E N T US D A T E | // current_date
            C U R R E N T US T I M E | //current_time
            C U R R E N T US T I M E S T A M P | //current_timestamp
//          S U B S T R I N G |  //substring
//          T R I M |  //trim
//          L E N G T H |  //length
//          L O C A T E |  //locate
//          A B S |  //abs
//          S Q R T |  //sqrt
//          B I T_ L E N G T H |  //bit_length
//          M O D |  //mod
//          C O A L E S C E |  //coalesce
//          N U L L I F |  //nullif
//          S T R |  //str
//          C A S T |  //cast
//          E X T R A C T |  //extract
//          I N D E X |  //index
//          S I Z E |  //size
//          M I N E L E M E N T |  //minelement
//          M A X E L E M E N T |  //maxelement
//          M I N I N D E X |  //minindex
//          M A X I N D E X |  //maxindex
//          E L E M E N T S |  //elements
//          S I G N |  //sign
//          T R U N C |  //trunc
//          R T R I M |  //rtrim
//          S I N |  //sin
            U P P E R | //upper
            L O W E R; //lower


OPERATOR : EQ | NE |  GT | LT | GE | LE ;
BOOLEAN_VALUE :  T R U E | F A L S E ;
NUMBER_VALUE : SINGLE_QUOTE NUMBER+(DOT NUMBER+)? SINGLE_QUOTE | NUMBER+(DOT NUMBER+)?
               | HYPHEN NUMBER+(DOT NUMBER+)? | SINGLE_QUOTE HYPHEN NUMBER+(DOT NUMBER+)? SINGLE_QUOTE;
STRING_VALUE : SINGLE_QUOTE STRING SINGLE_QUOTE;

KEY:  (ALPHABET ALPHA_NUMERIC*) (DOT ALPHABET ALPHA_NUMERIC*)*;

