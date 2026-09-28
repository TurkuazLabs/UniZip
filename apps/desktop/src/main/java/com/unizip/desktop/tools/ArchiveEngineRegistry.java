/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ArchiveEngineRegistry.java
# 📌 Amac: Arsiv formatina gore uygun engine secimini yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Built-in ve ServiceLoader engine provider kaynaklarini tek merkezde toplar

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveEngineDescriptor;
import com.unizip.desktop.models.ArchiveFormat;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class ArchiveEngineRegistry {
    private final ArchiveFormatDetector detector;
    private final Map<String, ArchiveEngine> engines;

    public ArchiveEngineRegistry(ArchiveFormatDetector detector) {
        this.detector = detector;
        this.engines = new LinkedHashMap<>();
    }

    public static ArchiveEngineRegistry createDefault() {
        ArchiveEngineRegistry registry = new ArchiveEngineRegistry(new ArchiveFormatDetector());
        registry.registerProvider(new BuiltInArchiveEngineProvider());
        registry.registerServiceLoaderProviders();
        return registry;
    }

    public void registerProvider(ArchiveEngineProvider provider) {
        if (provider == null || provider.engines() == null) {
            return;
        }
        for (ArchiveEngine engine : provider.engines()) {
            register(engine);
        }
    }

    public void register(ArchiveEngine engine) {
        if (engine == null || engine.engineId() == null || engine.engineId().isBlank()) {
            return;
        }
        engines.put(engine.engineId(), engine);
    }

    public ArchiveEngine resolve(Path archivePath) {
        ArchiveFormat detectedFormat = detector.detect(archivePath);
        return engines.values().stream()
                .filter(engine -> engine.format() == detectedFormat)
                .filter(engine -> safeCanOpen(engine, archivePath))
                .findFirst()
                .orElse(new UnsupportedArchiveEngine(detectedFormat));
    }

    public ArchiveFormat detect(Path archivePath) {
        return detector.detect(archivePath);
    }

    public List<ArchiveEngineDescriptor> descriptors() {
        List<ArchiveEngineDescriptor> descriptors = new ArrayList<>();
        for (ArchiveEngine engine : engines.values()) {
            descriptors.add(engine.descriptor());
        }
        descriptors.sort(Comparator.comparing(ArchiveEngineDescriptor::engineId));
        return descriptors;
    }

    public boolean hasEngineFor(ArchiveFormat format) {
        return engines.values().stream().anyMatch(engine -> engine.format() == format);
    }

    private void registerServiceLoaderProviders() {
        ServiceLoader<ArchiveEngineProvider> loader = ServiceLoader.load(ArchiveEngineProvider.class);
        for (ArchiveEngineProvider provider : loader) {
            if (!(provider instanceof BuiltInArchiveEngineProvider)) {
                registerProvider(provider);
            }
        }
    }

    private boolean safeCanOpen(ArchiveEngine engine, Path archivePath) {
        try {
            return engine.canOpen(archivePath);
        } catch (Exception exception) {
            return false;
        }
    }
}
