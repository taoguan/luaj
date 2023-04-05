package io.github.taoguan.luaj.server;

import java.io.InputStream;
import java.io.Reader;

/** Interface to launch lua scripts using the {@link LuajClassLoader}.
 * <P>
 * <em>Note: This class is experimental and subject to change in future versions.</em>
 * <P>
 * This interface is purposely genericized to defer class loading so that 
 * luaj classes can come from the class loader.
 * <P>
 * The implementation should be acquired using {@link LuajClassLoader#NewLauncher()}
 * or {@link LuajClassLoader#NewLauncher(Class)} which ensure that the classes are 
 * loaded to give each Launcher instance a pristine set of Globals, including 
 * the shared metatables.
 *
 * @see LuajClassLoader
 * @see LuajClassLoader#NewLauncher()
 * @see LuajClassLoader#NewLauncher(Class)
 * @see DefaultLauncher
 */
public interface Launcher {
	
	/** Launch a script contained in a String.
	 * 
	 * @param script The script contents. 
	 * @param arg Optional arguments supplied to the script.
	 * @return return values from the script.
	 */
	public Object[] launch(String script, Object[] arg); 

	/** Launch a script from an InputStream.
	 * 
	 * @param script The script as an InputStream. 
	 * @param arg Optional arguments supplied to the script.
	 * @return return values from the script.
	 */
	public Object[] launch(InputStream script, Object[] arg); 

	/** Launch a script from a Reader.
	 * 
	 * @param script The script as a Reader. 
	 * @param arg Optional arguments supplied to the script.
	 * @return return values from the script.
	 */
	public Object[] launch(Reader script, Object[] arg);
}