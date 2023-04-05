package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Stat;
import io.github.taoguan.luaj.compiler.ast.exps.FuncDefExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalFuncDefStat extends Stat {

    private String name;
    private FuncDefExp exp;

    public LocalFuncDefStat(String name, FuncDefExp exp) {
        this.name = name;
        this.exp = exp;
    }

}
