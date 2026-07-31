package compiler.parser;

import compiler.ast.ASTNode;
import compiler.lexer.*;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            skipNewlines();
            if (check(TokenType.EOF)) break;
            stmts.add(parseStatement());
        }
        return new ASTNode.Program(stmts);
    }

    private void skipNewlines() {
        while (check(TokenType.NEWLINE, TokenType.INDENT, TokenType.DEDENT)) advance();
    }

    private ASTNode parseStatement() {
        if (check(TokenType.PAGE)) return parsePage();
        if (check(TokenType.ROUTE)) return parseRoute();
        if (check(TokenType.COMPONENT)) return parseComponent();
        if (check(TokenType.WINDOW)) return parseWindow();
        if (check(TokenType.COLUMN) || check(TokenType.ROW) ||
            check(TokenType.TEXT) || check(TokenType.BUTTON) ||
            check(TokenType.HEADING) || check(TokenType.PARAGRAPH)) return parseUIElement();
        if (check(TokenType.VAR) || check(TokenType.CONST) || check(TokenType.LET)) return parseVarDecl();
        if (check(TokenType.IF)) return parseIf();
        if (check(TokenType.WHILE)) return parseWhile();
        if (check(TokenType.FOR)) return parseFor();
        if (check(TokenType.RETURN)) return parseReturn();
        if (check(TokenType.FUNCTION) || check(TokenType.ASYNC)) return parseFunction();
        if (check(TokenType.CLASS)) return parseClass();
        if (check(TokenType.INTERFACE)) return parseInterface();
        if (check(TokenType.ENUM)) return parseEnum();
        if (check(TokenType.IMPORT)) return parseImport();
        if (check(TokenType.PRINT)) return parsePrint();
        if (check(TokenType.ANIMATE)) return parseAnimate();
        if (check(TokenType.DATABASE)) return parseDatabase();
        if (check(TokenType.LBRACE)) return parseBlock();
        if (check(TokenType.FETCH)) return parseFetch();
        return parseExpressionStatement();
    }

    private ASTNode parsePage() {
        expect(TokenType.PAGE);
        String name = expect(TokenType.IDENTIFIER).getValue();
        List<ASTNode> body = parseBody();
        return new ASTNode.Page(name, body);
    }

    private ASTNode parseRoute() {
        expect(TokenType.ROUTE);
        String path = expect(TokenType.STRING).getValue();
        List<ASTNode> body = parseBody();
        return new ASTNode.Route(path, body);
    }

    private ASTNode parseComponent() {
        expect(TokenType.COMPONENT);
        String name = expect(TokenType.IDENTIFIER).getValue();
        List<String> params = new ArrayList<>();
        if (check(TokenType.LPAREN)) {
            expect(TokenType.LPAREN);
            if (!check(TokenType.RPAREN)) {
                params.add(expect(TokenType.IDENTIFIER).getValue());
                while (check(TokenType.COMMA)) {
                    expect(TokenType.COMMA);
                    params.add(expect(TokenType.IDENTIFIER).getValue());
                }
            }
            expect(TokenType.RPAREN);
        }
        List<ASTNode> body = parseBody();
        return new ASTNode.Component(name, params, body);
    }

    private ASTNode parseWindow() {
        expect(TokenType.WINDOW);
        Map<String, ASTNode> props = new LinkedHashMap<>();
        List<ASTNode> children = new ArrayList<>();

        if (check(TokenType.LBRACE)) {
            expect(TokenType.LBRACE);
            while (!check(TokenType.RBRACE) && !check(TokenType.DEDENT) && !check(TokenType.EOF)) {
                skipNewlines();
                if (check(TokenType.RBRACE) || check(TokenType.DEDENT) || check(TokenType.EOF)) break;
                if (isPropertyName() && peekNext().is(TokenType.ASSIGN)) {
                    String key = advance().getValue();
                    expect(TokenType.ASSIGN);
                    ASTNode value = parseExpression();
                    props.put(key, value);
                } else {
                    children.add(parseStatement());
                }
            }
            // Skip DEDENT tokens before closing brace
            while (check(TokenType.DEDENT)) advance();
            expect(TokenType.RBRACE);
        } else {
            children = parseBody();
        }
        return new ASTNode.Window(props, children);
    }

    private ASTNode parseUIElement() {
        String tag = advance().getValue();
        Map<String, ASTNode> props = new LinkedHashMap<>();
        List<ASTNode> children = new ArrayList<>();
        List<ASTNode.EventBinding> events = new ArrayList<>();

        if (check(TokenType.LBRACE)) {
            expect(TokenType.LBRACE);
            while (!check(TokenType.RBRACE) && !check(TokenType.DEDENT) && !check(TokenType.EOF)) {
                skipNewlines();
                if (check(TokenType.RBRACE) || check(TokenType.DEDENT) || check(TokenType.EOF)) break;
                if (check(TokenType.ON)) {
                    advance();
                    String event = advance().getValue();
                    List<ASTNode> body = parseBody();
                    events.add(new ASTNode.EventBinding(event, body));
                } else if (isPropertyName() && peekNext().is(TokenType.ASSIGN)) {
                    String key = advance().getValue();
                    expect(TokenType.ASSIGN);
                    ASTNode value = parseExpression();
                    props.put(key, value);
                } else {
                    children.add(parseStatement());
                }
            }
            // Skip DEDENT tokens before closing brace
            while (check(TokenType.DEDENT)) advance();
            expect(TokenType.RBRACE);
        }
        return new ASTNode.UIElement(tag, props, children, events);
    }

    private ASTNode parseVarDecl() {
        boolean isConst = check(TokenType.CONST);
        advance();
        String name = expect(TokenType.IDENTIFIER).getValue();
        ASTNode value = null;
        if (check(TokenType.ASSIGN)) {
            expect(TokenType.ASSIGN);
            value = parseExpression();
        }
        return new ASTNode.Var(name, value, isConst);
    }

    private ASTNode parseIf() {
        expect(TokenType.IF);
        ASTNode cond = parseExpression();
        List<ASTNode> thenBody = parseBody();
        List<ASTNode> elseBody = null;
        if (check(TokenType.ELSE)) {
            advance();
            if (check(TokenType.IF)) {
                elseBody = new ArrayList<>();
                elseBody.add(parseIf());
            } else {
                elseBody = parseBody();
            }
        }
        return new ASTNode.If(cond, thenBody, elseBody);
    }

    private ASTNode parseWhile() {
        expect(TokenType.WHILE);
        ASTNode cond = parseExpression();
        List<ASTNode> body = parseBody();
        return new ASTNode.While(cond, body);
    }

    private ASTNode parseFor() {
        expect(TokenType.FOR);
        String var = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.IN);
        ASTNode iterable = parseExpression();
        List<ASTNode> body = parseBody();
        return new ASTNode.For(var, iterable, body);
    }

    private ASTNode parseReturn() {
        expect(TokenType.RETURN);
        ASTNode value = null;
        if (!check(TokenType.NEWLINE) && !check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            value = parseExpression();
        }
        return new ASTNode.Return(value);
    }

    private ASTNode parseFunction() {
        boolean isAsync = check(TokenType.ASYNC);
        if (isAsync) advance();
        expect(TokenType.FUNCTION);
        String name = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.LPAREN);
        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(expect(TokenType.IDENTIFIER).getValue());
            while (check(TokenType.COMMA)) {
                expect(TokenType.COMMA);
                params.add(expect(TokenType.IDENTIFIER).getValue());
            }
        }
        expect(TokenType.RPAREN);
        List<ASTNode> body = parseBody();
        return new ASTNode.Function(name, params, body, isAsync);
    }

    private ASTNode parseClass() {
        expect(TokenType.CLASS);
        String name = expect(TokenType.IDENTIFIER).getValue();
        String superClass = null;
        List<String> interfaces = new ArrayList<>();
        if (check(TokenType.EXTENDS)) {
            advance();
            superClass = expect(TokenType.IDENTIFIER).getValue();
        }
        if (check(TokenType.IMPLEMENTS)) {
            advance();
            interfaces.add(expect(TokenType.IDENTIFIER).getValue());
            while (check(TokenType.COMMA)) {
                expect(TokenType.COMMA);
                interfaces.add(expect(TokenType.IDENTIFIER).getValue());
            }
        }
        List<ASTNode> members = parseBody();
        return new ASTNode.ClassDef(name, superClass, interfaces, members);
    }

    private ASTNode parseInterface() {
        expect(TokenType.INTERFACE);
        String name = expect(TokenType.IDENTIFIER).getValue();
        List<ASTNode> members = parseBody();
        return new ASTNode.InterfaceDef(name, members);
    }

    private ASTNode parseEnum() {
        expect(TokenType.ENUM);
        String name = expect(TokenType.IDENTIFIER).getValue();
        List<String> values = new ArrayList<>();
        expect(TokenType.LBRACE);
        values.add(expect(TokenType.IDENTIFIER).getValue());
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            if (!check(TokenType.RBRACE)) {
                values.add(expect(TokenType.IDENTIFIER).getValue());
            }
        }
        expect(TokenType.RBRACE);
        return new ASTNode.EnumDef(name, values);
    }

    private ASTNode parseImport() {
        expect(TokenType.IMPORT);
        String path = expect(TokenType.STRING).getValue();
        String alias = null;
        if (check(TokenType.AS)) {
            advance();
            alias = expect(TokenType.IDENTIFIER).getValue();
        }
        return new ASTNode.Import(path, alias);
    }

    private ASTNode parsePrint() {
        expect(TokenType.PRINT);
        expect(TokenType.LPAREN);
        ASTNode value = parseExpression();
        expect(TokenType.RPAREN);
        return new ASTNode.Print(value);
    }

    private ASTNode parseFetch() {
        expect(TokenType.FETCH);
        expect(TokenType.LPAREN);
        ASTNode url = parseExpression();
        ASTNode options = null;
        if (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            options = parseExpression();
        }
        expect(TokenType.RPAREN);
        return new ASTNode.Fetch(url, options);
    }

    private ASTNode parseAnimate() {
        expect(TokenType.ANIMATE);
        String target = advance().getValue();
        Map<String, ASTNode> props = new LinkedHashMap<>();
        List<ASTNode.AnimationStep> steps = new ArrayList<>();

        expect(TokenType.LBRACE);
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            if (check(TokenType.IDENTIFIER) && peekNext().is(TokenType.ASSIGN)) {
                String key = expect(TokenType.IDENTIFIER).getValue();
                expect(TokenType.ASSIGN);
                ASTNode value = parseExpression();
                props.put(key, value);
            } else {
                String prop = advance().getValue();
                expect(TokenType.LBRACE);
                ASTNode from = null, to = null;
                while (!check(TokenType.RBRACE)) {
                    String field = advance().getValue();
                    expect(TokenType.ASSIGN);
                    ASTNode val = parseExpression();
                    if (field.equals("from")) from = val;
                    else if (field.equals("to")) to = val;
                }
                expect(TokenType.RBRACE);
                steps.add(new ASTNode.AnimationStep(prop, from, to));
            }
        }
        expect(TokenType.RBRACE);
        return new ASTNode.Animate(target, props, steps);
    }

    private ASTNode parseDatabase() {
        expect(TokenType.DATABASE);
        String name = expect(TokenType.IDENTIFIER).getValue();
        return new ASTNode.Database(name, "declare", null);
    }

    private ASTNode parseBlock() {
        expect(TokenType.LBRACE);
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        expect(TokenType.RBRACE);
        return new ASTNode.Block(stmts);
    }

    private ASTNode parseExpressionStatement() {
        return parseExpression();
    }

    // ─── Expression Parsing ─────────────────────────

    private ASTNode parseExpression() {
        return parseAssignment();
    }

    private ASTNode parseAssignment() {
        ASTNode left = parseOr();
        if (check(TokenType.ASSIGN) || check(TokenType.PLUS_ASSIGN) ||
            check(TokenType.MINUS_ASSIGN) || check(TokenType.MULTIPLY_ASSIGN) ||
            check(TokenType.DIVIDE_ASSIGN)) {
            String op = advance().getValue();
            ASTNode right = parseExpression();
            return new ASTNode.Assign(left, right, op);
        }
        return left;
    }

    private ASTNode parseOr() {
        ASTNode left = parseAnd();
        while (check(TokenType.OR)) {
            advance();
            ASTNode right = parseAnd();
            left = new ASTNode.Binary(left, right, "||");
        }
        return left;
    }

    private ASTNode parseAnd() {
        ASTNode left = parseEquality();
        while (check(TokenType.AND)) {
            advance();
            ASTNode right = parseEquality();
            left = new ASTNode.Binary(left, right, "&&");
        }
        return left;
    }

    private ASTNode parseEquality() {
        ASTNode left = parseComparison();
        while (check(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            String op = advance().getValue();
            ASTNode right = parseComparison();
            left = new ASTNode.Binary(left, right, op);
        }
        return left;
    }

    private ASTNode parseComparison() {
        ASTNode left = parseAddSub();
        while (check(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            String op = advance().getValue();
            ASTNode right = parseAddSub();
            left = new ASTNode.Binary(left, right, op);
        }
        return left;
    }

    private ASTNode parseAddSub() {
        ASTNode left = parseMulDiv();
        while (check(TokenType.PLUS, TokenType.MINUS)) {
            String op = advance().getValue();
            ASTNode right = parseMulDiv();
            left = new ASTNode.Binary(left, right, op);
        }
        return left;
    }

    private ASTNode parseMulDiv() {
        ASTNode left = parseUnary();
        while (check(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            String op = advance().getValue();
            ASTNode right = parseUnary();
            left = new ASTNode.Binary(left, right, op);
        }
        return left;
    }

    private ASTNode parseUnary() {
        if (check(TokenType.MINUS, TokenType.NOT)) {
            String op = advance().getValue();
            ASTNode operand = parseUnary();
            return new ASTNode.Unary(operand, op, true);
        }
        return parsePostfix();
    }

    private ASTNode parsePostfix() {
        ASTNode expr = parsePrimary();
        while (true) {
            if (check(TokenType.LPAREN)) {
                expect(TokenType.LPAREN);
                List<ASTNode> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    args.add(parseExpression());
                    while (check(TokenType.COMMA)) {
                        expect(TokenType.COMMA);
                        args.add(parseExpression());
                    }
                }
                expect(TokenType.RPAREN);
                expr = new ASTNode.Call(expr, args);
            } else if (check(TokenType.DOT)) {
                advance();
                String prop = expect(TokenType.IDENTIFIER).getValue();
                expr = new ASTNode.Property(expr, prop);
            } else if (check(TokenType.LBRACKET)) {
                advance();
                ASTNode index = parseExpression();
                expect(TokenType.RBRACKET);
                expr = new ASTNode.Index(expr, index);
            } else {
                break;
            }
        }
        return expr;
    }

    private ASTNode parsePrimary() {
        skipNewlines();
        if (check(TokenType.STRING)) {
            return new ASTNode.Str(advance().getValue());
        }
        if (check(TokenType.NUMBER)) {
            String val = advance().getValue();
            boolean isInt = !val.contains(".");
            return new ASTNode.Num(Double.parseDouble(val), isInt);
        }
        if (check(TokenType.UNIT)) {
            String val = advance().getValue();
            String num = val.replaceAll("[a-zA-Z]+$", "");
            String unit = val.replaceFirst(num, "");
            return new ASTNode.Unit(Double.parseDouble(num), unit);
        }
        if (check(TokenType.TRUE)) { advance(); return new ASTNode.Bool(true); }
        if (check(TokenType.FALSE)) { advance(); return new ASTNode.Bool(false); }
        if (check(TokenType.NULL)) { advance(); return new ASTNode.Null(); }
        if (check(TokenType.COLOR)) {
            return new ASTNode.Color(advance().getValue());
        }
        if (check(TokenType.LPAREN)) {
            advance();
            ASTNode expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }
        if (check(TokenType.LBRACKET)) {
            advance();
            List<ASTNode> elements = new ArrayList<>();
            if (!check(TokenType.RBRACKET)) {
                elements.add(parseExpression());
                while (check(TokenType.COMMA)) {
                    expect(TokenType.COMMA);
                    if (!check(TokenType.RBRACKET)) elements.add(parseExpression());
                }
            }
            expect(TokenType.RBRACKET);
            return new ASTNode.Array(elements);
        }
        if (check(TokenType.LBRACE)) {
            advance();
            List<Map.Entry<String, ASTNode>> entries = new ArrayList<>();
            if (!check(TokenType.RBRACE)) {
                do {
                    String key = expect(TokenType.IDENTIFIER).getValue();
                    expect(TokenType.COLON);
                    ASTNode value = parseExpression();
                    entries.add(new AbstractMap.SimpleEntry<>(key, value));
                } while (check(TokenType.COMMA) && advance() != null);
            }
            expect(TokenType.RBRACE);
            return new ASTNode.MapLit(entries);
        }
        if (check(TokenType.IDENTIFIER)) {
            return new ASTNode.Identifier(advance().getValue());
        }
        if (check(TokenType.FETCH)) {
            return parseFetch();
        }
        throw error("Unexpected token: " + current());
    }

    // ─── Helpers ────────────────────────────────────

    private List<ASTNode> parseBody() {
        skipNewlines();
        if (check(TokenType.LBRACE)) {
            expect(TokenType.LBRACE);
            List<ASTNode> stmts = new ArrayList<>();
            while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
                skipNewlines();
                if (check(TokenType.RBRACE) || check(TokenType.EOF)) break;
                stmts.add(parseStatement());
            }
            expect(TokenType.RBRACE);
            return stmts;
        }
        if (check(TokenType.INDENT)) {
            advance(); // consume INDENT
            List<ASTNode> stmts = new ArrayList<>();
            while (!check(TokenType.DEDENT) && !check(TokenType.EOF)) {
                skipNewlines();
                if (check(TokenType.DEDENT) || check(TokenType.EOF)) break;
                stmts.add(parseStatement());
            }
            if (check(TokenType.DEDENT)) advance(); // consume DEDENT
            return stmts;
        }
        // Single statement body (no braces or indent)
        List<ASTNode> stmts = new ArrayList<>();
        stmts.add(parseStatement());
        return stmts;
    }

    private Token expect(TokenType type) {
        if (!check(type)) {
            throw error("Expected " + type + " but got " + current());
        }
        return advance();
    }

    private boolean check(TokenType... types) {
        for (TokenType t : types) {
            if (pos < tokens.size() && current().is(t)) return true;
        }
        return false;
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token peekNext() {
        return tokens.get(pos + 1);
    }

    private boolean isPropertyName() {
        // IDENTIFIER handles most property names; TEXT is the only keyword
        // that commonly conflicts (e.g. text = "..." inside heading {})
        return check(TokenType.IDENTIFIER) || check(TokenType.TEXT);
    }

    private RuntimeException error(String msg) {
        Token t = current();
        return new RuntimeException("Parse error at line " + t.getLine() + ":" + t.getColumn() + " - " + msg);
    }
}
