package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableAccessExp extends PrefixExp {

    private Exp prefixExp;
    private Exp keyExp;

    public TableAccessExp(int lastLine, Exp prefixExp, Exp keyExp) {
        setLastLine(lastLine);
        this.prefixExp = prefixExp;
        this.keyExp = keyExp;
    }

}
