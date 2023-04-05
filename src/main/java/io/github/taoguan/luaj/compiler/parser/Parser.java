package io.github.taoguan.luaj.compiler.parser;

import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.lexer.Lexer;
import io.github.taoguan.luaj.compiler.lexer.TokenKind;

import java.io.IOException;
import java.io.InputStream;

public class Parser {

    public static Block parse(InputStream chunk, String chunkName) throws IOException {
        Lexer lexer = new Lexer(chunk, chunkName);
        Block block = BlockParser.parseBlock(lexer);
        lexer.nextTokenOfKind(TokenKind.TOKEN_EOF);
        return block;
    }

}
