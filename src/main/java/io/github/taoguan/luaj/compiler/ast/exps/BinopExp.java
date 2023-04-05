package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.lexer.Token;
import io.github.taoguan.luaj.compiler.lexer.TokenKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinopExp extends Exp {

    private TokenKind op; // operator
    private Exp exp1;
    private Exp exp2;

    public BinopExp(Token op, Exp exp1, Exp exp2) {
        setLine(op.getLine());
        this.exp1 = exp1;
        this.exp2 = exp2;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_SUB;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BXOR;
        } else {
            this.op = op.getKind();
        }
    }

}
