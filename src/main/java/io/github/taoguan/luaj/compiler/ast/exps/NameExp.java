package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameExp extends PrefixExp {

    private String name;

    public NameExp(int line, String name) {
        setLine(line);
        this.name = name;
    }

}
