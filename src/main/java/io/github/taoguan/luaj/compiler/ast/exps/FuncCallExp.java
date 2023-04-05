package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FuncCallExp extends PrefixExp {

    private Exp prefixExp;
    private StringExp nameExp;
    private List<Exp> args;

}
