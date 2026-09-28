/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/UpdateService.java
# 📌 Amac: UniZip otomatik guncelleme kontrolu ve guvenli paket indirme kurallarini yonetmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Manifest kontrolu, kanal dogrulama, surum karsilastirma ve SHA-256 kontrolu yapar

Bagimli Oldugu Katman: Service | Repo/Model | Tool | Config
*/
package com.unizip.desktop.services;

import com.unizip.desktop.config.AppVersion;
import com.unizip.desktop.models.AppFeature;
import com.unizip.desktop.models.UpdateCheckResult;
import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.models.UpdateManifest;
import com.unizip.desktop.repositories.UpdateConfigRepository;
import com.unizip.desktop.repositories.UpdateRepository;
import com.unizip.desktop.tools.ChecksumTool;
import com.unizip.desktop.tools.UpdateApiTool;
import com.unizip.desktop.tools.VersionTool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public final class UpdateService {
    private final UpdateConfig config;
    private final UpdateRepository updateRepository;
    private final UpdateApiTool updateApiTool;
    private final VersionTool versionTool;
    private final ChecksumTool checksumTool;
    private final FeatureGateService featureGateService;

    public UpdateService(
            UpdateConfigRepository configRepository,
            UpdateRepository updateRepository,
            UpdateApiTool updateApiTool,
            VersionTool versionTool,
            ChecksumTool checksumTool,
            FeatureGateService featureGateService
    ) {
        this.config = configRepository.load();
        this.updateRepository = updateRepository;
        this.updateApiTool = updateApiTool;
        this.versionTool = versionTool;
        this.checksumTool = checksumTool;
        this.featureGateService = featureGateService;
    }

    public boolean shouldCheckOnStartup() {
        return config.enabled() && config.checkOnStartup() && featureGateService.isAllowed(AppFeature.AUTO_UPDATE);
    }

    public boolean autoDownloadEnabled() {
        return config.autoDownload();
    }

    public UpdateCheckResult checkForUpdates() throws Exception {
        featureGateService.require(AppFeature.AUTO_UPDATE);
        UpdateManifest manifest = updateApiTool.fetchManifest(config.manifestUrl());
        if (!config.channel().equalsIgnoreCase(manifest.channel())) {
            updateRepository.saveCheck(Instant.now(), "");
            return UpdateCheckResult.current(AppVersion.CURRENT, manifest);
        }
        boolean available = versionTool.compare(manifest.version(), AppVersion.CURRENT) > 0;
        updateRepository.saveCheck(Instant.now(), available ? manifest.version() : "");
        return available
                ? UpdateCheckResult.available(AppVersion.CURRENT, manifest)
                : UpdateCheckResult.current(AppVersion.CURRENT, manifest);
    }

    public Path download(UpdateManifest manifest) throws Exception {
        if (manifest == null) {
            throw new IllegalArgumentException("Guncelleme manifesti bulunamadi");
        }
        manifest.validate();
        Files.createDirectories(updateRepository.downloadsDirectory());
        Path target = updateRepository.downloadsDirectory().resolve(manifest.fileName());
        Path partial = target.resolveSibling(target.getFileName() + ".part");
        Files.deleteIfExists(partial);
        try {
            updateApiTool.download(manifest.downloadUrl(), partial);
            if (manifest.sizeBytes() > 0L && Files.size(partial) != manifest.sizeBytes()) {
                throw new IllegalStateException("Guncelleme dosya boyutu manifest ile uyusmuyor");
            }
            String actualHash = checksumTool.sha256(partial);
            if (!actualHash.equalsIgnoreCase(manifest.sha256())) {
                throw new IllegalStateException("Guncelleme SHA-256 dogrulamasi basarisiz");
            }
            try {
                return Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception exception) {
                return Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            Files.deleteIfExists(partial);
            throw exception;
        }
    }
}
