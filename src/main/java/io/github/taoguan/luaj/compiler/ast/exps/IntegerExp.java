package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.LuaInteger;
import io.github.taoguan.luaj.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntegerExp extends Exp {

    private LuaInteger luaInteger;

    public IntegerExp(int line, int val) {
        this.luaInteger = LuaInteger.valueOf(val);
        setLine(line);
    }

    public IntegerExp(int line, LuaInteger luaInteger) {
        this.luaInteger = luaInteger;
        setLine(line);
    }

    public int getVal(){
        return luaInteger.toint();
    }

    public void setVal(int val){
        luaInteger = LuaInteger.valueOf(val);
    }

}
