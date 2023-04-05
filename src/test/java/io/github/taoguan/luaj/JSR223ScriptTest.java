package io.github.taoguan.luaj;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSR223ScriptTest {

    @Test
    public void test() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine e = mgr.getEngineByName("luaj");
        e.put("x", 25);
        e.eval("y = math.sqrt(x)");
        System.out.println( "y="+e.get("y") );
    }

}
