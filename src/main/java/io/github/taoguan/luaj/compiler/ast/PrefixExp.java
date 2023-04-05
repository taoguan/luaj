package io.github.taoguan.luaj.compiler.ast;

/*
prefixexp ::= Name |
              ‘(’ exp ‘)’ |
              prefixexp ‘[’ exp ‘]’ |
              prefixexp ‘.’ Name |
              prefixexp ‘:’ Name args |
              prefixexp args
*/
public abstract class PrefixExp extends Exp {

}
