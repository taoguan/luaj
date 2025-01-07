package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;

/**
 * Subclass of {@link LibFunction} which implements the lua standard {@code coroutine}
 * library.
 * <p>
 * The coroutine library in luaj has the same behavior as the
 * coroutine library in C, but is implemented using Java Threads to maintain
 * the call state between invocations.  Therefore it can be yielded from anywhere,
 * similar to the "Coco" yield-from-anywhere patch available for C-based lua.
 * However, coroutines that are yielded but never resumed to complete their execution
 * may not be collected by the garbage collector.
 * <p>
 * Typically, this library is included as part of a call to either
 * {@link io.github.taoguan.luaj.lib.jse.JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("coroutine").get("running").call() );
 * } </pre>
 * <p>
 * To instantiate and use it directly,
 * link it into your globals table via {@link io.github.taoguan.luaj.LuaValue#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new CoroutineLib());
 * System.out.println( globals.get("coroutine").get("running").call() );
 * } </pre>
 * <p>
 * @see LibFunction
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.2">Lua 5.3 Coroutine Lib Reference</a>
 */
public class CoroutineLib extends TwoArgFunction {

	static int coroutine_count = 0;

	Globals globals;
	
	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, which must be a Globals instance.
	 */
	public LuaValue call(LuaValue modname, LuaValue env) {
		globals = env.checkglobals();
		LuaTable coroutine = new LuaTable();
		coroutine.set("create", new create());
		coroutine.set("resume", new resume());
		coroutine.set("running", new running());
		coroutine.set("status", new status());
		coroutine.set("yield", new yield());
		coroutine.set("wrap", new wrap());
		env.set("coroutine", coroutine);
		if (!env.get("package").isnil()) env.get("package").get("loaded").set("coroutine", coroutine);
		return coroutine;
	}

	final class create extends LibFunction {
		public LuaValue call(LuaValue f) {
			return new LuaThread(globals, f.checkfunction());
		}
	}

	static final class resume extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			final LuaThread t = args.checkthread(1);
			return t.resume( args.subargs(2) );
		}
	}

	final class running extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			final LuaThread r = globals.running;
			return varargsOf(r, valueOf(r.isMainThread()));
		}
	}

	static final class status extends LibFunction {
		public LuaValue call(LuaValue t) {
			LuaThread lt = t.checkthread();
			return valueOf( lt.getStatus() );
		}
	}
	
	final class yield extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			return globals.yield( args );
		}
	}

	final class wrap extends LibFunction {
		public LuaValue call(LuaValue f) {
			final LuaValue func = f.checkfunction();
			final LuaThread thread = new LuaThread(globals, func);
			return new wrapper(thread);
		}
	}

	static final class wrapper extends VarArgFunction {
		final LuaThread luathread;
		wrapper(LuaThread luathread) {
			this.luathread = luathread;
		}
		public Varargs invoke(Varargs args) {
			final Varargs result = luathread.resume(args);
			if ( result.arg1().toboolean() ) {
				return result.subargs(2);
			} else {
				return error( result.arg(2).tojstring() );
			}
		}
	}
}
