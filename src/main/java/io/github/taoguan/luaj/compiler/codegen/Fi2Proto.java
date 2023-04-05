package io.github.taoguan.luaj.compiler.codegen;


import io.github.taoguan.luaj.*;

import java.util.List;

class Fi2Proto {

    static io.github.taoguan.luaj.Prototype toProto(FuncInfo fi) {
        io.github.taoguan.luaj.Prototype proto = new io.github.taoguan.luaj.Prototype();
        proto.linedefined = fi.line;
        proto.lastlinedefined = fi.lastLine;
        proto.numparams =fi.numParams;
        proto.maxstacksize = fi.maxRegs;
        proto.code = (fi.insts.stream().mapToInt(Integer::intValue).toArray());
        proto.k = (getConstants(fi));
        proto.upvalues = (getUpvalues(fi));
        proto.p = (toProtos(fi.subFuncs));
        proto.lineinfo = (fi.lineNums.stream().mapToInt(Integer::intValue).toArray());
        proto.locvars = (getLocVars(fi));

        if (fi.line == 0) {
            proto.lastlinedefined = (0);
        }
        if (proto.maxstacksize < 2) {
            proto.maxstacksize = 2; // todo
        }
        if (fi.isVararg) {
            proto.is_vararg = ((byte) 1); // todo
        }

        return proto;
    }

    private static io.github.taoguan.luaj.Prototype[] toProtos(List<FuncInfo> fis) {
        return fis.stream()
                .map(Fi2Proto::toProto)
                .toArray(io.github.taoguan.luaj.Prototype[]::new);
    }

    private static io.github.taoguan.luaj.LuaValue[] getConstants(FuncInfo fi) {
        io.github.taoguan.luaj.LuaValue[] consts = new io.github.taoguan.luaj.LuaValue[fi.constants.size()];
        fi.constants.forEach((c, idx) -> consts[idx] = c);
        return consts;
    }

    private static io.github.taoguan.luaj.LocVars[] getLocVars(FuncInfo fi) {
        return fi.locVars.stream()
                .map(locVarInfo -> {
                    io.github.taoguan.luaj.LocVars var = new io.github.taoguan.luaj.LocVars(io.github.taoguan.luaj.LuaString.valueOf(locVarInfo.name), locVarInfo.startPC, locVarInfo.endPC);
                    return var;
                })
                .toArray(io.github.taoguan.luaj.LocVars[]::new);
    }

    private static io.github.taoguan.luaj.Upvaldesc[] getUpvalues(FuncInfo fi) {
        io.github.taoguan.luaj.Upvaldesc[] upvals = new io.github.taoguan.luaj.Upvaldesc[fi.upvalues.size()];

        fi.upvalues.forEach((name, uvInfo) -> {
            boolean instack = uvInfo.locVarSlot >= 0 ? true : false;
            int idx = uvInfo.locVarSlot >= 0 ? uvInfo.locVarSlot : uvInfo.upvalIndex;
            io.github.taoguan.luaj.Upvaldesc upval = new io.github.taoguan.luaj.Upvaldesc(io.github.taoguan.luaj.LuaString.valueOf(name), instack, idx);
            upvals[uvInfo.index] = upval;
        });
        return upvals;
    }

    private static String[] getUpvalueNames(FuncInfo fi) {
        String[] names = new String[fi.upvalues.size()];
        fi.upvalues.forEach((name, uvInfo) -> names[uvInfo.index] = name);
        return names;
    }

}
