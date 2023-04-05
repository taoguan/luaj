package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;

/** Abstract base class for Java function implementations that take two arguments and 
 * return one value. 
 * <p>
 * Subclasses need only implement {@link io.github.taoguan.luaj.LuaValue#call(io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue)} to complete this class,
 * simplifying development.  
 * All other uses of {@link #call()}, {@link #invoke(Varargs)},etc, 
 * are routed through this method by this class, 
 * dropping or extending arguments with {@code nil} values as required.
 * <p>
 * If more or less than three arguments are required,  
 * or variable argument or variable return values, 
 * then use one of the related function
 * {@link ZeroArgFunction}, {@link OneArgFunction}, {@link TwoArgFunction}, or {@link VarArgFunction}.
 * <p>
 * See {@link LibFunction} for more information on implementation libraries and library functions.
 * @see #call(io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue, io.github.taoguan.luaj.LuaValue)
 * @see LibFunction
 * @see ZeroArgFunction
 * @see OneArgFunction
 * @see TwoArgFunction
 * @see VarArgFunction
 */
abstract public class ThreeArgFunction extends LibFunction {

	abstract public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2, io.github.taoguan.luaj.LuaValue arg3);
	
	/** Default constructor */
	public ThreeArgFunction() {
	}
	
	public final io.github.taoguan.luaj.LuaValue call() {
		return call(NIL, NIL, NIL);
	}

	public final io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
		return call(arg, NIL, NIL);
	}

	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
		return call(arg1, arg2, NIL);
	}
	
	public Varargs invoke(Varargs varargs) {
		return call(varargs.arg1(),varargs.arg(2),varargs.arg(3));
	}
	
} 
