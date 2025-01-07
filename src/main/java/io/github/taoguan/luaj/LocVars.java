package io.github.taoguan.luaj;

/**
 * Data class to hold debug information relating to local variables for a {@link Prototype}
 */
public class LocVars {
	/** The local variable name */
	public LuaString varname;
	
	/** The instruction offset when the variable comes into scope */ 
	public int startpc;
	
	/** The instruction offset when the variable goes out of scope */ 
	public int endpc;
	
	/**
	 * Construct a LocVars instance. 
	 * @param varname The local variable name
	 * @param startpc The instruction offset when the variable comes into scope
	 * @param endpc The instruction offset when the variable goes out of scope
	 */
	public LocVars(LuaString varname, int startpc, int endpc) {
		this.varname = varname;
		this.startpc = startpc;
		this.endpc = endpc;
	}
	
	public String tojstring() {
		return varname+" "+startpc+"-"+endpc;
	}
}
