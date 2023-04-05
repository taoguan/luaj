package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepeatStat extends Stat {

    private Block block;
    private Exp exp;

    public RepeatStat(Block block, Exp exp) {
        this.block = block;
        this.exp = exp;
    }

}
