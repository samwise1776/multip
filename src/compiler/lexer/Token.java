package compiler.lexer;

public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    public boolean is(TokenType t) { return type == t; }
    public boolean is(TokenType... types) {
        for (TokenType t : types) if (type == t) return true;
        return false;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")@" + line + ":" + column;
    }
}
