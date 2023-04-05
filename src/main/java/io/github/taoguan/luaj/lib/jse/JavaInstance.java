package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

import java.lang.reflect.Field;

/**
 * LuaValue that represents a Java instance.
 * <p>
 * Will respond to get() and set() by returning field values or methods. 
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when a subclass of Object is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class JavaInstance extends io.github.taoguan.luaj.LuaUserdata {

	JavaClass jclass;
	
	public JavaInstance(Object instance) {
		super(instance);
	}

	public io.github.taoguan.luaj.LuaValue get(io.github.taoguan.luaj.LuaValue key) {
		if ( jclass == null )
			jclass = JavaClass.forClass(m_instance.getClass());
		Field f = jclass.getField(key);
		if ( f != null )
			try {
				return CoerceJavaToLua.coerce(f.get(m_instance));
			} catch (Exception e) {
				throw new LuaError(e);
			}
		io.github.taoguan.luaj.LuaValue m = jclass.getMethod(key);
		if ( m != null )
			return m;
		Class c = jclass.getInnerClass(key);
		if ( c != null )
			return JavaClass.forClass(c);
		return super.get(key);
	}

	public void set(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value) {
		if ( jclass == null )
			jclass = JavaClass.forClass(m_instance.getClass());
		Field f = jclass.getField(key);
		if ( f != null )
			try {
				f.set(m_instance, CoerceLuaToJava.coerce(value, f.getType()));
				return;
			} catch (Exception e) {
				throw new LuaError(e);
			}
		super.set(key, value);
	} 	
	
}
