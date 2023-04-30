package io.github.taoguan.luaj;

import io.github.taoguan.luaj.lib.jse.JsePlatform;
import org.junit.Test;

import java.io.FileInputStream;

/**
 * @author taohuan
 */
public class GrammarTest {

    @Test
    public void testAll() throws Exception {
        String luaFileName = GrammarTest.class.getClassLoader().getResource("grammar-test.lua").toURI().getPath();
        Globals globals = JsePlatform.standardGlobals();
        //LuaJC.install(globals);
        LuaValue result = globals.loadfile(luaFileName).call();
        System.out.println(result);
        LuaValue func = result.get(LuaValue.valueOf("addVar"));
        int funcResult  = func.invoke(LuaValue.varargsOf(new LuaValue[]{result, LuaValue.valueOf(1),
                LuaValue.valueOf(2), LuaValue.valueOf(3)})).arg1().checkint();
        System.out.println(String.format("funcResult=%s", funcResult));
        Prototype p = globals.compilePrototype(new FileInputStream(luaFileName), "grammer-test");
        Print.print(p);
    }

}
