package compiler.ast;

import java.util.List;
import java.util.Map;

public class ASTNode {
    public String toString() { return "ASTNode"; }

    // ─── Program ────────────────────────────────────
    public static class Program extends ASTNode {
        public List<ASTNode> statements;
        public Program(List<ASTNode> s) { statements = s; }
        public String toString() { return "Program(" + statements.size() + " stmts)"; }
    }

    // ─── Page ───────────────────────────────────────
    public static class Page extends ASTNode {
        public String name;
        public List<ASTNode> body;
        public Page(String name, List<ASTNode> body) { this.name = name; this.body = body; }
        public String toString() { return "Page(" + name + ")"; }
    }

    // ─── Route ──────────────────────────────────────
    public static class Route extends ASTNode {
        public String path;
        public List<ASTNode> body;
        public Route(String path, List<ASTNode> body) { this.path = path; this.body = body; }
        public String toString() { return "Route(" + path + ")"; }
    }

    // ─── Component Definition ───────────────────────
    public static class Component extends ASTNode {
        public String name;
        public List<String> params;
        public List<ASTNode> body;
        public Component(String name, List<String> params, List<ASTNode> body) {
            this.name = name; this.params = params; this.body = body;
        }
        public String toString() { return "Component(" + name + ")"; }
    }

    // ─── Window ─────────────────────────────────────
    public static class Window extends ASTNode {
        public Map<String, ASTNode> props;
        public List<ASTNode> children;
        public Window(Map<String, ASTNode> props, List<ASTNode> children) {
            this.props = props; this.children = children;
        }
        public String toString() { return "Window(" + props.size() + " props)"; }
    }

    // ─── UI Element ─────────────────────────────────
    public static class UIElement extends ASTNode {
        public String tag;
        public Map<String, ASTNode> props;
        public List<ASTNode> children;
        public List<EventBinding> events;
        public UIElement(String tag, Map<String, ASTNode> props, List<ASTNode> children, List<EventBinding> events) {
            this.tag = tag; this.props = props; this.children = children; this.events = events;
        }
        public String toString() { return "UI(" + tag + ")"; }
    }

    public static class EventBinding {
        public String event;
        public List<ASTNode> body;
        public EventBinding(String event, List<ASTNode> body) { this.event = event; this.body = body; }
    }

    // ─── Variable Declaration ───────────────────────
    public static class Var extends ASTNode {
        public String name;
        public ASTNode value;
        public boolean isConst;
        public Var(String name, ASTNode value, boolean isConst) {
            this.name = name; this.value = value; this.isConst = isConst;
        }
        public String toString() { return "Var(" + name + ")"; }
    }

    // ─── Assignment ─────────────────────────────────
    public static class Assign extends ASTNode {
        public ASTNode target;
        public ASTNode value;
        public String op;
        public Assign(ASTNode target, ASTNode value, String op) {
            this.target = target; this.value = value; this.op = op;
        }
        public String toString() { return "Assign(" + op + ")"; }
    }

    // ─── Binary Expression ──────────────────────────
    public static class Binary extends ASTNode {
        public ASTNode left;
        public ASTNode right;
        public String op;
        public Binary(ASTNode left, ASTNode right, String op) {
            this.left = left; this.right = right; this.op = op;
        }
        public String toString() { return "Binary(" + op + ")"; }
    }

    // ─── Unary Expression ───────────────────────────
    public static class Unary extends ASTNode {
        public ASTNode operand;
        public String op;
        public boolean prefix;
        public Unary(ASTNode operand, String op, boolean prefix) {
            this.operand = operand; this.op = op; this.prefix = prefix;
        }
        public String toString() { return "Unary(" + op + ")"; }
    }

    // ─── Function/Method Call ───────────────────────
    public static class Call extends ASTNode {
        public ASTNode callee;
        public List<ASTNode> args;
        public Call(ASTNode callee, List<ASTNode> args) { this.callee = callee; this.args = args; }
        public String toString() { return "Call(" + callee + ")"; }
    }

