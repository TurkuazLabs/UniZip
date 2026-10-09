/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/MainApp.java
# 📌 Amac: UniZip Desktop uygulamasini baslatmak
# 📌 Modul - FileType
# Version: 0.3.0
# Aciklama: Community default veya harici edition runtime service bundle ile lisans, update policy, Explorer CLI ve arsiv acma baglantilarini kurar

Bagimli Oldugu Katman: Controller | Service | View
*/
package com.unizip.desktop;

import com.unizip.desktop.controllers.ArchiveController;
import com.unizip.desktop.controllers.StartupController;
import com.unizip.desktop.repositories.HistoryRepository;
import com.unizip.desktop.repositories.RecentArchiveRepository;
import com.unizip.desktop.repositories.SettingsRepository;
import com.unizip.desktop.repositories.UpdateConfigRepository;
import com.unizip.desktop.repositories.UpdateRepository;
import com.unizip.desktop.repositories.LicenseRepository;
import com.unizip.desktop.services.ArchiveService;
import com.unizip.desktop.services.DesktopRuntimeServices;
import com.unizip.desktop.services.FeatureGateService;
import com.unizip.desktop.services.FileAssociationService;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.LogService;
import com.unizip.desktop.services.LicenseGuardService;
import com.unizip.desktop.services.LicenseService;
import com.unizip.desktop.services.RecentArchiveService;
import com.unizip.desktop.services.SettingsService;
import com.unizip.desktop.services.ThemeService;
import com.unizip.desktop.services.UpdateService;
import com.unizip.desktop.tools.ChecksumTool;
import com.unizip.desktop.tools.ExternalEditorTool;
import com.unizip.desktop.tools.FileSystemTool;
import com.unizip.desktop.tools.DeviceFingerprintTool;
import com.unizip.desktop.tools.LicenseApiTool;
import com.unizip.desktop.tools.FlatLafTool;
import com.unizip.desktop.tools.JavaZipTool;
import com.unizip.desktop.tools.SafeExtractTool;
import com.unizip.desktop.tools.SimpleYamlTool;
import com.unizip.desktop.tools.UserDirectoryTool;
import com.unizip.desktop.tools.UpdateApiTool;
import com.unizip.desktop.tools.VersionTool;
import com.unizip.desktop.tools.WindowsRegistryTool;
import com.unizip.desktop.views.MainFrame;

