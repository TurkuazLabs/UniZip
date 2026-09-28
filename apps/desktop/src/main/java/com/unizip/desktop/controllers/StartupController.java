/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/controllers/StartupController.java
# 📌 Amac: UniZip acilis lisans ve otomatik guncelleme akisini koordine etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: UI threadini bloklamadan lisans validate, update check ve paket indirme islemlerini yonetir

Bagimli Oldugu Katman: Controller | Service | View
*/
package com.unizip.desktop.controllers;

import com.unizip.desktop.models.UpdateCheckResult;
import com.unizip.desktop.models.UpdateManifest;
import com.unizip.desktop.services.LicenseGuardService;
import com.unizip.desktop.services.UpdateService;
import com.unizip.desktop.views.MainFrame;

import javax.swing.SwingWorker;
import java.nio.file.Path;

public final class StartupController {
    private final LicenseGuardService licenseGuardService;
    private final UpdateService updateService;
    private final MainFrame mainFrame;

    public StartupController(LicenseGuardService licenseGuardService, UpdateService updateService, MainFrame mainFrame) {
        this.licenseGuardService = licenseGuardService;
        this.updateService = updateService;
        this.mainFrame = mainFrame;
    }

    public void start() {
        refreshLicenseAsync();
        if (updateService.shouldCheckOnStartup()) {
            checkForUpdates(false);
        }
    }

    public void checkForUpdatesManually() {
        checkForUpdates(true);
    }

    public void downloadUpdate(UpdateManifest manifest) {
        mainFrame.showUpdateDownloadStarted();
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return updateService.download(manifest);
            }

            @Override
            protected void done() {
                try {
                    mainFrame.showUpdateDownloaded(get());
                } catch (Exception exception) {
                    mainFrame.showUpdateError(rootMessage(exception));
                }
            }
        }.execute();
    }

    private void refreshLicenseAsync() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                licenseGuardService.refreshAtStartup();
                return null;
            }

            @Override
            protected void done() {
                mainFrame.refreshLicenseState();
            }
        }.execute();
    }

    private void checkForUpdates(boolean manual) {
        if (manual) {
            mainFrame.showUpdateCheckStarted();
        }
        new SwingWorker<UpdateCheckResult, Void>() {
            @Override
            protected UpdateCheckResult doInBackground() throws Exception {
                return updateService.checkForUpdates();
            }

            @Override
            protected void done() {
                try {
                    UpdateCheckResult result = get();
                    if (result.updateAvailable()) {
                        if (updateService.autoDownloadEnabled()) {
                            downloadUpdate(result.manifest());
                        } else {
                            mainFrame.showUpdateAvailable(result.manifest());
                        }
                    } else if (manual) {
                        mainFrame.showNoUpdateAvailable(result.currentVersion());
                    }
                } catch (Exception exception) {
                    if (manual) {
                        mainFrame.showUpdateError(rootMessage(exception));
                    }
                }
            }
        }.execute();
    }

    private String rootMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null || current.getMessage().isBlank()
                ? current.getClass().getSimpleName()
                : current.getMessage();
    }
}
