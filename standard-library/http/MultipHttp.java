package standardlibrary.http;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/**
 * Multip Standard Library — HTTP Module
 * Provides HTTP client functionality.
 */
public class MultipHttp {
    public static Map<String, Object> get(String url) throws Exception {
        return request("GET", url, null, null);
    }
    public static Map<String, Object> post(String url, String body) throws Exception {
        return request("POST", url, body, null);
    }
    public static Map<String, Object> put(String url, String body) throws Exception {
        return request("PUT", url, body, null);
    }
    public static Map<String, Object> delete(String url) throws Exception {
        return request("DELETE", url, null, null);
    }
    public static Map<String, Object> patch(String url, String body) throws Exception {
        return request("PATCH", url, body, null);
    }

    public static Map<String, Object> request(String method, String url, String body, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "Multip/1.0");

        if (headers != null) {
            for (Map.Entry<String, String> h : headers.entrySet()) {
                conn.setRequestProperty(h.getKey(), h.getValue());
            }
        }

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = readStream(is);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", (double) status);
        response.put("body", responseBody);
        response.put("ok", status >= 200 && status < 300);
        response.put("url", conn.getURL().toString());

        Map<String, String> responseHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            if (entry.getKey() != null) responseHeaders.put(entry.getKey(), String.join(", ", entry.getValue()));
        }
        response.put("headers", responseHeaders);

        conn.disconnect();
        return response;
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        return sb.toString().trim();
    }

    public static String encodeUrl(String s) throws UnsupportedEncodingException { return URLEncoder.encode(s, "UTF-8"); }
    public static String decodeUrl(String s) throws UnsupportedEncodingException { return URLDecoder.decode(s, "UTF-8"); }
}
