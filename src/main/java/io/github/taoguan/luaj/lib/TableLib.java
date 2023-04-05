package io.github.taoguan.luaj.lib;

import io.github.taoguan.luaj.*;
import io.github.taoguan.luaj.lib.*;
import io.github.taoguan.luaj.lib.jse.*;

/**
 * Subclass of {@link LibFunction} which implements the lua standard {@code table}
 * library.
 * 
 * <p>
 * Typically, this library is included as part of a call to either
 * {@link JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("table").get("length").call( LuaValue.tableOf() ) );
 * } </pre>
 * <p>
 * To instantiate and use it directly,
 * link it into your globals table via {@link io.github.taoguan.luaj.LuaValue#load(io.github.taoguan.luaj.LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new TableLib());
 * System.out.println( globals.get("table").get("length").call( LuaValue.tableOf() ) );
 * } </pre>
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * @see LibFunction
 * @see JsePlatform
 * @see <a href="http://www.lua.org/manual/5.3/manual.html#6.5">Lua 5.3 Table Lib Reference</a>
 */
public class TableLib extends TwoArgFunction {

	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, typically a Globals instance.
	 */
	public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue modname, io.github.taoguan.luaj.LuaValue env) {
		LuaTable table = new LuaTable();
		table.set("concat", new concat());
		table.set("insert", new insert());
		table.set("pack", new pack());
		table.set("remove", new remove());
		table.set("sort", new sort());
		table.set("unpack", new unpack());
		env.set("table", table);
		if (!env.get("package").isnil()) env.get("package").get("loaded").set("table", table);
		return NIL;
	}

	static class TableLibFunction extends LibFunction {
		public io.github.taoguan.luaj.LuaValue call() {
			return argerror(1, "table expected, got no value");
		}
	}
	
	// "concat" (table [, sep [, i [, j]]]) -> string
	static class concat extends TableLibFunction {
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue list) {
			return list.checktable().concat(EMPTYSTRING,1,list.length());
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue list, io.github.taoguan.luaj.LuaValue sep) {
			return list.checktable().concat(sep.checkstring(),1,list.length());
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue list, io.github.taoguan.luaj.LuaValue sep, io.github.taoguan.luaj.LuaValue i) {
			return list.checktable().concat(sep.checkstring(),i.checkint(),list.length());
		}
		public io.github.taoguan.luaj.LuaValue call(io.github.taoguan.luaj.LuaValue list, io.github.taoguan.luaj.LuaValue sep, io.github.taoguan.luaj.LuaValue i, io.github.taoguan.luaj.LuaValue j) {
			return list.checktable().concat(sep.checkstring(),i.checkint(),j.checkint());
		}
	}

	// "insert" (table, [pos,] value)
	static class insert extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			switch (args.narg()) {
			case 0: case 1: {
				return argerror(2, "value expected");
			}
			case 2: {
				LuaTable table = args.arg1().checktable();
				table.insert(table.length()+1,args.arg(2));
				return NONE;
			}
			default: {
				args.arg1().checktable().insert(args.checkint(2),args.arg(3));
				return NONE;
			}
			}
		}
	}
	
	// "pack" (...) -> table
	static class pack extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			io.github.taoguan.luaj.LuaValue t = tableOf(args, 1);
			t.set("n", args.narg());
			return t;
		}
	}

	// "remove" (table [, pos]) -> removed-ele
	static class remove extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			return args.arg1().checktable().remove(args.optint(2, 0));
		}
	}

	// "sort" (table [, comp])
	static class sort extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			args.arg1().checktable().sort(
					args.arg(2).isnil()? NIL: args.arg(2).checkfunction());
			return NONE;
		}
	}

	
	// "unpack", // (list [,i [,j]]) -> result1, ...
	static class unpack extends VarArgFunction {
		public io.github.taoguan.luaj.Varargs invoke(io.github.taoguan.luaj.Varargs args) {
			LuaTable t = args.checktable(1);
			switch (args.narg()) {
			case 1: return t.unpack();
			case 2: return t.unpack(args.checkint(2));
			default: return t.unpack(args.checkint(2), args.checkint(3));
			}
		}
	}
}
