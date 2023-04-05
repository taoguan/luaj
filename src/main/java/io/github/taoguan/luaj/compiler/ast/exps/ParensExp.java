package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParensExp extends PrefixExp {

    private Exp exp;

    public ParensExp(Exp exp) {
        this.exp = exp;
    }

}
