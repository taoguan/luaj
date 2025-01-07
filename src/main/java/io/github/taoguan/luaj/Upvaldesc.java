package io.github.taoguan.luaj;

public class Upvaldesc {

	/* upvalue name (for debug information) */
	public LuaString name;
	
	/* whether it is in stack */
	public final boolean instack;
	
	/* index of upvalue (in stack or in outer function's list) */
	public final short idx;
	
	public Upvaldesc(LuaString name, boolean instack, int idx) {
		this.name = name;
		this.instack = instack;
		this.idx = (short) idx;
	}
	
	public String toString() {
		return idx + (instack? " instack ": " closed ") + String.valueOf(name); 
	}
}
