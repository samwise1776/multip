package browser.engine;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
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

    public Browser() {
        setLayout(new BorderLayout());

        // ── Tab Bar ─────────────────────────────────
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setBackground(new Color(45, 45, 48));
        tabPanel.setPreferredSize(new Dimension(0, 35));

        tabModel = new DefaultListModel<>();
        tabModel.addElement("New Tab");
        tabList = new JList<>(tabModel);
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.setFixedCellHeight(30);
        tabList.setBackground(new Color(45, 45, 48));
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
        newTabBtn.setBackground(new Color(45, 45, 48));
        newTabBtn.setForeground(Color.WHITE);
        newTabBtn.setBorderPainted(false);
        newTabBtn.addActionListener(e -> openTab());

        tabPanel.add(tabScroll, BorderLayout.CENTER);
        tabPanel.add(newTabBtn, BorderLayout.EAST);
        add(tabPanel, BorderLayout.NORTH);

        // ── Toolbar ─────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(5, 0));
        toolbar.setBackground(new Color(37, 37, 38));
        toolbar.setBorder(new EmptyBorder(5, 8, 5, 8));
        toolbar.setPreferredSize(new Dimension(0, 40));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        navButtons.setOpaque(false);

        JButton backBtn = createNavButton("◀", "Back");
        backBtn.addActionListener(e -> back());

        JButton fwdBtn = createNavButton("▶", "Forward");
        fwdBtn.addActionListener(e -> forward());

        JButton refreshBtn = createNavButton("↻", "Refresh");
        refreshBtn.addActionListener(e -> reload());

        JButton homeBtn = createNavButton("🏠", "Home");
        homeBtn.addActionListener(e -> home());

        navButtons.add(backBtn);
        navButtons.add(fwdBtn);
        navButtons.add(refreshBtn);
        navButtons.add(homeBtn);

        toolbar.add(navButtons, BorderLayout.WEST);

        // ── URL Bar ─────────────────────────────────
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlPanel.setOpaque(false);

        urlField = new JTextField("https://");
        urlField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        urlField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
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
        goBtn.setBackground(new Color(0, 120, 215));
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

        JButton bookmarkBtn = createNavButton("★", "Bookmark");
        bookmarkBtn.addActionListener(e -> bookmark(urlField.getText()));

        JButton historyBtn = createNavButton("⏱", "History");
        historyBtn.addActionListener(e -> showHistory());

        JButton downloadsBtn = createNavButton("⬇", "Downloads");
        downloadsBtn.addActionListener(e -> showDownloads());

        JButton settingsBtn = createNavButton("⚙", "Settings");
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
        statusBar.setBackground(new Color(0, 120, 215));
        statusBar.setPreferredSize(new Dimension(0, 25));
        statusBar.setBorder(new EmptyBorder(2, 8, 2, 8));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel versionLabel = new JLabel("Multip Browser v1.0");
        versionLabel.setForeground(new Color(200, 220, 255));
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

        // Standard HTTP/HTTPS
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        final String finalUrl = url;
        urlField.setText(finalUrl);
        statusLabel.setText("Loading: " + finalUrl + "...");
        addHistory(finalUrl);

        // Simulate page load
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    Thread.sleep(500); // Simulate network delay
                } catch (InterruptedException ignored) {}
                return finalUrl;
            }

            @Override
            protected void done() {
                try {
                    String loadedUrl = get();
                    displayUrl(loadedUrl);
                    statusLabel.setText("Done: " + loadedUrl);
                } catch (Exception e) {
                    statusLabel.setText("Error loading page");
                }
            }
        };
        worker.execute();
    }

    private void navigateInternal(String url) {
        String page = url.replace("multip://", "");
        urlField.setText(url);
        addHistory(url);
        statusLabel.setText("Loading multip://" + page);

        // Check if we have a matching route
        JPanel pagePanel = createInternalPage(page);
        String key = "page_" + page;
        contentPanel.add(pagePanel, key);
        contentLayout.show(contentPanel, key);
    }

    private void loadFile(String path) {
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

    private void displayUrl(String url) {
        JPanel page = createWebPage(url);
        String key = "web_" + url.hashCode();
        contentPanel.add(page, key);
        contentLayout.show(contentPanel, key);
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

        urlField.setText("https://");
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
        page.setBorder(new EmptyBorder(40, 40, 40, 40));
        page.setBackground(Color.WHITE);

        // Logo / Title
        JLabel logo = new JLabel("Multip Browser");
        logo.setFont(new Font("SansSerif", Font.BOLD, 36));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(logo);

        page.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Welcome to the Multip Platform");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(subtitle);

        page.add(Box.createVerticalStrut(30));

        // Search / URL box in center
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField searchField = new JTextField(40);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
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
        searchBtn.setBackground(new Color(0, 120, 215));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setPreferredSize(new Dimension(70, 40));
        searchBtn.addActionListener(e -> navigate(searchField.getText()));

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        page.add(searchPanel);

        page.add(Box.createVerticalStrut(40));

        // Quick links
        JLabel linksTitle = new JLabel("Quick Links");
        linksTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        linksTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(linksTitle);

        page.add(Box.createVerticalStrut(10));

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        links.setAlignmentX(Component.CENTER_ALIGNMENT);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String[][] quickLinks = {
            {"Multip Home", "multip://home"},
            {"Settings", "multip://settings"},
            {"About", "multip://about"},
        };

        for (String[] ql : quickLinks) {
            JButton link = new JButton(ql[0]);
            link.setBackground(new Color(245, 245, 245));
            link.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(8, 16, 8, 16)
            ));
            link.addActionListener(e -> navigate(ql[1]));
            links.add(link);
        }

        page.add(links);

        return page;
    }

    private JPanel createInternalPage(String pageName) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 30, 30, 30));
        page.setBackground(Color.WHITE);

        JLabel title = new JLabel(pageName.substring(0, 1).toUpperCase() + pageName.substring(1));
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        page.add(title);

        page.add(Box.createVerticalStrut(20));

        switch (pageName) {
            case "home":
                page.add(createInfoBlock("Welcome to Multip",
                    "Multip is a unified programming language and application platform.\n" +
                    "Write entire applications in .multip files."));
                break;
            case "settings":
                page.add(createInfoBlock("Settings",
                    "Language: Multip v1.0\nBrowser: Multip Browser\nRenderer: Swing"));
                break;
            case "about":
                page.add(createInfoBlock("About Multip",
                    "Multip combines HTML, CSS, JavaScript, and backend\n" +
                    "programming into one unified language.\n\n" +
                    "Version: 1.0\nLicense: Open Source"));
                break;
            default:
                page.add(createInfoBlock("Page Not Found",
                    "The multip://" + pageName + " page does not exist."));
        }

        return page;
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

    private JLabel createInfoBlock(String title, String body) {
        JLabel label = new JLabel("<html><h2>" + title + "</h2><pre style='color:gray'>" +
            body + "</pre></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(10, 0, 10, 0));
        return label;
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
