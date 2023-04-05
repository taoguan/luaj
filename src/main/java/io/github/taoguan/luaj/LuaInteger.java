package io.github.taoguan.luaj;

import io.github.taoguan.luaj.lib.MathLib;

/**
 * Extension of {@link LuaNumber} which can hold a Java int as its value. 
 * <p>
 * These instance are not instantiated directly by clients, but indirectly 
 * via the static functions {@link io.github.taoguan.luaj.LuaValue#valueOf(int)} or {@link io.github.taoguan.luaj.LuaValue#valueOf(double)}
 * functions.  This ensures that policies regarding pooling of instances are 
 * encapsulated.  
 * <p>
 * There are no API's specific to LuaInteger that are useful beyond what is already 
 * exposed in {@link io.github.taoguan.luaj.LuaValue}.
 * 
 * @see io.github.taoguan.luaj.LuaValue
 * @see LuaNumber
 * @see LuaDouble
 * @see io.github.taoguan.luaj.LuaValue#valueOf(int)
 * @see io.github.taoguan.luaj.LuaValue#valueOf(double)
 */
public class LuaInteger extends LuaNumber {

	private static final LuaInteger[] intValues = new LuaInteger[512];
	static {
		for ( int i=0; i<512; i++ )
			intValues[i] = new LuaInteger(i-256);
	}

	public static LuaInteger valueOf(int i) {
		return i<=255 && i>=-256? intValues[i+256]: new LuaInteger(i);
	};
	
	/** Return a LuaNumber that represents the value provided
	 * @param l long value to represent.
	 * @return LuaNumber that is eithe LuaInteger or LuaDouble representing l
	 * @see io.github.taoguan.luaj.LuaValue#valueOf(int)
	 * @see io.github.taoguan.luaj.LuaValue#valueOf(double)
	 */
	public static LuaNumber valueOf(long l) {
		int i = (int) l;
		return l==i? (i<=255 && i>=-256? intValues[i+256]: 
			(LuaNumber) new LuaInteger(i)): 
			(LuaNumber) LuaDouble.valueOf(l);
	}
	
	/** The value being held by this instance. */
	public final int v;
	
	/** 
	 * Package protected constructor. 
	 * @see io.github.taoguan.luaj.LuaValue#valueOf(int)
	 **/
	LuaInteger(int i) {
		this.v = i;
	}
	
	public boolean isint() {		return true;	}
	public boolean isinttype() {	return true;	}
	public boolean islong() {		return true;	}
	
	public byte    tobyte()        { return (byte) v; }
	public char    tochar()        { return (char) v; }
	public double  todouble()      { return v; }
	public float   tofloat()       { return v; }
	public int     toint()         { return v; }
	public long    tolong()        { return v; }
	public short   toshort()       { return (short) v; }

	public double      optdouble(double defval)            { return v; }
	public int         optint(int defval)                  { return v;  }
	public LuaInteger  optinteger(LuaInteger defval)       { return this; }
	public long        optlong(long defval)                { return v; }

	public String tojstring() {
		return Integer.toString(v);
	}

	public io.github.taoguan.luaj.LuaString strvalue() {
		return io.github.taoguan.luaj.LuaString.valueOf(Integer.toString(v));
	}
		
	public io.github.taoguan.luaj.LuaString optstring(io.github.taoguan.luaj.LuaString defval) {
		return io.github.taoguan.luaj.LuaString.valueOf(Integer.toString(v));
	}
	
	public io.github.taoguan.luaj.LuaValue tostring() {
		return io.github.taoguan.luaj.LuaString.valueOf(Integer.toString(v));
	}
		
	public String optjstring(String defval) { 
		return Integer.toString(v); 
	}
	
	public LuaInteger checkinteger() {
		return this;
	}
	
	public boolean isstring() {
		return true;
	}
	
	public int hashCode() {
		return v;
	}

	public static int hashCode(int x) {
		return x;
	}

	// unary operators
	public io.github.taoguan.luaj.LuaValue neg() { return valueOf(-(long)v); }
	
	// object equality, used for key comparison
	public boolean equals(Object o) { return o instanceof LuaInteger? ((LuaInteger)o).v == v: false; }
	
	// equality w/ metatable processing
	public io.github.taoguan.luaj.LuaValue eq(io.github.taoguan.luaj.LuaValue val )    { return val.raweq(v)? TRUE: FALSE; }
	public boolean eq_b( io.github.taoguan.luaj.LuaValue val )   { return val.raweq(v); }
	
	// equality w/o metatable processing
	public boolean raweq( io.github.taoguan.luaj.LuaValue val )  { return val.raweq(v); }
	public boolean raweq( double val )    { return v == val; }
	public boolean raweq( int val )       { return v == val; }
	
