package io.github.taoguan.luaj;

class NonTableMetatable implements Metatable {

	private final LuaValue value;

	public NonTableMetatable(LuaValue value) {
		this.value = value;
	}

	public LuaValue toLuaValue() {
		return value;
	}

	public LuaTable.Slot entry(LuaValue key, LuaValue value) {
		return LuaTable.defaultEntry(key, value);
	}

	public LuaValue wrap(LuaValue value) {
		return value;
	}

	public LuaValue arrayget(LuaValue[] array, int index) {
		return array[index];
	}
}
