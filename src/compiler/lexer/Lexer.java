package compiler.lexer;

import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int pos = 0;
    private int line = 1;
    private int column = 1;
    private final Stack<Integer> indentStack = new Stack<>();

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("page", TokenType.PAGE);
        KEYWORDS.put("window", TokenType.WINDOW);
        KEYWORDS.put("column", TokenType.COLUMN);
        KEYWORDS.put("row", TokenType.ROW);
        KEYWORDS.put("text", TokenType.TEXT);
        KEYWORDS.put("button", TokenType.BUTTON);
        KEYWORDS.put("heading", TokenType.HEADING);
        KEYWORDS.put("paragraph", TokenType.PARAGRAPH);
        KEYWORDS.put("route", TokenType.ROUTE);
        KEYWORDS.put("component", TokenType.COMPONENT);
        KEYWORDS.put("database", TokenType.DATABASE);
        KEYWORDS.put("animate", TokenType.ANIMATE);
        KEYWORDS.put("fetch", TokenType.FETCH);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("in", TokenType.IN);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("import", TokenType.IMPORT);
        KEYWORDS.put("from", TokenType.FROM);
        KEYWORDS.put("as", TokenType.AS);
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("interface", TokenType.INTERFACE);
        KEYWORDS.put("enum", TokenType.ENUM);
        KEYWORDS.put("extends", TokenType.EXTENDS);
        KEYWORDS.put("implements", TokenType.IMPLEMENTS);
        KEYWORDS.put("new", TokenType.NEW);
        KEYWORDS.put("async", TokenType.ASYNC);
        KEYWORDS.put("await", TokenType.AWAIT);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("const", TokenType.CONST);
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("this", TokenType.THIS);
        KEYWORDS.put("super", TokenType.SUPER);
        KEYWORDS.put("null", TokenType.NULL);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("self", TokenType.SELF);
        KEYWORDS.put("on", TokenType.ON);
        KEYWORDS.put("click", TokenType.CLICK);
        KEYWORDS.put("enter", TokenType.ENTER);
        KEYWORDS.put("submit", TokenType.SUBMIT);
        KEYWORDS.put("change", TokenType.CHANGE);
        KEYWORDS.put("hover", TokenType.HOVER);
        KEYWORDS.put("focus", TokenType.FOCUS);
        KEYWORDS.put("blur", TokenType.BLUR);
        KEYWORDS.put("router", TokenType.ROUTER);
        KEYWORDS.put("browser", TokenType.BROWSER);
        KEYWORDS.put("app", TokenType.APP);
    }

    public Lexer(String source) {
        this.source = source;
        this.indentStack.push(0);
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            char c = source.charAt(pos);

            if (c == '#' && pos + 1 < source.length() && isHexDigit(source.charAt(pos + 1))) {
                readColor();
            } else if (c == '#') {
                skipComment();
            } else if (c == '/' && pos + 1 < source.length() && source.charAt(pos + 1) == '/') {
                skipLineComment();
            } else if (c == '\n') {
                addToken(TokenType.NEWLINE, "\\n");
                pos++;
                line++;
                column = 1;
                handleIndent();
            } else if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
                column++;
            } else if (c == '"') {
                readString('"');
            } else if (c == '\'') {
                readString('\'');
            } else if (Character.isDigit(c)) {
                readNumber();
            } else if (c == '-' && pos + 1 < source.length() && Character.isDigit(source.charAt(pos + 1))) {
                readNumber();
            } else if (isIdentifierStart(c)) {
                readIdentifier();
            } else {
                readOperator();
            }
        }

        // Emit remaining dedents
        while (indentStack.size() > 1) {
            indentStack.pop();
            addToken(TokenType.DEDENT, "DEDENT");
        }

        addToken(TokenType.EOF, "EOF");
        return tokens;
    }

    private void handleIndent() {
        int indent = 0;
        while (pos < source.length() && (source.charAt(pos) == ' ' || source.charAt(pos) == '\t')) {
            indent += (source.charAt(pos) == '\t') ? 4 : 1;
            pos++;
            column++;
        }

        // Skip blank lines
        if (pos < source.length() && source.charAt(pos) == '\n') {
            return;
        }

        int current = indentStack.peek();
        if (indent > current) {
            indentStack.push(indent);
            addToken(TokenType.INDENT, "INDENT");
        } else if (indent < current) {
            while (indentStack.peek() > indent) {
                indentStack.pop();
                addToken(TokenType.DEDENT, "DEDENT");
            }
        }
    }

    private void skipComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') {
            pos++;
            column++;
        }
    }

    private void skipLineComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') {
            pos++;
            column++;
        }
    }

    private void readString(char quote) {
        int startLine = line;
        int startCol = column;
        pos++;
        column++;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && source.charAt(pos) != quote) {
            if (source.charAt(pos) == '\\' && pos + 1 < source.length()) {
                pos++;
                column++;
                char esc = source.charAt(pos);
                switch (esc) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    case '\'': sb.append('\''); break;
                    default: sb.append(esc); break;
                }
            } else {
                if (source.charAt(pos) == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                sb.append(source.charAt(pos));
            }
            pos++;
        }

        if (pos < source.length()) {
            pos++;
            column++;
        }

        addToken(TokenType.STRING, sb.toString(), startLine, startCol);
    }

    private void readColor() {
        int startLine = line;
        int startCol = column;
        pos++; // skip '#'
        column++;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && isHexDigit(source.charAt(pos))) {
            sb.append(source.charAt(pos));
            pos++;
            column++;
        }
        addToken(TokenType.COLOR, sb.toString(), startLine, startCol);
    }

    private void readNumber() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        if (source.charAt(pos) == '-') {
            sb.append('-');
            pos++;
            column++;
        }

        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            sb.append(source.charAt(pos));
            pos++;
            column++;
        }

        if (pos < source.length() && source.charAt(pos) == '.') {
            sb.append('.');
            pos++;
            column++;
            while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
                sb.append(source.charAt(pos));
                pos++;
                column++;
            }
        }

        // Check for unit suffix (ms, px, s, em, %, etc.)
        if (pos < source.length() && Character.isLetter(source.charAt(pos))) {
            StringBuilder unit = new StringBuilder();
            while (pos < source.length() && Character.isLetter(source.charAt(pos))) {
                unit.append(source.charAt(pos));
                pos++;
                column++;
            }
            addToken(TokenType.UNIT, sb.toString() + unit.toString(), startLine, startCol);
        } else {
            addToken(TokenType.NUMBER, sb.toString(), startLine, startCol);
        }
    }

    private void readIdentifier() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && isIdentifierPart(source.charAt(pos))) {
            sb.append(source.charAt(pos));
            pos++;
            column++;
        }

        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);

        if (word.equals("true")) addToken(TokenType.TRUE, word, startLine, startCol);
        else if (word.equals("false")) addToken(TokenType.FALSE, word, startLine, startCol);
        else if (word.equals("null")) addToken(TokenType.NULL, word, startLine, startCol);
        else addToken(type, word, startLine, startCol);
    }

    private void readOperator() {
        int startLine = line;
        int startCol = column;
        char c = source.charAt(pos);
        pos++;
        column++;

        switch (c) {
            case '=':
                if (peek() == '=') { pos++; column++; addToken(TokenType.EQUAL, "==", startLine, startCol); }
                else addToken(TokenType.ASSIGN, "=", startLine, startCol);
                break;
            case '!':
                if (peek() == '=') { pos++; column++; addToken(TokenType.NOT_EQUAL, "!=", startLine, startCol); }
                else addToken(TokenType.NOT, "!", startLine, startCol);
                break;
            case '<':
                if (peek() == '=') { pos++; column++; addToken(TokenType.LESS_EQUAL, "<=", startLine, startCol); }
                else addToken(TokenType.LESS, "<", startLine, startCol);
                break;
            case '>':
                if (peek() == '=') { pos++; column++; addToken(TokenType.GREATER_EQUAL, ">=", startLine, startCol); }
                else addToken(TokenType.GREATER, ">", startLine, startCol);
                break;
            case '+':
                if (peek() == '=') { pos++; column++; addToken(TokenType.PLUS_ASSIGN, "+=", startLine, startCol); }
                else addToken(TokenType.PLUS, "+", startLine, startCol);
                break;
            case '-':
                if (peek() == '>') { pos++; column++; addToken(TokenType.ARROW, "->", startLine, startCol); }
                else if (peek() == '=') { pos++; column++; addToken(TokenType.MINUS_ASSIGN, "-=", startLine, startCol); }
                else addToken(TokenType.MINUS, "-", startLine, startCol);
                break;
            case '*':
                if (peek() == '=') { pos++; column++; addToken(TokenType.MULTIPLY_ASSIGN, "*=", startLine, startCol); }
                else addToken(TokenType.MULTIPLY, "*", startLine, startCol);
                break;
            case '/':
                if (peek() == '=') { pos++; column++; addToken(TokenType.DIVIDE_ASSIGN, "/=", startLine, startCol); }
                else addToken(TokenType.DIVIDE, "/", startLine, startCol);
                break;
            case '%': addToken(TokenType.MODULO, "%", startLine, startCol); break;
            case '&':
                if (peek() == '&') { pos++; column++; addToken(TokenType.AND, "&&", startLine, startCol); }
                break;
            case '|':
                if (peek() == '|') { pos++; column++; addToken(TokenType.OR, "||", startLine, startCol); }
                break;
            case '(': addToken(TokenType.LPAREN, "(", startLine, startCol); break;
            case ')': addToken(TokenType.RPAREN, ")", startLine, startCol); break;
            case '{': addToken(TokenType.LBRACE, "{", startLine, startCol); break;
            case '}': addToken(TokenType.RBRACE, "}", startLine, startCol); break;
            case '[': addToken(TokenType.LBRACKET, "[", startLine, startCol); break;
            case ']': addToken(TokenType.RBRACKET, "]", startLine, startCol); break;
            case ';': addToken(TokenType.SEMICOLON, ";", startLine, startCol); break;
            case ',': addToken(TokenType.COMMA, ",", startLine, startCol); break;
            case '.': addToken(TokenType.DOT, ".", startLine, startCol); break;
            case ':': addToken(TokenType.COLON, ":", startLine, startCol); break;
            case '?': addToken(TokenType.QUESTION, "?", startLine, startCol); break;
            default:
                break;
        }
    }

    private char peek() {
        return (pos + 1 < source.length()) ? source.charAt(pos + 1) : '\0';
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '@' || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '@' || c == '$';
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private void addToken(TokenType type, String value) {
        addToken(type, value, line, column);
    }

    private void addToken(TokenType type, String value, int line, int col) {
        tokens.add(new Token(type, value, line, col));
    }
}
