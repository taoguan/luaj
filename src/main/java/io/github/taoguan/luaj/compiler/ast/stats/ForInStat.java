package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForInStat extends Stat {

    private int lineOfDo;
    private List<String> nameList;
    private List<Exp> expList;
    private Block block;

}
