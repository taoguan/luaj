package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConcatExp extends Exp {

    private List<Exp> exps;

    public ConcatExp(int line, List<Exp> exps) {
        setLine(line);
        this.exps = exps;
    }

}
