package io.github.taoguan.luaj.compiler.ast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Node {

    private int line;
    private int lastLine;

}
