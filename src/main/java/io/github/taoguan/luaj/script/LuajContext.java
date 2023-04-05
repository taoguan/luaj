package io.github.taoguan.luaj.script;

import io.github.taoguan.luaj.Globals;
import io.github.taoguan.luaj.lib.jse.JsePlatform;
import io.github.taoguan.luaj.luajc.LuaJC;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.io.*;

/** 
 * Context for LuaScriptEngine execution which maintains its own Globals, 
 * and manages the input and output redirection.
 */
public class LuajContext extends SimpleScriptContext implements ScriptContext {

	/** Globals for this context instance. */
	public final Globals globals;

	/** The initial value of globals.STDIN */
	private final InputStream stdin;
	/** The initial value of globals.STDOUT */
	private final PrintStream stdout;
	/** The initial value of globals.STDERR */
	private final PrintStream stderr;
	
	/** Construct a LuajContext with its own globals which may
	 * be debug globals depending on the value of the system
	 * property 'io.github.taoguan.luaj.debug'
	 * <p>
	 * If the system property 'io.github.taoguan.luaj.debug' is set, the globals
	 * created will be a debug globals that includes the debug 
	 * library.  This may provide better stack traces, but may 
	 * have negative impact on performance.
	 */
	public LuajContext() {
		this("true".equals(System.getProperty("io.github.taoguan.luaj.debug")),
			"true".equals(System.getProperty("io.github.taoguan.luaj.luajc")));
	}

	/** Construct a LuajContext with its own globals, which
	 * which optionally are debug globals, and optionally use the
	 * luajc direct lua to java bytecode compiler.
	 * <p>
	 * If createDebugGlobals is set, the globals
	 * created will be a debug globals that includes the debug 
	 * library.  This may provide better stack traces, but may 
	 * have negative impact on performance.
	 * @param createDebugGlobals true to create debug globals, 
	 * false for standard globals.
	 * @param useLuaJCCompiler true to use the luajc compiler, 
	 * reqwuires bcel to be on the class path.
	 */
	public LuajContext(boolean createDebugGlobals, boolean useLuaJCCompiler) {
		globals = createDebugGlobals?
    		JsePlatform.debugGlobals():
    		JsePlatform.standardGlobals();
    	if (useLuaJCCompiler)
    		LuaJC.install(globals);
    	stdin = globals.STDIN;
    	stdout = globals.STDOUT;
    	stderr = globals.STDERR;
	}
	
	@Override
	public void setErrorWriter(Writer writer) {
		globals.STDERR = writer != null?
				new PrintStream(new WriterOutputStream(writer)):
				stderr;
	}

	@Override
	public void setReader(Reader reader) {
		globals.STDIN = reader != null?
				new ReaderInputStream(reader):
				stdin;
	}

	@Override
	public void setWriter(Writer writer) {
		globals.STDOUT = writer != null?
				new PrintStream(new WriterOutputStream(writer), true):
				stdout;
	}

	static final class WriterOutputStream extends OutputStream {
		final Writer w;
		WriterOutputStream(Writer w) {
			this.w = w;
		}
		public void write(int b) throws IOException {
			w.write(new String(new byte[] {(byte)b}));
		}
		public void write(byte[] b, int o, int l) throws IOException {
			w.write(new String(b, o, l));
		}
		public void write(byte[] b) throws IOException {
			w.write(new String(b));
		}
		public void close() throws IOException {
			w.close();
		}
		public void flush() throws IOException {
			w.flush();
		}
	}
	
	static final class ReaderInputStream extends InputStream {
		final Reader r;
		ReaderInputStream(Reader r) {
			this.r = r;
		}
		public int read() throws IOException {
			return r.read();
		}
	}
}
