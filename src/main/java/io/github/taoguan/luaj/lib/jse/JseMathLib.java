package io.github.taoguan.luaj.lib.jse;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;

/**
 * Subclass of {@link LibFunction} which implements the lua standard {@code math} 
 * library. 
 * <p> 
 * It contains all lua math functions, including those not available on the JME platform.  
 * See {@link io.github.taoguan.luaj.lib.MathLib} for the exception list.  
 * <p>
 * Typically, this library is included as part of a call to 
 * {@link io.github.taoguan.luaj.lib.jse.JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("math").get("sqrt").call( LuaValue.valueOf(2) ) );
 * } </pre>
 * <p>
 * For special cases where the smallest possible footprint is desired, 
 * a minimal set of libraries could be loaded
 * directly via {@link io.github.taoguan.luaj.Globals#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new JseMathLib());
 * System.out.println( globals.get("math").get("sqrt").call( LuaValue.valueOf(2) ) );
 * } </pre>
 * <p>However, other libraries such as <em>CoroutineLib</em> are not loaded in this case.
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * @see LibFunction
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.6">Lua 5.3 Math Lib Reference</a>
 */
public class JseMathLib extends MathLib {
	
	public JseMathLib() {}


	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * <P>Specifically, adds all library functions that can be implemented directly
	 * in JSE but not JME: acos, asin, atan, atan2, cosh, exp, log, pow, sinh, and tanh.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, which must be a Globals instance.
	 */
	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue modname, io.github.taoguan.luaj.LuaValue env) {
		super.call(modname, env);
		io.github.taoguan.luaj.LuaValue math = env.get("math");
		math.set("acos", new acos());
		math.set("asin", new asin());
		io.github.taoguan.luaj.LuaValue atan =  new atan2();
		math.set("atan", atan);
		math.set("atan2", atan);
		math.set("cosh", new cosh());
		math.set("exp", new exp());
		math.set("log", new log());
		math.set("pow", new pow());
		math.set("sinh", new sinh());
		math.set("tanh", new tanh());
		return math;
	}

	static final class acos extends UnaryOp { protected double call(double d) { return Math.acos(d); } }
	static final class asin extends UnaryOp { protected double call(double d) { return Math.asin(d); } }
	static final class atan2 extends TwoArgFunction { 
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue x, io.github.taoguan.luaj.LuaValue y) {
			return valueOf(Math.atan2(x.checkdouble(), y.optdouble(1)));
		} 
	}
	static final class cosh extends UnaryOp { protected double call(double d) { return Math.cosh(d); } }
	static final class exp extends UnaryOp { protected double call(double d) { return Math.exp(d); } }
	static final class log extends TwoArgFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue x, io.github.taoguan.luaj.LuaValue base) {
			double nat = Math.log(x.checkdouble());
			double b = base.optdouble(Math.E);
			if (b != Math.E) nat /= Math.log(b);
			return valueOf(nat);
		}
	}
	static final class pow extends BinaryOp { protected double call(double x, double y) { return Math.pow(x, y); } }
	static final class sinh extends UnaryOp { protected double call(double d) { return Math.sinh(d); } }
	static final class tanh extends UnaryOp { protected double call(double d) { return Math.tanh(d); } }

	/** Faster, better version of pow() used by arithmetic operator ^ */
	public double dpow_lib(double a, double b) {
		return Math.pow(a, b);
	}
	
	
}

