package browser.engine;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.parser.Parser;
import compiler.ast.ASTNode;
import renderer.Renderer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class Browser extends JPanel {
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final JTextField urlField;
    private final JLabel statusLabel;
    private final DefaultListModel<String> tabModel;
    private final JList<String> tabList;
    private final List<String> history = new ArrayList<>();
    private final List<String> bookmarks = new ArrayList<>();
    private final Set<String> downloads = new LinkedHashSet<>();
    private final Map<String, String> cache = new LinkedHashMap<>();
    private int historyIndex = -1;
    private int currentTab = 0;

    // Purple Multip branding colors
    private static final Color MULTIP_PURPLE = new Color(124, 58, 237);
    private static final Color MULTIP_DARK_PURPLE = new Color(109, 40, 217);
    private static final Color MULTIP_LIGHT_PURPLE = new Color(167, 139, 250);
    private static final Color MULTIP_BG = new Color(249, 250, 251);
    private static final Color MULTIP_TEXT = new Color(31, 41, 55);
    private static final Color MULTIP_GRAY = new Color(107, 114, 128);

    public Browser() {
        setLayout(new BorderLayout());

        // ── Tab Bar ─────────────────────────────────
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setBackground(new Color(30, 30, 35));
        tabPanel.setPreferredSize(new Dimension(0, 35));

        tabModel = new DefaultListModel<>();
        tabModel.addElement("Home");
        tabList = new JList<>(tabModel);
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setFixedCellHeight(30);
        tabList.setBackground(new Color(30, 30, 35));
        tabList.setForeground(Color.WHITE);
        tabList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabList.setVisibleRowCount(1);
        tabList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        JScrollPane tabScroll = new JScrollPane(tabList);
        tabScroll.setBorder(null);
        tabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tabScroll.setPreferredSize(new Dimension(0, 35));

        JButton newTabBtn = new JButton("+");
        newTabBtn.setPreferredSize(new Dimension(35, 30));
        newTabBtn.setBackground(new Color(30, 30, 35));
        newTabBtn.setForeground(Color.WHITE);
        newTabBtn.setBorderPainted(false);
        newTabBtn.addActionListener(e -> openTab());

        tabPanel.add(tabScroll, BorderLayout.CENTER);
        tabPanel.add(newTabBtn, BorderLayout.EAST);
        add(tabPanel, BorderLayout.NORTH);

        // ── Toolbar ─────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(5, 0));
        toolbar.setBackground(new Color(40, 40, 45));
        toolbar.setBorder(new EmptyBorder(5, 8, 5, 8));
        toolbar.setPreferredSize(new Dimension(0, 40));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        navButtons.setOpaque(false);

        JButton backBtn = createNavButton("\u25C0", "Back");
        backBtn.addActionListener(e -> back());

        JButton fwdBtn = createNavButton("\u25B6", "Forward");
        fwdBtn.addActionListener(e -> forward());

        JButton refreshBtn = createNavButton("\u21BB", "Refresh");
        refreshBtn.addActionListener(e -> reload());

        JButton homeBtn = createNavButton("\u2302", "Home");
        homeBtn.addActionListener(e -> home());

        navButtons.add(backBtn);
        navButtons.add(fwdBtn);
        navButtons.add(refreshBtn);
        navButtons.add(homeBtn);

        toolbar.add(navButtons, BorderLayout.WEST);

        // ── URL Bar ─────────────────────────────────
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlPanel.setOpaque(false);

        urlField = new JTextField("multip://home");
        urlField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        urlField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MULTIP_PURPLE, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        urlField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    navigate(urlField.getText());
                }
            }
        });

        JButton goBtn = new JButton("Go");
        goBtn.setBackground(MULTIP_PURPLE);
        goBtn.setForeground(Color.WHITE);
        goBtn.setBorderPainted(false);
        goBtn.setPreferredSize(new Dimension(50, 30));
        goBtn.addActionListener(e -> navigate(urlField.getText()));

        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(goBtn, BorderLayout.EAST);
        toolbar.add(urlPanel, BorderLayout.CENTER);

        // ── Menu Buttons ────────────────────────────
        JPanel menuButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        menuButtons.setOpaque(false);

        JButton bookmarkBtn = createNavButton("\u2605", "Bookmark");
        bookmarkBtn.addActionListener(e -> bookmark(urlField.getText()));

        JButton historyBtn = createNavButton("\u23F1", "History");
        historyBtn.addActionListener(e -> showHistory());

        JButton downloadsBtn = createNavButton("\u2B07", "Downloads");
        downloadsBtn.addActionListener(e -> showDownloads());

        JButton settingsBtn = createNavButton("\u2699", "Settings");
        settingsBtn.addActionListener(e -> showSettings());

        menuButtons.add(bookmarkBtn);
        menuButtons.add(historyBtn);
        menuButtons.add(downloadsBtn);
        menuButtons.add(settingsBtn);

        toolbar.add(menuButtons, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // ── Content Area ────────────────────────────
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(Color.WHITE);

        // Default home page
        JPanel homePage = createHomePage();
        contentPanel.add(homePage, "home");
        contentLayout.show(contentPanel, "home");

        add(contentPanel, BorderLayout.CENTER);

        // ── Status Bar ──────────────────────────────
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(MULTIP_PURPLE);
        statusBar.setPreferredSize(new Dimension(0, 25));
        statusBar.setBorder(new EmptyBorder(2, 8, 2, 8));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel versionLabel = new JLabel("Multip Browser v1.0");
        versionLabel.setForeground(new Color(220, 200, 255));
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
    }

    // ─── Navigation ─────────────────────────────────

    public void navigate(String url) {
        if (url == null || url.isEmpty()) return;

        // Handle multip:// URLs
        if (url.startsWith("multip://")) {
            navigateInternal(url);
            return;
        }

        // Handle file:// URLs
        if (url.startsWith("file://")) {
            loadFile(url.substring(7));
            return;
        }

        // Handle .multip file paths (local)
        if (url.endsWith(".multip") && !url.startsWith("http")) {
            loadMultipFile(url);
            return;
        }

        // Standard HTTP/HTTPS
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        final String finalUrl = url;
        urlField.setText(finalUrl);
        statusLabel.setText("Loading: " + finalUrl + "...");
        addHistory(finalUrl);

        // Fetch URL content - if it's a .multip file, render it
        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected JPanel doInBackground() {
                try {
                    java.net.URL urlObj = new java.net.URL(finalUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(10000);
                    conn.setRequestProperty("User-Agent", "Multip Browser/1.0");

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        return createErrorPage("HTTP " + responseCode, "Failed to load: " + finalUrl);
                    }

                    java.io.InputStream is = conn.getInputStream();
                    String content = new String(is.readAllBytes());
                    is.close();
                    conn.disconnect();

                    // Check if content looks like Multip code
                    if (isMultipContent(content, finalUrl)) {
                        return renderMultipSource(content, finalUrl);
                    } else {
                        return createWebPageContent(finalUrl, content);
                    }
                } catch (Exception e) {
                    return createErrorPage("Error", e.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    JPanel panel = get();
                    String key = "web_" + finalUrl.hashCode();
                    contentPanel.add(panel, key);
                    contentLayout.show(contentPanel, key);

                    // Update tab title
                    String title = extractTitle(finalUrl);
                    tabModel.set(currentTab, title);
                    statusLabel.setText("Done: " + finalUrl);
                } catch (Exception e) {
                    statusLabel.setText("Error loading page");
                }
            }
        };
        worker.execute();
    }

    private boolean isMultipContent(String content, String url) {
        // Check URL ends with .multip
        if (url.toLowerCase().endsWith(".multip")) return true;

        // Check content starts with Multip keywords
        String trimmed = content.strip();
        if (trimmed.startsWith("page ") || trimmed.startsWith("window") ||
            trimmed.startsWith("component ") || trimmed.startsWith("route ") ||
            trimmed.startsWith("server ") || trimmed.startsWith("const ") ||
            trimmed.startsWith("var ") || trimmed.startsWith("function ")) {
            return true;
        }

        return false;
    }

    private JPanel renderMultipSource(String source, String url) {
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            ASTNode node = parser.parse();
            ASTNode.Program program = (ASTNode.Program) node;
            return renderMultipProgram(program, url);
        } catch (Exception e) {
            return createErrorPage("Parse Error", e.getMessage());
        }
    }

    private JPanel createWebPageContent(String url, String content) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 30, 30, 30));
        page.setBackground(Color.WHITE);

        JLabel title = new JLabel("Web Page");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(MULTIP_TEXT);
        page.add(title);

        page.add(Box.createVerticalStrut(10));

        JLabel urlLabel = new JLabel(url);
        urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        urlLabel.setForeground(MULTIP_GRAY);
        page.add(urlLabel);

        page.add(Box.createVerticalStrut(20));

        // Try to display as HTML if it looks like HTML
        if (content.contains("<html") || content.contains("<!DOCTYPE")) {
            JLabel htmlLabel = new JLabel("<html><div style='width:600px'>" + content + "</div></html>");
            htmlLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            page.add(htmlLabel);
        } else {
            JLabel contentLabel = new JLabel("<html><pre style='font-family:monospace'>" + escapeHtml(content.substring(0, Math.min(content.length(), 5000))) + "</pre></html>");
            contentLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
            page.add(contentLabel);
        }

        return page;
    }

    private JPanel createErrorPage(String title, String message) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 30, 30, 30));
        page.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.RED);
        page.add(titleLabel);

        page.add(Box.createVerticalStrut(10));

        JLabel msgLabel = new JLabel("<html><pre>" + escapeHtml(message != null ? message : "Unknown error") + "</pre></html>");
        msgLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        page.add(msgLabel);

        return page;
    }

    private String extractTitle(String url) {
        try {
            if (url.contains("/")) {
                String path = url.substring(url.lastIndexOf('/') + 1);
                if (path.contains("?")) path = path.substring(0, path.indexOf('?'));
                if (!path.isEmpty()) return path;
            }
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void navigateInternal(String url) {
        String page = url.replace("multip://", "");
        urlField.setText(url);
        addHistory(url);
        statusLabel.setText("Loading multip://" + page);

        JPanel pagePanel = createInternalPage(page);
        String key = "page_" + page;
        contentPanel.add(pagePanel, key);
        contentLayout.show(contentPanel, key);
        tabModel.set(currentTab, page.substring(0, 1).toUpperCase() + page.substring(1));
    }

    public void loadMultipFile(String path) {
        urlField.setText(path);
        statusLabel.setText("Loading: " + path + "...");
        addHistory(path);

        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected JPanel doInBackground() {
                try {
                    String source = Files.readString(Path.of(path));
                    Lexer lexer = new Lexer(source);
                    List<Token> tokens = lexer.tokenize();
                    Parser parser = new Parser(tokens);
                    ASTNode node = parser.parse();
                    ASTNode.Program program = (ASTNode.Program) node;
                    return renderMultipProgram(program, path);
                } catch (Exception e) {
                    JPanel errorPanel = new JPanel();
                    errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
                    errorPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
                    errorPanel.setBackground(Color.WHITE);

                    JLabel title = new JLabel("Error Loading File");
                    title.setFont(new Font("SansSerif", Font.BOLD, 24));
                    title.setForeground(Color.RED);
                    errorPanel.add(title);

                    errorPanel.add(Box.createVerticalStrut(10));

                    JLabel msg = new JLabel("<html><pre>" + e.getMessage() + "</pre></html>");
                    msg.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    errorPanel.add(msg);

                    return errorPanel;
                }
            }

            @Override
            protected void done() {
                try {
                    JPanel panel = get();
                    String key = "multip_" + path.hashCode();
                    contentPanel.add(panel, key);
                    contentLayout.show(contentPanel, key);

                    String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                    tabModel.set(currentTab, fileName);
                    statusLabel.setText("Loaded: " + path);
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    @SuppressWarnings("unchecked")
    private JPanel renderMultipProgram(ASTNode.Program program, String sourcePath) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);

        String windowTitle = "Multip App";

        for (ASTNode stmt : program.statements) {
            if (stmt instanceof ASTNode.Window) {
                ASTNode.Window win = (ASTNode.Window) stmt;
                if (win.props.containsKey("title")) {
                    windowTitle = evaluateMultipExpr(win.props.get("title"));
                }

                for (ASTNode child : win.children) {
                    JComponent comp = renderMultipNode(child);
                    if (comp != null) root.add(comp);
                }
            } else if (stmt instanceof ASTNode.Page) {
                ASTNode.Page page = (ASTNode.Page) stmt;
                for (ASTNode child : page.body) {
                    if (child instanceof ASTNode.Window) {
                        ASTNode.Window win = (ASTNode.Window) child;
                        if (win.props.containsKey("title")) {
                            windowTitle = evaluateMultipExpr(win.props.get("title"));
                        }
                        for (ASTNode wc : win.children) {
                            JComponent comp = renderMultipNode(wc);
                            if (comp != null) root.add(comp);
                        }
                    } else {
                        JComponent comp = renderMultipNode(child);
                        if (comp != null) root.add(comp);
                    }
                }
            } else if (stmt instanceof ASTNode.Function) {
                // Functions are stored for later use, skip rendering
            } else if (stmt instanceof ASTNode.Var) {
                // Variables are stored for later use
            } else {
                JComponent comp = renderMultipNode(stmt);
                if (comp != null) root.add(comp);
            }
        }

        // Wrap in scroll pane
        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private JComponent renderMultipNode(ASTNode node) {
        if (node == null) return null;

        if (node instanceof ASTNode.UIElement) {
            ASTNode.UIElement elem = (ASTNode.UIElement) node;
            String type = elem.tag;
            Map<String, ASTNode> props = elem.props;
            List<ASTNode> children = elem.children;

            switch (type) {
                case "column":
                    return renderMultipColumn(props, children);
                case "row":
                    return renderMultipRow(props, children);
                case "text":
                    return renderMultipText(props);
                case "heading":
                    return renderMultipHeading(props);
                case "paragraph":
                    return renderMultipParagraph(props);
                case "button":
                    return renderMultipButton(props, children);
                case "textfield":
                case "input":
                    return renderMultipTextField(props);
                case "image":
                    return renderMultipImage(props);
                case "box":
                    return renderMultipBox(props, children);
                case "space":
                    return renderMultipSpace(props);
                case "divider":
                    return renderMultipDivider();
                default:
                    return renderMultipGeneric(type, props, children);
            }
        }

        if (node instanceof ASTNode.Var) {
            return null;
        }

        if (node instanceof ASTNode.Assign) {
            return null;
        }

        if (node instanceof ASTNode.For) {
            ASTNode.For forNode = (ASTNode.For) node;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (ASTNode child : forNode.body) {
                JComponent comp = renderMultipNode(child);
                if (comp != null) panel.add(comp);
            }
            return panel;
        }

        if (node instanceof ASTNode.If) {
            ASTNode.If ifNode = (ASTNode.If) node;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (ASTNode child : ifNode.thenBody) {
                JComponent comp = renderMultipNode(child);
                if (comp != null) panel.add(comp);
            }
            return panel;
        }

        if (node instanceof ASTNode.Print) {
            ASTNode.Print printNode = (ASTNode.Print) node;
            String value = evaluateMultipExpr(printNode.value);
            JLabel label = new JLabel(value);
            label.setFont(new Font("Monospaced", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(2, 5, 2, 5));
            return label;
        }

        return null;
    }

    private JPanel renderMultipColumn(Map<String, ASTNode> props, List<ASTNode> children) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        applyMultipProps(panel, props);
        renderMultipChildren(panel, children);
        return panel;
    }

    private JPanel renderMultipRow(Map<String, ASTNode> props, List<ASTNode> children) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        applyMultipProps(panel, props);
        renderMultipChildren(panel, children);
        return panel;
    }

    private JLabel renderMultipText(Map<String, ASTNode> props) {
        String value = getMultipStringProp(props, "value", "");
        int size = getMultipIntProp(props, "size", 16);
        JLabel label = new JLabel(value);
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        applyMultipFg(label, props);
        return label;
    }

    private JLabel renderMultipHeading(Map<String, ASTNode> props) {
        String text = getMultipStringProp(props, "text", getMultipStringProp(props, "value", ""));
        int size = getMultipIntProp(props, "size", 28);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        applyMultipFg(label, props);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JLabel renderMultipParagraph(Map<String, ASTNode> props) {
        String text = getMultipStringProp(props, "text", getMultipStringProp(props, "value", ""));
        int size = getMultipIntProp(props, "size", 16);
        JLabel label = new JLabel("<html><div style='width: 600px'>" + text + "</div></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        applyMultipFg(label, props);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    private JButton renderMultipButton(Map<String, ASTNode> props, List<ASTNode> children) {
        String text = getMultipStringProp(props, "text", getMultipStringProp(props, "value", "Button"));
        JButton button = new JButton(text);
        int size = getMultipIntProp(props, "size", 14);
        button.setFont(new Font("SansSerif", Font.PLAIN, size));

        if (props.containsKey("background")) {
            button.setBackground(MULTIP_PURPLE);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorderPainted(false);
        }

        button.addActionListener(e -> {
            statusLabel.setText("[Button] " + text + " clicked");
        });

        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private JTextField renderMultipTextField(Map<String, ASTNode> props) {
        String placeholder = getMultipStringProp(props, "placeholder", "");
        int cols = getMultipIntProp(props, "cols", 20);
        JTextField field = new JTextField(cols);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private JLabel renderMultipImage(Map<String, ASTNode> props) {
        String src = getMultipStringProp(props, "src", "");
        int w = getMultipIntProp(props, "width", 100);
        int h = getMultipIntProp(props, "height", 100);
        JLabel label = new JLabel("[Image: " + src + "]");
        label.setPreferredSize(new Dimension(w, h));
        label.setOpaque(true);
        label.setBackground(new Color(240, 240, 240));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel renderMultipBox(Map<String, ASTNode> props, List<ASTNode> children) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        applyMultipProps(panel, props);
        int pad = getMultipIntProp(props, "padding", 10);
        panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        renderMultipChildren(panel, children);
        return panel;
    }

    private JPanel renderMultipSpace(Map<String, ASTNode> props) {
        int size = getMultipIntProp(props, "size", 20);
        JPanel space = new JPanel();
        space.setPreferredSize(new Dimension(0, size));
        space.setMaximumSize(new Dimension(Integer.MAX_VALUE, size));
        return space;
    }

    private JSeparator renderMultipDivider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JPanel renderMultipGeneric(String type, Map<String, ASTNode> props, List<ASTNode> children) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        applyMultipProps(panel, props);
        renderMultipChildren(panel, children);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private void renderMultipChildren(JPanel panel, List<ASTNode> children) {
        if (children == null) return;
        for (ASTNode child : children) {
            JComponent comp = renderMultipNode(child);
            if (comp != null) panel.add(comp);
        }
    }

    private void applyMultipProps(JPanel panel, Map<String, ASTNode> props) {
        if (props.containsKey("background")) {
            panel.setBackground(multipParseColor(evaluateMultipExpr(props.get("background"))));
        }
        if (props.containsKey("padding")) {
            int pad = getMultipIntProp(props, "padding", 10);
            panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        }
    }

    private void applyMultipFg(JLabel label, Map<String, ASTNode> props) {
        if (props.containsKey("color")) {
            label.setForeground(multipParseColor(evaluateMultipExpr(props.get("color"))));
        } else if (props.containsKey("foreground")) {
            label.setForeground(multipParseColor(evaluateMultipExpr(props.get("foreground"))));
        }
    }

    private String getMultipStringProp(Map<String, ASTNode> props, String key, String def) {
        ASTNode val = props.get(key);
        if (val == null) return def;
        return evaluateMultipExpr(val);
    }

    private int getMultipIntProp(Map<String, ASTNode> props, String key, int def) {
        ASTNode val = props.get(key);
        if (val instanceof ASTNode.Num) {
            return (int) ((ASTNode.Num) val).value;
        }
        if (val != null) {
            try { return Integer.parseInt(evaluateMultipExpr(val)); } catch (Exception e) {}
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    private String evaluateMultipExpr(ASTNode node) {
        if (node == null) return "";
        if (node instanceof ASTNode.Str) return ((ASTNode.Str) node).value;
        if (node instanceof ASTNode.Num) return String.valueOf((int) ((ASTNode.Num) node).value);
        if (node instanceof ASTNode.Bool) return String.valueOf(((ASTNode.Bool) node).value);
        if (node instanceof ASTNode.Color) return "#" + ((ASTNode.Color) node).hex;
        if (node instanceof ASTNode.Identifier) return ((ASTNode.Identifier) node).name;
        if (node instanceof ASTNode.Null) return "null";
        if (node instanceof ASTNode.Binary) {
            ASTNode.Binary bin = (ASTNode.Binary) node;
            String left = evaluateMultipExpr(bin.left);
            String right = evaluateMultipExpr(bin.right);
            switch (bin.op) {
                case "+": return left + right;
                case "-": return String.valueOf(parseIntSafe(left) - parseIntSafe(right));
                case "*": return String.valueOf(parseIntSafe(left) * parseIntSafe(right));
                case "/": return String.valueOf(parseIntSafe(left) / Math.max(1, parseIntSafe(right)));
                case "==": return String.valueOf(left.equals(right));
                case "!=": return String.valueOf(!left.equals(right));
                default: return left;
            }
        }
        return node.toString();
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private Color multipParseColor(String str) {
        if (str == null || str.isEmpty()) return MULTIP_TEXT;
        if (str.startsWith("#")) {
            try { return Color.decode(str); } catch (Exception e) {}
        }
        switch (str.toLowerCase()) {
            case "white": return Color.WHITE;
            case "black": return Color.BLACK;
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "yellow": return Color.YELLOW;
            case "gray": case "grey": return Color.GRAY;
            case "orange": return Color.ORANGE;
            case "pink": return Color.PINK;
            case "purple": return MULTIP_PURPLE;
        }
        try { return Color.decode(str); } catch (Exception e) {}
        return MULTIP_TEXT;
    }

    private void loadFile(String path) {
        if (path.endsWith(".multip")) {
            loadMultipFile(path);
            return;
        }

        urlField.setText("file://" + path);
        statusLabel.setText("Loading: " + path);

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        filePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Local File");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        filePanel.add(titleLabel);

        JLabel pathLabel = new JLabel(path);
        pathLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        pathLabel.setForeground(Color.GRAY);
        filePanel.add(pathLabel);

        contentPanel.add(filePanel, "file_" + path);
        contentLayout.show(contentPanel, "file_" + path);
    }

    // ─── Tab Management ─────────────────────────────

    public void openTab() {
        tabModel.addElement("New Tab");
        currentTab = tabModel.size() - 1;
        tabList.setSelectedIndex(currentTab);

        JPanel homePage = createHomePage();
        String key = "tab_" + currentTab;
        contentPanel.add(homePage, key);
        contentLayout.show(contentPanel, key);

        urlField.setText("multip://home");
        statusLabel.setText("New tab opened");
    }

    public void closeTab(int index) {
        if (tabModel.size() <= 1) return;
        tabModel.remove(index);
        currentTab = Math.min(currentTab, tabModel.size() - 1);
        tabList.setSelectedIndex(currentTab);
    }

    // ─── History ────────────────────────────────────

    private void addHistory(String url) {
        history.add(url);
        historyIndex = history.size() - 1;
    }

    public void back() {
        if (historyIndex > 0) {
            historyIndex--;
            String url = history.get(historyIndex);
            urlField.setText(url);
            navigate(url);
        }
    }

    public void forward() {
        if (historyIndex < history.size() - 1) {
            historyIndex++;
            String url = history.get(historyIndex);
            urlField.setText(url);
            navigate(url);
        }
    }

    public void reload() {
        String url = urlField.getText();
        if (!url.isEmpty() && !url.equals("https://")) {
            navigate(url);
        }
    }

    public void home() {
        urlField.setText("multip://home");
        navigateInternal("multip://home");
    }

    // ─── Bookmarks ──────────────────────────────────

    public void bookmark(String url) {
        if (!bookmarks.contains(url)) {
            bookmarks.add(url);
            statusLabel.setText("Bookmarked: " + url);
        } else {
            bookmarks.remove(url);
            statusLabel.setText("Bookmark removed: " + url);
        }
    }

    // ─── Downloads ──────────────────────────────────

    public void download(String url) {
        downloads.add(url);
        statusLabel.setText("Downloaded: " + url);
    }

    // ─── UI Creators ────────────────────────────────

    private JPanel createHomePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(60, 60, 60, 60));
        page.setBackground(Color.WHITE);

        // Purple M logo
        JLabel logo = new JLabel("M");
        logo.setFont(new Font("SansSerif", Font.BOLD, 72));
        logo.setForeground(MULTIP_PURPLE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(logo);

        page.add(Box.createVerticalStrut(5));

        JLabel title = new JLabel("Multip Browser");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(MULTIP_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(title);

        page.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("A unified programming language for everything");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(MULTIP_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(subtitle);

        page.add(Box.createVerticalStrut(40));

        // Search / URL box in center
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField searchField = new JTextField(40);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MULTIP_LIGHT_PURPLE, 2, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    navigate(searchField.getText());
                }
            }
        });

        JButton searchBtn = new JButton("Go");
        searchBtn.setBackground(MULTIP_PURPLE);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setPreferredSize(new Dimension(70, 42));
        searchBtn.addActionListener(e -> navigate(searchField.getText()));

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        page.add(searchPanel);

        page.add(Box.createVerticalStrut(40));

        // Quick links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        links.setAlignmentX(Component.CENTER_ALIGNMENT);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String[][] quickLinks = {
            {"Home", "multip://home"},
            {"Settings", "multip://settings"},
            {"About", "multip://about"},
        };

        for (String[] ql : quickLinks) {
            JButton link = new JButton(ql[0]);
            link.setBackground(new Color(249, 250, 251));
            link.setForeground(MULTIP_PURPLE);
            link.setFont(new Font("SansSerif", Font.PLAIN, 13));
            link.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MULTIP_LIGHT_PURPLE, 1, true),
                new EmptyBorder(8, 18, 8, 18)
            ));
            link.addActionListener(e -> navigate(ql[1]));
            links.add(link);
        }

        page.add(links);

        page.add(Box.createVerticalStrut(60));

        // Feature cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        cards.setAlignmentX(Component.CENTER_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        String[][] features = {
            {"Language", "Write code in .multip files with HTML/CSS/JS"},
            {"Browser", "Built-in browser with tabs and navigation"},
            {"Platform", "Compile, publish, and run anywhere"},
        };

        for (String[] feat : features) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(new Color(249, 250, 251));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            JLabel cardTitle = new JLabel(feat[0]);
            cardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
            cardTitle.setForeground(MULTIP_PURPLE);
            card.add(cardTitle);

            card.add(Box.createVerticalStrut(5));

            JLabel cardDesc = new JLabel("<html><div style='width:180px'>" + feat[1] + "</div></html>");
            cardDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cardDesc.setForeground(MULTIP_GRAY);
            card.add(cardDesc);

            cards.add(card);
        }

        page.add(cards);

        return page;
    }

    private JPanel createInternalPage(String pageName) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(40, 40, 40, 40));
        page.setBackground(Color.WHITE);

        // Purple M header
        JLabel logo = new JLabel("M");
        logo.setFont(new Font("SansSerif", Font.BOLD, 48));
        logo.setForeground(MULTIP_PURPLE);
        page.add(logo);

        page.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel(pageName.substring(0, 1).toUpperCase() + pageName.substring(1));
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(MULTIP_TEXT);
        page.add(title);

        page.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(MULTIP_LIGHT_PURPLE);
        page.add(sep);

        page.add(Box.createVerticalStrut(20));

        switch (pageName) {
            case "home":
                page.add(createInfoBlock("Welcome to Multip",
                    "Multip is a unified programming language and application platform.\n" +
                    "Write entire applications in .multip files.\n\n" +
                    "Features:\n" +
                    "  - HTML/CSS/JS in one file\n" +
                    "  - Built-in browser engine\n" +
                    "  - Server-side rendering\n" +
                    "  - Package manager\n" +
                    "  - VS Code extension"));
                break;
            case "settings":
                page.add(createInfoBlock("Settings",
                    "Language: Multip v1.0\n" +
                    "Browser: Multip Browser v1.0\n" +
                    "Renderer: Java Swing\n" +
                    "Engine: Multip Runtime\n\n" +
                    "Theme: Purple (Default)\n" +
                    "Font: SansSerif"));
                break;
            case "about":
                page.add(createInfoBlock("About Multip",
                    "Multip combines HTML, CSS, JavaScript, and backend\n" +
                    "programming into one unified language.\n\n" +
                    "Version: 1.0\n" +
                    "License: MIT\n" +
                    "Author: Multip Language Team\n\n" +
                    "Website: https://github.com/samwise1776/multip"));
                break;
            default:
                page.add(createInfoBlock("Page Not Found",
                    "The multip://" + pageName + " page does not exist."));
        }

        return page;
    }

    private JLabel createInfoBlock(String title, String body) {
        JLabel label = new JLabel("<html><h2 style='color:#7C3AED'>" + title + "</h2><pre style='color:#6B7280;font-family:SansSerif'>" +
            body + "</pre></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JPanel createWebPage(String url) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 30, 30, 30));
        page.setBackground(Color.WHITE);

        JLabel title = new JLabel("Web Page");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        page.add(title);

        page.add(Box.createVerticalStrut(10));

        JLabel urlLabel = new JLabel(url);
        urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        urlLabel.setForeground(Color.GRAY);
        page.add(urlLabel);

        page.add(Box.createVerticalStrut(20));

        JLabel content = new JLabel("<html><div style='width:600px'>" +
            "<p>This is a web page loaded from: " + url + "</p>" +
            "<p>In a full implementation, this would render HTML content.</p>" +
            "</div></html>");
        content.setFont(new Font("SansSerif", Font.PLAIN, 14));
        page.add(content);

        return page;
    }

    // ─── Dialog Helpers ─────────────────────────────

    private void showHistory() {
        StringBuilder sb = new StringBuilder("Browsing History:\n\n");
        for (int i = history.size() - 1; i >= 0 && i >= history.size() - 20; i--) {
            sb.append(history.get(i)).append("\n");
        }
        if (history.isEmpty()) sb.append("No history yet.");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "History", JOptionPane.PLAIN_MESSAGE);
    }

    private void showDownloads() {
        StringBuilder sb = new StringBuilder("Downloads:\n\n");
        for (String d : downloads) {
            sb.append(d).append("\n");
        }
        if (downloads.isEmpty()) sb.append("No downloads yet.");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "Downloads", JOptionPane.PLAIN_MESSAGE);
    }

    private void showSettings() {
        String[] options = {"Close"};
        JOptionPane.showOptionDialog(this,
            "Multip Browser Settings\n\n" +
            "Version: 1.0\n" +
            "Engine: Multip Runtime\n" +
            "Renderer: Java Swing",
            "Settings", JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    // ─── Helpers ────────────────────────────────────

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBackground(new Color(45, 45, 48));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return btn;
    }

    public JTextField getUrlField() { return urlField; }
    public JLabel getStatusLabel() { return statusLabel; }
}
