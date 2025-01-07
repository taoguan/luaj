package io.github.taoguan.luaj;

/**
 * Base class for representing numbers as lua values directly. 
 * <p>
 * The main subclasses are {@link io.github.taoguan.luaj.LuaInteger} which holds values that fit in a java int,
 * and {@link LuaDouble} which holds all other number values.
 * @see io.github.taoguan.luaj.LuaInteger
 * @see LuaDouble
 * @see io.github.taoguan.luaj.LuaValue
 * 
 */
abstract
public class LuaNumber extends LuaValue {

	/** Shared static metatable for all number values represented in lua. */
	public static LuaValue s_metatable;
	
	public int type() {
		return TNUMBER;
	}
	
	public String typename() {
		return "number";
	}
	
	public LuaNumber checknumber() {
		return this; 
	}
	
	public LuaNumber checknumber(String errmsg) {
		return this; 
	}
	
	public LuaNumber optnumber(LuaNumber defval) {
		return this; 
	}
	
	public LuaValue tonumber() {
		return this;
	}
	
	public boolean isnumber() {
		return true;
	}
	
	public boolean isstring() {
		return true;
	}
	
	public LuaValue getmetatable() {
		return s_metatable; 
	}

	public LuaValue concat(LuaValue rhs)      { return rhs.concatTo(this); }
	public Buffer concat(Buffer rhs)        { return rhs.concatTo(this); }
	public LuaValue concatTo(LuaNumber lhs)   { return strvalue().concatTo(lhs.strvalue()); }
	public LuaValue concatTo(LuaString lhs)   { return strvalue().concatTo(lhs); }

}
