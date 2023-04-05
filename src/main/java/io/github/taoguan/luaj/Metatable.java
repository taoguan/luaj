package io.github.taoguan.luaj;


/**
 * Provides operations that depend on the __mode key of the metatable.
 */
interface Metatable {

	/** Return this metatable as a LuaValue. */
	io.github.taoguan.luaj.LuaValue toLuaValue();

	/** Return an instance of Slot appropriate for the given key and value. */
	io.github.taoguan.luaj.LuaTable.Slot entry(io.github.taoguan.luaj.LuaValue key, io.github.taoguan.luaj.LuaValue value );

	/** Returns the given value wrapped in a weak reference if appropriate. */
	io.github.taoguan.luaj.LuaValue wrap(io.github.taoguan.luaj.LuaValue value );

	/**
	 * Returns the value at the given index in the array, or null if it is a weak reference that
	 * has been dropped.
	 */
	io.github.taoguan.luaj.LuaValue arrayget(io.github.taoguan.luaj.LuaValue[] array, int index);
}
