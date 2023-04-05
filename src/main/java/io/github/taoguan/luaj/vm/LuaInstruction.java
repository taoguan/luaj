package io.github.taoguan.luaj.vm;

public class LuaInstruction {

    /** version is supplied by ant build task */
    public static final String _VERSION = "Luaj 0.0";

    /* number of list items to accumulate before a SETLIST instruction */
    public static final int LFIELDS_PER_FLUSH = 50;

    /*
     ** size and position of opcode arguments.
     */
    public static final int SIZE_C		= 9;
    public static final int SIZE_B		= 9;
    public static final int SIZE_Bx		= (SIZE_C + SIZE_B);
    public static final int SIZE_A		= 8;
    public static final int SIZE_Ax		= (SIZE_C + SIZE_B + SIZE_A);

    public static final int SIZE_OP		= 6;

    public static final int POS_OP		= 0;
    public static final int POS_A		= (POS_OP + SIZE_OP);
    public static final int POS_C		= (POS_A + SIZE_A);
    public static final int POS_B		= (POS_C + SIZE_C);
    public static final int POS_Bx		= POS_C;
    public static final int POS_Ax		= POS_A;


    public static final int MAX_OP          = ((1<<SIZE_OP)-1);
    public static final int MAXARG_A        = ((1<<SIZE_A)-1);
    public static final int MAXARG_B        = ((1<<SIZE_B)-1);
    public static final int MAXARG_C        = ((1<<SIZE_C)-1);
    public static final int MAXARG_Bx       = ((1<<SIZE_Bx)-1); // 262143
    public static final int MAXARG_sBx      = (MAXARG_Bx>>1);   // 131071  	/* `sBx' is signed */
    public static final int MAXARG_Ax       = ((1<<SIZE_Ax)-1);

    public static final int MASK_OP = ((1<<SIZE_OP)-1)<<POS_OP;
    public static final int MASK_A  = ((1<<SIZE_A)-1)<<POS_A;
    public static final int MASK_B  = ((1<<SIZE_B)-1)<<POS_B;
    public static final int MASK_C  = ((1<<SIZE_C)-1)<<POS_C;
    public static final int MASK_Bx = ((1<<SIZE_Bx)-1)<<POS_Bx;

    public static final int MASK_NOT_OP = ~MASK_OP;
    public static final int MASK_NOT_A  = ~MASK_A;
    public static final int MASK_NOT_B  = ~MASK_B;
    public static final int MASK_NOT_C  = ~MASK_C;
    public static final int MASK_NOT_Bx = ~MASK_Bx;

    public static OpCode getOpCode(int i) {
        int index = i & 0x3F;
        if(index >= OpCode.values().length){
            return null;
        }
        return OpCode.values()[index];
    }

    public static int getA(int i) {
        return (i >> 6) & 0xFF;
    }

    public static int getC(int i) {
        return (i >> 14) & 0x1FF;
    }

    public static int getB(int i) {
        return i >>> 23;
        //return (i >> 23) & 0x1FF;
    }

    public static int getBx(int i) {
        return i >>> 14;
    }

    public static int getSBx(int i) {
        return getBx(i) - MAXARG_sBx;
    }

    public static int getAx(int i) {
        return i >>> 6;
    }

    /** this bit 1 means constant (0 means register) */
    public static final int BITRK		= (1 << (SIZE_B - 1));

    /** test whether value is a constant */
    public static boolean ISK(int x) {
        return 0 != ((x) & BITRK);
    }

    /** gets the index of the constant */
    public static int INDEXK(int r) {
        return ((int)(r) & ~BITRK);
    }

    public static final int MAXINDEXRK	= (BITRK - 1);

    /** code a constant index as a RK value */
    public static int RKASK(int x) {
        return ((x) | BITRK);
    }

}
