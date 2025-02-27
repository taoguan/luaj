package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LuaValue that represents a Java class.
 * <p>
 * Will respond to get() and set() by returning field values, or java methods. 
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when a Class is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class JavaClass extends JavaInstance implements CoerceJavaToLua.Coercion {

	static final Map<Class, JavaClass> classes = new ConcurrentHashMap();

	static final LuaValue NEW = valueOf("new");

	Map fields;
	Map methods;
	Map innerclasses;

	public static JavaClass forClass(Class c) {
		JavaClass j = classes.get(c);
		if ( j == null ) {
			JavaClass present = classes.putIfAbsent(c, j = new JavaClass(c));
			if(present != null){
				j = present;
			}
		}
		return j;
	}
	
	JavaClass(Class c) {
		super(c);
		this.jclass = this;
	}

	public LuaValue coerce(Object javaValue) {
		return this;
	}
		
	Field getField(LuaValue key) {
		if ( fields == null ) {
			Map m = new HashMap();
			Field[] f = ((Class)m_instance).getFields();
			for ( int i=0; i<f.length; i++ ) {
				Field fi = f[i];
				if ( Modifier.isPublic(fi.getModifiers()) ) {
					m.put(LuaValue.valueOf(fi.getName()), fi);
					try {
						if (!fi.isAccessible())
							fi.setAccessible(true);
					} catch (SecurityException s) {
					}
				}
			}
			fields = m;
		}
		return (Field) fields.get(key);
	}
	
	LuaValue getMethod(LuaValue key) {
		if ( methods == null ) {
			Map namedlists = new HashMap();
			Method[] m = ((Class)m_instance).getMethods();
			for ( int i=0; i<m.length; i++ ) {
				Method mi = m[i];
				if ( Modifier.isPublic( mi.getModifiers()) ) {
					String name = mi.getName();
					List list = (List) namedlists.get(name);
					if ( list == null )
						namedlists.put(name, list = new ArrayList());
					list.add( JavaMethod.forMethod(mi) );
				}
			}
			Map map = new HashMap();
			Constructor[] c = ((Class)m_instance).getConstructors();
			List list = new ArrayList();
			for ( int i=0; i<c.length; i++ ) 
				if ( Modifier.isPublic(c[i].getModifiers()) )
					list.add( JavaConstructor.forConstructor(c[i]) );
			switch ( list.size() ) {
			case 0: break;
			case 1: map.put(NEW, list.get(0)); break;
			default: map.put(NEW, JavaConstructor.forConstructors( (JavaConstructor[])list.toArray(new JavaConstructor[list.size()]) ) ); break;
			}
			
			for ( Iterator it=namedlists.entrySet().iterator(); it.hasNext(); ) {
				Entry e = (Entry) it.next();
				String name = (String) e.getKey();
				List methods = (List) e.getValue();
				map.put( LuaValue.valueOf(name),
					methods.size()==1? 
						methods.get(0): 
						JavaMethod.forMethods( (JavaMethod[])methods.toArray(new JavaMethod[methods.size()])) );
			}
			methods = map;
		}
		return (LuaValue) methods.get(key);
	}
	
	Class getInnerClass(LuaValue key) {
		if ( innerclasses == null ) {
			Map m = new HashMap();
			Class[] c = ((Class)m_instance).getClasses();
			for ( int i=0; i<c.length; i++ ) {
				Class ci = c[i];
				String name = ci.getName();
				String stub = name.substring(Math.max(name.lastIndexOf('$'), name.lastIndexOf('.'))+1);
				m.put(LuaValue.valueOf(stub), ci);
			}
			innerclasses = m;
		}
		return (Class) innerclasses.get(key);
	}

	public LuaValue getConstructor() {
		return getMethod(NEW);
	}
}
