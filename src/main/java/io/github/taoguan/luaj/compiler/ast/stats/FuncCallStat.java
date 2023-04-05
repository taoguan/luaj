package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Stat;
import io.github.taoguan.luaj.compiler.ast.exps.FuncCallExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncCallStat extends Stat {

    private FuncCallExp exp;

    public FuncCallStat(FuncCallExp exp) {
        this.exp = exp;
    }

}
