package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.Globals;

import java.io.InputStream;

/** 
 * Interface for opening application resource files such as scripts sources.  
 * <p>
 * This is used by required to load files that are part of 
 * the application, and implemented by BaseLib
 * for both the Jme and Jse platforms. 
 * <p>
 * The Jme version of base lib {@link BaseLib} 
 * implements {@link Globals#finder} via {@link Class#getResourceAsStream(String)}, 
 * while the Jse version {@link io.github.taoguan.luaj.lib.jse.JseBaseLib} implements it using {@link java.io.File#File(String)}.
 * <p>
 * The io library does not use this API for file manipulation.
 * <p>
 * @see BaseLib
 * @see Globals#finder
 * @see io.github.taoguan.luaj.lib.jse.JseBaseLib
 * @see io.github.taoguan.luaj.lib.jse.JsePlatform
 */
public interface ResourceFinder {
	
	/** 
	 * Try to open a file, or return null if not found.
	 * 
	 * @see io.github.taoguan.luaj.lib.BaseLib
	 * @see io.github.taoguan.luaj.lib.jse.JseBaseLib
	 * 
	 * @param filename
	 * @return InputStream, or null if not found. 
	 */
	public InputStream findResource( String filename );
}