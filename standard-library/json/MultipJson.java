package standardlibrary.json;

import java.util.*;
import java.util.regex.*;

/**
 * Multip Standard Library — JSON Module
 * Lightweight JSON parser and serializer.
 */
public class MultipJson {
    private static int pos;
    private static String src;

    public static String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Boolean) return value.toString();
        if (value instanceof Number) return value.toString();
        if (value instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(stringify(list.get(i)));
            }
            return sb.append("]").toString();
        }
        if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) value;
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(", ");
                sb.append("\"").append(escapeJson(e.getKey().toString())).append("\": ");
                sb.append(stringify(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    public static Object parse(String json) {
        src = json.trim();
        pos = 0;
        return parseValue();
    }

    private static Object parseValue() {
        skipWhitespace();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        if (c == '"') return parseString();
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') { pos += 4; return null; }
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        return null;
    }

    private static String parseString() {
        pos++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (pos < src.length() && src.charAt(pos) != '"') {
            if (src.charAt(pos) == '\\') {
                pos++;
                char esc = src.charAt(pos);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u': sb.append((char) Integer.parseInt(src.substring(pos + 1, pos + 5), 16)); pos += 4; break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(src.charAt(pos));
            }
            pos++;
        }
        pos++; // skip closing quote
        return sb.toString();
    }

    private static Map<String, Object> parseObject() {
        pos++; // skip {
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == '}') { pos++; return map; }
        while (pos < src.length()) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            pos++; // skip :
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            else break;
        }
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == '}') pos++;
        return map;
    }

    private static List<Object> parseArray() {
        pos++; // skip [
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == ']') { pos++; return list; }
        while (pos < src.length()) {
            list.add(parseValue());
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            else break;
        }
        skipWhitespace();
        if (pos < src.length() && src.charAt(pos) == ']') pos++;
        return list;
    }

    private static Number parseNumber() {
        int start = pos;
        if (src.charAt(pos) == '-') pos++;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        boolean isFloat = false;
        if (pos < src.length() && src.charAt(pos) == '.') {
            isFloat = true;
            pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            isFloat = true;
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        String num = src.substring(start, pos);
        if (isFloat) return Double.parseDouble(num);
        long l = Long.parseLong(num);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
        return l;
    }

    private static boolean parseBoolean() {
        if (src.startsWith("true", pos)) { pos += 4; return true; }
        pos += 5; return false;
    }

    private static void skipWhitespace() {
        while (pos < src.length() && " \t\r\n".indexOf(src.charAt(pos)) >= 0) pos++;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r")
                .replace("\t", "\\t").replace("\b", "\\b").replace("\f", "\\f");
    }
}
