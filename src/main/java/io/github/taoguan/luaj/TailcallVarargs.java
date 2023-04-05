package io.github.taoguan.luaj;

/**
 * Subclass of {@link Varargs} that represents a lua tail call 
 * in a Java library function execution environment. 
 * <p>
 * Since Java doesn't have direct support for tail calls, 
 * any lua function whose {@link io.github.taoguan.luaj.Prototype} contains the
 * {@link io.github.taoguan.luaj.vm.OpCode#TAILCALL} bytecode needs a mechanism
 * for tail calls when converting lua-bytecode to java-bytecode.
 * <p>
 * The tail call holds the next function and arguments, 
 * and the client a call to {@link #eval()} executes the function
 * repeatedly until the tail calls are completed. 
 * <p>
 * Normally, users of luaj need not concern themselves with the 
 * details of this mechanism, as it is built into the core 
 * execution framework. 
 * @see io.github.taoguan.luaj.Prototype
 * @see io.github.taoguan.luaj.luajc.LuaJC
 */
public class TailcallVarargs extends Varargs {

	private io.github.taoguan.luaj.LuaValue func;
	private Varargs args;
	private Varargs result;
	
	public TailcallVarargs(io.github.taoguan.luaj.LuaValue f, Varargs args) {
		this.func = f;
		this.args = args;
	}
	
	public TailcallVarargs(io.github.taoguan.luaj.LuaValue object, io.github.taoguan.luaj.LuaValue methodname, Varargs args) {
		this.func = object.get(methodname);
		this.args = io.github.taoguan.luaj.LuaValue.varargsOf(object, args);
	}
	
	public boolean isTailcall() {
		return true;
	}
	
	public Varargs eval() {
		while ( result == null ) {
			Varargs r = func.onInvoke(args);
			if (r.isTailcall()) {
				TailcallVarargs t = (TailcallVarargs) r;
				func = t.func;
				args = t.args;
			}
			else {
				result = r;			
				func = null;
				args = null;
			}
		}
		return result;
	}
	
	public io.github.taoguan.luaj.LuaValue arg(int i ) {
		if ( result == null )
			eval();
		return result.arg(i);
	}
	
	public io.github.taoguan.luaj.LuaValue arg1() {
		if (result == null)
			eval();
		return result.arg1();
	}
	
	public int narg() {
		if (result == null)
			eval();
		return result.narg();
	}

	public Varargs subargs(int start) {
		if (result == null)
			eval();
		return result.subargs(start);
	}
}