package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.LuaString;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.lexer.Token;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StringExp extends Exp {

    private LuaString str;

    public StringExp(Token token) {
        setLine(token.getLine());
        this.str = token.getLuaString();
    }

    public StringExp(int line, LuaString str) {
        setLine(line);
        this.str = str;
    }

    public StringExp(int line, String str) {
       this(line, LuaString.valueOf(str));
    }

}