    // ─── Function Definition ────────────────────────
    public static class Function extends ASTNode {
        public String name;
        public List<String> params;
        public List<ASTNode> body;
        public boolean isAsync;
        public Function(String name, List<String> params, List<ASTNode> body, boolean isAsync) {
            this.name = name; this.params = params; this.body = body; this.isAsync = isAsync;
        }
        public String toString() { return "Function(" + name + ")"; }
    }

    // ─── Class Definition ───────────────────────────
    public static class ClassDef extends ASTNode {
        public String name;
        public String superClass;
        public List<String> interfaces;
        public List<ASTNode> members;
        public ClassDef(String name, String superClass, List<String> interfaces, List<ASTNode> members) {
            this.name = name; this.superClass = superClass;
            this.interfaces = interfaces; this.members = members;
        }
        public String toString() { return "Class(" + name + ")"; }
    }

    // ─── Interface Definition ───────────────────────
    public static class InterfaceDef extends ASTNode {
        public String name;
        public List<ASTNode> members;
        public InterfaceDef(String name, List<ASTNode> members) { this.name = name; this.members = members; }
        public String toString() { return "Interface(" + name + ")"; }
    }

    // ─── Enum Definition ────────────────────────────
    public static class EnumDef extends ASTNode {
        public String name;
        public List<String> values;
        public EnumDef(String name, List<String> values) { this.name = name; this.values = values; }
        public String toString() { return "Enum(" + name + ")"; }
    }

    // ─── Import ─────────────────────────────────────
    public static class Import extends ASTNode {
        public String path;
        public String alias;
        public Import(String path, String alias) { this.path = path; this.alias = alias; }
        public String toString() { return "Import(" + path + ")"; }
    }

    // ─── Return ─────────────────────────────────────
    public static class Return extends ASTNode {
        public ASTNode value;
        public Return(ASTNode value) { this.value = value; }
        public String toString() { return "Return"; }
    }

    // ─── If/Else ────────────────────────────────────
    public static class If extends ASTNode {
        public ASTNode condition;
        public List<ASTNode> thenBody;
        public List<ASTNode> elseBody;
        public If(ASTNode condition, List<ASTNode> thenBody, List<ASTNode> elseBody) {
            this.condition = condition; this.thenBody = thenBody; this.elseBody = elseBody;
        }
        public String toString() { return "If"; }
    }

    // ─── While Loop ─────────────────────────────────
    public static class While extends ASTNode {
        public ASTNode condition;
        public List<ASTNode> body;
        public While(ASTNode condition, List<ASTNode> body) {
            this.condition = condition; this.body = body;
        }
        public String toString() { return "While"; }
    }

    // ─── For Loop ───────────────────────────────────
    public static class For extends ASTNode {
        public String variable;
        public ASTNode iterable;
        public List<ASTNode> body;
        public For(String variable, ASTNode iterable, List<ASTNode> body) {
            this.variable = variable; this.iterable = iterable; this.body = body;
        }
        public String toString() { return "For"; }
    }

    // ─── Identifier ─────────────────────────────────
    public static class Identifier extends ASTNode {
        public String name;
        public Identifier(String name) { this.name = name; }
        public String toString() { return "Id(" + name + ")"; }
    }

    // ─── Property Access ────────────────────────────
    public static class Property extends ASTNode {
        public ASTNode object;
        public String property;
        public Property(ASTNode object, String property) { this.object = object; this.property = property; }
        public String toString() { return "Prop(" + property + ")"; }
    }

    // ─── Index Access ───────────────────────────────
    public static class Index extends ASTNode {
        public ASTNode object;
        public ASTNode index;
        public Index(ASTNode object, ASTNode index) { this.object = object; this.index = index; }
        public String toString() { return "Index"; }
    }

    // ─── String Literal ─────────────────────────────
    public static class Str extends ASTNode {
        public String value;
        public Str(String value) { this.value = value; }
        public String toString() { return "String(\"" + value + "\")"; }
    }

    // ─── Number Literal ─────────────────────────────
    public static class Num extends ASTNode {
        public double value;
        public boolean isInt;
        public Num(double value, boolean isInt) { this.value = value; this.isInt = isInt; }
        public String toString() { return "Number(" + value + ")"; }
    }

