package io.github.taoguan.luaj;

import io.github.taoguan.luaj.vm.LuaInstruction;
import io.github.taoguan.luaj.vm.OpArgMask;
import io.github.taoguan.luaj.vm.OpCode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.github.taoguan.luaj.vm.OpCode.*;
import static io.github.taoguan.luaj.vm.OpCode.JMP;


/**
 * Debug helper class to pretty-print lua bytecodes. 
 * @see io.github.taoguan.luaj.Prototype
 * @see LuaClosure 
 */
public class Print {

	/** opcode names */
	private static final String STRING_FOR_NULL = "null";
	public static PrintStream ps = System.out;

	static void printString(PrintStream ps, final LuaString s) {
		
		ps.print('"');
		for (int i = 0, n = s.m_length; i < n; i++) {
			int c = s.m_bytes[s.m_offset+i];
			if ( c >= ' ' && c <= '~' && c != '\"' && c != '\\' )
				ps.print((char) c);
			else {
				switch (c) {
					case '"':
						ps.print("\\\"");
						break;
					case '\\':
						ps.print("\\\\");
						break;
					case 0x0007: /* bell */
						ps.print("\\a");
						break;
					case '\b': /* backspace */
						ps.print("\\b");
						break;
					case '\f':  /* form feed */
						ps.print("\\f");
						break;
					case '\t':  /* tab */
						ps.print("\\t");
						break;
					case '\r': /* carriage return */
						ps.print("\\r");
						break;
					case '\n': /* newline */
						ps.print("\\n");
						break;
					case 0x000B: /* vertical tab */
						ps.print("\\v");
						break;
					default:
						ps.print('\\');
						ps.print(Integer.toString(1000 + 0xff&c).substring(1));
						break;
				}
			}
		}
		ps.print('"');
	}

	static void printValue( PrintStream ps, LuaValue v ) {
		if (v == null) {
			ps.print("null");
			return;
		}
		switch ( v.type() ) {
		case LuaValue.TSTRING: printString( ps, (LuaString) v ); break;
		default: ps.print( v.tojstring() );
		
		}
	}
	
	static void printConstant(PrintStream ps, Prototype f, int i) {
		printValue( ps, i < f.k.length ? f.k[i] : LuaValue.valueOf("UNKNOWN_CONST_" + i) );
	}

	static void printUpvalue(PrintStream ps, Upvaldesc u) {
		ps.print( u.idx + " " );
		printValue( ps, u.name );
	}

	/** 
	 * Print the code in a prototype
	 * @param f the {@link Prototype}
	 */
	public static void printCode(Prototype f) {
		int[] code = f.code;
		int pc, n = code.length;
		for (pc = 0; pc < n; pc++) {
			pc = printOpCode(f, pc);
			ps.println();
		}
	}

	/** 
	 * Print an opcode in a prototype
	 * @param f the {@link Prototype}
	 * @param pc the program counter to look up and print
	 * @return pc same as above or changed
	 */
	public static int printOpCode(Prototype f, int pc) {
		return printOpCode(ps,f,pc);
	}
	
