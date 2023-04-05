package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

import java.lang.reflect.Array;

/**
 * LuaValue that represents a Java instance of array type.
 * <p>
 * Can get elements by their integer key index, as well as the length.
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when an array is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class JavaArray extends io.github.taoguan.luaj.LuaUserdata {

	private static final class LenFunction extends OneArgFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue u) {
			return io.github.taoguan.luaj.LuaValue.valueOf(Array.getLength(((io.github.taoguan.luaj.LuaUserdata)u).m_instance));
		}
	}

	static final io.github.taoguan.luaj.LuaValue LENGTH = valueOf("length");
	
	static final io.github.taoguan.luaj.LuaTable array_metatable;
	static {
		array_metatable = new io.github.taoguan.luaj.LuaTable();
		array_metatable.rawset(io.github.taoguan.luaj.LuaValue.LEN, new LenFunction());
	}
	
	JavaArray(Object instance) {
		super(instance);
		setmetatable(array_metatable);
	}
	
	public io.github.taoguan.luaj.LuaValue get(io.github.taoguan.luaj.LuaValue key) {
		if ( key.equals(LENGTH) )
			return valueOf(Array.getLength(m_instance));
		if ( key.islong() ) {
			int i = (int)(key.tolong() - 1);
			return i>=0 && i<Array.getLength(m_instance)?
				CoerceJavaToLua.coerce(Array.get(m_instance,i)):
				NIL;
		}
		return super.get(key);
	}

	public void set(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value) {
		if ( key.islong() ) {
			int i = (int)(key.tolong() - 1);
			if ( i>=0 && i<Array.getLength(m_instance) )
				Array.set(m_instance,i,CoerceLuaToJava.coerce(value, m_instance.getClass().getComponentType()));
			else if ( m_metatable==null || ! settable(this,key,value) )
					error("array index out of bounds");
		}
		else
			super.set(key, value);
	} 	
}
