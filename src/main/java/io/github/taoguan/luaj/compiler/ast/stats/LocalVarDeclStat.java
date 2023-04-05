package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class LocalVarDeclStat extends Stat {

    private List<String> nameList;
    private List<Exp> expList;

    public LocalVarDeclStat(int lastLine,
                            List<String> nameList, List<Exp> expList) {
        setLastLine(lastLine);
        this.nameList = nameList != null ? nameList : Collections.emptyList();
        this.expList = expList != null ? expList : Collections.emptyList();
    }

}
