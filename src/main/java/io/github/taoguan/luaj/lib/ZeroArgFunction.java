package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;

/** Abstract base class for Java function implementations that take no arguments and 
 * return one value. 
 * <p>
 * Subclasses need only implement {@link io.github.taoguan.luaj.LuaValue#call()} to complete this class,
 * simplifying development.  
 * All other uses of {@link #call(io.github.taoguan.luaj.LuaValue)}, {@link #invoke(Varargs)},etc,
 * are routed through this method by this class.
 * <p>
 * If one or more arguments are required, or variable argument or variable return values, 
 * then use one of the related function
 * {@link OneArgFunction}, {@link TwoArgFunction}, {@link ThreeArgFunction}, or {@link VarArgFunction}.
 * <p>
 * See {@link LibFunction} for more information on implementation libraries and library functions.
 * @see #call()
 * @see LibFunction
 * @see OneArgFunction
 * @see TwoArgFunction
 * @see ThreeArgFunction
 * @see VarArgFunction
 */
abstract public class ZeroArgFunction extends LibFunction {

	abstract public LuaValue call();

	/** Default constructor */
	public ZeroArgFunction() {
	}
	
	public LuaValue call(LuaValue arg) {
		return call();
	}

	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		return call();
	}

	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		return call();
	}

	public Varargs invoke(Varargs varargs) {
		return call();
	}
} 
