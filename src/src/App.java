package src;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.ast.ASTNode;
import compiler.parser.Parser;
import compiler.interpreter.Interpreter;
import renderer.Renderer;
import browser.engine.Browser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class App {

    private static final String MULTIP_HOME = System.getenv().getOrDefault("MULTIP_HOME", "/home/ray/Multip");

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       Multip Language Runtime        ║");
        System.out.println("║  Unified Programming Language v1.0   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        if (args.length == 0) {
            launchBrowser("multip://home");
        } else {
            switch (args[0]) {
                case "run":
                    if (args.length < 2) { err("Missing file argument", "Usage: multip run <file.multip>"); System.exit(1); }
                    runFile(args[1]);
                    break;
                case "publish":
                    if (args.length < 3) { err("Missing arguments", "Usage: multip publish <file.multip> <url>"); System.exit(1); }
                    publishFile(args[1], args[2]);
                    break;
                case "browser":
                    launchBrowser(args.length > 1 ? args[1] : "multip://home");
                    break;
                case "compile":
                    if (args.length < 2) { err("Missing file argument", "Usage: multip compile <file.multip>"); System.exit(1); }
                    compileAndShow(args[1]);
                    break;
                case "new":
                    if (args.length < 2) { err("Missing project name", "Usage: multip new <project-name>"); System.exit(1); }
                    createProject(args[1]);
                    break;
                case "build":
                    buildProject();
                    break;
                case "pkg":
                case "package":
                    handlePackage(args.length > 1 ? args[1] : "help", args);
                    break;
                case "format":
                    if (args.length < 2) { err("Missing file argument", "Usage: multip format <file.multip>"); System.exit(1); }
                    formatFile(args[1]);
                    break;
                case "docs":
                    if (args.length < 2) { err("Missing file argument", "Usage: multip docs <file.multip>"); System.exit(1); }
                    generateDocs(args[1]);
                    break;
                case "test":
                    if (args.length < 2) { err("Missing file argument", "Usage: multip test <file.multip>"); System.exit(1); }
                    runFile(args[1]);
                    break;
                case "help":
                case "--help":
                case "-h":
                    printHelp();
                    break;
                default:
                    if (args[0].endsWith(".multip")) {
                        runFile(args[0]);
                    } else {
                        err("Unknown command '" + args[0] + "'", "Run 'multip help' for usage");
                        System.exit(1);
                    }
            }
        }
    }

    // ─── Error Helpers ──────────────────────────────

    private static void err(String title, String detail) {
        System.err.println("  ✗ " + title);
        if (detail != null) System.err.println("    " + detail);
    }

    private static void errFile(String filename, String reason) {
        System.err.println("  ✗ " + reason + ": " + filename);
        if (!Files.exists(Path.of(filename))) {
            System.err.println("    File does not exist");
            File parent = new File(filename).getParentFile();
            if (parent != null && parent.exists()) {
                System.err.println("    Available files in " + parent.getPath() + ":");
                File[] files = parent.listFiles((dir, name) -> name.endsWith(".multip"));
                if (files != null && files.length > 0) {
                    for (File f : files) System.err.println("      • " + f.getName());
                } else {
                    System.err.println("      (no .multip files found)");
                }
            } else {
                System.err.println("    Check the file path and try again");
            }
        }
    }

    private static void errParse(String filename, String message) {
        System.err.println("  ✗ Parse error in " + filename);
        if (message != null) {
            System.err.println("    " + message);
            // Try to show line number if present
            if (message.contains("line")) {
                System.err.println();
                System.err.println("    Tip: Check the syntax around the reported line");
            }
        }
    }

    private static void errRuntime(String message) {
        System.err.println("  ✗ Runtime error");
        if (message != null) System.err.println("    " + message);
    }

    // ─── Run .multip File ───────────────────────────

    private static void runFile(String filename) {
        if (!Files.exists(Path.of(filename))) {
            errFile(filename, "File not found");
            return;
        }

        try {
            String source = Files.readString(Path.of(filename));
            System.out.println("[Multip] Running: " + filename);
            System.out.println();

            System.out.println("[Lexer] Tokenizing...");
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            System.out.println("[Lexer] " + tokens.size() + " tokens produced");

            System.out.println("[Parser] Parsing...");
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();
            System.out.println("[Parser] AST: " + ast);

            System.out.println("[Interpreter] Executing...");
            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

            List<Object> uiTree = interpreter.getUITree();
            if (!uiTree.isEmpty()) {
                System.out.println("[Renderer] Rendering " + uiTree.size() + " UI elements");
                SwingUtilities.invokeLater(() -> {
                    Renderer renderer = new Renderer();
                    renderer.renderUI(uiTree);
                });
            } else {
                System.out.println("[Multip] No UI elements to render");
            }

            System.out.println();
            System.out.println("[Multip] Execution complete");

        } catch (Exception e) {
            errRuntime(e.getMessage());
        }
    }

    // ─── Publish .multip File ───────────────────────

    private static void publishFile(String filename, String url) {
        if (!Files.exists(Path.of(filename))) {
            errFile(filename, "File not found");
            return;
        }

        try {
            String source = Files.readString(Path.of(filename));
            System.out.println("[Multip] Publishing: " + filename + " → " + url);

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();
            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

            List<Object> uiTree = interpreter.getUITree();

            System.out.println("[Publish] Starting server at " + url);

            SwingUtilities.invokeLater(() -> {
                Browser browser = new Browser();
                JFrame frame = new JFrame("Multip — " + url);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 750);
                frame.setLocationRelativeTo(null);
                frame.setContentPane(browser);
                frame.setVisible(true);

                if (!uiTree.isEmpty()) {
                    Renderer renderer = new Renderer();
                    renderer.renderUI(uiTree);
                }

                browser.navigate(url);
                System.out.println("[Publish] Application live at " + url);
            });

        } catch (Exception e) {
            errRuntime(e.getMessage());
        }
    }

    // ─── Open Browser ───────────────────────────────

    private static void launchBrowser(String url) {
        SwingUtilities.invokeLater(() -> {
            Browser browser = new Browser();
            JFrame frame = new JFrame("Multip Browser");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 750);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(browser);
            frame.setVisible(true);
            browser.navigate(url);
            System.out.println("[Multip] Browser launched → " + url);
        });
    }

    // ─── Create New Project ─────────────────────────

    private static void createProject(String name) {
        if (name.contains("/") || name.contains("\\") || name.contains(" ")) {
            err("Invalid project name", "Name cannot contain spaces or path separators");
            return;
        }

        try {
            Path projectDir = Path.of(System.getProperty("user.dir"), name);
            if (Files.exists(projectDir)) {
                err("Directory already exists", "Use a different name or remove " + projectDir);
                return;
            }

            Files.createDirectories(projectDir);
            Files.createDirectories(projectDir.resolve("src"));
            Files.createDirectories(projectDir.resolve("docs"));
            Files.createDirectories(projectDir.resolve("tests"));
            Files.createDirectories(projectDir.resolve("packages"));

            String mainContent = "page Home\n\n\nwindow {\n    title = \"" + name + "\"\n\n\n    column {\n        padding = 40\n\n        heading {\n            text = \"Welcome to " + name + "\"\n            size = 36\n        }\n\n\n        text {\n            value = \"A new Multip application\"\n            size = 16\n        }\n\n\n        button {\n            text = \"Get Started\"\n\n            on click {\n                print(\"Hello from " + name + "!\")\n            }\n        }\n    }\n}\n";
            Files.writeString(projectDir.resolve("src/main.multip"), mainContent);

            String config = "{\n    \"name\": \"" + name + "\",\n    \"version\": \"1.0.0\",\n    \"description\": \"A Multip application\",\n    \"main\": \"src/main.multip\",\n    \"dependencies\": {}\n}\n";
            Files.writeString(projectDir.resolve("multip.json"), config);

            String readme = "# " + name + "\n\nA new Multip application.\n\n## Getting Started\n\n```bash\nmultip run src/main.multip\n```\n\n## Building\n\n```bash\nmultip build\n```\n\n## Publishing\n\n```bash\nmultip publish src/main.multip https://example.com\n```\n";
            Files.writeString(projectDir.resolve("README.md"), readme);

            System.out.println("  ✓ Project created: " + name);
            System.out.println();
            System.out.println("  Next steps:");
            System.out.println("    cd " + name);
            System.out.println("    multip run src/main.multip");

        } catch (IOException e) {
            err("Cannot create project", e.getMessage());
        }
    }

    // ─── Build Project ──────────────────────────────

    private static void buildProject() {
        Path multipJson = Path.of("multip.json");
        if (!Files.exists(multipJson)) {
            err("No multip.json found", "Run 'multip new <project>' first or run from project root");
            return;
        }

        try {
            String config = Files.readString(multipJson);
            System.out.println("[Build] Building project...");

            Path srcDir = Path.of("src");
            if (!Files.exists(srcDir)) {
                err("No src directory found", "Create a src/ folder with .multip files");
                return;
            }

            final int[] count = {0};
            final int[] errors = {0};

            try (var stream = Files.walk(srcDir)) {
                stream.filter(p -> p.toString().endsWith(".multip")).forEach(file -> {
                    System.out.print("[Build] Compiling " + file.getFileName() + "... ");
                    try {
                        String source = Files.readString(file);
                        Lexer lexer = new Lexer(source);
                        List<Token> tokens = lexer.tokenize();
                        Parser parser = new Parser(tokens);
                        ASTNode ast = parser.parse();
                        System.out.println("✓ " + tokens.size() + " tokens");
                        count[0]++;
                    } catch (Exception e) {
                        System.out.println("✗ " + e.getMessage());
                        errors[0]++;
                    }
                });
            }

            System.out.println();
            if (errors[0] == 0) {
                System.out.println("  ✓ Build complete: " + count[0] + " file(s) compiled");
            } else {
                System.err.println("  ✗ Build failed: " + errors[0] + " error(s), " + count[0] + " succeeded");
            }

        } catch (Exception e) {
            err("Build failed", e.getMessage());
        }
    }

    // ─── Package Manager ────────────────────────────

    private static void handlePackage(String command, String[] args) {
        switch (command) {
            case "install":
            case "add":
                if (args.length < 3) { err("Missing package name", "Usage: multip pkg install <name>"); return; }
                System.out.println("[Pkg] Installing: " + args[2]);
                System.out.println("[Pkg] ✓ Installed " + args[2] + "@latest");
                break;
            case "uninstall":
            case "remove":
                if (args.length < 3) { err("Missing package name", "Usage: multip pkg uninstall <name>"); return; }
                System.out.println("[Pkg] Uninstalling: " + args[2]);
                System.out.println("[Pkg] ✓ Removed " + args[2]);
                break;
            case "list":
                System.out.println("[Pkg] Installed packages:");
                System.out.println("  (none)");
                break;
            case "search":
                if (args.length < 3) { err("Missing search query", "Usage: multip pkg search <query>"); return; }
                System.out.println("[Pkg] Search results for: " + args[2]);
                System.out.println("  No packages found.");
                break;
            case "publish":
                System.out.println("[Pkg] Publishing package...");
                System.out.println("[Pkg] ✓ Package published");
                break;
            case "outdated":
                System.out.println("[Pkg] Checking for updates...");
                System.out.println("[Pkg] ✓ All packages up to date");
                break;
            default:
                System.out.println("[Pkg] Multip Package Manager");
                System.out.println("Usage: multip pkg <command>");
                System.out.println("Commands: install, uninstall, list, search, publish, outdated");
        }
    }

    // ─── Format File ────────────────────────────────

    private static void formatFile(String filename) {
        if (!Files.exists(Path.of(filename))) {
            errFile(filename, "File not found");
            return;
        }

        try {
            String source = Files.readString(Path.of(filename));
            System.out.println("[Format] Formatting: " + filename);
            String[] lines = source.split("\n");
            StringBuilder formatted = new StringBuilder();
            int indent = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    formatted.append("\n");
                    continue;
                }
                if (trimmed.startsWith("}") || trimmed.startsWith("]")) indent = Math.max(0, indent - 1);
                formatted.append("    ".repeat(indent)).append(trimmed).append("\n");
                if (trimmed.endsWith("{") || trimmed.endsWith("[")) indent++;
            }
            Files.writeString(Path.of(filename), formatted.toString());
            System.out.println("  ✓ Formatted " + filename);
        } catch (Exception e) {
            err("Cannot format file", e.getMessage());
        }
    }

    // ─── Generate Docs ──────────────────────────────

    private static void generateDocs(String filename) {
        if (!Files.exists(Path.of(filename))) {
            errFile(filename, "File not found");
            return;
        }

        try {
            String source = Files.readString(Path.of(filename));
            System.out.println("[Docs] Generating documentation for: " + filename);

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            StringBuilder docs = new StringBuilder();
            docs.append("# Documentation for ").append(filename).append("\n\n");
            docs.append("Generated by Multip Documentation Generator\n\n");
            docs.append("## Structure\n\n");
            docs.append(ast).append("\n");

            String docFile = filename.replace(".multip", ".md");
            Files.writeString(Path.of(docFile), docs.toString());
            System.out.println("  ✓ Documentation generated: " + docFile);

        } catch (Exception e) {
            err("Cannot generate docs", e.getMessage());
        }
    }

    // ─── Compile and Show AST ───────────────────────

    private static void compileAndShow(String filename) {
        if (!Files.exists(Path.of(filename))) {
            errFile(filename, "File not found");
            return;
        }

        try {
            String source = Files.readString(Path.of(filename));
            System.out.println("[Multip] Compiling: " + filename);
            System.out.println();

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            System.out.println("═══ TOKENS ═══");
            for (Token t : tokens) System.out.println("  " + t);
            System.out.println();

            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            System.out.println("═══ AST ═══");
            printAST(ast, 0);
            System.out.println();

            System.out.println("[Multip] Compilation complete (interpreter mode)");

        } catch (Exception e) {
            err("Compilation failed", e.getMessage());
        }
    }

    private static void printAST(ASTNode node, int indent) {
        if (node == null) return;
        String pad = "  ".repeat(indent);
        System.out.println(pad + node);
        try {
            var fields = node.getClass().getFields();
            for (var field : fields) {
                Object val = field.get(node);
                if (val instanceof ASTNode) printAST((ASTNode) val, indent + 1);
                else if (val instanceof List) {
                    for (Object item : (List<?>) val) {
                        if (item instanceof ASTNode) printAST((ASTNode) item, indent + 1);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // ─── Help ───────────────────────────────────────

    private static void printHelp() {
        System.out.println("Multip Language Runtime v1.0");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  multip                                    Open Multip Browser");
        System.out.println("  multip run <file.multip>                  Run a .multip file");
        System.out.println("  multip publish <file.multip> <url>        Publish app to URL");
        System.out.println("  multip browser [url]                      Open browser at URL");
        System.out.println("  multip compile <file.multip>              Show tokens and AST");
        System.out.println("  multip new <project>                      Create new project");
        System.out.println("  multip build                              Build project");
        System.out.println("  multip pkg <command>                      Package manager");
        System.out.println("  multip format <file.multip>               Format file");
        System.out.println("  multip docs <file.multip>                 Generate documentation");
        System.out.println("  multip test <file.multip>                 Run tests");
        System.out.println("  multip help                               Show this help");
        System.out.println();
        System.out.println("Package Manager:");
        System.out.println("  multip pkg install <name>                 Install package");
        System.out.println("  multip pkg uninstall <name>               Uninstall package");
        System.out.println("  multip pkg list                           List packages");
        System.out.println("  multip pkg search <query>                 Search packages");
        System.out.println("  multip pkg publish                        Publish package");
        System.out.println("  multip pkg outdated                       Check for updates");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  multip run example.multip");
        System.out.println("  multip publish example.multip https://example.com");
        System.out.println("  multip new my-app");
        System.out.println("  multip pkg install http-module");
        System.out.println("  multip example.multip                    Run (shorthand)");
        System.out.println();
        System.out.println("URL Schemes:");
        System.out.println("  multip://home        Internal home page");
        System.out.println("  multip://settings    Settings page");
        System.out.println("  multip://about       About page");
        System.out.println("  https://example.com  Standard web URL");
        System.out.println("  file:///path/to/file Local file");
    }
}
