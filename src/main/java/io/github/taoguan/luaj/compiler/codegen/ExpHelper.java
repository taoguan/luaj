package io.github.taoguan.luaj.compiler.codegen;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.ast.exps.*;

import java.util.List;

class ExpHelper {

    static boolean isVarargOrFuncCall(Exp exp) {
        return exp instanceof VarargExp
                || exp instanceof FuncCallExp;
    }

    static List<Exp> removeTailNils(List<Exp> exps) {
        while (!exps.isEmpty()) {
            if (exps.get(exps.size() - 1) instanceof NilExp) {
                exps.remove(exps.size() - 1);
            } else {
                break;
            }
        }
        return exps;
    }

    static int lineOf(Exp exp) {
        if (exp instanceof TableAccessExp) {
            return lineOf(((TableAccessExp) exp).getPrefixExp());
        }
        if (exp instanceof ConcatExp) {
            return lineOf(((ConcatExp) exp).getExps().get(0));
        }
        if (exp instanceof BinopExp) {
            return lineOf(((BinopExp) exp).getExp1());
        }
        return exp.getLine();
    }

    static int lastLineOf(Exp exp) {
        if (exp instanceof TableAccessExp) {
            return lastLineOf(((TableAccessExp) exp).getPrefixExp());
        }
        if (exp instanceof ConcatExp) {
            return lastLineOf(((ConcatExp) exp).getExps().get(0));
        }
        if (exp instanceof BinopExp) {
            return lastLineOf(((BinopExp) exp).getExp1());
        }
        if (exp instanceof UnopExp) {
            return lastLineOf(((UnopExp) exp).getExp());
        }
        int lastLine = exp.getLastLine();
        if (lastLine > 0) {
            return lastLine;
        }
        return exp.getLine();
    }

}
