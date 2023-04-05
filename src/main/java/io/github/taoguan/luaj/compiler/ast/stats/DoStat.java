package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoStat extends Stat {

    private Block block;

    public DoStat(Block block) {
        this.block = block;
    }

}
