package io.github.taoguan.luaj.compiler.ast.stats;

import io.github.taoguan.luaj.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelStat extends Stat {

    private String name;

    public LabelStat(String name, int line) {
        this.name = name;
        setLine(line);
        setLastLine(line);
    }

}