	// arithmetic operators
	public io.github.taoguan.luaj.LuaValue add(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.add(v); }
	public io.github.taoguan.luaj.LuaValue add(double lhs )     { return LuaDouble.valueOf(lhs + v); }
	public io.github.taoguan.luaj.LuaValue add(int lhs )        { return LuaInteger.valueOf(lhs + (long)v); }
	public io.github.taoguan.luaj.LuaValue sub(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.subFrom(v); }
	public io.github.taoguan.luaj.LuaValue sub(double rhs )        { return LuaDouble.valueOf(v - rhs); }
	public io.github.taoguan.luaj.LuaValue sub(int rhs )        { return LuaDouble.valueOf(v - rhs); }
	public io.github.taoguan.luaj.LuaValue subFrom(double lhs )   { return LuaDouble.valueOf(lhs - v); }
	public io.github.taoguan.luaj.LuaValue subFrom(int lhs )      { return LuaInteger.valueOf(lhs - (long)v); }
	public io.github.taoguan.luaj.LuaValue mul(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.mul(v); }
	public io.github.taoguan.luaj.LuaValue mul(double lhs )   { return LuaDouble.valueOf(lhs * v); }
	public io.github.taoguan.luaj.LuaValue mul(int lhs )      { return LuaInteger.valueOf(lhs * (long)v); }
	public io.github.taoguan.luaj.LuaValue pow(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.powWith(v); }
	public io.github.taoguan.luaj.LuaValue pow(double rhs )        { return MathLib.dpow(v,rhs); }
	public io.github.taoguan.luaj.LuaValue pow(int rhs )        { return MathLib.dpow(v,rhs); }
	public io.github.taoguan.luaj.LuaValue powWith(double lhs )   { return MathLib.dpow(lhs,v); }
	public io.github.taoguan.luaj.LuaValue powWith(int lhs )      { return MathLib.dpow(lhs,v); }
	public io.github.taoguan.luaj.LuaValue div(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.divInto(v); }
	public io.github.taoguan.luaj.LuaValue div(double rhs )        { return LuaDouble.ddiv(v,rhs); }
	public io.github.taoguan.luaj.LuaValue div(int rhs )        { return LuaDouble.ddiv(v,rhs); }
	public io.github.taoguan.luaj.LuaValue divInto(double lhs )   { return LuaDouble.ddiv(lhs,v); }
	public io.github.taoguan.luaj.LuaValue idiv(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.idivInto(v); }
	public io.github.taoguan.luaj.LuaValue idiv(double rhs )        { return LuaDouble.valueOf(MathLib.floorDiv(v,rhs)); }
	public io.github.taoguan.luaj.LuaValue idiv(int rhs )        { return LuaDouble.valueOf(MathLib.floorDiv(v,rhs)); }
	public io.github.taoguan.luaj.LuaValue idivInto(double lhs )   { return LuaDouble.valueOf(MathLib.floorDiv(lhs,v)); }
	public io.github.taoguan.luaj.LuaValue mod(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.modFrom(v); }
	public io.github.taoguan.luaj.LuaValue mod(double rhs )        { return LuaDouble.dmod(v,rhs); }
	public io.github.taoguan.luaj.LuaValue mod(int rhs )        { return LuaDouble.dmod(v,rhs); }
	public io.github.taoguan.luaj.LuaValue modFrom(double lhs )   { return LuaDouble.dmod(lhs,v); }
	
	// relational operators
	public io.github.taoguan.luaj.LuaValue lt(io.github.taoguan.luaj.LuaValue rhs )         { return rhs.gt_b(v)? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lt(double rhs )      { return v < rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lt(int rhs )         { return v < rhs? TRUE: FALSE; }
	public boolean lt_b( io.github.taoguan.luaj.LuaValue rhs )       { return rhs.gt_b(v); }
	public boolean lt_b( int rhs )         { return v < rhs; }
	public boolean lt_b( double rhs )      { return v < rhs; }
	public io.github.taoguan.luaj.LuaValue lteq(io.github.taoguan.luaj.LuaValue rhs )       { return rhs.gteq_b(v)? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lteq(double rhs )    { return v <= rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lteq(int rhs )       { return v <= rhs? TRUE: FALSE; }
	public boolean lteq_b( io.github.taoguan.luaj.LuaValue rhs )     { return rhs.gteq_b(v); }
	public boolean lteq_b( int rhs )       { return v <= rhs; }
	public boolean lteq_b( double rhs )    { return v <= rhs; }
	public io.github.taoguan.luaj.LuaValue gt(io.github.taoguan.luaj.LuaValue rhs )         { return rhs.lt_b(v)? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gt(double rhs )      { return v > rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gt(int rhs )         { return v > rhs? TRUE: FALSE; }
	public boolean gt_b( io.github.taoguan.luaj.LuaValue rhs )       { return rhs.lt_b(v); }
	public boolean gt_b( int rhs )         { return v > rhs; }
	public boolean gt_b( double rhs )      { return v > rhs; }
	public io.github.taoguan.luaj.LuaValue gteq(io.github.taoguan.luaj.LuaValue rhs )       { return rhs.lteq_b(v)? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gteq(double rhs )    { return v >= rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gteq(int rhs )       { return v >= rhs? TRUE: FALSE; }
	public boolean gteq_b( io.github.taoguan.luaj.LuaValue rhs )     { return rhs.lteq_b(v); }
	public boolean gteq_b( int rhs )       { return v >= rhs; }
	public boolean gteq_b( double rhs )    { return v >= rhs; }
	
	// string comparison
	public int strcmp( io.github.taoguan.luaj.LuaString rhs )      { typerror("attempt to compare number with string"); return 0; }
	
	public int checkint() { 
		return v; 
	}
	public long checklong() {
		return v; 
	}
	public double checkdouble() {
		return v;
	}
	public String checkjstring() { 
		return String.valueOf(v); 
	}
	public io.github.taoguan.luaj.LuaString checkstring() {
		return valueOf( String.valueOf(v) ); 
	}

}
