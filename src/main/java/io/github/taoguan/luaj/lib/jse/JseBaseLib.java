package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

import java.io.*;

/** 
 * Subclass of {@link BaseLib} and {@link LibFunction} which implements the lua basic library functions
 * and provides a directory based {@link ResourceFinder} as the {@link io.github.taoguan.luaj.Globals#finder}.
 * <p>
 * Since JME has no file system by default, {@link BaseLib} implements 
 * {@link ResourceFinder} using {@link Class#getResource(String)}. 
 * The {@link JseBaseLib} implements {@link io.github.taoguan.luaj.Globals#finder} by scanning the current directory
 * first, then falling back to   {@link Class#getResource(String)} if that fails.
 * Otherwise, the behavior is the same as that of {@link BaseLib}.  
 * <p>  
 * Typically, this library is included as part of a call to 
 * {@link JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * <p>
 * For special cases where the smallest possible footprint is desired, 
 * a minimal set of libraries could be loaded
 * directly via {@link io.github.taoguan.luaj.Globals#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * <p>However, other libraries such as <em>PackageLib</em> are not loaded in this case.
 * <p>
 * This is a direct port of the corresponding library in C.
 * @see io.github.taoguan.luaj.Globals
 * @see BaseLib
 * @see ResourceFinder
 * @see io.github.taoguan.luaj.Globals#finder
 * @see LibFunction
 * @see JsePlatform
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.1">Lua 5.3 Base Lib Reference</a>
 */

public class JseBaseLib extends BaseLib {


	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * <P>Specifically, extend the library loading to set the default value for {@link io.github.taoguan.luaj.Globals#STDIN}
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, which must be a Globals instance.
	 */
	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue modname, io.github.taoguan.luaj.LuaValue env) {
		super.call(modname, env);
		env.checkglobals().STDIN = System.in;
		return env;
	}


	/** 
	 * Try to open a file in the current working directory, 
	 * or fall back to base opener if not found.
	 * 
	 * This implementation attempts to open the file using new File(filename).  
	 * It falls back to the base implementation that looks it up as a resource
	 * in the class path if not found as a plain file. 
	 *  
	 * @param filename
	 * @return InputStream, or null if not found. 
	 */
	public InputStream findResource(String filename) {
		File f = new File(filename);
		if ( ! f.exists() )
			return super.findResource(filename);
		try {
			return new BufferedInputStream(new FileInputStream(f));
		} catch ( IOException ioe ) {
			return null;
		}
	}
}

