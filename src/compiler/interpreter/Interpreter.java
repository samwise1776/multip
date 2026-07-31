package compiler.interpreter;

import compiler.ast.ASTNode;
import compiler.lexer.*;

import java.util.*;
import java.util.concurrent.*;

public class Interpreter {
    private final Map<String, Object> globals = new HashMap<>();
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();
    private final Map<String, ASTNode.Function> functions = new HashMap<>();
    private final Map<String, ASTNode.Component> components = new HashMap<>();
    private final Map<String, ASTNode.ClassDef> classes = new HashMap<>();
    private final List<ASTNode.Route> routes = new ArrayList<>();
    private final List<Object> uiTree = new ArrayList<>();

    public Interpreter() {
        scopes.push(new HashMap<>());
        initStandardLibrary();
    }

    private void initStandardLibrary() {
        Map<String, Object> math = new HashMap<>();
        math.put("PI", Math.PI);
        math.put("E", Math.E);
        globals.put("Math", math);

        globals.put("len", (NativeFunction) args -> {
            if (args.get(0) instanceof String) return (double)((String)args.get(0)).length();
            if (args.get(0) instanceof List) return (double)((List<?>)args.get(0)).size();
            return 0.0;
        });

        globals.put("print", (NativeFunction) args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(formatValue(arg));
            }
            System.out.println(sb.toString());
            return null;
        });

        globals.put("str", (NativeFunction) args -> formatValue(args.get(0)));

        globals.put("int", (NativeFunction) args -> {
            Object val = args.get(0);
            if (val instanceof String) return Double.parseDouble((String) val);
            if (val instanceof Double) return ((Double) val).intValue();
            return val;
        });

        globals.put("float", (NativeFunction) args -> {
            Object val = args.get(0);
            if (val instanceof String) return Double.parseDouble((String) val);
            if (val instanceof Double) return val;
            return 0.0;
        });

        globals.put("typeof", (NativeFunction) args -> {
            Object val = args.get(0);
            if (val == null) return "null";
            if (val instanceof String) return "string";
            if (val instanceof Double || val instanceof Integer) return "number";
            if (val instanceof Boolean) return "boolean";
            if (val instanceof List) return "array";
            if (val instanceof Map) return "map";
            return "object";
        });

        Map<String, Object> browser = new HashMap<>();
        browser.put("navigate", (NativeFunction) args -> {
            System.out.println("[Browser] Navigate to: " + args.get(0));
            return null;
        });
        browser.put("reload", (NativeFunction) args -> { System.out.println("[Browser] Reload"); return null; });
        browser.put("back", (NativeFunction) args -> { System.out.println("[Browser] Back"); return null; });
        browser.put("forward", (NativeFunction) args -> { System.out.println("[Browser] Forward"); return null; });
        globals.put("browser", browser);

        globals.put("fetch", (NativeFunction) args -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("body", "{}");
            response.put("ok", true);
            return response;
        });
    }

    private String formatValue(Object val) {
        if (val == null) return "null";
        if (val instanceof Double) {
            double d = (Double) val;
            return d == Math.floor(d) && !Double.isInfinite(d) ? String.valueOf((long)d) : String.valueOf(d);
        }
        if (val instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) val;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(e.getKey()).append(": ").append(formatValue(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        if (val instanceof List) {
            List<?> l = (List<?>) val;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < l.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatValue(l.get(i)));
            }
            return sb.append("]").toString();
        }
        return val.toString();
    }

    // ─── Main Execute ───────────────────────────────

    public Object execute(ASTNode node) {
        if (node instanceof ASTNode.Program) return executeProgram((ASTNode.Program) node);
        if (node instanceof ASTNode.Page) return executePage((ASTNode.Page) node);
        if (node instanceof ASTNode.Route) return executeRoute((ASTNode.Route) node);
        if (node instanceof ASTNode.Component) return executeComponent((ASTNode.Component) node);
        if (node instanceof ASTNode.Window) return executeWindow((ASTNode.Window) node);
        if (node instanceof ASTNode.UIElement) return executeUIElement((ASTNode.UIElement) node);
        if (node instanceof ASTNode.Var) return executeVar((ASTNode.Var) node);
        if (node instanceof ASTNode.Assign) return executeAssign((ASTNode.Assign) node);
        if (node instanceof ASTNode.Binary) return executeBinary((ASTNode.Binary) node);
        if (node instanceof ASTNode.Unary) return executeUnary((ASTNode.Unary) node);
        if (node instanceof ASTNode.Call) return executeCall((ASTNode.Call) node);
        if (node instanceof ASTNode.Function) return executeFunctionDef((ASTNode.Function) node);
        if (node instanceof ASTNode.ClassDef) return executeClassDef((ASTNode.ClassDef) node);
        if (node instanceof ASTNode.If) return executeIf((ASTNode.If) node);
        if (node instanceof ASTNode.While) return executeWhile((ASTNode.While) node);
        if (node instanceof ASTNode.For) return executeFor((ASTNode.For) node);
        if (node instanceof ASTNode.Return) return executeReturn((ASTNode.Return) node);
        if (node instanceof ASTNode.Print) return executePrint((ASTNode.Print) node);
        if (node instanceof ASTNode.Identifier) return executeIdentifier((ASTNode.Identifier) node);
        if (node instanceof ASTNode.Property) return executeProperty((ASTNode.Property) node);
        if (node instanceof ASTNode.Index) return executeIndex((ASTNode.Index) node);
        if (node instanceof ASTNode.Str) return ((ASTNode.Str) node).value;
        if (node instanceof ASTNode.Num) return ((ASTNode.Num) node).value;
        if (node instanceof ASTNode.Bool) return ((ASTNode.Bool) node).value;
        if (node instanceof ASTNode.Null) return null;
        if (node instanceof ASTNode.Color) return "#" + ((ASTNode.Color) node).hex;
        if (node instanceof ASTNode.Unit) return ((ASTNode.Unit) node).value;
        if (node instanceof ASTNode.Array) return executeArray((ASTNode.Array) node);
        if (node instanceof ASTNode.MapLit) return executeMap((ASTNode.MapLit) node);
        if (node instanceof ASTNode.Fetch) return executeFetch((ASTNode.Fetch) node);
        if (node instanceof ASTNode.Database) return executeDatabase((ASTNode.Database) node);
        if (node instanceof ASTNode.Animate) return executeAnimate((ASTNode.Animate) node);
        if (node instanceof ASTNode.Block) return executeBlock((ASTNode.Block) node);
        return null;
    }

    private Object executeProgram(ASTNode.Program node) {
        Object result = null;
        for (ASTNode stmt : node.statements) result = execute(stmt);
        return result;
    }

    private Object executePage(ASTNode.Page node) {
        scopes.push(new HashMap<>());
        Object result = null;
        for (ASTNode stmt : node.body) result = execute(stmt);
        scopes.pop();
        return result;
    }

    private Object executeRoute(ASTNode.Route node) {
        routes.add(node);
        return node;
    }

    private Object executeComponent(ASTNode.Component node) {
        components.put(node.name, node);
        return node;
    }

    private Object executeWindow(ASTNode.Window node) {
        Map<String, Object> window = new HashMap<>();
        for (Map.Entry<String, ASTNode> prop : node.props.entrySet()) {
            window.put(prop.getKey(), execute(prop.getValue()));
        }
        List<Object> children = new ArrayList<>();
        for (ASTNode child : node.children) children.add(execute(child));
        window.put("_children", children);
        window.put("_type", "window");
        uiTree.add(window);
        return window;
    }

    private Object executeUIElement(ASTNode.UIElement node) {
        Map<String, Object> element = new HashMap<>();
        element.put("_type", node.tag);
        for (Map.Entry<String, ASTNode> prop : node.props.entrySet()) {
            element.put(prop.getKey(), execute(prop.getValue()));
        }
        for (ASTNode child : node.children) {
            Object c = execute(child);
            if (c != null) {
                if (!element.containsKey("_children")) element.put("_children", new ArrayList<>());
                ((List<Object>) element.get("_children")).add(c);
            }
        }
        if (!node.events.isEmpty()) element.put("_events", node.events);
        return element;
    }

    private Object executeVar(ASTNode.Var node) {
        Object value = node.value != null ? execute(node.value) : null;
        scopes.peek().put(node.name, value);
        return value;
    }

    private Object executeAssign(ASTNode.Assign node) {
        Object value = execute(node.value);
        if (node.target instanceof ASTNode.Identifier) {
            String name = ((ASTNode.Identifier) node.target).name;
            if (node.op.equals("=")) {
                setVariable(name, value);
            } else {
                Object current = getVariable(name);
                value = applyOp(current, value, node.op.substring(0, 1));
                setVariable(name, value);
            }
            return value;
        } else if (node.target instanceof ASTNode.Property) {
            ASTNode.Property prop = (ASTNode.Property) node.target;
            Object obj = execute(prop.object);
            if (obj instanceof Map) ((Map<String, Object>) obj).put(prop.property, value);
            return value;
        }
        return value;
    }

    private Object executeBinary(ASTNode.Binary node) {
        if (node.op.equals("&&")) {
            Object left = execute(node.left);
            if (!isTruthy(left)) return left;
            return execute(node.right);
        }
        if (node.op.equals("||")) {
            Object left = execute(node.left);
            if (isTruthy(left)) return left;
            return execute(node.right);
        }
        Object left = execute(node.left);
        Object right = execute(node.right);
        if (node.op.equals("+") && (left instanceof String || right instanceof String)) {
            return formatValue(left) + formatValue(right);
        }
        return applyOp(left, right, node.op);
    }

    private Object applyOp(Object left, Object right, String op) {
        double l = toNumber(left);
        double r = toNumber(right);
        switch (op) {
            case "+": return l + r;
            case "-": return l - r;
            case "*": return l * r;
            case "/": return r != 0 ? l / r : Double.NaN;
            case "%": return l % r;
            case "==": return left == right || (left != null && left.equals(right));
            case "!=": return !equals(left, right);
            case "<": return l < r;
            case ">": return l > r;
            case "<=": return l <= r;
            case ">=": return l >= r;
            default: throw new RuntimeException("Unknown operator: " + op);
        }
    }

    private Object executeUnary(ASTNode.Unary node) {
        Object val = execute(node.operand);
        if (node.op.equals("-")) return -toNumber(val);
        if (node.op.equals("!")) return !isTruthy(val);
        return val;
    }

    @SuppressWarnings("unchecked")
    private Object executeCall(ASTNode.Call node) {
        Object callee = execute(node.callee);
        List<Object> args = new ArrayList<>();
        for (ASTNode arg : node.args) args.add(execute(arg));

        if (callee instanceof NativeFunction) return ((NativeFunction) callee).call(args);

        if (callee instanceof ASTNode.Function) {
            ASTNode.Function fn = (ASTNode.Function) callee;
            scopes.push(new HashMap<>());
            for (int i = 0; i < fn.params.size(); i++) {
                scopes.peek().put(fn.params.get(i), i < args.size() ? args.get(i) : null);
            }
            Object result = null;
            for (ASTNode stmt : fn.body) {
                if (stmt instanceof ASTNode.Return) { result = execute(((ASTNode.Return) stmt).value); break; }
                result = execute(stmt);
            }
            scopes.pop();
            return result;
        }

        if (callee instanceof ASTNode.Component) {
            ASTNode.Component comp = (ASTNode.Component) callee;
            scopes.push(new HashMap<>());
            for (int i = 0; i < comp.params.size(); i++) {
                scopes.peek().put(comp.params.get(i), i < args.size() ? args.get(i) : null);
            }
            List<Object> result = new ArrayList<>();
            for (ASTNode stmt : comp.body) result.add(execute(stmt));
            scopes.pop();
            return result;
        }

        if (callee instanceof ASTNode.ClassDef) {
            ASTNode.ClassDef cls = (ASTNode.ClassDef) callee;
            Map<String, Object> instance = new HashMap<>();
            instance.put("_class", cls.name);
            return instance;
        }

        if (node.callee instanceof ASTNode.Property) {
            ASTNode.Property prop = (ASTNode.Property) node.callee;
            Object obj = execute(prop.object);
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                if (map.containsKey(prop.property)) {
                    Object fn = map.get(prop.property);
                    if (fn instanceof NativeFunction) return ((NativeFunction) fn).call(args);
                }
            }
        }

        throw new RuntimeException("Cannot call: " + callee);
    }

    private Object executeFunctionDef(ASTNode.Function node) {
        scopes.peek().put(node.name, node);
        functions.put(node.name, node);
        return node;
    }

    private Object executeClassDef(ASTNode.ClassDef node) {
        classes.put(node.name, node);
        scopes.peek().put(node.name, node);
        return node;
    }

    private Object executeIf(ASTNode.If node) {
        if (isTruthy(execute(node.condition))) {
            Object result = null;
            for (ASTNode stmt : node.thenBody) result = execute(stmt);
            return result;
        } else if (node.elseBody != null) {
            Object result = null;
            for (ASTNode stmt : node.elseBody) result = execute(stmt);
            return result;
        }
        return null;
    }

    private Object executeWhile(ASTNode.While node) {
        Object result = null;
        while (isTruthy(execute(node.condition))) {
            for (ASTNode stmt : node.body) result = execute(stmt);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object executeFor(ASTNode.For node) {
        Object iterable = execute(node.iterable);
        Object result = null;
        if (iterable instanceof List) {
            for (Object item : (List<?>) iterable) {
                scopes.peek().put(node.variable, item);
                for (ASTNode stmt : node.body) result = execute(stmt);
            }
        } else if (iterable instanceof String) {
            for (char c : ((String) iterable).toCharArray()) {
                scopes.peek().put(node.variable, String.valueOf(c));
                for (ASTNode stmt : node.body) result = execute(stmt);
            }
        }
        return result;
    }

    private Object executeReturn(ASTNode.Return node) {
        return node.value != null ? execute(node.value) : null;
    }

    private Object executePrint(ASTNode.Print node) {
        Object val = execute(node.value);
        System.out.println(formatValue(val));
        return val;
    }

    private Object executeIdentifier(ASTNode.Identifier node) {
        return getVariable(node.name);
    }

    @SuppressWarnings("unchecked")
    private Object executeProperty(ASTNode.Property node) {
        Object obj = execute(node.object);
        if (obj instanceof Map) return ((Map<String, Object>) obj).get(node.property);
        if (obj instanceof String && node.property.equals("length")) return (double)((String) obj).length();
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object executeIndex(ASTNode.Index node) {
        Object obj = execute(node.object);
        Object idx = execute(node.index);
        if (obj instanceof List && idx instanceof Double) return ((List<?>) obj).get(((Double) idx).intValue());
        if (obj instanceof Map) return ((Map<String, Object>) obj).get(formatValue(idx));
        return null;
    }

    private Object executeArray(ASTNode.Array node) {
        List<Object> arr = new ArrayList<>();
        for (ASTNode elem : node.elements) arr.add(execute(elem));
        return arr;
    }

    private Object executeMap(ASTNode.MapLit node) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, ASTNode> entry : node.entries) map.put(entry.getKey(), execute(entry.getValue()));
        return map;
    }

    private Object executeFetch(ASTNode.Fetch node) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("body", "{}");
        response.put("ok", true);
        return response;
    }

    private Object executeDatabase(ASTNode.Database node) { return new HashMap<>(); }

    private Object executeAnimate(ASTNode.Animate node) {
        Map<String, Object> anim = new HashMap<>();
        anim.put("_type", "animation");
        anim.put("target", node.target);
        for (Map.Entry<String, ASTNode> prop : node.props.entrySet()) anim.put(prop.getKey(), execute(prop.getValue()));
        return anim;
    }

    private Object executeBlock(ASTNode.Block node) {
        scopes.push(new HashMap<>());
        Object result = null;
        for (ASTNode stmt : node.statements) result = execute(stmt);
        scopes.pop();
        return result;
    }

    // ─── Scope Helpers ──────────────────────────────

    private Object getVariable(String name) {
        for (Map<String, Object> scope : scopes) if (scope.containsKey(name)) return scope.get(name);
        if (globals.containsKey(name)) return globals.get(name);
        if (functions.containsKey(name)) return functions.get(name);
        if (components.containsKey(name)) return components.get(name);
        if (classes.containsKey(name)) return classes.get(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    private void setVariable(String name, Object value) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) { scope.put(name, value); return; }
        }
        globals.put(name, value);
    }

    private boolean isTruthy(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Double) return (Double) val != 0;
        if (val instanceof String) return !((String) val).isEmpty();
        return true;
    }

    private boolean equals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private double toNumber(Object val) {
        if (val instanceof Double) return (Double) val;
        if (val instanceof String) return Double.parseDouble((String) val);
        if (val instanceof Boolean) return (Boolean) val ? 1 : 0;
        return 0;
    }

    public List<Object> getUITree() { return uiTree; }
    public List<ASTNode.Route> getRoutes() { return routes; }
    public Map<String, ASTNode.Component> getComponents() { return components; }

    @FunctionalInterface
    interface NativeFunction { Object call(List<Object> args); }
}
