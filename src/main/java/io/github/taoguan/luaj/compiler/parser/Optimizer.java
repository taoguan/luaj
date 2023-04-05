package io.github.taoguan.luaj.compiler.parser;

import io.github.taoguan.luaj.compiler.ast.*;
import io.github.taoguan.luaj.compiler.ast.exps.*;
import io.github.taoguan.luaj.lib.MathLib;

import static io.github.taoguan.luaj.compiler.lexer.TokenKind.*;
import static io.github.taoguan.luaj.compiler.lexer.TokenKind.TOKEN_OP_POW;

class Optimizer {

    static Exp optimizeLogicalOr(BinopExp exp) {
        if (isTrue(exp.getExp1())) {
            return exp.getExp1(); // true or x => true
        }
        if (isFalse(exp.getExp1()) && !isVarargOrFuncCall(exp.getExp2())) {
            return exp.getExp2(); // false or x => x
        }
        return exp;
    }

    static Exp optimizeLogicalAnd(BinopExp exp) {
        if (isFalse(exp.getExp1())) {
            return exp.getExp1(); // false and x => false
        }
        if (isTrue(exp.getExp1()) && !isVarargOrFuncCall(exp.getExp2())) {
            return exp.getExp2(); // true and x => x
        }
        return exp;
    }

    static Exp optimizeBitwiseBinaryOp(BinopExp exp) {
        Integer i = castToInteger(exp.getExp1());
        if (i != null) {
            Integer j = castToInteger(exp.getExp2());
            if (j != null) {
                switch (exp.getOp()) {
                    case TOKEN_OP_BAND: return new IntegerExp(exp.getLine(), i & j);
                    case TOKEN_OP_BOR:  return new IntegerExp(exp.getLine(), i | j);
                    case TOKEN_OP_BXOR: return new IntegerExp(exp.getLine(), i ^ j);
                    case TOKEN_OP_SHL:  return new IntegerExp(exp.getLine(), (int) MathLib.shiftLeft(i, j));
                    case TOKEN_OP_SHR:  return new IntegerExp(exp.getLine(), (int) MathLib.shiftRight(i, j));
                }
            }
        }
        return exp;
    }

    static Exp optimizeArithBinaryOp(BinopExp exp) {
        if (exp.getExp1() instanceof IntegerExp
                && exp.getExp2() instanceof IntegerExp) {
            IntegerExp x = (IntegerExp) exp.getExp1();
            IntegerExp y = (IntegerExp) exp.getExp2();
            switch (exp.getOp()) {
                case TOKEN_OP_ADD: return new IntegerExp(exp.getLine(), x.getVal() + y.getVal());
                case TOKEN_OP_SUB: return new IntegerExp(exp.getLine(), x.getVal() - y.getVal());
                case TOKEN_OP_MUL: return new IntegerExp(exp.getLine(), x.getVal() * y.getVal());
                case TOKEN_OP_IDIV:
                    if (y.getVal() != 0) {
                        return new IntegerExp(exp.getLine(), Math.floorDiv(x.getVal(), y.getVal()));
                    }
                    break;
                case TOKEN_OP_MOD:
                    if (y.getVal() != 0) {
                        return new IntegerExp(exp.getLine(), Math.floorMod(x.getVal(), y.getVal()));
                    }
                    break;
            }
        }

        Double f = castToFloat(exp.getExp1());
        if (f != null) {
            Double g = castToFloat(exp.getExp2());
            if (g != null) {
                switch (exp.getOp()) {
                    case TOKEN_OP_ADD: return new FloatExp(exp.getLine(), f + g);
                    case TOKEN_OP_SUB: return new FloatExp(exp.getLine(), f - g);
                    case TOKEN_OP_MUL: return new FloatExp(exp.getLine(), f * g);
                    case TOKEN_OP_POW: return new FloatExp(exp.getLine(), Math.pow(f, g));
                }
                if (g != 0) {
                    switch (exp.getOp()) {
                        case TOKEN_OP_DIV:  return new FloatExp(exp.getLine(), f / g);
                        case TOKEN_OP_IDIV: return new FloatExp(exp.getLine(), MathLib.floorDiv(f, g));
                        case TOKEN_OP_MOD:  return new FloatExp(exp.getLine(), MathLib.floorMod(f, g));
                    }
                }
            }
        }

        return exp;
    }

    static Exp optimizePow(Exp exp) {
        if (exp instanceof BinopExp) {
            BinopExp binopExp = (BinopExp) exp;
            if (binopExp.getOp() == TOKEN_OP_POW) {
                binopExp.setExp2(optimizePow(binopExp.getExp2()));
            }
            return optimizeArithBinaryOp(binopExp);
        }
        return exp;
    }

    static Exp optimizeUnaryOp(UnopExp exp) {
        switch (exp.getOp()) {
            case TOKEN_OP_UNM:  return optimizeUnm(exp);
            case TOKEN_OP_NOT:  return optimizeNot(exp);
            case TOKEN_OP_BNOT: return optimizeBnot(exp);
            default: return exp;
        }
    }

    private static Exp optimizeUnm(UnopExp exp) {
        if (exp.getExp() instanceof IntegerExp) {
            IntegerExp iExp = (IntegerExp) exp.getExp();
            iExp.setVal(-iExp.getVal());
            return iExp;
        }
        if (exp.getExp() instanceof FloatExp) {
            FloatExp fExp = (FloatExp) exp.getExp();
            fExp.setVal(-fExp.getVal());
            return fExp;
        }
        return exp;
    }

    private static Exp optimizeNot(UnopExp exp) {
        Exp subExp = exp.getExp();
        if (subExp instanceof NilExp
                || subExp instanceof FalseExp) {
            return new TrueExp(exp.getLine());
        }
        if (subExp instanceof TrueExp
                || subExp instanceof IntegerExp
                || subExp instanceof FloatExp
                || subExp instanceof StringExp) {
            return new FalseExp(exp.getLine());
        }
        return exp;
    }

    private static Exp optimizeBnot(UnopExp exp) {
        if (exp.getExp() instanceof IntegerExp) {
            IntegerExp iExp = (IntegerExp) exp.getExp();
            iExp.setVal(~iExp.getVal());
            return iExp;
        }
        if (exp.getExp() instanceof FloatExp) {
            FloatExp fExp = (FloatExp) exp.getExp();
            double f = fExp.getVal();
            if (HelpNumber.isInteger(f)) {
                return new IntegerExp(fExp.getLine(), ~((int) f));
            }
        }
        return exp;
    }

    private static boolean isFalse(Exp exp) {
        return exp instanceof FalseExp
                || exp instanceof NilExp;
    }

    private static boolean isTrue(Exp exp) {
        return exp instanceof TrueExp
                || exp instanceof IntegerExp
                || exp instanceof FloatExp
                || exp instanceof StringExp;
    }

    private static boolean isVarargOrFuncCall(Exp exp) {
        return exp instanceof VarargExp
                || exp instanceof FuncCallExp;
    }

    private static Integer castToInteger(Exp exp) {
        if (exp instanceof IntegerExp) {
            return ((IntegerExp) exp).getVal();
        }
        if (exp instanceof FloatExp) {
            double f = ((FloatExp) exp).getVal();
            return HelpNumber.isInteger(f) ? (int) f : null;
        }
        return null;
    }

    private static Double castToFloat(Exp exp) {
        if (exp instanceof IntegerExp) {
            return (double) ((IntegerExp) exp).getVal();
        }
        if (exp instanceof FloatExp) {
            return ((FloatExp) exp).getVal();
        }
        return null;
    }

}
