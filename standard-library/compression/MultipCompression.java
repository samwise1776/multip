package standardlibrary.compression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.*;

/**
 * Multip Standard Library — Compression Module
 * Provides gzip compression and decompression.
 */
public class MultipCompression {
    public static String gzip(String input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(input.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String gunzip(String compressed) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(compressed);
        ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        BufferedReader reader = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        return sb.toString().trim();
    }

    public static String deflate(String input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(input.getBytes(StandardCharsets.UTF_8));
        dos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String inflate(String compressed) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(compressed);
        ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
        InflaterInputStream iis = new InflaterInputStream(bais);
        BufferedReader reader = new BufferedReader(new InputStreamReader(iis, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        return sb.toString().trim();
    }
}
