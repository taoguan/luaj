package io.github.taoguan.luaj.luajc;

import io.github.taoguan.luaj.vm.LuaInstruction;
import io.github.taoguan.luaj.vm.OpCode;

/**
 *
 */
public class JavaGen {

	public final String classname;
	public final byte[] bytecode;
	public final JavaGen[] inners;
	
	public JavaGen(io.github.taoguan.luaj.Prototype p, String classname, String filename, boolean genmain ) {
		this( new ProtoInfo(p,classname), classname, filename, genmain );
	}
	
	private JavaGen( ProtoInfo pi, String classname, String filename, boolean genmain ) {
		this.classname = classname;
		
		// build this class
		JavaBuilder builder = new JavaBuilder(pi, classname, filename);
		scanInstructions(pi, classname, builder);
		for (int i = 0; i < pi.prototype.locvars.length; ++i) {
			io.github.taoguan.luaj.LocVars l = pi.prototype.locvars[i];
			builder.setVarStartEnd(i, l.startpc, l.endpc, l.varname.tojstring());
		}
		this.bytecode = builder.completeClass(genmain);
		
		// build sub-prototypes
		if ( pi.subprotos != null ) {
			int n = pi.subprotos.length;
			inners = new JavaGen[n];
			for ( int i=0; i<n; i++ )
				inners[i] = new JavaGen(pi.subprotos[i], pi.subprotos[i].name, filename, false);
		} else {
			inners = null;
		}
	}

