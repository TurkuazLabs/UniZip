/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/UpdateApiTool.java
# 📌 Amac: UniZip guncelleme manifestini ve yayin paketini HTTPS uzerinden almak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: YAML manifest okuma ve dosya indirme adaptorudur

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.UpdateManifest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public final class UpdateApiTool {
    private static final String USER_AGENT = "UniZip-UpdateClient";

    private final HttpClient httpClient;
    private final SimpleYamlTool yamlTool;

    public UpdateApiTool(SimpleYamlTool yamlTool) {
        this.yamlTool = yamlTool;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public UpdateManifest fetchManifest(String manifestUrl) throws Exception {
        URI uri = secureUri(manifestUrl);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(25))
                .header("Accept", "application/yaml,text/yaml,text/plain")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requireSuccess(response.statusCode(), "Guncelleme manifesti");
        Map<String, String> values = yamlTool.readFlattened(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
        UpdateManifest manifest = new UpdateManifest(
                values.get("release.product"),
                values.get("release.version"),
                values.get("release.channel"),
                Boolean.parseBoolean(values.getOrDefault("release.mandatory", "false")),
                values.get("release.minimum_version"),
                values.get("release.download_url"),
                values.get("release.sha256"),
                parseLong(values.get("release.size_bytes")),
                values.get("release.published_at"),
                values.get("release.notes")
        );
        manifest.validate();
        return manifest;
    }

    public Path download(String downloadUrl, Path targetFile) throws Exception {
        URI uri = secureUri(downloadUrl);
        Files.createDirectories(targetFile.getParent());
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMinutes(10))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(targetFile));
        requireSuccess(response.statusCode(), "Guncelleme paketi");
        return response.body();
    }

    private URI secureUri(String url) {
        URI uri = URI.create(url == null ? "" : url.trim());
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(java.util.Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(java.util.Locale.ROOT);
        boolean localDevelopment = "http".equals(scheme) && ("localhost".equals(host) || "127.0.0.1".equals(host));
        if (!"https".equals(scheme) && !localDevelopment) {
            throw new IllegalArgumentException("Guncelleme adresi HTTPS olmali");
        }
        return uri;
    }

    private void requireSuccess(int statusCode, String operation) {
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException(operation + " HTTP " + statusCode);
        }
    }

    private long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? 0L : Long.parseLong(value.trim());
        } catch (Exception exception) {
            return 0L;
        }
    }
}
