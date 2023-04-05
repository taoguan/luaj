package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignStat extends Stat {

    private List<Exp> varList;
    private List<Exp> expList;

    public AssignStat(int lastLine,
                      List<Exp> varList, List<Exp> expList) {
        setLastLine(lastLine);
        this.varList = varList;
        this.expList = expList;
    }

}