import com.unizip.desktop.models.ArchiveOperationResult;
import com.unizip.desktop.models.ExtractOverwriteMode;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class MainApp {
    private MainApp() {
    }

    public static void main(String[] args) {
        launch(
                args,
                DesktopRuntimeServices.community());
    }

    public static void launch(
            String[] args,
            DesktopRuntimeServices runtimeServices) {
        DesktopRuntimeServices safeRuntimeServices =
                java.util.Objects.requireNonNull(
                        runtimeServices,
                        "runtimeServices");
        String[] safeArgs =
                args == null
                        ? new String[0]
                        : args.clone();

        SwingUtilities.invokeLater(
                () -> start(
                        safeArgs,
                        safeRuntimeServices));
    }

    private static void start(
            String[] args,
            DesktopRuntimeServices runtimeServices) {
        SimpleYamlTool yamlTool = new SimpleYamlTool();
        SettingsRepository settingsRepository = new SettingsRepository();
        ThemeService themeService = new ThemeService(yamlTool, settingsRepository);
        FlatLafTool flatLafTool = new FlatLafTool();
        flatLafTool.install(themeService.activeThemeMode());
        LanguageService languageService = new LanguageService(yamlTool, settingsRepository);
        FileAssociationService fileAssociationService = new FileAssociationService(new WindowsRegistryTool());
        SettingsService settingsService = new SettingsService(settingsRepository, themeService, languageService, fileAssociationService);
        LogService logService = new LogService(new HistoryRepository());
        RecentArchiveService recentArchiveService = new RecentArchiveService(new RecentArchiveRepository());
        LicenseService licenseService = new LicenseService(new LicenseRepository(), new LicenseApiTool(), new DeviceFingerprintTool());
        LicenseGuardService licenseGuardService = new LicenseGuardService(licenseService);
        FeatureGateService featureGateService = new FeatureGateService(licenseService);
        ChecksumTool checksumTool = new ChecksumTool();
        UpdateService updateService = new UpdateService(
                new UpdateConfigRepository(yamlTool),
                new UpdateRepository(),
                new UpdateApiTool(yamlTool),
                new VersionTool(),
                checksumTool,
                featureGateService,
                runtimeServices.updateSourcePolicyService()
        );

        SafeExtractTool safeExtractTool = new SafeExtractTool();
        JavaZipTool javaZipTool = new JavaZipTool(safeExtractTool);
        FileSystemTool fileSystemTool = new FileSystemTool();
        ExternalEditorTool externalEditorTool = new ExternalEditorTool();
        ArchiveService archiveService = new ArchiveService(javaZipTool, fileSystemTool, logService, checksumTool, externalEditorTool, settingsService);
        if (handleShellCommand(args, archiveService, checksumTool)) {
            return;
        }
        UserDirectoryTool userDirectoryTool = new UserDirectoryTool();

        MainFrame mainFrame = new MainFrame(themeService, languageService, settingsService, logService, flatLafTool, licenseService, featureGateService);
        ArchiveController archiveController = new ArchiveController(archiveService, mainFrame, languageService, logService, settingsService, recentArchiveService, userDirectoryTool);
        mainFrame.setArchiveController(archiveController);
        StartupController startupController = new StartupController(licenseGuardService, updateService, mainFrame);
        mainFrame.setStartupController(startupController);
        mainFrame.showFrame();
        startupController.start();
        openArchiveFromArguments(args, archiveController);
    }

    private static void openArchiveFromArguments(String[] args, ArchiveController archiveController) {
        if (args == null || args.length == 0 || args[0] == null || args[0].isBlank()) {
            return;
        }
        if (args[0].startsWith("--")) {
            return;
        }
        archiveController.openRecentArchive(Path.of(args[0]));
    }

    private static boolean handleShellCommand(String[] args, ArchiveService archiveService, ChecksumTool checksumTool) {
        if (args == null || args.length < 2 || args[0] == null || !args[0].startsWith("--")) {
            return false;
        }
        try {
            String command = args[0].trim().toLowerCase(Locale.ROOT);
            Path inputPath = Path.of(args[1]).toAbsolutePath().normalize();
            ArchiveOperationResult result = switch (command) {
                case "--extract-here" -> archiveService.extractZip(
                        inputPath,
                        shellOutputDirectory(inputPath),
                        false,
                        List.of(),
                        ExtractOverwriteMode.RENAME
                );
                case "--extract-to-folder" -> archiveService.extractZip(
                        inputPath,
                        shellOutputDirectory(inputPath),
                        true,
                        List.of(),
                        ExtractOverwriteMode.RENAME
                );
                case "--test" -> archiveService.testZip(inputPath);
                case "--hash-sha256" -> new ArchiveOperationResult(
                        true, "SHA-256 dosyasi olusturuldu: "
                        + checksumTool.writeSha256Sidecar(inputPath), 1);
                case "--add-to-archive" -> archiveService.createZip(inputPath, uniqueArchivePath(inputPath));
                default -> null;
            };
            if (result == null) {
                return false;
            }
            JOptionPane.showMessageDialog(null, result.message(), "UniZip", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "UniZip", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    private static Path shellOutputDirectory(Path archivePath) {
        Path parent = archivePath == null ? null : archivePath.getParent();
        return parent == null ? Path.of(System.getProperty("user.dir", ".")) : parent;
    }

    private static Path uniqueArchivePath(Path inputPath) throws Exception {
        Path parent = inputPath.getParent();
        if (parent == null) {
            parent = Path.of(System.getProperty("user.dir", "."));
        }
        String fileName = inputPath.getFileName() == null ? "archive" : inputPath.getFileName().toString();
        String baseName = stripExtension(fileName);
        Path candidate = parent.resolve(baseName + ".zip");
        int index = 2;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(baseName + " (" + index + ").zip");
            index++;
        }
        return candidate;
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "archive";
        }
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex <= 0) {
            return name;
        }
        return name.substring(0, dotIndex);
    }

}
