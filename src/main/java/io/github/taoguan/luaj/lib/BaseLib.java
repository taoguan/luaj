package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.jse.JseBaseLib;
import io.github.taoguan.luaj.lib.jse.JsePlatform;
import io.github.taoguan.luaj.vm.LuaInstruction;
import io.github.taoguan.luaj.lib.BaseLib.error;

import java.io.IOException;
import java.io.InputStream;

/** 
 * Subclass of {@link LibFunction} which implements the lua basic library functions. 
 * <p>
 * This contains all library functions listed as "basic functions" in the lua documentation for JME. 
 * The functions dofile and loadfile use the 
 * {@link io.github.taoguan.luaj.Globals#finder} instance to find resource files.
 * Since JME has no file system by default, {@link BaseLib} implements 
 * {@link ResourceFinder} using {@link Class#getResource(String)}, 
 * which is the closest equivalent on JME.     
 * The default loader chain in {@link PackageLib} will use these as well.
 * <p>  
 * To use basic library functions that include a {@link ResourceFinder} based on 
 * directory lookup, use {@link JseBaseLib} instead.
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#standardGlobals()} or
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * <p>
 * For special cases where the smallest possible footprint is desired, 
 * a minimal set of libraries could be loaded
 * directly via {@link io.github.taoguan.luaj.Globals#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * Doing so will ensure the library is properly initialized 
 * and loaded into the globals table. 
 * <p>
 * This is a direct port of the corresponding library in C.
 * @see io.github.taoguan.luaj.lib.jse.JseBaseLib
 * @see ResourceFinder
 * @see io.github.taoguan.luaj.Globals#finder
 * @see LibFunction
 * @see io.github.taoguan.luaj.lib.jse.JsePlatform
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.1">Lua 5.3 Base Lib Reference</a>
 */
public class BaseLib extends TwoArgFunction implements ResourceFinder {
	
	io.github.taoguan.luaj.Globals globals;
	

