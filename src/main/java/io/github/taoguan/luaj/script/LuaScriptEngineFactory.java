package io.github.taoguan.luaj.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;

/**
 * Jsr 223 scripting engine factory.
 * 
 * Exposes metadata to support the lua language, and constructs 
 * instances of LuaScriptEngine to handl lua scripts.
 */
public class LuaScriptEngineFactory implements ScriptEngineFactory {
    
 	private static final String [] EXTENSIONS = {
 		"lua",
 		".lua",
 	};
    
    private static final String [] MIMETYPES = {
        "text/lua",
        "application/lua"
    };
    
    private static final String [] NAMES = {
        "lua", 
        "luaj",
    };
    
    private List<String> extensions;
    private List<String> mimeTypes;
    private List<String> names;
    
    public LuaScriptEngineFactory() {
        extensions = Arrays.asList(EXTENSIONS);
        mimeTypes = Arrays.asList(MIMETYPES);
        names = Arrays.asList(NAMES);
    }
    
    public String getEngineName() {
        return getScriptEngine().get(ScriptEngine.ENGINE).toString();
    }
    
    public String getEngineVersion() {
        return getScriptEngine().get(ScriptEngine.ENGINE_VERSION).toString();
    }
    
    public List<String> getExtensions() {
        return extensions;
    }
    
    public List<String> getMimeTypes() {
        return mimeTypes;
    }
    
    public List<String> getNames() {
        return names;
    }
    
    public String getLanguageName() {
        return getScriptEngine().get(ScriptEngine.LANGUAGE).toString();
    }
    
    public String getLanguageVersion() {
        return getScriptEngine().get(ScriptEngine.LANGUAGE_VERSION).toString();
    }
    
    public Object getParameter(String key) {
        return getScriptEngine().get(key).toString();
    }
    
    public String getMethodCallSyntax(String obj, String m, String... args)  {
        StringBuffer sb = new StringBuffer();
        sb.append(obj + ":" + m + "(");
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }
    
    public String getOutputStatement(String toDisplay) {
        return "print(" + toDisplay + ")";
    }
    
    public String getProgram(String ... statements) {
        StringBuffer sb = new StringBuffer();
        int len = statements.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(statements[i]);
        }
        return sb.toString();
    }
    
    public ScriptEngine getScriptEngine() {
    	return new LuaScriptEngine();
    }
}
