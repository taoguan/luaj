package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.vm.LuaInstruction;
import io.github.taoguan.luaj.vm.OpCode;

/**
 * Subclass of {@link LibFunction} which implements the lua standard {@code debug}
 * library.
 * <p>
 * The debug library in luaj tries to emulate the behavior of the corresponding C-based lua library.
 * To do this, it must maintain a separate stack of calls to {@link LuaClosure} and {@link LibFunction}
 * instances.
 * Especially when lua-to-java bytecode compiling is being used
 * via a {@link io.github.taoguan.luaj.Globals.Compiler} such as {@link io.github.taoguan.luaj.luajc.LuaJC},
 * this cannot be done in all cases.
 * <p>
 * Typically, this library is included as part of a call to either
 * {@link io.github.taoguan.luaj.lib.jse.JsePlatform#debugGlobals()} or
 * <pre> {@code
 * Globals globals = JsePlatform.debugGlobals();
 * System.out.println( globals.get("debug").get("traceback").call() );
 * } </pre>
 * <p>
 * To instantiate and use it directly,
 * link it into your globals table via {@link io.github.taoguan.luaj.LuaValue#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new DebugLib());
 * System.out.println( globals.get("debug").get("traceback").call() );
 * } </pre>
 * <p>
 * This library exposes the entire state of lua code, and provides method to see and modify
 * all underlying lua values within a Java VM so should not be exposed to client code
 * in a shared server environment.
 * 
 * @see LibFunction
 * @see io.github.taoguan.luaj.lib.jse.JsePlatform
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.10">Lua 5.3 Debug Lib Reference</a>
 */
public class DebugLib extends TwoArgFunction {
	public static boolean CALLS;
	public static boolean TRACE;
	static {
		try { CALLS = (null != System.getProperty("CALLS")); } catch (Exception e) {}
		try { TRACE = (null != System.getProperty("TRACE")); } catch (Exception e) {}
	}
	
	static final io.github.taoguan.luaj.LuaString LUA             = valueOf("Lua");
	private static final io.github.taoguan.luaj.LuaString QMARK           = valueOf("?");
	private static final io.github.taoguan.luaj.LuaString CALL            = valueOf("call");
	private static final io.github.taoguan.luaj.LuaString LINE            = valueOf("line");
	private static final io.github.taoguan.luaj.LuaString COUNT           = valueOf("count");
	private static final io.github.taoguan.luaj.LuaString RETURN          = valueOf("return");
	
	static final io.github.taoguan.luaj.LuaString FUNC            = valueOf("func");
	static final io.github.taoguan.luaj.LuaString ISTAILCALL      = valueOf("istailcall");
	static final io.github.taoguan.luaj.LuaString ISVARARG        = valueOf("isvararg");
	static final io.github.taoguan.luaj.LuaString NUPS            = valueOf("nups");
	static final io.github.taoguan.luaj.LuaString NPARAMS         = valueOf("nparams");
	static final io.github.taoguan.luaj.LuaString NAME            = valueOf("name");
	static final io.github.taoguan.luaj.LuaString NAMEWHAT        = valueOf("namewhat");
	static final io.github.taoguan.luaj.LuaString WHAT            = valueOf("what");
	static final io.github.taoguan.luaj.LuaString SOURCE          = valueOf("source");
	static final io.github.taoguan.luaj.LuaString SHORT_SRC       = valueOf("short_src");
	static final io.github.taoguan.luaj.LuaString LINEDEFINED     = valueOf("linedefined");
	static final io.github.taoguan.luaj.LuaString LASTLINEDEFINED = valueOf("lastlinedefined");
	static final io.github.taoguan.luaj.LuaString CURRENTLINE     = valueOf("currentline");
	static final io.github.taoguan.luaj.LuaString ACTIVELINES     = valueOf("activelines");