	private void scanInstructions(ProtoInfo pi, String classname, JavaBuilder builder) {
		io.github.taoguan.luaj.Prototype p = pi.prototype;
		int vresultbase = -1;
		
		for ( int bi=0; bi<pi.blocklist.length; bi++ ) {
			BasicBlock b0 = pi.blocklist[bi];

			// convert upvalues that are phi-variables
			for ( int slot=0; slot<p.maxstacksize; slot++ ) {
				int pc = b0.pc0;
				boolean c = pi.isUpvalueCreate(pc, slot);
				if ( c && pi.vars[slot][pc].isPhiVar() )
					builder.convertToUpvalue(pc, slot);
			}
			
			for ( int pc=b0.pc0; pc<=b0.pc1; pc++ ) {
				
				final int pc0 = pc; // closure changes pc
				final int ins = p.code[pc];
				final int line = pc < p.lineinfo.length? p.lineinfo[pc]: -1;
				final OpCode o = LuaInstruction.getOpCode(ins);
				int a = LuaInstruction.getA(ins);
				int b = LuaInstruction.getB(ins);
				int bx = LuaInstruction.getBx(ins);
				int sbx = LuaInstruction.getSBx(ins);
				int c = LuaInstruction.getC(ins);
	
				switch ( o ) {	
				case GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
					builder.loadUpvalue( b );
					builder.storeLocal( pc, a );
	                break;
	                
				case SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
					builder.storeUpvalue( pc, b, a );
	                break;
	                
				case NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
					builder.newTable( b, c );
					builder.storeLocal( pc, a );
	                break;
	                
				case MOVE:/*	A B	R(A):= R(B)					*/
					builder.loadLocal( pc, b );
					builder.storeLocal( pc, a );
					break;
					
				case UNM: /*	A B	R(A):= -R(B)					*/
				case NOT: /*	A B	R(A):= not R(B)				*/
				case LEN: /*	A B	R(A):= length of R(B)				*/
				case BNOT: /*	A B	R(A) := ~R(B)			*/
					builder.loadLocal( pc, b );
					builder.unaryop( o );
					builder.storeLocal( pc, a );
					break;
					
				case LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
					builder.loadConstant( p.k[bx] );
					builder.storeLocal( pc, a );
					break;
					
				case LOADNIL: /*	A B	R(A):= ...:= R(A+B):= nil			*/
					builder.loadNil();
					for ( ; b>=0; a++, b-- ) {
						if ( b > 0 ) 
							builder.dup();
						builder.storeLocal( pc, a );
					}
					break;
					
				case GETTABUP: /*	A B C	R(A) := UpValue[B][RK(C)]			*/
					builder.loadUpvalue( b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.getTable();
					builder.storeLocal( pc, a );
					break;

				case GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
					builder.loadLocal( pc, b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.getTable();
					builder.storeLocal( pc, a );
					break;
					
				case SETTABUP: /*	A B C	UpValue[A][RK(B)] := RK(C)			*/
					builder.loadUpvalue( a );
					loadLocalOrConstant( p, builder, pc, b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.setTable();
					break;
	
				case SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
					builder.loadLocal( pc, a );
					loadLocalOrConstant( p, builder, pc, b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.setTable();
					break;
					
				case ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
				case SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
				case MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
				case DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
				case IDIV: /*	A B C	R(A) := RK(B) // RK(C)				*/
				case MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
				case POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
				case BAND: /*	A B C	R(A) := RK(B) & RK(C)				*/
				case BOR: /*	A B C	R(A) := RK(B) | RK(C)				*/
				case BXOR: /*	A B C	R(A) := RK(B) ~ RK(C)				*/
				case SHL: /*	A B C	R(A) := RK(B) << RK(C)				*/
				case SHR: /*	A B C	R(A) := RK(B) >> RK(C)				*/
					loadLocalOrConstant( p, builder, pc, b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.binaryop( o );
					builder.storeLocal( pc, a );
					break;
					
				case SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
					builder.loadLocal(pc,b);
					builder.dup();
					builder.storeLocal(pc, a+1);
					loadLocalOrConstant( p, builder, pc, c );
					builder.getTable();
					builder.storeLocal(pc, a);
					break;
					
				case CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
					for ( int k=b; k<=c; k++ )
						builder.loadLocal(pc, k);					
					if ( c > b+1 ) {
						builder.tobuffer();
						for ( int k=c; --k>=b; )
							builder.concatbuffer();
						builder.tovalue();
					} else {
						builder.concatvalue();
					}
					builder.storeLocal(pc, a);
					break;
					
				case LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
					builder.loadBoolean( b!=0 );
					builder.storeLocal( pc, a );
					if ( c!=0 ) 
						builder.addBranch(pc, JavaBuilder.BRANCH_GOTO, pc+2);
					break;
					
				case JMP: /*	sBx	pc+=sBx					*/
					if (a > 0) {
						for (int i = a-1; i < pi.openups.length; ++i) {
							builder.closeUpvalue(pc, i);
						}
					}
					builder.addBranch(pc, JavaBuilder.BRANCH_GOTO, pc+1+sbx);
					break;
					
				case EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
				case LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
				case LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
					loadLocalOrConstant( p, builder, pc, b );
					loadLocalOrConstant( p, builder, pc, c );
					builder.compareop(o);
					builder.addBranch(pc, (a!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
					break;
	
				case TEST: /*	A C	if not (R(A) <=> C) then pc++			*/
					builder.loadLocal( pc, a );
					builder.toBoolean();
					builder.addBranch(pc, (c!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
					break;
					
				case TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
					builder.loadLocal( pc, b );
					builder.toBoolean();
					builder.addBranch(pc, (c!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
					builder.loadLocal( pc, b );
					builder.storeLocal( pc, a );
					break;
					
				case CALL: { /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
	
					// load function
					builder.loadLocal(pc, a);
					
					// load args
					int narg = b - 1;
					switch ( narg ) {
					case 0: case 1: case 2: case 3:
						for ( int i=1; i<b; i++ )
							builder.loadLocal(pc, a+i);
						break;
					default: // fixed arg count > 3
						builder.newVarargs( pc, a+1, b-1 );
						narg = -1;
						break;
					case -1: // prev vararg result
						loadVarargResults( builder, pc, a+1, vresultbase );
						narg = -1;
						break;
					}
					
					// call or invoke
					boolean useinvoke = narg<0 || c<1 || c>2;
					if ( useinvoke )
						builder.invoke(narg);
					else
						builder.call(narg);
					
					// handle results
					switch ( c ) {
					case 1: 
						builder.pop(); 
						break;
					case 2:
						if ( useinvoke ) 
							builder.arg( 1 );
						builder.storeLocal(pc, a);
						break;
					default: // fixed result count - unpack args
						for ( int i=1; i<c; i++ ) {
							if ( i+1 < c )
								builder.dup();
							builder.arg( i );
							builder.storeLocal(pc, a+i-1);
						}
						break;
					case 0: // vararg result
						vresultbase = a;
						builder.storeVarresult();
						break;
					}
					}
					break;
					
				case TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
					
					// load function
					builder.loadLocal(pc, a);
					
					// load args
					switch ( b ) {
					case 1: 
						builder.loadNone();
						break;
					case 2: 
						builder.loadLocal(pc, a+1);
						break;
					default: // fixed arg count > 1
						builder.newVarargs( pc, a+1, b-1 );
						break;
					case 0: // prev vararg result
						loadVarargResults( builder, pc, a+1, vresultbase );
						break;
					}
					builder.newTailcallVarargs();
					builder.areturn();
					break;
					
				case RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
					if ( c == 1 ) {
						builder.loadNone();
					} else {
						switch ( b ) {
						case 0: loadVarargResults( builder, pc, a, vresultbase ); break;
						case 1: builder.loadNone(); break;
						case 2: builder.loadLocal(pc, a); break;
						default: builder.newVarargs(pc, a, b-1); break;
						}
					}
					builder.areturn(); 
					break;
					
				case FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
					builder.loadLocal(pc, a);
					builder.loadLocal(pc, a+2);
					builder.binaryop( OpCode.SUB );
					builder.storeLocal(pc, a);
					builder.addBranch(pc, JavaBuilder.BRANCH_GOTO, pc+1+sbx);
					break;
					
				case FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
					builder.loadLocal(pc, a);
					builder.loadLocal(pc, a+2);
					builder.binaryop( OpCode.ADD );
					builder.dup();
					builder.dup();
					builder.storeLocal(pc, a);
					builder.storeLocal(pc, a+3);
					builder.loadLocal(pc, a+1); // limit
					builder.loadLocal(pc, a+2); // step
					builder.testForLoop();
					builder.addBranch(pc, JavaBuilder.BRANCH_IFNE, pc+1+sbx);
					break;
				
				case TFORCALL: /* A C	R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));	*/
					builder.loadLocal(pc, a);
					builder.loadLocal(pc, a+1);
					builder.loadLocal(pc, a+2);
					builder.invoke(2);
					for ( int i=1; i<=c; i++ ) {
						if ( i < c )
							builder.dup();
						builder.arg( i );
						builder.storeLocal(pc, a+2+i);
					}
					break;
					
				case TFORLOOP:/* A sBx   if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx } */
					builder.loadLocal(pc, a+1);
					builder.dup();
					builder.storeLocal(pc, a);
					builder.isNil();
					builder.addBranch(pc, JavaBuilder.BRANCH_IFEQ, pc+1+sbx);
					break;
					
				case SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
					int index0 = (c-1)* LuaInstruction.LFIELDS_PER_FLUSH + 1;
					builder.loadLocal( pc, a );
					if ( b == 0 ) {
						int nstack = vresultbase - (a+1);
						if ( nstack > 0 ) {
							builder.setlistStack( pc, a+1, index0, nstack );
							index0 += nstack;
						}
						builder.setlistVarargs( index0, vresultbase );
					} else {
						builder.setlistStack( pc, a+1, index0, b );
						builder.pop();
					}
					break;
					
				case CLOSURE: /*	A Bx	R(A):= closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
				{
					io.github.taoguan.luaj.Prototype newp = p.p[bx];
					int nup = newp.upvalues.length;
					String protoname = pi.subprotos[bx].name;
					builder.closureCreate( protoname );
					if ( nup > 0 )
						builder.dup();
					builder.storeLocal( pc, a );
					for ( int up=0; up<nup; ++up ) {
						if ( up+1 < nup )
							builder.dup();
						io.github.taoguan.luaj.Upvaldesc u = newp.upvalues[up];
						if (u.instack)
							builder.closureInitUpvalueFromLocal( protoname, up, pc, u.idx );
						else
							builder.closureInitUpvalueFromUpvalue( protoname, up, u.idx );
					}
					break;
				}				
				case VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
					if ( b == 0 ) {
						builder.loadVarargs();
						builder.storeVarresult();
						vresultbase = a;
					} else {
						for ( int i=1; i<b; ++a, ++i ) { 
							builder.loadVarargs( i );
							builder.storeLocal(pc, a);
						}
					}
					break;				
				}
				
				// let builder process branch instructions
				builder.onEndOfLuaInstruction( pc0, line );
			}
		}
	}

	private void loadVarargResults(JavaBuilder builder, int pc, int a, int vresultbase) {
		if ( vresultbase <= a ) {
			builder.loadVarresult();
			builder.subargs( a+1-vresultbase );
		} else if ( vresultbase == a ) {
			builder.loadVarresult();
		} else {
			builder.newVarargsVarresult(pc, a, vresultbase-a);
		}
	}

	private void loadLocalOrConstant(io.github.taoguan.luaj.Prototype p, JavaBuilder builder, int pc, int borc) {
		if ( borc<=0xff )
			builder.loadLocal( pc, borc );
		else
			builder.loadConstant( p.k[borc&0xff] );
	}
}
