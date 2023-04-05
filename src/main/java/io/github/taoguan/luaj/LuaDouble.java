package io.github.taoguan.luaj;

import io.github.taoguan.luaj.lib.MathLib;

/**
 * Extension of {@link io.github.taoguan.luaj.LuaNumber} which can hold a Java double as its value.
 * <p>
 * These instance are not instantiated directly by clients, but indirectly 
 * via the static functions {@link io.github.taoguan.luaj.LuaValue#valueOf(int)} or {@link io.github.taoguan.luaj.LuaValue#valueOf(double)}
 * functions.  This ensures that values which can be represented as int 
 * are wrapped in {@link io.github.taoguan.luaj.LuaInteger} instead of {@link LuaDouble}.
 * <p>
 * Almost all API's implemented in LuaDouble are defined and documented in {@link io.github.taoguan.luaj.LuaValue}.
 * <p>
 * However the constants {@link #NAN}, {@link #POSINF}, {@link #NEGINF},
 * {@link #JSTR_NAN}, {@link #JSTR_POSINF}, and {@link #JSTR_NEGINF} may be useful 
 * when dealing with Nan or Infinite values. 
 * <p>
 * LuaDouble also defines functions for handling the unique math rules of lua devision and modulo in
 * <ul>
 * <li>{@link #ddiv(double, double)}</li>
 * <li>{@link #ddiv_d(double, double)}</li>
 * <li>{@link #dmod(double, double)}</li>
 * <li>{@link #dmod_d(double, double)}</li>
 * </ul> 
 * <p>
 * @see io.github.taoguan.luaj.LuaValue
 * @see io.github.taoguan.luaj.LuaNumber
 * @see io.github.taoguan.luaj.LuaInteger
 * @see io.github.taoguan.luaj.LuaValue#valueOf(int)
 * @see io.github.taoguan.luaj.LuaValue#valueOf(double)
 */
public class LuaDouble extends io.github.taoguan.luaj.LuaNumber {

	/** Constant LuaDouble representing NaN (not a number) */
	public static final LuaDouble NAN    = new LuaDouble( Double.NaN );
	
	/** Constant LuaDouble representing positive infinity */
	public static final LuaDouble POSINF = new LuaDouble( Double.POSITIVE_INFINITY );
	
	/** Constant LuaDouble representing negative infinity */
	public static final LuaDouble NEGINF = new LuaDouble( Double.NEGATIVE_INFINITY );
	
	/** Constant String representation for NaN (not a number), "nan" */
	public static final String JSTR_NAN    = "nan";
	
	/** Constant String representation for positive infinity, "inf" */
	public static final String JSTR_POSINF = "inf";

	/** Constant String representation for negative infinity, "-inf" */
	public static final String JSTR_NEGINF = "-inf";
	
	/** The value being held by this instance. */
	final double v;

	public static io.github.taoguan.luaj.LuaNumber valueOf(double d) {
		int id = (int) d;
		return d==id? (io.github.taoguan.luaj.LuaNumber) io.github.taoguan.luaj.LuaInteger.valueOf(id): (io.github.taoguan.luaj.LuaNumber) new LuaDouble(d);
	}
	
	/** Don't allow ints to be boxed by DoubleValues  */
	private LuaDouble(double d) {
		this.v = d;
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(v + 1);
		return ((int)(l>>32)) + (int) l;
	}
	
	public boolean islong() {
		return v == (long) v; 
	}
	
	public byte    tobyte()        { return (byte) (long) v; }
	public char    tochar()        { return (char) (long) v; }
	public double  todouble()      { return v; }
	public float   tofloat()       { return (float) v; }
	public int     toint()         { return (int) (long) v; }
	public long    tolong()        { return (long) v; }
	public short   toshort()       { return (short) (long) v; }

	public double      optdouble(double defval)        { return v; }
	public int         optint(int defval)              { return (int) (long) v;  }
	public io.github.taoguan.luaj.LuaInteger optinteger(io.github.taoguan.luaj.LuaInteger defval)   { return io.github.taoguan.luaj.LuaInteger.valueOf((int) (long)v); }
	public long        optlong(long defval)            { return (long) v; }
	
	public io.github.taoguan.luaj.LuaInteger checkinteger()                  { return io.github.taoguan.luaj.LuaInteger.valueOf( (int) (long) v ); }
	
	// unary operators
	public io.github.taoguan.luaj.LuaValue neg() { return valueOf(-v); }
	
	// object equality, used for key comparison
	public boolean equals(Object o) { return o instanceof LuaDouble? ((LuaDouble)o).v == v: false; }
	
