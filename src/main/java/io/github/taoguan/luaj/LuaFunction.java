package io.github.taoguan.luaj;


/**
 * Base class for functions implemented in Java. 
 * <p>
 * Direct subclass include {@link io.github.taoguan.luaj.lib.LibFunction} 
 * which is the base class for 
 * all built-in library functions coded in Java, 
 * and {@link LuaClosure}, which represents a lua closure
 * whose bytecode is interpreted when the function is invoked.    
 * @see io.github.taoguan.luaj.LuaValue
 * @see LuaClosure
 * @see io.github.taoguan.luaj.lib.LibFunction
 */
abstract
public class LuaFunction extends io.github.taoguan.luaj.LuaValue {
	
	/** Shared static metatable for all functions and closures. */
	public static io.github.taoguan.luaj.LuaValue s_metatable;

	public int type() {
		return TFUNCTION;
	}
	
	public String typename() {
		return "function";
	}
	
	public boolean isfunction() {
		return true;
	}

	public LuaFunction checkfunction()  {
		return this;
	}
	
	public LuaFunction optfunction(LuaFunction defval) {
		return this; 
	}

	public io.github.taoguan.luaj.LuaValue getmetatable() {
		return s_metatable; 
	}

	public String tojstring() {
		return "function: " + classnamestub();
	}

	public io.github.taoguan.luaj.LuaString strvalue() {
		return valueOf(tojstring());
	}

	/** Return the last part of the class name, to be used as a function name in tojstring and elsewhere. 
	 * @return String naming the last part of the class name after the last dot (.) or dollar sign ($).
	 */
	public String classnamestub() {
		String s = getClass().getName();
		return s.substring(Math.max(s.lastIndexOf('.'),s.lastIndexOf('$'))+1);
	}
	
	/** Return a human-readable name for this function.  Returns the last part of the class name by default.
	 * Is overridden by LuaClosure to return the source file and line, and by LibFunctions to return the name.
	 * @return common name for this function.  */
	public String name() {
		return classnamestub();
	}
}
