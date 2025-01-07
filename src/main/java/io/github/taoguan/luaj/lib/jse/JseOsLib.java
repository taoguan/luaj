package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

import java.io.File;
import java.io.IOException;

/**
 * Subclass of {@link LibFunction} which implements the standard lua {@code os} library.
 * <p>
 * This contains more complete implementations of the following functions 
 * using features that are specific to JSE:   
 * <ul>
 * <li>{@code execute()}</li>
 * <li>{@code remove()}</li>
 * <li>{@code rename()}</li>
 * <li>{@code tmpname()}</li>
 * </ul>
 * <p>
 * Because the nature of the {@code os} library is to encapsulate 
 * os-specific features, the behavior of these functions varies considerably 
 * from their counterparts in the C platform.  
 * <p>
 * Typically, this library is included as part of a call to 
 * {@link io.github.taoguan.luaj.lib.jse.JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("os").get("time").call() );
 * } </pre>
 * <p>
 * For special cases where the smallest possible footprint is desired, 
 * a minimal set of libraries could be loaded
 * directly via {@link io.github.taoguan.luaj.Globals#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new JseOsLib());
 * System.out.println( globals.get("os").get("time").call() );
 * } </pre>
 * <p>However, other libraries such as <em>MathLib</em> are not loaded in this case.
 * <p>
 * @see LibFunction
 * @see OsLib
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.9">Lua 5.3 OS Lib Reference</a>
 */
public class JseOsLib extends OsLib {
	
	/** return code indicating the execute() threw an I/O exception */
	public static final int EXEC_IOEXCEPTION =  1;
	
	/** return code indicating the execute() was interrupted */
	public static final int EXEC_INTERRUPTED = -2;
	
	/** return code indicating the execute() threw an unknown exception */
	public static final int EXEC_ERROR       = -3;
	
	/** public constructor */
	public JseOsLib() {
	}

	protected String getenv(String varname) {
		String s = System.getenv(varname);
		return s != null? s : System.getProperty(varname);
	}

	protected Varargs execute(String command) {
		int exitValue;
		try {
			exitValue = new JseProcess(command, null, globals.STDOUT, globals.STDERR).waitFor();
		} catch (IOException ioe) {
			exitValue = EXEC_IOEXCEPTION;
		} catch (InterruptedException e) {
			exitValue = EXEC_INTERRUPTED;
		} catch (Throwable t) {
			exitValue = EXEC_ERROR;
		}
		if (exitValue == 0)
			return varargsOf(TRUE, valueOf("exit"), ZERO);
		return varargsOf(NIL, valueOf("signal"), valueOf(exitValue));
	}

	protected void remove(String filename) throws IOException {
		File f = new File(filename);
		if ( ! f.exists() )
			throw new IOException("No such file or directory");
		if ( ! f.delete() )
			throw new IOException("Failed to delete");
	}

	protected void rename(String oldname, String newname) throws IOException {
		File f = new File(oldname);
		if ( ! f.exists() )
			throw new IOException("No such file or directory");
		if ( ! f.renameTo(new File(newname)) )
			throw new IOException("Failed to rename");
	}

	protected String tmpname() {
		try {
			File f = File.createTempFile(TMP_PREFIX ,TMP_SUFFIX);
			return f.getName();
		} catch ( IOException ioe ) {
			return super.tmpname();
		}
	}
	
}