	/** Perform one-time initialization on the library by adding base functions
	 * to the supplied environment, and returning it as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, which must be a Globals instance.
	 */
	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue modname, io.github.taoguan.luaj.LuaValue env) {
		globals = env.checkglobals();
		globals.finder = this;
		globals.baselib = this;
		env.set( "_G", env );
		env.set( "_VERSION", LuaInstruction._VERSION );
		env.set("assert", new _assert());
		env.set("collectgarbage", new collectgarbage());
		env.set("dofile", new dofile());
		env.set("error", new error());
		env.set("getmetatable", new getmetatable());
		env.set("load", new load());
		env.set("loadfile", new loadfile());
		env.set("pcall", new pcall());
		env.set("print", new print(this));
		env.set("rawequal", new rawequal());
		env.set("rawget", new rawget());
		env.set("rawlen", new rawlen());
		env.set("rawset", new rawset());
		env.set("select", new select());
		env.set("setmetatable", new setmetatable());
		env.set("tonumber", new tonumber());
		env.set("tostring", new tostring());
		env.set("type", new type());
		env.set("xpcall", new xpcall());

		next next;
		env.set("next", next = new next());
		env.set("pairs", new pairs(next));
		env.set("ipairs", new ipairs());
		
		return env;
	}

	/** ResourceFinder implementation 
	 * 
	 * Tries to open the file as a resource, which can work for JSE and JME. 
	 */
	public InputStream findResource(String filename) {
		return getClass().getResourceAsStream(filename.startsWith("/")? filename: "/"+filename);
	}

	
	// "assert", // ( v [,message] ) -> v, message | ERR
	static final class _assert extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			if ( !args.arg1().toboolean() ) 
				error( args.narg()>1? args.optjstring(2,"assertion failed!"): "assertion failed!" );
			return args;
		}
	}

	// "collectgarbage", // ( opt [,arg] ) -> value
	static final class collectgarbage extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			String s = args.optjstring(1, "collect");
			if ( "collect".equals(s) ) {
				System.gc();
				return ZERO;
			} else if ( "count".equals(s) ) {
				Runtime rt = Runtime.getRuntime();
				long used = rt.totalMemory() - rt.freeMemory();
				return varargsOf(valueOf(used/1024.), valueOf(used%1024));
			} else if ( "step".equals(s) ) {
				System.gc();
				return io.github.taoguan.luaj.LuaValue.TRUE;
			} else {
				this.argerror("gc op");
			}
			return NIL;
		}
	}

	// "dofile", // ( filename ) -> result1, ...
	final class dofile extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil");
			String filename = args.isstring(1)? args.tojstring(1): null;
			io.github.taoguan.luaj.Varargs v = filename == null?
					loadStream( globals.STDIN, "=stdin", "bt", globals ):
					loadFile( args.checkjstring(1), "bt", globals );
			return v.isnil(1)? error(v.tojstring(2)): v.arg1().invoke();			
		}
	}

	// "error", // ( message [,level] ) -> ERR
	static final class error extends TwoArgFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
			throw arg1.isnil()? new io.github.taoguan.luaj.LuaError(null, arg2.optint(1)):
				arg1.isstring()? new io.github.taoguan.luaj.LuaError(arg1.tojstring(), arg2.optint(1)):
					new io.github.taoguan.luaj.LuaError(arg1);
		}
	}

	// "getmetatable", // ( object ) -> table 
	static final class getmetatable extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return argerror(1, "value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			io.github.taoguan.luaj.LuaValue mt = arg.getmetatable();
			return mt!=null? mt.rawget(METATABLE).optvalue(mt): NIL;
		}
	}
	// "load", // ( ld [, source [, mode [, env]]] ) -> chunk | nil, msg
	final class load extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue ld = args.arg1();
			args.argcheck(ld.isstring() || ld.isfunction(), 1, "ld must be string or function");
			String source = args.optjstring(2, ld.isstring()? ld.tojstring(): "=(load)");
			String mode = args.optjstring(3, "bt");
			io.github.taoguan.luaj.LuaValue env = args.optvalue(4, globals);
			return loadStream(ld.isstring()? ld.strvalue().toInputStream(): 
				new StringInputStream(ld.checkfunction()), source, mode, env);
		}
	}

	// "loadfile", // ( [filename [, mode [, env]]] ) -> chunk | nil, msg
	final class loadfile extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil");
			String filename = args.isstring(1)? args.tojstring(1): null;
			String mode = args.optjstring(2, "bt");
			io.github.taoguan.luaj.LuaValue env = args.optvalue(3, globals);
			return filename == null? 
				loadStream( globals.STDIN, "=stdin", mode, env ):
				loadFile( filename, mode, env );
		}
	}
		
	// "pcall", // (f, arg1, ...) -> status, result1, ...
	final class pcall extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue func = args.checkvalue(1);
			if (globals != null && globals.debuglib != null)
				globals.debuglib.onCall(this);
			try {
				return varargsOf(TRUE, func.invoke(args.subargs(2)));
			} catch ( io.github.taoguan.luaj.LuaError le ) {
				final io.github.taoguan.luaj.LuaValue m = le.getMessageObject();
				return varargsOf(FALSE, m!=null? m: NIL);
			} catch ( Exception e ) {
				final String m = e.getMessage();
				return varargsOf(FALSE, valueOf(m!=null? m: e.toString()));
			} finally {
				if (globals != null && globals.debuglib != null)
					globals.debuglib.onReturn();
			}
		}
	}

	// "print", // (...) -> void
	final class print extends VarArgFunction {
		final BaseLib baselib;
		print(BaseLib baselib) {
			this.baselib = baselib;
		}
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue tostring = globals.get("tostring");
			for ( int i=1, n=args.narg(); i<=n; i++ ) {
				if ( i>1 ) globals.STDOUT.print( " \t" );
				io.github.taoguan.luaj.LuaString s = tostring.call( args.arg(i) ).strvalue();
				globals.STDOUT.print(s.tojstring());
			}
			globals.STDOUT.println();
			return NONE;
		}
	}
	

	// "rawequal", // (v1, v2) -> boolean
	static final class rawequal extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return argerror(1, "value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			return argerror(2, "value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
			return valueOf(arg1.raweq(arg2));
		}
	}

	// "rawget", // (table, index) -> value
	static final class rawget extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return argerror(1, "value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			return argerror(2, "value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg1, io.github.taoguan.luaj.LuaValue arg2) {
			return arg1.checktable().rawget(arg2);
		}
	}

	
	// "rawlen", // (v) -> value
	static final class rawlen extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			return valueOf(arg.rawlen());
		}
	}

	// "rawset", // (table, index, value) -> table
	static final class rawset extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue table) {
			return argerror(2,"value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue table, io.github.taoguan.luaj.LuaValue index) {
			return argerror(3,"value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue table, io.github.taoguan.luaj.LuaValue index, io.github.taoguan.luaj.LuaValue value) {
			io.github.taoguan.luaj.LuaTable t = table.checktable();
			if (!index.isvalidkey()) argerror(2, "value");
			t.rawset(index, value);
			return t;
		}
	}
	
	// "select", // (f, ...) -> value1, ...
	static final class select extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int n = args.narg()-1; 				
			if ( args.arg1().equals(valueOf("#")) )
				return valueOf(n);
			int i = args.checkint(1);
			if ( i == 0 || i < -n )
				argerror(1,"index out of range");
			return args.subargs(i<0? n+i+2: i+1);
		}
	}
	
	// "setmetatable", // (table, metatable) -> table
	static final class setmetatable extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue table) {
			return argerror(2,"value");
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue table, io.github.taoguan.luaj.LuaValue metatable) {
			final io.github.taoguan.luaj.LuaValue mt0 = table.checktable().getmetatable();
			if ( mt0!=null && !mt0.rawget(METATABLE).isnil() )
				error("cannot change a protected metatable");
			return table.setmetatable(metatable.isnil()? null: metatable.checktable());
		}
	}
	
	// "tonumber", // (e [,base]) -> value
	static final class tonumber extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue e) {
			return e.tonumber();
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue e, io.github.taoguan.luaj.LuaValue base) {
			if (base.isnil())
				return e.tonumber();
			final int b = base.checkint();
			if ( b < 2 || b > 36 )
				argerror(2, "base out of range");
			return e.checkstring().tonumber(b);
		}
	}
	
	// "tostring", // (e) -> value
	static final class tostring extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			io.github.taoguan.luaj.LuaValue h = arg.metatag(TOSTRING);
			if ( ! h.isnil() ) 
				return h.call(arg);
			io.github.taoguan.luaj.LuaValue v = arg.tostring();
			if ( ! v.isnil() ) 
				return v;
			return valueOf(arg.tojstring());
		}
	}

	// "type",  // (v) -> value
	static final class type extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue arg) {
			return valueOf(arg.typename());
		}
	}

	// "xpcall", // (f, err) -> result1, ...				
	final class xpcall extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			final io.github.taoguan.luaj.LuaThread t = globals.running;
			final io.github.taoguan.luaj.LuaValue preverror = t.errorfunc;
			t.errorfunc = args.checkvalue(2);
			try {
				if (globals != null && globals.debuglib != null)
					globals.debuglib.onCall(this);
				try {
					return varargsOf(TRUE, args.arg1().invoke(args.subargs(3)));
				} catch ( io.github.taoguan.luaj.LuaError le ) {
					final io.github.taoguan.luaj.LuaValue m = le.getMessageObject();
					return varargsOf(FALSE, m!=null? m: NIL);
				} catch ( Exception e ) {
					final String m = e.getMessage();
					return varargsOf(FALSE, valueOf(m!=null? m: e.toString()));
				} finally {
					if (globals != null && globals.debuglib != null)
						globals.debuglib.onReturn();
				}
			} finally {
				t.errorfunc = preverror;
			}
		}
	}
	
	// "pairs" (t) -> iter-func, t, nil
	static final class pairs extends VarArgFunction {
		final next next;
		pairs(next next) {
			this.next = next;
		}
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
				return varargsOf( next, args.checktable(1), NIL );
		}
	}
	
	// // "ipairs", // (t) -> iter-func, t, 0
	static final class ipairs extends VarArgFunction {
		inext inext = new inext();
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			return varargsOf( inext, args.checktable(1), ZERO );
		}
	}
	
	// "next"  ( table, [index] ) -> next-index, next-value
	static final class next extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			return args.checktable(1).next(args.arg(2));
		}
	}
	
	// "inext" ( table, [int-index] ) -> next-index, next-value
	static final class inext extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			return args.checktable(1).inext(args.arg(2));
		}
	}
	
	/** 
	 * Load from a named file, returning the chunk or nil,error of can't load
	 * @param env 
	 * @param mode 
	 * @return Varargs containing chunk, or NIL,error-text on error
	 */
	public io.github.taoguan.luaj.Varargs loadFile(String filename, String mode, io.github.taoguan.luaj.LuaValue env) {
		InputStream is = globals.finder.findResource(filename);
		if ( is == null )
			return varargsOf(NIL, valueOf("cannot open "+filename+": No such file or directory"));
		try {
			return loadStream(is, "@"+filename, mode, env);
		} finally {
			try {
				is.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	public io.github.taoguan.luaj.Varargs loadStream(InputStream is, String chunkname, String mode, io.github.taoguan.luaj.LuaValue env) {
		try {
			if ( is == null )
				return varargsOf(NIL, valueOf("not found: "+chunkname));
			return globals.load(is, chunkname, mode, env);
		} catch (Exception e) {
			return varargsOf(NIL, valueOf(e.getMessage()));
		}
	}
	
	
	private static class StringInputStream extends InputStream {
		final io.github.taoguan.luaj.LuaValue func;
		byte[] bytes; 
		int offset, remaining = 0;
		StringInputStream(io.github.taoguan.luaj.LuaValue func) {
			this.func = func;
		}
		public int read() throws IOException {
			if ( remaining <= 0 ) {
				io.github.taoguan.luaj.LuaValue s = func.call();
				if ( s.isnil() )
					return -1;
				io.github.taoguan.luaj.LuaString ls = s.strvalue();
				bytes = ls.m_bytes;
				offset = ls.m_offset;
				remaining = ls.m_length;
				if (remaining <= 0)
					return -1;
			}
			--remaining;
			return bytes[offset++];
		}
	}
}