	// equality w/ metatable processing
	public io.github.taoguan.luaj.LuaValue eq(io.github.taoguan.luaj.LuaValue val )        { return val.raweq(v)? TRUE: FALSE; }
	public boolean eq_b( io.github.taoguan.luaj.LuaValue val )       { return val.raweq(v); }

	// equality w/o metatable processing
	public boolean raweq( io.github.taoguan.luaj.LuaValue val )      { return val.raweq(v); }
	public boolean raweq( double val )        { return v == val; }
	public boolean raweq( int val )           { return v == val; }
	
	// basic binary arithmetic
	public io.github.taoguan.luaj.LuaValue add(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.add(v); }
	public io.github.taoguan.luaj.LuaValue add(double lhs )     { return LuaDouble.valueOf(lhs + v); }
	public io.github.taoguan.luaj.LuaValue sub(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.subFrom(v); }
	public io.github.taoguan.luaj.LuaValue sub(double rhs )        { return LuaDouble.valueOf(v - rhs); }
	public io.github.taoguan.luaj.LuaValue sub(int rhs )        { return LuaDouble.valueOf(v - rhs); }
	public io.github.taoguan.luaj.LuaValue subFrom(double lhs )   { return LuaDouble.valueOf(lhs - v); }
	public io.github.taoguan.luaj.LuaValue mul(io.github.taoguan.luaj.LuaValue rhs )        { return rhs.mul(v); }
	public io.github.taoguan.luaj.LuaValue mul(double lhs )   { return LuaDouble.valueOf(lhs * v); }
	public io.github.taoguan.luaj.LuaValue mul(int lhs )      { return LuaDouble.valueOf(lhs * v); }
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
	
	
	/** Divide two double numbers according to lua math, and return a {@link io.github.taoguan.luaj.LuaValue} result.
	 * @param lhs Left-hand-side of the division.
	 * @param rhs Right-hand-side of the division.
	 * @return {@link io.github.taoguan.luaj.LuaValue} for the result of the division,
	 * taking into account positive and negiative infinity, and Nan
	 * @see #ddiv_d(double, double) 
	 */
	public static io.github.taoguan.luaj.LuaValue ddiv(double lhs, double rhs) {
		return rhs!=0? valueOf( lhs / rhs ): lhs>0? POSINF: lhs==0? NAN: NEGINF;	
	}
	
	/** Divide two double numbers according to lua math, and return a double result.
	 * @param lhs Left-hand-side of the division.
	 * @param rhs Right-hand-side of the division.
	 * @return Value of the division, taking into account positive and negative infinity, and Nan
	 * @see #ddiv(double, double)
	 */
	public static double ddiv_d(double lhs, double rhs) {
		return rhs!=0? lhs / rhs: lhs>0? Double.POSITIVE_INFINITY: lhs==0? Double.NaN: Double.NEGATIVE_INFINITY;	
	}
	
	/** Take modulo double numbers according to lua math, and return a {@link io.github.taoguan.luaj.LuaValue} result.
	 * @param lhs Left-hand-side of the modulo.
	 * @param rhs Right-hand-side of the modulo.
	 * @return {@link io.github.taoguan.luaj.LuaValue} for the result of the modulo,
	 * using lua's rules for modulo
	 * @see #dmod_d(double, double) 
	 */
	public static io.github.taoguan.luaj.LuaValue dmod(double lhs, double rhs) {
		if (rhs == 0 || lhs == Double.POSITIVE_INFINITY || lhs == Double.NEGATIVE_INFINITY) return NAN;
		if (rhs == Double.POSITIVE_INFINITY) {
			return lhs < 0 ? POSINF : valueOf(lhs);
		}
		if (rhs == Double.NEGATIVE_INFINITY) {
			return lhs > 0 ? NEGINF : valueOf(lhs);
		}
		return valueOf( lhs-rhs*Math.floor(lhs/rhs) );
	}

	/** Take modulo for double numbers according to lua math, and return a double result.
	 * @param lhs Left-hand-side of the modulo.
	 * @param rhs Right-hand-side of the modulo.
	 * @return double value for the result of the modulo, 
	 * using lua's rules for modulo
	 * @see #dmod(double, double)
	 */
	public static double dmod_d(double lhs, double rhs) {
		if (rhs == 0 || lhs == Double.POSITIVE_INFINITY || lhs == Double.NEGATIVE_INFINITY) return Double.NaN;
		if (rhs == Double.POSITIVE_INFINITY) {
			return lhs < 0 ? Double.POSITIVE_INFINITY : lhs;
		}
		if (rhs == Double.NEGATIVE_INFINITY) {
			return lhs > 0 ? Double.NEGATIVE_INFINITY : lhs;
		}
		return lhs-rhs*Math.floor(lhs/rhs);
	}

