package io.github.taoguan.luaj.compiler.lexer;

import io.github.taoguan.luaj.LuaError;
import io.github.taoguan.luaj.LuaString;

import java.io.IOException;
import java.io.InputStream;

import static io.github.taoguan.luaj.compiler.lexer.TokenKind.*;
import static io.github.taoguan.luaj.compiler.parser.HelpNumber.*;

public class Lexer {

    private static final int EOZ = (-1);
    private static final int MAX_INT = Integer.MAX_VALUE-2;
    private static final int LUA_COMPAT_LSTR = 1;
    private static final int UCHAR_MAX = 255;

    int current;  /* current character (charint) */
    private String chunkName;
    private int line;
    // to support lookahead
    private Token cachedNextToken;
    private int lineBackup;

    private InputStream z;  /* input stream */

    char[] buff;  /* buffer for tokens */
    int nbuff; /* length of buffer */

    public Lexer(InputStream inputStream, String chunkName) throws IOException {
        this.z = inputStream;
        this.chunkName = chunkName;
        this.line = 1;
        this.nbuff = 0;   /* initialize buffer */
        this.current = this.z.read(); /* read first char */
        this.skipShebang();
    }

    public int line() {
        return cachedNextToken != null ? lineBackup : line;
    }

    <T> T error(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        msg = String.format("%s:%d: %s", chunkName, line(), msg);
        throw new LuaError(msg);
    }

    public TokenKind LookAhead() {
        if (cachedNextToken == null) {
            lineBackup = line;
            cachedNextToken = nextToken();
        }
        return cachedNextToken.getKind();
    }

    public Token nextIdentifier() {
        return nextTokenOfKind(TOKEN_IDENTIFIER);
    }

    public Token nextTokenOfKind(TokenKind kind) {
        Token token = nextToken();
        if (token.getKind() != kind) {
            error("syntax error near '%s'", token.getValue());
        }
        return token;
    }

