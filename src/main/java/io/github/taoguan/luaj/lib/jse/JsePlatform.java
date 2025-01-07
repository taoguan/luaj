package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.compiler.LuaC;
import io.github.taoguan.luaj.lib.*;

/** The {@link JsePlatform} class is a convenience class to standardize
 * how globals tables are initialized for the JSE platform.
 * <p>
 * It is used to allocate either a set of standard globals using
 * {@link #standardGlobals()} or debug globals using {@link #debugGlobals()}
 * <p>
 * A simple example of initializing globals and using them from Java is:
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * <p>
 * Once globals are created, a simple way to load and run a script is:
 * <pre> {@code
 * globals.load( new FileInputStream("main.lua"), "main.lua" ).call();
 * } </pre>
 * <p>
 * although {@code require} could also be used:
 * <pre> {@code
 * globals.get("require").call(LuaValue.valueOf("main"));
 * } </pre>
 * For this to succeed, the file "main.lua" must be in the current directory or a resource.
 * See {@link JseBaseLib} for details on finding scripts using {@link ResourceFinder}.
 * <p>
 * The standard globals will contain all standard libraries plus {@code luajava}:
 * <ul>
 * <li>{@link io.github.taoguan.luaj.Globals}</li>
 * <li>{@link JseBaseLib}</li>
 * <li>{@link PackageLib}</li>
 * <li>{@link Bit32Lib}</li>
 * <li>{@link TableLib}</li>
 * <li>{@link StringLib}</li>
 * <li>{@link CoroutineLib}</li>
 * <li>{@link JseMathLib}</li>
 * <li>{@link JseIoLib}</li>
 * <li>{@link JseOsLib}</li>
 * <li>{@link LuajavaLib}</li>
 * </ul>
 * In addition, the {@link LuaC} compiler is installed so lua files may be loaded in their source form.
 * <p>
 * The debug globals are simply the standard globals plus the {@code debug} library {@link DebugLib}.
 * <p>
 * The class ensures that initialization is done in the correct order.
 * 
 * @see io.github.taoguan.luaj.Globals
 */
public class JsePlatform {

	/**
	 * Create a standard set of globals for JSE including all the libraries.
	 * 
	 * @return Table of globals initialized with the standard JSE libraries
	 * @see #debugGlobals()
	 * @see JsePlatform
	 */
	public static Globals standardGlobals() {
		Globals globals = new Globals();
		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new TableLib());
		globals.load(new StringLib());
		globals.load(new CoroutineLib());
		globals.load(new JseMathLib());
		globals.load(new JseIoLib());
		globals.load(new JseOsLib());
		globals.load(new LuajavaLib());
		LoadState.install(globals);
		LuaC.install(globals);
		return globals;
	}

	/** Create standard globals including the {@link DebugLib} library.
	 * 
	 * @return Table of globals initialized with the standard JSE and debug libraries
	 * @see #standardGlobals()
	 * @see DebugLib
	 */
	public static Globals debugGlobals() {
		Globals globals = standardGlobals();
		globals.load(new DebugLib());
		return globals;
	}


	/** Simple wrapper for invoking a lua function with command line arguments.
	 * The supplied function is first given a new Globals object as its environment
	 * then the program is run with arguments.
	 * @return {@link Varargs} containing any values returned by mainChunk.
	 */
	public static Varargs luaMain(LuaValue mainChunk, String[] args) {
		Globals g = standardGlobals();
		int n = args.length;
		LuaValue[] vargs = new LuaValue[args.length];
		for (int i = 0; i < n; ++i)
			vargs[i] = LuaValue.valueOf(args[i]);
		LuaValue arg = LuaValue.listOf(vargs);
		arg.set("n", n);
		g.set("arg", arg);
		mainChunk.initupvalue1(g);
		return mainChunk.invoke(LuaValue.varargsOf(vargs));
	}
}