    // ─── Boolean Literal ────────────────────────────
    public static class Bool extends ASTNode {
        public boolean value;
        public Bool(boolean value) { this.value = value; }
        public String toString() { return "Boolean(" + value + ")"; }
    }

    // ─── Null Literal ───────────────────────────────
    public static class Null extends ASTNode {
        public Null() {}
        public String toString() { return "Null"; }
    }

    // ─── Color Literal ──────────────────────────────
    public static class Color extends ASTNode {
        public String hex;
        public Color(String hex) { this.hex = hex; }
        public String toString() { return "Color(#" + hex + ")"; }
    }

    // ─── Unit Literal ───────────────────────────────
    public static class Unit extends ASTNode {
        public double value;
        public String unit;
        public Unit(double value, String unit) { this.value = value; this.unit = unit; }
        public String toString() { return "Unit(" + value + unit + ")"; }
    }

    // ─── Array Literal ──────────────────────────────
    public static class Array extends ASTNode {
        public List<ASTNode> elements;
        public Array(List<ASTNode> elements) { this.elements = elements; }
        public String toString() { return "Array(" + elements.size() + ")"; }
    }

    // ─── Map/Object Literal ─────────────────────────
    public static class MapLit extends ASTNode {
        public List<Map.Entry<String, ASTNode>> entries;
        public MapLit(List<Map.Entry<String, ASTNode>> entries) { this.entries = entries; }
        public String toString() { return "Map(" + entries.size() + ")"; }
    }

    // ─── Lambda Expression ──────────────────────────
    public static class Lambda extends ASTNode {
        public List<String> params;
        public List<ASTNode> body;
        public Lambda(List<String> params, List<ASTNode> body) { this.params = params; this.body = body; }
        public String toString() { return "Lambda"; }
    }

    // ─── Print Statement ────────────────────────────
    public static class Print extends ASTNode {
        public ASTNode value;
        public Print(ASTNode value) { this.value = value; }
        public String toString() { return "Print"; }
    }

    // ─── Fetch ──────────────────────────────────────
    public static class Fetch extends ASTNode {
        public ASTNode url;
        public ASTNode options;
        public Fetch(ASTNode url, ASTNode options) { this.url = url; this.options = options; }
        public String toString() { return "Fetch"; }
    }

    // ─── Database Operation ─────────────────────────
    public static class Database extends ASTNode {
        public String name;
        public String operation;
        public ASTNode query;
        public Database(String name, String operation, ASTNode query) {
            this.name = name; this.operation = operation; this.query = query;
        }
        public String toString() { return "Database(" + name + "." + operation + ")"; }
    }

    // ─── Animation ──────────────────────────────────
    public static class Animate extends ASTNode {
        public String target;
        public Map<String, ASTNode> props;
        public List<AnimationStep> steps;
        public Animate(String target, Map<String, ASTNode> props, List<AnimationStep> steps) {
            this.target = target; this.props = props; this.steps = steps;
        }
        public String toString() { return "Animate(" + target + ")"; }
    }

    public static class AnimationStep {
        public String property;
        public ASTNode from;
        public ASTNode to;
        public AnimationStep(String property, ASTNode from, ASTNode to) {
            this.property = property; this.from = from; this.to = to;
        }
    }

    // ─── Async Block ────────────────────────────────
    public static class Async extends ASTNode {
        public List<ASTNode> body;
        public Async(List<ASTNode> body) { this.body = body; }
        public String toString() { return "Async"; }
    }

    // ─── Await Expression ───────────────────────────
    public static class Await extends ASTNode {
        public ASTNode expression;
        public Await(ASTNode expression) { this.expression = expression; }
        public String toString() { return "Await"; }
    }

    // ─── Block ──────────────────────────────────────
    public static class Block extends ASTNode {
        public List<ASTNode> statements;
        public Block(List<ASTNode> statements) { this.statements = statements; }
        public String toString() { return "Block(" + statements.size() + ")"; }
    }
}