	/** 
	 * Print an opcode in a prototype
	 * @param ps the {@link PrintStream} to print to
	 * @param f the {@link Prototype}
	 * @param pc the program counter to look up and print
	 * @return pc same as above or changed
	 */
	public static int printOpCode(PrintStream ps, Prototype f, int pc) {
		int[] code = f.code;
		int i = code[pc];
		OpCode o = LuaInstruction.getOpCode(i);
		int a = LuaInstruction.getA(i);
		int b = LuaInstruction.getB(i);
		int c = LuaInstruction.getC(i);
		int bx = LuaInstruction.getBx(i);
		int sbx = LuaInstruction.getSBx(i);
		int line = getline(f, pc);
		ps.print("  " + (pc + 1) + "  ");
		if (line > 0)
			ps.print("[" + line + "]  ");
		else
			ps.print("[-]  ");
		if (o == null) {
			ps.print("UNKNOWN_OP_" + o + "  ");
		} else {
//			ps.print(o.name() + "  " + String.format("%32s",
//					Integer.toBinaryString(i)).replaceAll(" ", "0") + "  ");
			ps.print(o.name() + "  ");
			switch (o.getOpMode()) {
			case iABC:
				ps.print( a );
				if (o.getArgBMode() != OpArgMask.OpArgN)
					ps.print(" "+(LuaInstruction.ISK(b) ? (-1 - LuaInstruction.INDEXK(b)) : b));
				if (o.getArgCMode() != OpArgMask.OpArgN)
					ps.print(" "+(LuaInstruction.ISK(c) ? (-1 - LuaInstruction.INDEXK(c)) : c));
				break;
			case iABx:
				if (o.getArgBMode() == OpArgMask.OpArgK) {
					ps.print(a + " " + (-1 - bx));
				} else {
					ps.print(a + " " + (bx));
				}
				break;
			case iAsBx:
				if (o == JMP)
					ps.print( sbx );
				else
					ps.print(a + " " + sbx);
				break;
			}
			switch (o) {
			case LOADK:
				ps.print("  ; ");
				printConstant(ps, f, bx);
				break;
			case GETUPVAL:
			case SETUPVAL:
				ps.print("  ; ");
				if (b < f.upvalues.length) {
					printUpvalue(ps, f.upvalues[b]);
				} else {
					ps.print("UNKNOWN_UPVALUE_" + b);
				}	
				break;
			case GETTABUP:
				ps.print("  ; ");
				if (b < f.upvalues.length) {
					printUpvalue(ps, f.upvalues[b]);
				} else {
					ps.print("UNKNOWN_UPVALUE_" + b);
				}
				ps.print(" ");
				if (LuaInstruction.ISK(c))
					printConstant(ps, f, LuaInstruction.INDEXK(c));
				else
					ps.print("-");
				break;
			case SETTABUP:
				ps.print("  ; ");
				if (a < f.upvalues.length) {
					printUpvalue(ps, f.upvalues[a]);
				} else {
					ps.print("UNKNOWN_UPVALUE_" + a);
				}
				ps.print(" ");
				if (LuaInstruction.ISK(b))
					printConstant(ps, f, LuaInstruction.INDEXK(b));
				else
					ps.print("-");
				ps.print(" ");
				if (LuaInstruction.ISK(c))
					printConstant(ps, f, LuaInstruction.INDEXK(c));
				else
					ps.print("-");
				break;
			case GETTABLE:
			case SELF:
				if (LuaInstruction.ISK(c)) {
					ps.print("  ; ");
					printConstant(ps, f, LuaInstruction.INDEXK(c));
				}
				break;
			case SETTABLE:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case POW:
			case EQ:
			case LT:
			case LE:
			case IDIV:
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				if (LuaInstruction.ISK(b) || LuaInstruction.ISK(c)) {
					ps.print("  ; ");
					if (LuaInstruction.ISK(b))
						printConstant(ps, f, LuaInstruction.INDEXK(b));
					else
						ps.print("-");
					ps.print(" ");
					if (LuaInstruction.ISK(c))
						printConstant(ps, f, LuaInstruction.INDEXK(c));
					else
						ps.print("-");
				}
				break;
			case JMP:
			case FORLOOP:
			case FORPREP:
				ps.print("  ; to " + (sbx + pc + 2));
				break;
			case CLOSURE:
				if (bx < f.p.length) {
					ps.print("  ; " + f.p[bx].getClass().getName());
				} else {
					ps.print("  ; UNKNOWN_PROTYPE_" + bx);
				}
				break;
			case SETLIST:
				if (c == 0)
					ps.print("  ; " + ((int) code[++pc]) + " (stored in the next OP)");
				else
					ps.print("  ; " + ((int) c));
				break;
			case VARARG:
				ps.print( "  ; is_vararg="+ f.is_vararg );
				break;			
			default:
				break;
			}
		}
		return pc;
	}

	private static int getline(Prototype f, int pc) {
		return pc>0 && f.lineinfo!=null && pc<f.lineinfo.length? f.lineinfo[pc]: -1;
	}

