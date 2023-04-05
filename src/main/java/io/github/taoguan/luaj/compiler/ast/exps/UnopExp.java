package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import io.github.taoguan.luaj.compiler.lexer.Token;
import io.github.taoguan.luaj.compiler.lexer.TokenKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnopExp extends Exp {

    private TokenKind op; // operator
    private Exp exp;

    public UnopExp(Token op, Exp exp) {
        setLine(op.getLine());
        this.exp = exp;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_UNM;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BNOT;
        } else {
            this.op = op.getKind();
        }
    }

}
