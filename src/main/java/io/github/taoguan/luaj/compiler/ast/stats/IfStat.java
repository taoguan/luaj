package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IfStat extends Stat {

    private List<Exp> exps;
    private List<Block> blocks;

    public IfStat(List<Exp> exps, List<Block> blocks) {
        this.exps = exps;
        this.blocks = blocks;
    }

}
