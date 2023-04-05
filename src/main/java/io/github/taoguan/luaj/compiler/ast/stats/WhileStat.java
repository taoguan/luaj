package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhileStat extends Stat {

    private Exp exp;
    private Block block;

    public WhileStat(Exp exp, Block block) {
        this.exp = exp;
        this.block = block;
    }

}
