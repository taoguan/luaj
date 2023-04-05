package io.github.taoguan.luaj.lib.jse;


import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.compiler.LuaC;
import io.github.taoguan.luaj.lib.*;

import java.lang.reflect.*;

/**
 * Subclass of {@link LibFunction} which implements the features of the luajava package.
 * <p>
 * Luajava is an approach to mixing lua and java using simple functions that bind
 * java classes and methods to lua dynamically.
 * 
 * <p>
 * Typically, this library is included as part of a call to
 * {@link JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("luajava").get("bindClass").call( LuaValue.valueOf("java.lang.System") ).invokeMethod("currentTimeMillis") );
 * } </pre>
 * <p>
 * To instantiate and use it directly,
 * link it into your globals table via {@link io.github.taoguan.luaj.Globals#load} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new LuajavaLib());
 * globals.load(
 *      "sys = luajava.bindClass('java.lang.System')\n"+
 *      "print ( sys:currentTimeMillis() )\n", "main.lua" ).call();
 * } </pre>
 * <p>
 * 
 * The {@code luajava} library is available
 * on all JSE platforms via the call to {@link JsePlatform#standardGlobals()}
 * and the luajava api's are simply invoked from lua.
 * Because it makes extensive use of Java's reflection API, it is not available
 * on JME, but can be used in Android applications.
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * 
 * @see LibFunction
 * @see JsePlatform
 * @see LuaC
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class LuajavaLib extends VarArgFunction {

	static final int INIT           = 0;
	static final int BINDCLASS      = 1;
	static final int NEWINSTANCE	= 2;
	static final int NEW			= 3;
	static final int CREATEPROXY	= 4;
	static final int LOADLIB		= 5;

	static final String[] NAMES = {
		"bindClass",
		"newInstance",
		"new",
		"createProxy",
		"loadLib",
	};
	
	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	public LuajavaLib() {
	}

	public Varargs invoke(Varargs args) {
		try {
			switch ( opcode ) {
			case INIT: {
				// LuaValue modname = args.arg1();
				io.github.taoguan.luaj.LuaValue env = args.arg(2);
				LuaTable t = new LuaTable();
				bind( t, this.getClass(), NAMES, BINDCLASS );
				env.set("luajava", t);
				if (!env.get("package").isnil()) env.get("package").get("loaded").set("luajava", t);
				return t;
			}
			case BINDCLASS: {
				final Class clazz = classForName(args.checkjstring(1));
				return JavaClass.forClass(clazz);
			}
			case NEWINSTANCE:
			case NEW: {
				// get constructor
				final io.github.taoguan.luaj.LuaValue c = args.checkvalue(1);
				final Class clazz = (opcode==NEWINSTANCE? classForName(c.tojstring()): (Class) c.checkuserdata(Class.class));
				final Varargs consargs = args.subargs(2);
				return JavaClass.forClass(clazz).getConstructor().invoke(consargs);
			}
				
			case CREATEPROXY: {
				final int niface = args.narg()-1;
				if ( niface <= 0 )
					throw new LuaError("no interfaces");
				final io.github.taoguan.luaj.LuaValue lobj = args.checktable(niface+1);
				
				// get the interfaces
				final Class[] ifaces = new Class[niface];
				for ( int i=0; i<niface; i++ )
					ifaces[i] = classForName(args.checkjstring(i+1));
				
				// create the invocation handler
				InvocationHandler handler = new ProxyInvocationHandler(lobj);
				
				// create the proxy object
				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
				
				// return the proxy
				return io.github.taoguan.luaj.LuaValue.userdataOf( proxy );
			}
			case LOADLIB: {
				// get constructor
				String classname = args.checkjstring(1);
				String methodname = args.checkjstring(2);
				Class clazz = classForName(classname);
				Method method = clazz.getMethod(methodname, new Class[] {});
				Object result = method.invoke(clazz, new Object[] {});
				if ( result instanceof io.github.taoguan.luaj.LuaValue) {
					return (io.github.taoguan.luaj.LuaValue) result;
				} else {
					return NIL;
				}
			}
			default:
				throw new LuaError("not yet supported: "+this);
			}
		} catch (LuaError e) {
			throw e;
		} catch (InvocationTargetException ite) {
			throw new LuaError(ite.getTargetException());
		} catch (Exception e) {
			throw new LuaError(e);
		}
	}

	// load classes using app loader to allow luaj to be used as an extension
	protected Class classForName(String name) throws ClassNotFoundException {
		return Class.forName(name, true, ClassLoader.getSystemClassLoader());
	}
	
	private static final class ProxyInvocationHandler implements InvocationHandler {
		private final io.github.taoguan.luaj.LuaValue lobj;

		private ProxyInvocationHandler(io.github.taoguan.luaj.LuaValue lobj) {
			this.lobj = lobj;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			io.github.taoguan.luaj.LuaValue func = lobj.get(name);
			if ( func.isnil() )
				return null;
			boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
			int n = args!=null? args.length: 0;
			io.github.taoguan.luaj.LuaValue[] v;
			if ( isvarargs ) {
				Object o = args[--n];
				int m = Array.getLength( o );
				v = new io.github.taoguan.luaj.LuaValue[n+m];
				for ( int i=0; i<n; i++ )
					v[i] = CoerceJavaToLua.coerce(args[i]);
				for ( int i=0; i<m; i++ )
					v[i+n] = CoerceJavaToLua.coerce(Array.get(o,i));
			} else {
				v = new io.github.taoguan.luaj.LuaValue[n];
				for ( int i=0; i<n; i++ )
					v[i] = CoerceJavaToLua.coerce(args[i]);
			}
			io.github.taoguan.luaj.LuaValue result = func.invoke(v).arg1();
			return CoerceLuaToJava.coerce(result, method.getReturnType());
		}
	}
	
}