/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/WindowsRegistryTool.java
# 📌 Amac: Windows Registry okuma/yazma islemlerini reg.exe uzerinden yapmak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: HKCU/HKLM dosya iliskilendirme anahtarlarini .reg import ile yazar, reg.exe ile okur

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class WindowsRegistryTool {
    public static final String ROOT_CURRENT_USER = "HKCU";
    public static final String ROOT_LOCAL_MACHINE = "HKLM";
    private static final int COMMAND_TIMEOUT_SECONDS = 20;

    public boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows");
    }

    public Optional<String> queryDefaultValue(String root, String key) {
        return queryValue(root, key, null);
    }

    public Optional<String> queryValue(String root, String key, String valueName) {
        if (!isWindows()) {
            return Optional.empty();
        }
        List<String> command = new ArrayList<>();
        command.add("reg");
        command.add("query");
        command.add(fullKey(root, key));
        if (valueName == null || valueName.isBlank()) {
            command.add("/ve");
        } else {
            command.add("/v");
            command.add(valueName);
        }
        try {
            ProcessResult result = run(command);
            if (result.exitCode() != 0) {
                return Optional.empty();
            }
            return parseRegistryValue(result.output());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }


    public void importRegistryScript(String registryScript) throws IOException, InterruptedException {
        if (!isWindows()) {
            throw new IOException("Windows Registry sadece Windows uzerinde desteklenir");
        }
        Path scriptPath = Files.createTempFile("unizip-registry-", ".reg");
        try {
            byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
            byte[] body = registryScript.getBytes(StandardCharsets.UTF_16LE);
            byte[] content = new byte[bom.length + body.length];
            System.arraycopy(bom, 0, content, 0, bom.length);
            System.arraycopy(body, 0, content, bom.length, body.length);
            Files.write(scriptPath, content);
            runOrThrow(List.of("reg", "import", scriptPath.toString()));
        } finally {
            try {
                Files.deleteIfExists(scriptPath);
            } catch (IOException ignored) {
                // Gecici .reg dosyasi silinemezse ana islem basarisiz sayilmaz.
            }
        }
    }

    public void addDefaultValue(String root, String key, String value) throws IOException, InterruptedException {
        runOrThrow(List.of(
                "reg",
                "add",
                fullKey(root, key),
                "/ve",
                "/d",
                value,
                "/f"
        ));
    }

    public void addStringValue(String root, String key, String valueName, String value) throws IOException, InterruptedException {
        addNamedValue(root, key, valueName, "REG_SZ", value);
    }

    public void addExpandableStringValue(String root, String key, String valueName, String value) throws IOException, InterruptedException {
        addNamedValue(root, key, valueName, "REG_EXPAND_SZ", value);
    }

    public void addNoneValue(String root, String key, String valueName) throws IOException, InterruptedException {
        addNamedValue(root, key, valueName, "REG_NONE", null);
    }

    private void addNamedValue(String root, String key, String valueName, String type, String value) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("reg");
        command.add("add");
        command.add(fullKey(root, key));
        command.add("/v");
        command.add(valueName);
        command.add("/t");
        command.add(type);
        if (value != null && !value.isEmpty()) {
            command.add("/d");
            command.add(value);
        }
        command.add("/f");
        runOrThrow(command);
    }

    public void deleteTree(String root, String key) throws IOException, InterruptedException {
        runOrThrow(List.of(
                "reg",
                "delete",
                fullKey(root, key),
                "/f"
        ));
    }

    public void deleteTreeIfExists(String root, String key) {
        if (!isWindows()) {
            return;
        }
        try {
            run(List.of(
                    "reg",
                    "delete",
                    fullKey(root, key),
                    "/f"
            ));
        } catch (Exception ignored) {
            // Existing UserChoice keys can be protected by Windows. Association write must continue.
        }
    }

    private void runOrThrow(List<String> command) throws IOException, InterruptedException {
        if (!isWindows()) {
            throw new IOException("Windows Registry sadece Windows uzerinde desteklenir");
        }
        ProcessResult result = run(command);
        if (result.exitCode() != 0) {
            throw new IOException(result.output().isBlank() ? "Registry komutu basarisiz" : result.output().trim());
        }
    }

    private ProcessResult run(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Registry komutu zaman asimina ugradi");
        }
        String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset());
        return new ProcessResult(process.exitValue(), output);
    }

    private String fullKey(String root, String key) {
        return root + "\\" + key;
    }

    private Optional<String> parseRegistryValue(String output) {
        if (output == null || output.isBlank()) {
            return Optional.empty();
        }
        for (String line : output.split("\\R")) {
            int typeIndex = line.indexOf("REG_");
            if (typeIndex < 0) {
                continue;
            }
            int valueStart = typeIndex;
            while (valueStart < line.length() && !Character.isWhitespace(line.charAt(valueStart))) {
                valueStart++;
            }
            while (valueStart < line.length() && Character.isWhitespace(line.charAt(valueStart))) {
                valueStart++;
            }
            if (valueStart < line.length()) {
                return Optional.of(line.substring(valueStart).trim());
            }
            return Optional.of("");
        }
        return Optional.empty();
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
