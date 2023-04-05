package io.github.taoguan.luaj.compiler.parser;

import io.github.taoguan.luaj.LuaValue;
import io.github.taoguan.luaj.lib.MathLib;

public class HelpNumber {

    public static boolean isInteger(double f) {
        return f == (int) f;
    }

    public static  boolean isalnum(int c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c == '_');
    }

    public static  boolean isalpha(int c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

    public static  boolean isdigit(int c) {
        return (c >= '0' && c <= '9');
    }

    public static  boolean isxdigit(int c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    public static  boolean isspace(int c) {
        return (c <= ' ');
    }

    public static int hexvalue(int c) {
        return c <= '9'? c - '0': c <= 'F'? c + 10 - 'A': c + 10 - 'a';
    }

    public static LuaValue strx2number(String str) {
        char[] c = str.toCharArray();
        int s = 0;
        while ( s < c.length && isspace(c[s]))
            ++s;
        // Check for negative sign
        double sgn = 1.0;
        if (s < c.length && c[s] == '-') {
            sgn = -1.0;
            ++s;
        }
        /* Check for "0x" */
        if (s + 2 >= c.length )
            return LuaValue.ZERO;
        if (c[s++] != '0')
            return LuaValue.ZERO;
        if (c[s] != 'x' && c[s] != 'X')
            return LuaValue.ZERO;
        ++s;

        // read integer part.
        double m = 0;
        int e = 0;
        while (s < c.length && isxdigit(c[s]))
            m = (m * 16) + hexvalue(c[s++]);
        if (s < c.length && c[s] == '.') {
            ++s;  // skip dot
            while (s < c.length && isxdigit(c[s])) {
                m = (m * 16) + hexvalue(c[s++]);
                e -= 4;  // Each fractional part shifts right by 2^4
            }
        }
        if (s < c.length && (c[s] == 'p' || c[s] == 'P')) {
            ++s;
            int exp1 = 0;
            boolean neg1 = false;
            if (s < c.length && c[s] == '-') {
                neg1 = true;
                ++s;
            }
            while (s < c.length && isdigit(c[s]))
                exp1 = exp1 * 10 + c[s++] - '0';
            if (neg1)
                exp1 = -exp1;
            e += exp1;
        }
        return LuaValue.valueOf(sgn * m * MathLib.dpow_d(2.0, e));
    }

    public static LuaValue str2d(String str) {
        if (str.indexOf('n')>=0 || str.indexOf('N')>=0)
            return LuaValue.ZERO;
        else if (str.indexOf('x')>=0 || str.indexOf('X')>=0)
            return strx2number(str);
        else
            return LuaValue.valueOf(Double.parseDouble(str.trim()));
    }

}