	io.github.taoguan.luaj.Globals globals;
	
	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, which must be a Globals instance.
	 */
	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue modname, io.github.taoguan.luaj.LuaValue env) {
		globals = env.checkglobals();
		globals.debuglib = this;
		io.github.taoguan.luaj.LuaTable debug = new io.github.taoguan.luaj.LuaTable();
		debug.set("debug", new debug());
		debug.set("gethook", new gethook());
		debug.set("getinfo", new getinfo());
		debug.set("getlocal", new getlocal());
		debug.set("getmetatable", new getmetatable());
		debug.set("getregistry", new getregistry());
		debug.set("getupvalue", new getupvalue());
		debug.set("getuservalue", new getuservalue());
		debug.set("sethook", new sethook());
		debug.set("setlocal", new setlocal());
		debug.set("setmetatable", new setmetatable());
		debug.set("setupvalue", new setupvalue());
		debug.set("setuservalue", new setuservalue());
		debug.set("traceback", new traceback());
		debug.set("upvalueid", new upvalueid());
		debug.set("upvaluejoin", new upvaluejoin());
		env.set("debug", debug);
		if (!env.get("package").isnil()) env.get("package").get("loaded").set("debug", debug);
		return debug;
	}

	// debug.debug()
	static final class debug extends ZeroArgFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return NONE;
		}
	}

	// debug.gethook ([thread])
	final class gethook extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			LuaThread t = args.narg() > 0 ? args.checkthread(1): globals.running;
			LuaThread.State s = t.state;
			return varargsOf(
					s.hookfunc != null? s.hookfunc: NIL,
					valueOf((s.hookcall?"c":"")+(s.hookline?"l":"")+(s.hookrtrn?"r":"")),
					valueOf(s.hookcount));
		}
	}

	//	debug.getinfo ([thread,] f [, what])
	final class getinfo extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running;
			io.github.taoguan.luaj.LuaValue func = args.arg(a++);
			String what = args.optjstring(a++, "flnStu");
			CallStack callstack = callstack(thread);

			// find the stack info
			CallFrame frame;
			if ( func.isnumber() ) {
				frame = callstack.getCallFrame(func.toint());
				if (frame == null)
					return NONE;
				func = frame.f;
			} else if ( func.isfunction() ) {
				frame = callstack.findCallFrame(func);
			} else {
				return argerror(a-2, "function or level");
			}

			// start a table
			DebugInfo ar = callstack.auxgetinfo(what, (io.github.taoguan.luaj.LuaFunction) func, frame);
			io.github.taoguan.luaj.LuaTable info = new io.github.taoguan.luaj.LuaTable();
			if (what.indexOf('S') >= 0) {
				info.set(WHAT, LUA);
				info.set(SOURCE, valueOf(ar.source));
				info.set(SHORT_SRC, valueOf(ar.short_src));
				info.set(LINEDEFINED, valueOf(ar.linedefined));
				info.set(LASTLINEDEFINED, valueOf(ar.lastlinedefined));
			}
			if (what.indexOf('l') >= 0) {
				info.set( CURRENTLINE, valueOf(ar.currentline) );
			}
			if (what.indexOf('u') >= 0) {
				info.set(NUPS, valueOf(ar.nups));
				info.set(NPARAMS, valueOf(ar.nparams));
				info.set(ISVARARG, ar.isvararg? ONE: ZERO);
			}
			if (what.indexOf('n') >= 0) {
				info.set(NAME, io.github.taoguan.luaj.LuaValue.valueOf(ar.name!=null? ar.name: "?"));
				info.set(NAMEWHAT, io.github.taoguan.luaj.LuaValue.valueOf(ar.namewhat));
			}
			if (what.indexOf('t') >= 0) {
				info.set(ISTAILCALL, ZERO);
			}
			if (what.indexOf('L') >= 0) {
				io.github.taoguan.luaj.LuaTable lines = new io.github.taoguan.luaj.LuaTable();
				info.set(ACTIVELINES, lines);
				CallFrame cf;
				for (int l = 1; (cf=callstack.getCallFrame(l)) != null; ++l)
					if (cf.f == func)
						lines.insert(-1, valueOf(cf.currentline()));
			}
			if (what.indexOf('f') >= 0) {
				if (func != null)
					info.set( FUNC, func );
			}
			return info;
		}
	}

	//	debug.getlocal ([thread,] f, local)
	final class getlocal extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running;
			int level = args.checkint(a++);
			int local = args.checkint(a++);
			CallFrame f = callstack(thread).getCallFrame(level);
			return f != null? f.getLocal(local): NONE;
		}
	}

	//	debug.getmetatable (value)
	static final class getmetatable extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue v) {
			io.github.taoguan.luaj.LuaValue mt = v.getmetatable();
			return mt != null? mt: NIL;
		}
	}

	//	debug.getregistry ()
	final class getregistry extends ZeroArgFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return globals;
		}
	}

	//	debug.getupvalue (f, up)
	static final class getupvalue extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				io.github.taoguan.luaj.LuaString name = findupvalue(c, up);
				if ( name != null ) {
					return varargsOf(name, c.upValues[up-1].getValue() );
				}
			}
			return NIL;
		}
	}

	//	debug.getuservalue (u)
	static final class getuservalue extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue u) {
			return u.isuserdata()? u: NIL;
		}
	}
	
	
	// debug.sethook ([thread,] hook, mask [, count])
	final class sethook extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int a=1;
			LuaThread t = args.isthread(a)? args.checkthread(a++): globals.running;
			io.github.taoguan.luaj.LuaValue func    = args.optfunction(a++, null);
			String str       = args.optjstring(a++,"");
			int count        = args.optint(a++,0);
			boolean call=false,line=false,rtrn=false;
			for ( int i=0; i<str.length(); i++ )
				switch ( str.charAt(i) ) {
					case 'c': call=true; break;
					case 'l': line=true; break;
					case 'r': rtrn=true; break;
				}
			LuaThread.State s = t.state;
			s.hookfunc = func;
			s.hookcall = call;
			s.hookline = line;
			s.hookcount = count;
			s.hookrtrn = rtrn;
			return NONE;
		}
	}

	//	debug.setlocal ([thread,] level, local, value)
	final class setlocal extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running;
			int level = args.checkint(a++);
			int local = args.checkint(a++);
			io.github.taoguan.luaj.LuaValue value = args.arg(a++);
			CallFrame f = callstack(thread).getCallFrame(level);
			return f != null? f.setLocal(local, value): NONE;
		}
	}

	//	debug.setmetatable (value, table)
	static final class setmetatable extends TwoArgFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue value, io.github.taoguan.luaj.LuaValue table) {
			io.github.taoguan.luaj.LuaValue mt = table.opttable(null);
			switch ( value.type() ) {
				case TNIL:      io.github.taoguan.luaj.LuaNil.s_metatable      = mt; break;
				case TNUMBER:   io.github.taoguan.luaj.LuaNumber.s_metatable   = mt; break;
				case TBOOLEAN:  io.github.taoguan.luaj.LuaBoolean.s_metatable  = mt; break;
				case TSTRING:   io.github.taoguan.luaj.LuaString.s_metatable   = mt; break;
				case TFUNCTION: io.github.taoguan.luaj.LuaFunction.s_metatable = mt; break;
				case TTHREAD:   LuaThread.s_metatable   = mt; break;
				default: value.setmetatable( mt );
			}
			return value;
		}
	}

	//	debug.setupvalue (f, up, value)
	static final class setupvalue extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			io.github.taoguan.luaj.LuaValue value = args.arg(3);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				io.github.taoguan.luaj.LuaString name = findupvalue(c, up);
				if ( name != null ) {
					c.upValues[up-1].setValue(value);
					return name;
				}
			}
			return NIL;
		}
	}

	//	debug.setuservalue (udata, value)
	static final class setuservalue extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			Object o = args.checkuserdata(1);
			io.github.taoguan.luaj.LuaValue v = args.checkvalue(2);
			LuaUserdata u = (LuaUserdata) args.arg1();
			u.m_instance = v.checkuserdata();
			u.m_metatable = v.getmetatable();
			return NONE;
		}
	}
	
	//	debug.traceback ([thread,] [message [, level]])
	final class traceback extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running;
			String message = args.optjstring(a++, null);
			int level = args.optint(a++,1);
			String tb = callstack(thread).traceback(level);
			return valueOf(message!=null? message+"\n"+tb: tb);
		}
	}
	
	//	debug.upvalueid (f, n)
	static final class upvalueid extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				if ( c.upValues != null && up > 0 && up <= c.upValues.length ) {
					return valueOf(c.upValues[up-1].hashCode());
				}
			}
			return NIL;
		}
	}

	//	debug.upvaluejoin (f1, n1, f2, n2)
	static final class upvaluejoin extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			LuaClosure f1 = args.checkclosure(1);
			int n1 = args.checkint(2);
			LuaClosure f2 = args.checkclosure(3);
			int n2 = args.checkint(4);
			if (n1 < 1 || n1 > f1.upValues.length)
				argerror("index out of range");
			if (n2 < 1 || n2 > f2.upValues.length)
				argerror("index out of range");
			f1.upValues[n1-1] = f2.upValues[n2-1];
			return NONE;
		}
	}

	public void onCall(io.github.taoguan.luaj.LuaFunction f) {
		LuaThread.State s = globals.running.state;
		if (s.inhook) return;
		callstack().onCall(f);
		if (s.hookcall) callHook(s, CALL, NIL);
	}

	public void onCall(LuaClosure c, io.github.taoguan.luaj.Varargs varargs, io.github.taoguan.luaj.LuaValue[] stack) {
		LuaThread.State s = globals.running.state;
		if (s.inhook) return;
		callstack().onCall(c, varargs, stack);
		if (s.hookcall) callHook(s, CALL, NIL);
	}

	public void onInstruction(int pc, io.github.taoguan.luaj.Varargs v, int top) {
		LuaThread.State s = globals.running.state;
		if (s.inhook) return;
		callstack().onInstruction(pc, v, top);
		if (s.hookfunc == null) return;
		if (s.hookcount > 0)
			if (++s.bytecodes % s.hookcount == 0)
				callHook(s, COUNT, NIL);
		if (s.hookline) {
			int newline = callstack().currentline();
			if ( newline != s.lastline ) {
				s.lastline = newline;
				callHook(s, LINE, io.github.taoguan.luaj.LuaValue.valueOf(newline));
			}
		}
	}

	public void onReturn() {
		LuaThread.State s = globals.running.state;
		if (s.inhook) return;
		callstack().onReturn();
		if (s.hookrtrn) callHook(s, RETURN, NIL);
	}

	public String traceback(int level) {
		return callstack().traceback(level);
	}
	
	void callHook(LuaThread.State s, io.github.taoguan.luaj.LuaValue type, io.github.taoguan.luaj.LuaValue arg) {
		if (s.inhook || s.hookfunc == null) return;
		s.inhook = true;
		try {
			s.hookfunc.call(type, arg);
		} catch (LuaError e) {
			throw e;
		} catch (RuntimeException e) {
			throw new LuaError(e);
		} finally {
			s.inhook = false;
		}
	}
	
	CallStack callstack() {
		return callstack(globals.running);
	}

	CallStack callstack(LuaThread t) {
		if (t.callstack == null)
			t.callstack = new CallStack();
		return (CallStack) t.callstack;
	}

	static class DebugInfo {
		  String name;	/* (n) */
		  String namewhat;	/* (n) 'global', 'local', 'field', 'method' */
		  String what;	/* (S) 'Lua', 'C', 'main', 'tail' */
		  String source;	/* (S) */
		  int currentline;	/* (l) */
		  int linedefined;	/* (S) */
		  int lastlinedefined;	/* (S) */
		  short nups;	/* (u) number of upvalues */
		  short nparams;/* (u) number of parameters */
		  boolean isvararg;        /* (u) */
		  boolean istailcall;	/* (t) */
		  String short_src; /* (S) */
		  CallFrame cf;  /* active function */

		public void funcinfo(io.github.taoguan.luaj.LuaFunction f) {
			if (f.isclosure()) {
				io.github.taoguan.luaj.Prototype p = f.checkclosure().p;
				this.source = p.source != null ? p.source.tojstring() : "=?";
				this.linedefined = p.linedefined;
				this.lastlinedefined = p.lastlinedefined;
				this.what = (this.linedefined == 0) ? "main" : "Lua";
				this.short_src = p.shortsource();
			} else {
				this.source = "=[Java]";
				this.linedefined = -1;
				this.lastlinedefined = -1;
				this.what = "Java";
				this.short_src = f.name();
			}
		}
	}
	
	public static class CallStack {
		final static CallFrame[] EMPTY = {};
		CallFrame[] frame = EMPTY;
		int calls  = 0;

		CallStack() {}
		
		synchronized int currentline() {
			return calls > 0? frame[calls-1].currentline(): -1;
		}

		private synchronized CallFrame pushcall() {
			if (calls >= frame.length) {
				int n = Math.max(4, frame.length * 3 / 2);
				CallFrame[] f = new CallFrame[n];
				System.arraycopy(frame, 0, f, 0, frame.length);
				for (int i = frame.length; i < n; ++i)
					f[i] = new CallFrame();
				frame = f;
				for (int i = 1; i < n; ++i)
					f[i].previous = f[i-1];
			}
			return frame[calls++];
		}
		
		final synchronized void onCall(io.github.taoguan.luaj.LuaFunction function) {
			pushcall().set(function);
		}

		final synchronized void onCall(LuaClosure function, io.github.taoguan.luaj.Varargs varargs, io.github.taoguan.luaj.LuaValue[] stack) {
			pushcall().set(function, varargs, stack);
		}
		
		final synchronized void onReturn() {
			if (calls > 0)
				frame[--calls].reset();
		}
		
		final synchronized void onInstruction(int pc, io.github.taoguan.luaj.Varargs v, int top) {
			if (calls > 0)
				frame[calls-1].instr(pc, v, top);
		}

		/**
		 * Get the traceback starting at a specific level.
		 * @param level
		 * @return String containing the traceback.
		 */
		synchronized String traceback(int level) {
			StringBuffer sb = new StringBuffer();
			sb.append( "stack traceback:" );
			for (CallFrame c; (c = getCallFrame(level++)) != null; ) {
				sb.append("\n\t");
				sb.append( c.shortsource() );
				sb.append( ':' );
				if (c.currentline() > 0)
					sb.append( c.currentline()+":" );
				sb.append( " in " );
				DebugInfo ar = auxgetinfo("n", c.f, c);
				if (c.linedefined() == 0)
					sb.append("main chunk");
				else if ( ar.name != null ) {
					sb.append( "function '" );
					sb.append( ar.name );
					sb.append( '\'' );
				} else {
					sb.append( "function <" );
					sb.append( c.shortsource() );
					sb.append( ':' );
					sb.append( c.linedefined() );
					sb.append( '>' );
				}
			}
			sb.append("\n\t[Java]: in ?");
			return sb.toString();
		}

		synchronized CallFrame getCallFrame(int level) {
			if (level < 1 || level > calls)
				return null;
			return frame[calls-level];
		}

		synchronized CallFrame findCallFrame(io.github.taoguan.luaj.LuaValue func) {
			for (int i = 1; i <= calls; ++i)
				if (frame[calls-i].f == func)
					return frame[i];
			return null;
		}


		synchronized DebugInfo auxgetinfo(String what, io.github.taoguan.luaj.LuaFunction f, CallFrame ci) {
			DebugInfo ar = new DebugInfo();
			for (int i = 0, n = what.length(); i < n; ++i) {
				switch (what.charAt(i)) {
			      case 'S':
			    	  ar.funcinfo(f);
			    	  break;
			      case 'l':
			    	  ar.currentline = ci != null && ci.f.isclosure()? ci.currentline(): -1;
			    	  break;
			      case 'u':
			    	  if (f != null && f.isclosure()) {
			    		  io.github.taoguan.luaj.Prototype p = f.checkclosure().p;
			    		  ar.nups = (short) p.upvalues.length;
			    		  ar.nparams = (short) p.numparams;
			    		  ar.isvararg = p.is_vararg != 0;
			    	  } else {
				    	  ar.nups = 0;
				    	  ar.isvararg = true;
				    	  ar.nparams = 0;
			    	  }
			    	  break;
			      case 't':
			    	  ar.istailcall = false;
			    	  break;
			      case 'n': {
			    	  /* calling function is a known Lua function? */
			    	  if (ci != null && ci.previous != null) {
			    		  if (ci.previous.f.isclosure()) {
			    			  NameWhat nw = getfuncname(ci.previous);
				    		  if (nw != null) {
				    			  ar.name = nw.name;
				    			  ar.namewhat = nw.namewhat;
				    		  }
			    		  }
			    	  }
			    	  if (ar.namewhat == null) {
			    		  ar.namewhat = "";  /* not found */
			    		  ar.name = null;
			    	  }
			    	  break;
			      }
			      case 'L':
			      case 'f':
			    	  break;
			      default:
			    	  // TODO: return bad status.
			    	  break;
				}
			}
			return ar;
		}

	}

	static class CallFrame {
		io.github.taoguan.luaj.LuaFunction f;
		int pc;
		int top;
		io.github.taoguan.luaj.Varargs v;
		io.github.taoguan.luaj.LuaValue[] stack;
		CallFrame previous;
		void set(LuaClosure function, io.github.taoguan.luaj.Varargs varargs, io.github.taoguan.luaj.LuaValue[] stack) {
			this.f = function;
			this.v = varargs;
			this.stack = stack;
		}
		public String shortsource() {
			return f.isclosure()? f.checkclosure().p.shortsource(): "[Java]";
		}
		void set(io.github.taoguan.luaj.LuaFunction function) {
			this.f = function;
		}
		void reset() {
			this.f = null;
			this.v = null;
			this.stack = null;
		}
		void instr(int pc, io.github.taoguan.luaj.Varargs v, int top) {
			this.pc = pc;
			this.v = v;
			this.top = top;
			if (TRACE)
				io.github.taoguan.luaj.Print.printState(f.checkclosure(), pc, stack, top, v);
		}
		io.github.taoguan.luaj.Varargs getLocal(int i) {
			io.github.taoguan.luaj.LuaString name = getlocalname(i);
			if ( i >= 1 && i <= stack.length && stack[i-1] != null )
				return varargsOf( name == null ? NIL : name, stack[i-1] );
			else
				return NIL;
		}
		io.github.taoguan.luaj.Varargs setLocal(int i, io.github.taoguan.luaj.LuaValue value) {
			io.github.taoguan.luaj.LuaString name = getlocalname(i);
			if ( i >= 1 && i <= stack.length && stack[i-1] != null ) {
				stack[i-1] = value;
				return name == null ? NIL : name;
			} else {
				return NIL;
			}
		}
		int currentline() {
			if ( !f.isclosure() ) return -1;
			int[] li = f.checkclosure().p.lineinfo;
			return li==null || pc<0 || pc>=li.length? -1: li[pc];
		}
		String sourceline() {
			if ( !f.isclosure() ) return f.tojstring();
			return f.checkclosure().p.shortsource() + ":" + currentline();
		}
		int linedefined() {
			return f.isclosure()? f.checkclosure().p.linedefined: -1;
		}
		io.github.taoguan.luaj.LuaString getlocalname(int index) {
			if ( !f.isclosure() ) return null;
			return f.checkclosure().p.getlocalname(index, pc);
		}
	}

	static io.github.taoguan.luaj.LuaString findupvalue(LuaClosure c, int up) {
		if ( c.upValues != null && up > 0 && up <= c.upValues.length ) {
			if ( c.p.upvalues != null && up <= c.p.upvalues.length )
				return c.p.upvalues[up-1].name;
			else
				return io.github.taoguan.luaj.LuaString.valueOf( "."+up );
		}
		return null;
	}
	
	static void lua_assert(boolean x) {
		if (!x) throw new RuntimeException("lua_assert failed");
	}
	
	static class NameWhat {
		final String name;
		final String namewhat;
		NameWhat(String name, String namewhat) {
			this.name = name;
			this.namewhat = namewhat;
		}
	}

	// Return the name info if found, or null if no useful information could be found.
	static NameWhat getfuncname(CallFrame frame) {
		if (!frame.f.isclosure())
			return new NameWhat(frame.f.classnamestub(), "Java");
		io.github.taoguan.luaj.Prototype p = frame.f.checkclosure().p;
		int pc = frame.pc;
		int i = p.code[pc]; /* calling instruction */
		io.github.taoguan.luaj.LuaString tm;
		switch (LuaInstruction.getOpCode(i)) {
			case CALL:
			case TAILCALL: /* get function name */
				return getobjname(p, pc, LuaInstruction.getA(i));
			case TFORCALL: /* for iterator */
		    	return new NameWhat("(for iterator)", "(for iterator");
		    /* all other instructions can call only through metamethods */
		    case SELF:
		    case GETTABUP:
		    case GETTABLE: tm = io.github.taoguan.luaj.LuaValue.INDEX; break;
		    case SETTABUP:
		    case SETTABLE: tm = io.github.taoguan.luaj.LuaValue.NEWINDEX; break;
		    case EQ: tm = io.github.taoguan.luaj.LuaValue.EQ; break;
		    case ADD: tm = io.github.taoguan.luaj.LuaValue.ADD; break;
		    case SUB: tm = io.github.taoguan.luaj.LuaValue.SUB; break;
		    case MUL: tm = io.github.taoguan.luaj.LuaValue.MUL; break;
		    case DIV: tm = io.github.taoguan.luaj.LuaValue.DIV; break;
			case IDIV: tm = io.github.taoguan.luaj.LuaValue.IDIV; break;
		    case MOD: tm = io.github.taoguan.luaj.LuaValue.MOD; break;
		    case POW: tm = io.github.taoguan.luaj.LuaValue.POW; break;
			case BAND: tm = io.github.taoguan.luaj.LuaValue.BAND; break;
			case BOR: tm = io.github.taoguan.luaj.LuaValue.BOR; break;
			case BXOR: tm = io.github.taoguan.luaj.LuaValue.BXOR; break;
			case SHL: tm = io.github.taoguan.luaj.LuaValue.SHL; break;
			case SHR: tm = io.github.taoguan.luaj.LuaValue.SHR; break;
		    case UNM: tm = io.github.taoguan.luaj.LuaValue.UNM; break;
		    case LEN: tm = io.github.taoguan.luaj.LuaValue.LEN; break;
			case BNOT: tm = io.github.taoguan.luaj.LuaValue.BNOT; break;
		    case LT: tm = io.github.taoguan.luaj.LuaValue.LT; break;
		    case LE: tm = io.github.taoguan.luaj.LuaValue.LE; break;
		    case CONCAT: tm = io.github.taoguan.luaj.LuaValue.CONCAT; break;
		    default:
		      return null;  /* else no useful name can be found */
		}
		return new NameWhat( tm.tojstring(), "metamethod" );
	}
	
	// return NameWhat if found, null if not
	public static NameWhat getobjname(io.github.taoguan.luaj.Prototype p, int lastpc, int reg) {
		int pc = lastpc; // currentpc(L, ci);
		io.github.taoguan.luaj.LuaString name = p.getlocalname(reg + 1, pc);
		if (name != null) /* is a local? */
			return new NameWhat( name.tojstring(), "local" );

		/* else try symbolic execution */
		pc = findsetreg(p, lastpc, reg);
		if (pc != -1) { /* could find instruction? */
			int i = p.code[pc];
			switch (LuaInstruction.getOpCode(i)) {
			case MOVE: {
				int a = LuaInstruction.getA(i);
				int b = LuaInstruction.getB(i); /* move from `b' to `a' */
				if (b < a)
					return getobjname(p, pc, b); /* get name for `b' */
				break;
			}
			case GETTABUP:
			case GETTABLE: {
				int k = LuaInstruction.getC(i); /* key index */
				int t = LuaInstruction.getB(i); /* table index */
		        io.github.taoguan.luaj.LuaString vn = (LuaInstruction.getOpCode(i) == OpCode.GETTABLE)  /* name of indexed variable */
	                    ? p.getlocalname(t + 1, pc)
	                    : (t < p.upvalues.length ? p.upvalues[t].name : QMARK);
				name = kname(p, k);
				return new NameWhat( name.tojstring(), vn != null && vn.eq_b(ENV)? "global": "field" );
			}
			case GETUPVAL: {
				int u = LuaInstruction.getB(i); /* upvalue index */
				name = u < p.upvalues.length ? p.upvalues[u].name : QMARK;
				return new NameWhat( name.tojstring(), "upvalue" );
			}
		    case LOADK:
		    case LOADKX: {
		        int b = (LuaInstruction.getOpCode(i) == OpCode.LOADK) ? LuaInstruction.getBx(i)
		                                                    : LuaInstruction.getAx(p.code[pc + 1]);
		        if (p.k[b].isstring()) {
		          name = p.k[b].strvalue();
		          return new NameWhat( name.tojstring(), "constant" );
		        }
		        break;
		    }
			case SELF: {
				int k = LuaInstruction.getC(i); /* key index */
				name = kname(p, k);
				return new NameWhat( name.tojstring(), "method" );
			}
			default:
				break;
			}
		}
		return null; /* no useful name found */
	}

	static io.github.taoguan.luaj.LuaString kname(io.github.taoguan.luaj.Prototype p, int c) {
		if (LuaInstruction.ISK(c) && p.k[LuaInstruction.INDEXK(c)].isstring())
			return p.k[LuaInstruction.INDEXK(c)].strvalue();
		else
			return QMARK;
	}

	/*
	** try to find last instruction before 'lastpc' that modified register 'reg'
	*/
	static int findsetreg (io.github.taoguan.luaj.Prototype p, int lastpc, int reg) {
	  int pc;
	  int setreg = -1;  /* keep last instruction that changed 'reg' */
	  for (pc = 0; pc < lastpc; pc++) {
	    int i = p.code[pc];
	    OpCode op = LuaInstruction.getOpCode(i);
	    int a = LuaInstruction.getA(i);
	    switch (op) {
	      case LOADNIL: {
	        int b = LuaInstruction.getB(i);
	        if (a <= reg && reg <= a + b)  /* set registers from 'a' to 'a+b' */
	          setreg = pc;
	        break;
	      }
	      case TFORCALL: {
	        if (reg >= a + 2) setreg = pc;  /* affect all regs above its base */
	        break;
	      }
	      case CALL:
	      case TAILCALL: {
	        if (reg >= a) setreg = pc;  /* affect all registers above base */
	        break;
	      }
	      case JMP: {
	        int b = LuaInstruction.getSBx(i);
	        int dest = pc + 1 + b;
	        /* jump is forward and do not skip `lastpc'? */
	        if (pc < dest && dest <= lastpc)
	          pc += b;  /* do the jump */
	        break;
	      }
	      case TEST: {
	        if (reg == a) setreg = pc;  /* jumped code can change 'a' */
	        break;
	      }
	      case SETLIST: { // Lua.testAMode(Lua.OP_SETLIST) == false
	    	if ( ((i>>14)&0x1ff) == 0 ) pc++; // if c == 0 then c stored in next op -> skip
		break;
	      }
	      default:
	        if (op.getSetAFlag() == 1 && reg == a)  /* any instruction that set A */
	          setreg = pc;
	        break;
	    }
	  }
	  return setreg;
	}
}
