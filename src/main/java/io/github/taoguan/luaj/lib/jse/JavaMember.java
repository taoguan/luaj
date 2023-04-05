package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

/**
 * Java method or constructor.
 * <p>
 * Primarily handles argument coercion for parameter lists including scoring of compatibility and 
 * java varargs handling.
 * <p>
 * This class is not used directly.  
 * It is an abstract base class for {@link JavaConstructor} and {@link JavaMethod}.
 * @see JavaConstructor
 * @see JavaMethod
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
abstract
class JavaMember extends VarArgFunction {
	
	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	final CoerceLuaToJava.Coercion[] fixedargs;
	final CoerceLuaToJava.Coercion varargs;
	
	protected JavaMember(Class[] params, int modifiers) {
		boolean isvarargs = ((modifiers & METHOD_MODIFIERS_VARARGS) != 0);
		fixedargs = new CoerceLuaToJava.Coercion[isvarargs? params.length-1: params.length];
		for ( int i=0; i<fixedargs.length; i++ )
			fixedargs[i] = CoerceLuaToJava.getCoercion( params[i] );
		varargs = isvarargs? CoerceLuaToJava.getCoercion( params[params.length-1] ): null;
	}
	
	int score(Varargs args) {
		int n = args.narg();
		int s = n>fixedargs.length? CoerceLuaToJava.SCORE_WRONG_TYPE * (n-fixedargs.length): 0;
		for ( int j=0; j<fixedargs.length; j++ )
			s += fixedargs[j].score( args.arg(j+1) );
		if ( varargs != null )
			for ( int k=fixedargs.length; k<n; k++ )
				s += varargs.score( args.arg(k+1) );
		return s;
	}
	
	protected Object[] convertArgs(Varargs args) {
		Object[] a;
		if ( varargs == null ) {
			a = new Object[fixedargs.length];
			for ( int i=0; i<a.length; i++ )
				a[i] = fixedargs[i].coerce( args.arg(i+1) );
		} else {
			int n = Math.max(fixedargs.length,args.narg());
			a = new Object[n];
			for ( int i=0; i<fixedargs.length; i++ )
				a[i] = fixedargs[i].coerce( args.arg(i+1) );
			for ( int i=fixedargs.length; i<n; i++ )
				a[i] = varargs.coerce( args.arg(i+1) );
		}
		return a;
	}
}
