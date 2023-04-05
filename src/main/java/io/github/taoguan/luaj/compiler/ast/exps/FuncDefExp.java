package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FuncDefExp extends Exp {

    private List<String> parList;
    private boolean IsVararg;
    private Block block;

}
