package io.github.taoguan.luaj.compiler;

import io.github.taoguan.luaj.LuaString;
import io.github.taoguan.luaj.Prototype;
import io.github.taoguan.luaj.compiler.ast.Block;
import io.github.taoguan.luaj.compiler.codegen.CodeGen;
import io.github.taoguan.luaj.compiler.parser.Parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author taohuan
 */
public class Compiler {

    public static Prototype compile(InputStream chunk, String chunkName) throws IOException {
        Block ast = Parser.parse(chunk, chunkName);
        Prototype proto = CodeGen.genProto(ast);
        setSource(proto, chunkName);
        return proto;
    }

    private static void setSource(Prototype proto, String chunkName) {
        proto.source = LuaString.valueOf(chunkName);
        for (Prototype subProto : proto.p) {
            setSource(subProto, chunkName);
        }
    }

}
