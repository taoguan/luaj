package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;

/** Abstract base class for Java function implementations that takes varaiable arguments and 
 * returns multiple return values. 
 * <p>
 * Subclasses need only implement {@link io.github.taoguan.luaj.LuaValue#invoke(Varargs)} to complete this class,
 * simplifying development.  
 * All other uses of {@link #call(io.github.taoguan.luaj.LuaValue)}, {@link #invoke()},etc,
 * are routed through this method by this class,
 * converting arguments to {@link Varargs} and  
 * dropping or extending return values with {@code nil} values as required.
 * <p>
 * If between one and three arguments are required, and only one return value is returned,   
 * {@link ZeroArgFunction}, {@link OneArgFunction}, {@link TwoArgFunction}, or {@link ThreeArgFunction}.
 * <p>
 * See {@link LibFunction} for more information on implementation libraries and library functions.
 * @see #invoke(Varargs)
 * @see LibFunction
 * @see ZeroArgFunction
 * @see OneArgFunction
 * @see TwoArgFunction
 * @see ThreeArgFunction
 */
abstract public class VarArgFunction extends LibFunction {

	public VarArgFunction() {
	}
	
	public io.github.taoguan.luaj.LuaValue call() {
		return invoke(NONE).arg1();
	}

	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
		return invoke(arg).arg1();
	}

	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
		return invoke(varargsOf(arg1,arg2)).arg1();
	}

	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2, io.github.taoguan.luaj.LuaValue arg3) {
		return invoke(varargsOf(arg1,arg2,arg3)).arg1();
	}

	/** 
	 * Subclass responsibility. 
	 * May not have expected behavior for tail calls. 
	 * Should not be used if:
	 * - function has a possibility of returning a TailcallVarargs
	 * @param args the arguments to the function call.
	 */
	public Varargs invoke(Varargs args) {
		return onInvoke(args).eval();
	}
	
	public Varargs onInvoke(Varargs args) {
		return invoke(args);
	}
} 