	// relational operators
	public io.github.taoguan.luaj.LuaValue lt(io.github.taoguan.luaj.LuaValue rhs )         { return rhs.gt_b(v)? io.github.taoguan.luaj.LuaValue.TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lt(double rhs )      { return v < rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lt(int rhs )         { return v < rhs? TRUE: FALSE; }
	public boolean lt_b( io.github.taoguan.luaj.LuaValue rhs )       { return rhs.gt_b(v); }
	public boolean lt_b( int rhs )         { return v < rhs; }
	public boolean lt_b( double rhs )      { return v < rhs; }
	public io.github.taoguan.luaj.LuaValue lteq(io.github.taoguan.luaj.LuaValue rhs )       { return rhs.gteq_b(v)? io.github.taoguan.luaj.LuaValue.TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lteq(double rhs )    { return v <= rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue lteq(int rhs )       { return v <= rhs? TRUE: FALSE; }
	public boolean lteq_b( io.github.taoguan.luaj.LuaValue rhs )     { return rhs.gteq_b(v); }
	public boolean lteq_b( int rhs )       { return v <= rhs; }
	public boolean lteq_b( double rhs )    { return v <= rhs; }
	public io.github.taoguan.luaj.LuaValue gt(io.github.taoguan.luaj.LuaValue rhs )         { return rhs.lt_b(v)? io.github.taoguan.luaj.LuaValue.TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gt(double rhs )      { return v > rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gt(int rhs )         { return v > rhs? TRUE: FALSE; }
	public boolean gt_b( io.github.taoguan.luaj.LuaValue rhs )       { return rhs.lt_b(v); }
	public boolean gt_b( int rhs )         { return v > rhs; }
	public boolean gt_b( double rhs )      { return v > rhs; }
	public io.github.taoguan.luaj.LuaValue gteq(io.github.taoguan.luaj.LuaValue rhs )       { return rhs.lteq_b(v)? io.github.taoguan.luaj.LuaValue.TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gteq(double rhs )    { return v >= rhs? TRUE: FALSE; }
	public io.github.taoguan.luaj.LuaValue gteq(int rhs )       { return v >= rhs? TRUE: FALSE; }
	public boolean gteq_b( io.github.taoguan.luaj.LuaValue rhs )     { return rhs.lteq_b(v); }
	public boolean gteq_b( int rhs )       { return v >= rhs; }
	public boolean gteq_b( double rhs )    { return v >= rhs; }
	
	// string comparison
	public int strcmp( io.github.taoguan.luaj.LuaString rhs )      { typerror("attempt to compare number with string"); return 0; }
			
	public String tojstring() {
		/*
		if ( v == 0.0 ) { // never occurs in J2me 
			long bits = Double.doubleToLongBits( v );
			return ( bits >> 63 == 0 ) ? "0" : "-0";
		}
		*/
		long l = (long) v;
		if ( l == v ) 
			return Long.toString(l);
		if ( Double.isNaN(v) )
			return JSTR_NAN;
		if ( Double.isInfinite(v) ) 
			return (v<0? JSTR_NEGINF: JSTR_POSINF);
		return Double.toString(v);
	}
	
	public io.github.taoguan.luaj.LuaString strvalue() {
		return io.github.taoguan.luaj.LuaString.valueOf(tojstring());
	}
	
	public io.github.taoguan.luaj.LuaString optstring(io.github.taoguan.luaj.LuaString defval) {
		return io.github.taoguan.luaj.LuaString.valueOf(tojstring());
	}
		
	public io.github.taoguan.luaj.LuaValue tostring() {
		return io.github.taoguan.luaj.LuaString.valueOf(tojstring());
	}
	
	public String optjstring(String defval) {
		return tojstring();
	}
	
	public io.github.taoguan.luaj.LuaNumber optnumber(io.github.taoguan.luaj.LuaNumber defval) {
		return this; 
	}
	
	public boolean isnumber() {
		return true; 
	}
	
	public boolean isstring() {
		return true;
	}
	
	public io.github.taoguan.luaj.LuaValue tonumber() {
		return this;
	}
	public int checkint()                { return (int) (long) v; }
	public long checklong()              { return (long) v; }
	public io.github.taoguan.luaj.LuaNumber checknumber()       { return this; }
	public double checkdouble()          { return v; }
	
	public String checkjstring() { 
		return tojstring();
	}
	public io.github.taoguan.luaj.LuaString checkstring() {
		return io.github.taoguan.luaj.LuaString.valueOf(tojstring());
	}
	
	public boolean isvalidkey() {
		return !Double.isNaN(v);
	}	
}
