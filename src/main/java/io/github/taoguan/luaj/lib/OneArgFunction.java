package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;

/** Abstract base class for Java function implementations that take one argument and 
 * return one value. 
 * <p>
 * Subclasses need only implement {@link io.github.taoguan.luaj.LuaValue#call(io.github.taoguan.luaj.LuaValue)} to complete this class,
 * simplifying development.  
 * All other uses of {@link #call()}, {@link #invoke(io.github.taoguan.luaj.Varargs)},etc,
 * are routed through this method by this class, 
 * dropping or extending arguments with {@code nil} values as required.
 * <p>
 * If more than one argument are required, or no arguments are required, 
 * or variable argument or variable return values, 
 * then use one of the related function
 * {@link ZeroArgFunction}, {@link TwoArgFunction}, {@link ThreeArgFunction}, or {@link VarArgFunction}.
 * <p>
 * See {@link LibFunction} for more information on implementation libraries and library functions.
 * @see #call(io.github.taoguan.luaj.LuaValue)
 * @see LibFunction
 * @see ZeroArgFunction
 * @see TwoArgFunction
 * @see ThreeArgFunction
 * @see VarArgFunction
 */
abstract public class OneArgFunction extends LibFunction {

	abstract public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg);
	
	/** Default constructor */
	public OneArgFunction() {
	}
		
	public final io.github.taoguan.luaj.LuaValue call() {
		return call(NIL);
	}

	public final io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
		return call(arg1);
	}

	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2, io.github.taoguan.luaj.LuaValue arg3) {
		return call(arg1);
	}

	public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs varargs) {
		return call(varargs.arg1());
	}
} 
