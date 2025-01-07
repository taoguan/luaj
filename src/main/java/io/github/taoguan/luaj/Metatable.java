package io.github.taoguan.luaj;


/**
 * Provides operations that depend on the __mode key of the metatable.
 */
interface Metatable {

	/** Return this metatable as a LuaValue. */
	LuaValue toLuaValue();

	/** Return an instance of Slot appropriate for the given key and value. */
	LuaTable.Slot entry(LuaValue key, LuaValue value );

	/** Returns the given value wrapped in a weak reference if appropriate. */
	LuaValue wrap(LuaValue value );

	/**
	 * Returns the value at the given index in the array, or null if it is a weak reference that
	 * has been dropped.
	 */
	LuaValue arrayget(LuaValue[] array, int index);
}
