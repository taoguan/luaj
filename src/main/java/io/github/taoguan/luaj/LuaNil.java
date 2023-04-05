package io.github.taoguan.luaj;

/**
 * Class to encapsulate behavior of the singleton instance {@code nil} 
 * <p>
 * There will be one instance of this class, {@link LuaValue#NIL},
 * per Java virtual machine.  
 * However, the {@link Varargs} instance {@link LuaValue#NONE}
 * which is the empty list, 
 * is also considered treated as a nil value by default.  
 * <p>
 * Although it is possible to test for nil using Java == operator, 
 * the recommended approach is to use the method {@link LuaValue#isnil()} 
 * instead.  By using that any ambiguities between 
 * {@link LuaValue#NIL} and {@link LuaValue#NONE} are avoided.
 * @see LuaValue
 * @see LuaValue#NIL
 */
public class LuaNil extends LuaValue {
	
	static final LuaNil _NIL = new LuaNil();
	
	public static LuaValue s_metatable;
	
	LuaNil() {}

	public int type() {
		return LuaValue.TNIL;
	}

	public String toString() {
		return "nil";		
	}
	
	public String typename() {
		return "nil";
	}
	
	public String tojstring() {
		return "nil";
	}

	public LuaValue not()  { 
		return LuaValue.TRUE;  
	}
	
	public boolean toboolean() { 
		return false; 
	}
	
	public boolean isnil() {
		return true;
	}
		
	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public boolean equals(Object o) {
		return o instanceof LuaNil;
	}

	public LuaValue checknotnil() { 
		return argerror("value");
	}
	
	public boolean isvalidkey() {
		return false;
	}

	// optional argument conversions - nil alwas falls badk to default value
	public boolean     optboolean(boolean defval)          { return defval; }
	public LuaClosure  optclosure(LuaClosure defval)       { return defval; }
	public double      optdouble(double defval)               { return defval; }
	public LuaFunction optfunction(LuaFunction defval)     { return defval; }
	public int         optint(int defval)                  { return defval; }
	public LuaInteger  optinteger(LuaInteger defval)       { return defval; }
	public long        optlong(long defval)                { return defval; }
	public LuaNumber   optnumber(LuaNumber defval)         { return defval; }
	public LuaTable    opttable(LuaTable defval)           { return defval; }
	public LuaThread   optthread(LuaThread defval)         { return defval; }
	public String      optjstring(String defval)            { return defval; }
	public LuaString optstring(LuaString defval)         { return defval; }
	public Object      optuserdata(Object defval)          { return defval; }
	public Object      optuserdata(Class c, Object defval) { return defval; }
	public LuaValue    optvalue(LuaValue defval)           { return defval; }
}
