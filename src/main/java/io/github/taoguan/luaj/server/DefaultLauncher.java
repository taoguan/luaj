package io.github.taoguan.luaj.server;

import io.github.taoguan.luaj.Globals;
import io.github.taoguan.luaj.LuaValue;
import io.github.taoguan.luaj.Varargs;
import io.github.taoguan.luaj.lib.jse.CoerceJavaToLua;
import io.github.taoguan.luaj.lib.jse.JsePlatform;

import java.io.InputStream;
import java.io.Reader;

/**
 * Default {@link Launcher} instance that creates standard globals 
 * and runs the supplied scripts with chunk name 'main'.
 * <P>
 * Arguments are coerced into lua using {@link CoerceJavaToLua#coerce(Object)}.
 * <P>
 * Return values with simple types are coerced into Java simple types. 
 * Tables, threads, and functions are returned as lua objects.
 * 
 * @see Launcher
 * @see LuajClassLoader
 * @see LuajClassLoader#NewLauncher()
 * @see LuajClassLoader#NewLauncher(Class)
 */
public class DefaultLauncher implements Launcher {
	protected Globals g;

	public DefaultLauncher() {
		g = JsePlatform.standardGlobals();
	}
	
	/** Launches the script with chunk name 'main' */
	public Object[] launch(String script, Object[] arg) {
		return launchChunk(g.load(script, "main"), arg);
	}

	/** Launches the script with chunk name 'main' and loading using modes 'bt' */
	public Object[] launch(InputStream script, Object[] arg) {
		return launchChunk(g.load(script, "main", "bt", g), arg);
	}

	/** Launches the script with chunk name 'main' */
	public Object[] launch(Reader script, Object[] arg) {
		return launchChunk(g.load(script, "main"), arg);
	}

	private Object[] launchChunk(LuaValue chunk, Object[] arg) {
		LuaValue args[] = new LuaValue[arg.length];
		for (int i = 0; i < args.length; ++i)
			args[i] = CoerceJavaToLua.coerce(arg[i]);
		Varargs results = chunk.invoke(LuaValue.varargsOf(args));

		final int n = results.narg();
		Object return_values[] = new Object[n];
		for (int i = 0; i < n; ++i) {
			LuaValue r = results.arg(i+1);
			switch (r.type()) {
			case LuaValue.TBOOLEAN:
				return_values[i] = r.toboolean();
				break;
			case LuaValue.TNUMBER:
				return_values[i] = r.todouble();
				break;
			case LuaValue.TINT:
				return_values[i] = r.toint();
				break;
			case LuaValue.TNIL:
				return_values[i] = null;
				break;
			case LuaValue.TSTRING:
				return_values[i] = r.tojstring();
				break;
			case LuaValue.TUSERDATA:
				return_values[i] = r.touserdata();
				break;
			default:
				return_values[i] = r;
			}
		}
		return return_values;
	}
}