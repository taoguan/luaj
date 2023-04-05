package io.github.taoguan.luaj.compiler;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.BaseLib;
import io.github.taoguan.luaj.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.InputStream;

/**
 * Compiler for Lua.
 * 
 * <p>
 * Compiles lua source files into lua bytecode within a {@link io.github.taoguan.luaj.Prototype},
 * loads lua binary files directly into a {@link io.github.taoguan.luaj.Prototype},
 * and optionaly instantiates a {@link LuaClosure} around the result 
 * using a user-supplied environment.  
 * 
 * <p>
 * Implements the {@link io.github.taoguan.luaj.Globals.Compiler} interface for loading
 * initialized chunks, which is an interface common to 
 * lua bytecode compiling and java bytecode compiling.
 *  
 * <p> 
 * The {@link LuaC} compiler is installed by default by both the 
 * {@link JsePlatform}
 * so in the following example, the default {@link LuaC} compiler 
 * will be used:
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.load(new StringReader("print 'hello'"), "main.lua" ).call();
 * } </pre>
 * 
 * To load the LuaC compiler manually, use the install method:
 * <pre> {@code
 * LuaC.install(globals);
 * } </pre>
 * 
 * @see #install(io.github.taoguan.luaj.Globals)
 * @see io.github.taoguan.luaj.Globals#compiler
 * @see io.github.taoguan.luaj.Globals#loader
 * @see JsePlatform
 * @see BaseLib
 * @see io.github.taoguan.luaj.LuaValue
 * @see io.github.taoguan.luaj.Prototype
 */
public class LuaC implements io.github.taoguan.luaj.Globals.Compiler, io.github.taoguan.luaj.Globals.Loader {

	/** A sharable instance of the LuaC compiler. */
	public static final LuaC instance = new LuaC();
	
	/** Install the compiler so that LoadState will first 
	 * try to use it when handed bytes that are 
	 * not already a compiled lua chunk.
	 * @param globals the Globals into which this is to be installed.
	 */
	public static void install(io.github.taoguan.luaj.Globals globals) {
		globals.compiler = instance;
		globals.loader = instance;
	}

	protected LuaC() {}

	/** Compile lua source into a Prototype.
	 * @param stream InputStream representing the text source conforming to lua source syntax.
	 * @param chunkname String name of the chunk to use.
	 * @return Prototype representing the lua chunk for this source.
	 * @throws IOException
	 */
	public io.github.taoguan.luaj.Prototype compile(InputStream stream, String chunkname) throws IOException {
		return Compiler.compile(stream, chunkname);
	}

	public LuaFunction load(io.github.taoguan.luaj.Prototype prototype, String chunkname, io.github.taoguan.luaj.LuaValue env) throws IOException {
		return new LuaClosure(prototype, env);
	}

}