	static void printHeader(Prototype f) {
		String s = String.valueOf(f.source);
		if (s.startsWith("@") || s.startsWith("="))
			s = s.substring(1);
		else if ("\033Lua".equals(s))
			s = "(bstring)";
		else
			s = "(string)";
		String a = (f.linedefined == 0) ? "main" : "function";
		ps.print("\n%" + a + " <" + s + ":" + f.linedefined + ","
				+ f.lastlinedefined + "> (" + f.code.length + " instructions, "
				+ f.code.length * 4 + " bytes at " + id(f) + ")\n");
		ps.print(f.numparams + " param, " + f.maxstacksize + " slot, "
				+ f.upvalues.length + " upvalue, ");
		ps.print(f.locvars.length + " local, " + f.k.length
				+ " constant, " + f.p.length + " function\n");
	}

	static void printConstants(Prototype f) {
		int i, n = f.k.length;
		ps.print("constants (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.print("  " + (i + 1) + "  ");
			printValue( ps, f.k[i] );
			ps.print( "\n");
		}
	}

	static void printLocals(Prototype f) {
		int i, n = f.locvars.length;
		ps.print("locals (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.println("  "+i+"  "+f.locvars[i].varname+" "+(f.locvars[i].startpc+1)+" "+(f.locvars[i].endpc+1));
		}
	}

	static void printUpValues(Prototype f) {
		int i, n = f.upvalues.length;
		ps.print("upvalues (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.print("  " + i + "  " + f.upvalues[i] + "\n");
		}
	}

	/** Pretty-prints contents of a Prototype.
	 * 
	 * @param prototype Prototype to print.
	 */
	public static void print(Prototype prototype) {
		printFunction(prototype, true);
	}
	
	/** Pretty-prints contents of a Prototype in short or long form.
	 * 
	 * @param prototype Prototype to print.
	 * @param full true to print all fields, false to print short form.
	 */
	public static void printFunction(Prototype prototype, boolean full) {
		int i, n = prototype.p.length;
		printHeader(prototype);
		printCode(prototype);
		if (full) {
			printConstants(prototype);
			printLocals(prototype);
			printUpValues(prototype);
		}
		for (i = 0; i < n; i++)
			printFunction(prototype.p[i], full);
	}

	private static void format( String s, int maxcols ) {
		int n = s.length();
		if ( n > maxcols )
			ps.print( s.substring(0,maxcols) );
		else {
			ps.print( s );
			for ( int i=maxcols-n; --i>=0; )
				ps.print( ' ' );
		}
	}

	private static String id(Prototype f) {
		return "Proto";
	}
	private void _assert(boolean b) {
		if ( !b ) 
			throw new NullPointerException("_assert failed");
	}

	/**
	 * Print the state of a {@link LuaClosure} that is being executed
	 * @param cl the {@link LuaClosure} 
	 * @param pc the program counter
	 * @param stack the stack of {@link LuaValue}
	 * @param top the top of the stack
	 * @param varargs any {@link Varargs} value that may apply
	 */
	public static void printState(LuaClosure cl, int pc, LuaValue[] stack, int top, Varargs varargs) {
		// print opcode into buffer
		PrintStream previous = ps;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ps = new PrintStream( baos );
		printOpCode( cl.p, pc );
		ps.flush();
		ps.close();
		ps = previous;
		format( baos.toString(), 50 );
		printStack(stack, top, varargs);
		ps.println();
	}

	public static void printStack(LuaValue[] stack, int top, Varargs varargs) {
		// print stack
		ps.print('[');
		for ( int i=0; i<stack.length; i++ ) {
			LuaValue v = stack[i];
			if ( v == null ) 
				ps.print(STRING_FOR_NULL);
			else switch ( v.type() ) {
			case LuaValue.TSTRING:
				LuaString s = v.checkstring();
				ps.print( s.length() < 48?
						s.tojstring():
						s.substring(0, 32).tojstring()+"...+"+(s.length()-32)+"b");					
				break;
			case LuaValue.TFUNCTION:
				ps.print( v.tojstring() );
				break;
			case LuaValue.TUSERDATA:
				Object o = v.touserdata();
				if ( o != null ) {
					String n = o.getClass().getName();
					n = n.substring(n.lastIndexOf('.')+1);
					ps.print( n+": "+Integer.toHexString(o.hashCode()) );
				} else {
					ps.print( v.toString() );
				}
				break;
			default:
				ps.print(v.tojstring());
			}
			if ( i+1 == top )
				ps.print(']');
			ps.print( " | " );
		}
		ps.print(varargs);
	}	

}
