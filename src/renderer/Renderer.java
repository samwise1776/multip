package renderer;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Renderer extends JFrame {
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> pages = new HashMap<>();
    private final Map<String, JPanel> componentDefs = new HashMap<>();

    public Renderer() {
        setTitle("Multip Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        add(contentPanel);
    }

    @SuppressWarnings("unchecked")
    public void renderUI(List<Object> uiTree) {
        for (Object node : uiTree) {
            if (node instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) node;
                Object typeObj = map.get("_type");
                String type = typeObj != null ? typeObj.toString() : "";
                if (type.equals("window")) {
                    renderWindow(map);
                }
            }
        }
        setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void renderWindow(Map<String, Object> window) {
        Object titleObj = window.get("title");
        if (titleObj != null) setTitle(titleObj.toString());

        contentPanel.removeAll();

        List<Object> children = (List<Object>) window.get("_children");
        if (children != null) {
            for (Object child : children) {
                JComponent comp = renderNode(child);
                if (comp != null) {
                    contentPanel.add(comp, "page_" + contentPanel.getComponentCount());
                }
            }
        }

        revalidate();
        repaint();
    }

    @SuppressWarnings("unchecked")
    public JComponent renderNode(Object node) {
        if (node == null) return null;

        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;
            Object typeObj = map.get("_type");
            String type = typeObj != null ? typeObj.toString() : "";

            switch (type) {
                case "column": return renderColumn(map);
                case "row": return renderRow(map);
                case "text": return renderText(map);
                case "heading": return renderHeading(map);
                case "paragraph": return renderParagraph(map);
                case "button": return renderButton(map);
                case "textfield":
                case "input": return renderTextField(map);
                case "image": return renderImage(map);
                case "box": return renderBox(map);
                case "space": return renderSpace(map);
                case "divider": return renderDivider(map);
                default: return renderGeneric(map);
            }
        }

        if (node instanceof List) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (Object child : (List<?>) node) {
                JComponent comp = renderNode(child);
                if (comp != null) panel.add(comp);
            }
            return panel;
        }

        return null;
    }

    private JPanel renderColumn(Map<String, Object> map) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        applyProps(panel, map);
        renderChildren(panel, map);
        return panel;
    }

    private JPanel renderRow(Map<String, Object> map) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        applyProps(panel, map);
        renderChildren(panel, map);
        return panel;
    }

    private JLabel renderText(Map<String, Object> map) {
        String value = getString(map, "value", "");
        int size = getInt(map, "size", 16);
        JLabel label = new JLabel(value);
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        applyFg(label, map);
        return label;
    }

    private JLabel renderHeading(Map<String, Object> map) {
        String text = getString(map, "text", getString(map, "value", ""));
        int size = getInt(map, "size", 28);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, size));
        applyFg(label, map);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    private JLabel renderParagraph(Map<String, Object> map) {
        String text = getString(map, "text", getString(map, "value", ""));
        int size = getInt(map, "size", 16);
        JLabel label = new JLabel("<html><div style='width: 600px'>" + text + "</div></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        applyFg(label, map);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    private JButton renderButton(Map<String, Object> map) {
        String text = getString(map, "text", getString(map, "value", "Button"));
        JButton button = new JButton(text);
        int size = getInt(map, "size", 14);
        button.setFont(new Font("SansSerif", Font.PLAIN, size));

        if (map.containsKey("background")) {
            button.setBackground(parseColor(String.valueOf(map.get("background"))));
            button.setOpaque(true);
        }
        if (map.containsKey("foreground")) {
            button.setForeground(parseColor(String.valueOf(map.get("foreground"))));
        }

        button.addActionListener(e -> System.out.println("[Button] " + text + " clicked"));

        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private JTextField renderTextField(Map<String, Object> map) {
        String placeholder = getString(map, "placeholder", "");
        int cols = getInt(map, "cols", 20);
        JTextField field = new JTextField(cols);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setToolTipText(placeholder);
        if (map.containsKey("value")) field.setText(String.valueOf(map.get("value")));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private JLabel renderImage(Map<String, Object> map) {
        String src = getString(map, "src", "");
        int w = getInt(map, "width", 100);
        int h = getInt(map, "height", 100);
        JLabel label = new JLabel("[Image: " + src + " " + w + "x" + h + "]");
        label.setPreferredSize(new Dimension(w, h));
        label.setOpaque(true);
        label.setBackground(new Color(240, 240, 240));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel renderBox(Map<String, Object> map) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        applyProps(panel, map);
        if (map.containsKey("background")) panel.setBackground(parseColor(String.valueOf(map.get("background"))));
        int pad = getInt(map, "padding", 10);
        panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        renderChildren(panel, map);
        return panel;
    }

    private JPanel renderSpace(Map<String, Object> map) {
        int size = getInt(map, "size", 20);
        JPanel space = new JPanel();
        space.setPreferredSize(new Dimension(0, size));
        space.setMaximumSize(new Dimension(Integer.MAX_VALUE, size));
        return space;
    }

    private JSeparator renderDivider(Map<String, Object> map) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JPanel renderGeneric(Map<String, Object> map) {
        JPanel panel = new JPanel();
        applyProps(panel, map);
        renderChildren(panel, map);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private void renderChildren(JPanel panel, Map<String, Object> map) {
        Object children = map.get("_children");
        if (children instanceof List) {
            for (Object child : (List<?>) children) {
                JComponent comp = renderNode(child);
                if (comp != null) panel.add(comp);
            }
        }
    }

    private void applyProps(JPanel panel, Map<String, Object> map) {
        if (map.containsKey("background")) panel.setBackground(parseColor(String.valueOf(map.get("background"))));
        if (map.containsKey("padding")) {
            int pad = getInt(map, "padding", 10);
            panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        }
    }

    private void applyFg(JLabel label, Map<String, Object> map) {
        if (map.containsKey("color")) label.setForeground(parseColor(String.valueOf(map.get("color"))));
        else if (map.containsKey("foreground")) label.setForeground(parseColor(String.valueOf(map.get("foreground"))));
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) { try { return Integer.parseInt(val.toString()); } catch (Exception e) {} }
        return def;
    }

    private Color parseColor(String str) {
        if (str == null || str.isEmpty()) return Color.BLACK;
        if (str.startsWith("#")) {
            try {
                return Color.decode(str);
            } catch (Exception e) {}
        }
        switch (str.toLowerCase()) {
            case "white": return Color.WHITE;
            case "black": return Color.BLACK;
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "yellow": return Color.YELLOW;
            case "gray":
            case "grey": return Color.GRAY;
            case "orange": return Color.ORANGE;
            case "pink": return Color.PINK;
            case "dark_gray": return Color.DARK_GRAY;
            case "light_gray": return Color.LIGHT_GRAY;
        }
        try { return Color.decode(str); } catch (Exception e) {}
        return Color.BLACK;
    }
}
