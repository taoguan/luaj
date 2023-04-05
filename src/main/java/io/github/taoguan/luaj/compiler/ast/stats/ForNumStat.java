package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForNumStat extends Stat {

    private int lineOfFor;
    private int lineOfDo;
    private String varName;
    private Exp InitExp;
    private Exp LimitExp;
    private Exp StepExp;
    private Block block;

}
