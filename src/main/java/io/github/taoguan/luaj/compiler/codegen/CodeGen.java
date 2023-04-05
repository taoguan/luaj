package io.github.taoguan.luaj.compiler.codegen;


import io.github.taoguan.luaj.Prototype;
import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.ast.exps.FuncDefExp;

public class CodeGen {

    public static Prototype genProto(Block chunk) {
        FuncDefExp fd = new FuncDefExp();
        fd.setLastLine(chunk.getLastLine());
        fd.setIsVararg(true);
        fd.setBlock(chunk);

        FuncInfo fi = new FuncInfo(null, fd);
        fi.addLocVar("_ENV", 0);
        ExpProcessor.processFuncDefExp(fi, fd, 0);
        return Fi2Proto.toProto(fi.subFuncs.get(0));
    }

}
