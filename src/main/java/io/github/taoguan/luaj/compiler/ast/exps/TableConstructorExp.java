package io.github.taoguan.luaj.compiler.ast.exps;

import io.github.taoguan.luaj.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TableConstructorExp extends Exp {

    private List<Exp> keyExps = new ArrayList<>();
    private List<Exp> valExps = new ArrayList<>();

    public void addKey(Exp key) {
        keyExps.add(key);
    }

    public void addVal(Exp val) {
        valExps.add(val);
    }

}
