package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.LuaDouble;
import io.github.taoguan.luaj.LuaNumber;
import io.github.taoguan.luaj.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FloatExp extends Exp {

    private LuaNumber luaNumber;

    public FloatExp(int line, Double val) {
        setLine(line);
        this.luaNumber = LuaDouble.valueOf(val);
    }

    public FloatExp(int line, LuaNumber luaNumber) {
        setLine(line);
        this.luaNumber = luaNumber;
    }

    public double getVal(){
        return luaNumber.todouble();
    }

    public void setVal(double val){
        this.luaNumber = LuaDouble.valueOf(val);
    }

}