    public Token nextToken() {
        if (cachedNextToken != null) {
            Token token = cachedNextToken;
            cachedNextToken = null;
            return token;
        }

        nbuff = 0;
        while (true) {
            switch (current) {
                case '\n':
                case '\r': {
                    inclinenumber();
                    continue;
                }
                case '-': {
                    nextChar();
                    if (current != '-')
                        return new Token(line, TOKEN_OP_MINUS, "-");
                    /* else is a comment */
                    nextChar();
                    if (current == '[') {
                        int sep = skip_sep();
                        nbuff = 0; /* `skip_sep' may dirty the buffer */
                        if (sep >= 0) {
                            read_long_string(true, sep); /* long comment */
                            nbuff = 0;
                            continue;
                        }
                    }
                    /* else short comment */
                    while (!currIsNewline() && current != EOZ)
                        nextChar();
                    continue;
                }
                case '[': {
                    int sep = skip_sep();
                    if (sep >= 0) {
                        LuaString luaString = read_long_string(false, sep);
                        return new Token(line, TOKEN_STRING, luaString);
                    } else if (sep == -1)
                        return new Token(line, TOKEN_SEP_LBRACK, "[");
                    else
                        error("invalid long string delimiter");
                }
                case '=': {
                    nextChar();
                    if (current != '=')
                        return new Token(line, TOKEN_OP_ASSIGN, "=");
                    else {
                        nextChar();
                        return new Token(line, TOKEN_OP_EQ, "==");
                    }
                }
                case '<': {
                    nextChar();
                    if (current == '<') {
                        nextChar();
                        return new Token(line, TOKEN_OP_SHL, "<<");
                    } else if (current == '=') {
                        nextChar();
                        return new Token(line, TOKEN_OP_LE, "<=");
                    } else {
                        return new Token(line, TOKEN_OP_LT, "<");
                    }
                }
                case '>': {
                    nextChar();
                    if (current == '>') {
                        nextChar();
                        return new Token(line, TOKEN_OP_SHR, ">>");
                    } else if (current == '=') {
                        nextChar();
                        return new Token(line, TOKEN_OP_GE, ">=");
                    } else {
                        return new Token(line, TOKEN_OP_GT, ">");
                    }
                }
                case '~': {
                    nextChar();
                    if (current != '=')
                        return new Token(line, TOKEN_OP_WAVE, "~");
                    else {
                        nextChar();
                        return new Token(line, TOKEN_OP_NE, "~=");
                    }
                }
                case ':': {
                    nextChar();
                    if (current != ':')
                        return new Token(line, TOKEN_SEP_COLON, ":");
                    else {
                        nextChar();
                        return new Token(line, TOKEN_SEP_LABEL, "::");
                    }
                }
                case '"':
                case '\'': {
                    LuaString shortString = read_string(current);
                    return new Token(line, TOKEN_STRING, shortString);
                }
                case '.': {
                    save_and_next();
                    if (check_next(".")) {
                        if (check_next("."))
                            return new Token(line, TOKEN_VARARG, "..."); /* ... */
                        else
                            return new Token(line, TOKEN_OP_CONCAT, ".."); /* .. */
                    } else if (!isdigit(current))
                        return new Token(line, TOKEN_SEP_DOT, ".");
                    else {
                        String strNumber = read_numeral();
                        return new Token(line, TOKEN_NUMBER, strNumber);
                    }
                }
                case '/': {
                    nextChar();
                    if (current == '/') {
                        nextChar();
                        return new Token(line, TOKEN_OP_IDIV, "//");
                    } else {
                        return new Token(line, TOKEN_OP_DIV, "/");
                    }
                }
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9': {
                    String strNumber = read_numeral();
                    return new Token(line, TOKEN_NUMBER, strNumber);
                }
                case EOZ: {
                    return new Token(line, TOKEN_EOF, "EOF");
                }
                default: {
                    if (isspace(current)) {
                        _assert (!currIsNewline());
                        nextChar();
                        continue;
                    } else if (isdigit(current)) {
                        String strNumber = read_numeral();
                        return new Token(line, TOKEN_NUMBER, strNumber);
                    } else if (isalpha(current) || current == '_') {
                        /* identifier or reserved word */
                        do {
                            save_and_next();
                        } while (isalnum(current) || current == '_');
                        String id = new String(buff, 0, nbuff);
                        return Token.keywords.containsKey(id)
                                ? new Token(line, Token.keywords.get(id), id)
                                : new Token(line, TOKEN_IDENTIFIER, id);

                    } else {
                        int c = current;
                        switch (c) {
                            case ';':
                                nextChar();
                                return new Token(line, TOKEN_SEP_SEMI, ";");
                            case ',':
                                nextChar();
                                return new Token(line, TOKEN_SEP_COMMA, ",");
                            case '(':
                                nextChar();
                                return new Token(line, TOKEN_SEP_LPAREN, "(");
                            case ')':
                                nextChar();
                                return new Token(line, TOKEN_SEP_RPAREN, ")");
                            case ']':
                                nextChar();
                                return new Token(line, TOKEN_SEP_RBRACK, "]");
                            case '{':
                                nextChar();
                                return new Token(line, TOKEN_SEP_LCURLY, "{");
                            case '}':
                                nextChar();
                                return new Token(line, TOKEN_SEP_RCURLY, "}");
                            case '+':
                                nextChar();
                                return new Token(line, TOKEN_OP_ADD, "+");
                            case '-':
                                nextChar();
                                return new Token(line, TOKEN_OP_MINUS, "-");
                            case '*':
                                nextChar();
                                return new Token(line, TOKEN_OP_MUL, "*");
                            case '^':
                                nextChar();
                                return new Token(line, TOKEN_OP_POW, "^");
                            case '%':
                                nextChar();
                                return new Token(line, TOKEN_OP_MOD, "%");
                            case '&':
                                nextChar();
                                return new Token(line, TOKEN_OP_BAND, "&");
                            case '|':
                                nextChar();
                                return new Token(line, TOKEN_OP_BOR, "|");
                            case '#':
                                nextChar();
                                return new Token(line, TOKEN_OP_LEN, "#");
                            /* single-char tokens (+ - / ...) */
                        }

                        return error("unexpected symbol near %c", current);
                    }
                }
            }
        }
    }

    void inclinenumber() {
        int old = current;
        _assert( currIsNewline() );
        nextChar(); /* skip '\n' or '\r' */
        if ( currIsNewline() && current != old )
            nextChar(); /* skip '\n\r' or '\r\n' */
        if ( ++line >= MAX_INT )
            error("chunk has too many lines");
    }
    static void _assert(boolean b) {
        if (!b)
            throw new LuaError("compiler assert failed");
    }

    private void nextChar() {
        try {
            current = z.read();
        } catch ( IOException e ) {
            e.printStackTrace();
            current = EOZ;
        }
    }

    private void skipShebang() {
        if ( current == '#' )
            while (!currIsNewline() && current != EOZ)
                nextChar();
    }

    private int skip_sep() {
        int count = 0;
        int s = current;
        _assert (s == '[' || s == ']');
        save_and_next();
        while (current == '=') {
            save_and_next();
            count++;
        }
        return (current == s) ? count : (-count) - 1;
    }

    void save_and_next() {
        save( current );
        nextChar();
    }

    void save(int c) {
        if ( buff == null || nbuff + 1 > buff.length )
            buff = realloc( buff, nbuff*2+1 );
        buff[nbuff++] = (char) c;
    }

    LuaString read_long_string(boolean isComment,  int sep) {
        int cont = 0;
        save_and_next(); /* skip 2nd `[' */
        if (currIsNewline()) /* string starts with a newline? */
            inclinenumber(); /* skip it */
        for (boolean endloop = false; !endloop;) {
            switch (current) {
                case EOZ:
                    error(isComment ? "unfinished long comment"
                            : "unfinished string");
                    break; /* to avoid warnings */
                case '[': {
                    if (skip_sep() == sep) {
                        save_and_next(); /* skip 2nd `[' */
                        cont++;
                        if (LUA_COMPAT_LSTR == 1) {
                            if (sep == 0)
                                error("nesting of [[...]] is deprecated");
                        }
                    }
                    break;
                }
                case ']': {
                    if (skip_sep() == sep) {
                        save_and_next(); /* skip 2nd `]' */
                        if (LUA_COMPAT_LSTR == 2) {
                            cont--;
                            if (sep == 0 && cont >= 0)
                                break;
                        }
                        endloop = true;
                    }
                    break;
                }
                case '\n':
                case '\r': {
                    save('\n');
                    inclinenumber();
                    if (isComment)
                        nbuff = 0; /* avoid wasting space */
                    break;
                }
                default: {
                    if (isComment)
                        nextChar();
                    else
                        save_and_next();
                }
            }
        }
        if(isComment){
            return LuaString.valueOf("");
        }else {
            return LuaString.valueOf(buff, 2 + sep, nbuff - 2 * (2 + sep));
        }
    }

    LuaString read_string(int del) {
        save_and_next();
        while (current != del) {
            switch (current) {
                case EOZ:
                case '\n':
                case '\r':
                    error("unfinished string");
                    continue; /* to avoid warnings */
                case '\\': {
                    int c;
                    nextChar(); /* do not save the `\' */
                    switch (current) {
                        case 'a': /* bell */
                            c = '\u0007';
                            break;
                        case 'b': /* backspace */
                            c = '\b';
                            break;
                        case 'f': /* form feed */
                            c = '\f';
                            break;
                        case 'n': /* newline */
                            c = '\n';
                            break;
                        case 'r': /* carriage return */
                            c = '\r';
                            break;
                        case 't': /* tab */
                            c = '\t';
                            break;
                        case 'v': /* vertical tab */
                            c = '\u000B';
                            break;
                        case 'x':
                            c = readhexaesc();
                            break;
                        case '\n': /* go through */
                        case '\r':
                            save('\n');
                            inclinenumber();
                            continue;
                        case EOZ:
                            continue; /* will raise an error next loop */
                        case 'z': {  /* zap following span of spaces */
                            nextChar();  /* skip the 'z' */
                            while (isspace(current)) {
                                if (currIsNewline()) inclinenumber();
                                else nextChar();
                            }
                            continue;
                        }
                        default: {
                            if (!isdigit(current))
                                save_and_next(); /* handles \\, \", \', and \? */
                            else { /* \xxx */
                                int i = 0;
                                c = 0;
                                do {
                                    c = 10 * c + (current - '0');
                                    nextChar();
                                } while (++i < 3 && isdigit(current));
                                if (c > UCHAR_MAX)
                                    error("escape sequence too large");
                                save(c);
                            }
                            continue;
                        }
                    }
                    save(c);
                    nextChar();
                    continue;
                }
                default:
                    save_and_next();
            }
        }
        save_and_next(); /* skip delimiter */
        return LuaString.valueOf(buff, 1, nbuff-2);
    }

    String read_numeral() {
        String expo = "Ee";
        int first = current;
        _assert (isdigit(current));
        save_and_next();
        if (first == '0' && check_next("Xx"))
            expo = "Pp";
        while (true) {
            if (check_next(expo))
                check_next("+-");
            if(isxdigit(current) || current == '.')
                save_and_next();
            else
                break;
        }
        //save('\0');
        String str = new String(buff, 0, nbuff);
        return str;
    }

    boolean check_next(String set) {
        if (set.indexOf(current) < 0)
            return false;
        save_and_next();
        return true;
    }

    int readhexaesc() {
        nextChar();
        int c1 = current;
        nextChar();
        int c2 = current;
        if (!isxdigit(c1) || !isxdigit(c2))
            error("hexadecimal digit expected 'x"+((char)c1)+((char)c2));
        return (hexvalue(c1) << 4) + hexvalue(c2);
    }



    static char[] realloc(char[] v, int n) {
        char[] a = new char[n];
        if ( v != null )
            System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
        return a;
    }

    private boolean currIsNewline() {
        return current == '\n' || current == '\r';
    }

}
