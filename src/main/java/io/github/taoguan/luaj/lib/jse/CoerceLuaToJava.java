package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to coerce values from lua to Java within the luajava library. 
 * <p>
 * This class is primarily used by the {@link io.github.taoguan.luaj.lib.jse.LuajavaLib},
 * but can also be used directly when working with Java/lua bindings. 
 * <p>
 * To coerce to specific Java values, generally the {@code toType()} methods 
 * on {@link io.github.taoguan.luaj.LuaValue} may be used:
 * <ul>
 * <li>{@link io.github.taoguan.luaj.LuaValue#toboolean()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tobyte()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tochar()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#toshort()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#toint()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tofloat()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#todouble()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#tojstring()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#touserdata()}</li>
 * <li>{@link io.github.taoguan.luaj.LuaValue#touserdata(Class)}</li>
 * </ul>
 * <p>
 * For data in lua tables, the various methods on {@link LuaTable} can be used directly 
 * to convert data to something more useful.
 * 
 * @see io.github.taoguan.luaj.lib.jse.LuajavaLib
 * @see CoerceJavaToLua
 */
public class CoerceLuaToJava {

	static int SCORE_NULL_VALUE     =    0x10;
	static int SCORE_WRONG_TYPE     =   0x100;
	public static int SCORE_UNCOERCIBLE    = 0x10000;
	
	public static interface Coercion {
		public int score( io.github.taoguan.luaj.LuaValue value );
		public Object coerce( io.github.taoguan.luaj.LuaValue value );
	};

	/** 
	 * Coerce a LuaValue value to a specified java class
	 * @param value LuaValue to coerce
	 * @param clazz Class to coerce into
	 * @return Object of type clazz (or a subclass) with the corresponding value.
	 */
	public static Object coerce(io.github.taoguan.luaj.LuaValue value, Class clazz) {
		return getCoercion(clazz).coerce(value);
	}
	
	static final Map COERCIONS = Collections.synchronizedMap(new HashMap());
	
	static final class BoolCoercion implements Coercion {
		public String toString() {
			return "BoolCoercion()";
		}
		public int score( io.github.taoguan.luaj.LuaValue value ) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TBOOLEAN:
				return 0;
			}
			return 1;
		}

		public Object coerce(io.github.taoguan.luaj.LuaValue value) {
			return value.toboolean()? Boolean.TRUE: Boolean.FALSE;
		}
	}

	static final class NumericCoercion implements Coercion {
		static final int TARGET_TYPE_BYTE = 0;
		static final int TARGET_TYPE_CHAR = 1;
		static final int TARGET_TYPE_SHORT = 2;
		static final int TARGET_TYPE_INT = 3;
		static final int TARGET_TYPE_LONG = 4;
		static final int TARGET_TYPE_FLOAT = 5;
		static final int TARGET_TYPE_DOUBLE = 6;
		static final String[] TYPE_NAMES = { "byte", "char", "short", "int", "long", "float", "double" };
		final int targetType;
		public String toString() {
			return "NumericCoercion("+TYPE_NAMES[targetType]+")";
		}
		NumericCoercion(int targetType) {
			this.targetType = targetType;
		}
		public int score( io.github.taoguan.luaj.LuaValue value ) {
			int fromStringPenalty = 0;
			if ( value.type() == io.github.taoguan.luaj.LuaValue.TSTRING ) {
				value = value.tonumber();
				if ( value.isnil() ) {
					return SCORE_UNCOERCIBLE;
				}
				fromStringPenalty = 4;
			}
			if ( value.isint() ) {
				switch ( targetType ) {
				case TARGET_TYPE_BYTE: {
					int i = value.toint();
					return fromStringPenalty + ((i==(byte)i)? 0: SCORE_WRONG_TYPE);
				}
				case TARGET_TYPE_CHAR: {
					int i = value.toint();
					return fromStringPenalty + ((i==(byte)i)? 1: (i==(char)i)? 0: SCORE_WRONG_TYPE);
				}
				case TARGET_TYPE_SHORT: {
					int i = value.toint();
					return fromStringPenalty +
							((i==(byte)i)? 1: (i==(short)i)? 0: SCORE_WRONG_TYPE);
				}
				case TARGET_TYPE_INT: { 
					int i = value.toint();
					return fromStringPenalty +
							((i==(byte)i)? 2: ((i==(char)i) || (i==(short)i))? 1: 0);
				}
				case TARGET_TYPE_FLOAT: return fromStringPenalty + 1;
				case TARGET_TYPE_LONG: return fromStringPenalty;
				case TARGET_TYPE_DOUBLE: return fromStringPenalty + 2;
				default: return SCORE_WRONG_TYPE;
				}
			} else if ( value.isnumber() ) {
				switch ( targetType ) {
				case TARGET_TYPE_BYTE: return SCORE_WRONG_TYPE;
				case TARGET_TYPE_CHAR: return SCORE_WRONG_TYPE;
				case TARGET_TYPE_SHORT: return SCORE_WRONG_TYPE;
				case TARGET_TYPE_INT: return SCORE_WRONG_TYPE;
				case TARGET_TYPE_LONG: {
					double d = value.todouble();
					return fromStringPenalty + ((d==(long)d)? 0: SCORE_WRONG_TYPE);
				}
				case TARGET_TYPE_FLOAT: {
					double d = value.todouble();
					return fromStringPenalty + ((d==(float)d)? 0: SCORE_WRONG_TYPE);
				}
				case TARGET_TYPE_DOUBLE: {
					double d = value.todouble();
					return fromStringPenalty + (((d==(long)d) || (d==(float)d))? 1: 0);
				}
				default: return SCORE_WRONG_TYPE;
				}
			} else {
				return SCORE_UNCOERCIBLE;
			}
		}

		public Object coerce(io.github.taoguan.luaj.LuaValue value) {
			switch ( targetType ) {
			case TARGET_TYPE_BYTE: return new Byte( (byte) value.toint() );
			case TARGET_TYPE_CHAR: return new Character( (char) value.toint() );
			case TARGET_TYPE_SHORT: return new Short( (short) value.toint() );
			case TARGET_TYPE_INT: return new Integer( (int) value.toint() );
			case TARGET_TYPE_LONG: return new Long( (long) value.tolong() );
			case TARGET_TYPE_FLOAT: return new Float( (float) value.todouble() );
			case TARGET_TYPE_DOUBLE: return new Double( (double) value.todouble() );
			default: return null;
			}
		}
	}

	static final class StringCoercion implements Coercion {
		public static final int TARGET_TYPE_STRING = 0;
		public static final int TARGET_TYPE_BYTES = 1;
		final int targetType;
		public StringCoercion(int targetType) {
			this.targetType = targetType;
		}
		public String toString() {
			return "StringCoercion("+(targetType==TARGET_TYPE_STRING? "String": "byte[]")+")";
		}
		public int score(io.github.taoguan.luaj.LuaValue value) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TSTRING:
				return value.checkstring().isValidUtf8()?
						(targetType==TARGET_TYPE_STRING? 0: 1):
						(targetType==TARGET_TYPE_BYTES? 0: SCORE_WRONG_TYPE);
			case io.github.taoguan.luaj.LuaValue.TNIL:
				return SCORE_NULL_VALUE;
			default:
				return targetType == TARGET_TYPE_STRING? SCORE_WRONG_TYPE: SCORE_UNCOERCIBLE;
			}
		}
		public Object coerce(io.github.taoguan.luaj.LuaValue value) {
			if ( value.isnil() )
				return null;
			if ( targetType == TARGET_TYPE_STRING )
				return value.tojstring();
			io.github.taoguan.luaj.LuaString s = value.checkstring();
			byte[] b = new byte[s.m_length];
			s.copyInto(0, b, 0, b.length);
			return b;
		}
	}

	static final class ArrayCoercion implements Coercion {
		final Class componentType;
		final Coercion componentCoercion;
		public ArrayCoercion(Class componentType) {
			this.componentType = componentType;
			this.componentCoercion = getCoercion(componentType);
		}
		public String toString() {
			return "ArrayCoercion("+componentType.getName()+")";
		}
		public int score(io.github.taoguan.luaj.LuaValue value) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TTABLE:
				return value.length()==0? 0: componentCoercion.score( value.get(1) );
			case io.github.taoguan.luaj.LuaValue.TUSERDATA:
				return inheritanceLevels( componentType, value.touserdata().getClass().getComponentType() );
			case io.github.taoguan.luaj.LuaValue.TNIL:
				return SCORE_NULL_VALUE;
			default: 
				return SCORE_UNCOERCIBLE;
			}
		}
		public Object coerce(io.github.taoguan.luaj.LuaValue value) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TTABLE: {
				int n = value.length();
				Object a = Array.newInstance(componentType, n);
				for ( int i=0; i<n; i++ )
					Array.set(a, i, componentCoercion.coerce(value.get(i+1)));
				return a;
			}
			case io.github.taoguan.luaj.LuaValue.TUSERDATA:
				return value.touserdata();
			case io.github.taoguan.luaj.LuaValue.TNIL:
				return null;
			default: 
				return null;
			}
			
		}
	}

	/** 
	 * Determine levels of inheritance between a base class and a subclass
	 * @param baseclass base class to look for
	 * @param subclass class from which to start looking
	 * @return number of inheritance levels between subclass and baseclass, 
	 * or SCORE_UNCOERCIBLE if not a subclass
	 */
	public static final int inheritanceLevels( Class baseclass, Class subclass ) {
		if ( subclass == null )
			return SCORE_UNCOERCIBLE;
		if ( baseclass == subclass )
			return 0;
		int min = Math.min( SCORE_UNCOERCIBLE, inheritanceLevels( baseclass, subclass.getSuperclass() ) + 1 );
		Class[] ifaces = subclass.getInterfaces();
		for ( int i=0; i<ifaces.length; i++ ) 
			min = Math.min(min, inheritanceLevels(baseclass, ifaces[i]) + 1 );
		return min;
	}
	
	static final class ObjectCoercion implements Coercion {
		final Class targetType;
		ObjectCoercion(Class targetType) {
			this.targetType = targetType;
		}
		public String toString() {
			return "ObjectCoercion("+targetType.getName()+")";
		}
		public int score(io.github.taoguan.luaj.LuaValue value) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TNUMBER:
				return inheritanceLevels( targetType, value.isint()? Integer.class: Double.class );
			case io.github.taoguan.luaj.LuaValue.TBOOLEAN:
				return inheritanceLevels( targetType, Boolean.class );
			case io.github.taoguan.luaj.LuaValue.TSTRING:
				return inheritanceLevels( targetType, String.class );
			case io.github.taoguan.luaj.LuaValue.TUSERDATA:
				return inheritanceLevels( targetType, value.touserdata().getClass() );
			case io.github.taoguan.luaj.LuaValue.TNIL:
				return SCORE_NULL_VALUE;
			default:
				return inheritanceLevels( targetType, value.getClass() );
			}
		}
		public Object coerce(io.github.taoguan.luaj.LuaValue value) {
			switch ( value.type() ) {
			case io.github.taoguan.luaj.LuaValue.TNUMBER:
				return value.isint()? (Object)new Integer(value.toint()): (Object)new Double(value.todouble());
			case io.github.taoguan.luaj.LuaValue.TBOOLEAN:
				return value.toboolean()? Boolean.TRUE: Boolean.FALSE;
			case io.github.taoguan.luaj.LuaValue.TSTRING:
				return value.tojstring();
			case io.github.taoguan.luaj.LuaValue.TUSERDATA:
				return value.optuserdata(targetType, null);
			case io.github.taoguan.luaj.LuaValue.TNIL:
				return null;
			default:
				return value;
			}
		}
	}

	static {
		Coercion boolCoercion   = new BoolCoercion();
		Coercion byteCoercion   = new NumericCoercion(NumericCoercion.TARGET_TYPE_BYTE);
		Coercion charCoercion   = new NumericCoercion(NumericCoercion.TARGET_TYPE_CHAR);
		Coercion shortCoercion  = new NumericCoercion(NumericCoercion.TARGET_TYPE_SHORT);
		Coercion intCoercion    = new NumericCoercion(NumericCoercion.TARGET_TYPE_INT);
		Coercion longCoercion   = new NumericCoercion(NumericCoercion.TARGET_TYPE_LONG);
		Coercion floatCoercion  = new NumericCoercion(NumericCoercion.TARGET_TYPE_FLOAT);
		Coercion doubleCoercion = new NumericCoercion(NumericCoercion.TARGET_TYPE_DOUBLE);
		Coercion stringCoercion = new StringCoercion(StringCoercion.TARGET_TYPE_STRING);
		Coercion bytesCoercion  = new StringCoercion(StringCoercion.TARGET_TYPE_BYTES);
		
		COERCIONS.put( Boolean.TYPE, boolCoercion );
		COERCIONS.put( Boolean.class, boolCoercion );
		COERCIONS.put( Byte.TYPE, byteCoercion );
		COERCIONS.put( Byte.class, byteCoercion );
		COERCIONS.put( Character.TYPE, charCoercion );
		COERCIONS.put( Character.class, charCoercion );
		COERCIONS.put( Short.TYPE, shortCoercion );
		COERCIONS.put( Short.class, shortCoercion );
		COERCIONS.put( Integer.TYPE, intCoercion );
		COERCIONS.put( Integer.class, intCoercion );
		COERCIONS.put( Long.TYPE, longCoercion );
		COERCIONS.put( Long.class, longCoercion );
		COERCIONS.put( Float.TYPE, floatCoercion );
		COERCIONS.put( Float.class, floatCoercion );
		COERCIONS.put( Double.TYPE, doubleCoercion );
		COERCIONS.put( Double.class, doubleCoercion );
		COERCIONS.put( String.class, stringCoercion );
		COERCIONS.put( byte[].class, bytesCoercion );
	}
	
	public static Coercion getCoercion(Class c) {
		Coercion co = (Coercion) COERCIONS.get( c );
		if ( co != null ) {
			return co;
		}
		if ( c.isArray() ) {
			Class typ = c.getComponentType();
			co = new ArrayCoercion(c.getComponentType());
		} else {
			co = new ObjectCoercion(c);
		}
		COERCIONS.put( c, co );
		return co;
	}
}
