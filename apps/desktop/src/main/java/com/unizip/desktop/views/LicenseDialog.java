/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/LicenseDialog.java
# 📌 Amac: UniZip OpenCart lisans giris ve durum penceresini gostermek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: Email/sifre girisi, token dogrulama, cikis ve API adresi ayarlarini sunar

Bagimli Oldugu Katman: View
*/
package com.unizip.desktop.views;

import com.unizip.desktop.models.LicenseState;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.LicenseService;
import com.unizip.desktop.services.ThemeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class LicenseDialog extends JDialog {
    private final LanguageService languageService;
    private final ThemeService themeService;
    private final LicenseService licenseService;

    private JTextField apiBaseUrlField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextArea statusArea;
    private JButton loginButton;
    private JButton validateButton;
    private JButton logoutButton;

    public LicenseDialog(JFrame owner, LanguageService languageService, ThemeService themeService, LicenseService licenseService) {
        super(owner, languageService.text("dialog.license_title"), true);
        this.languageService = languageService;
        this.themeService = themeService;
        this.licenseService = licenseService;
        buildLayout();
        refreshState();
    }

    private void buildLayout() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 430);
        setResizable(false);

        JPanel rootPanel = new JPanel(new BorderLayout(0, themeService.spacing("md")));
        rootPanel.setBackground(themeService.color("panel.background"));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md")
        ));
        rootPanel.add(buildFormPanel(), BorderLayout.NORTH);
        rootPanel.add(buildStatusPanel(), BorderLayout.CENTER);
        rootPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        apiBaseUrlField = new JTextField(licenseService.currentState().apiBaseUrl());
        emailField = new JTextField(licenseService.currentState().customerEmail());
        passwordField = new JPasswordField();

        addRow(panel, 0, languageService.text("license.api_base_url"), apiBaseUrlField);
        addRow(panel, 1, languageService.text("license.email"), emailField);
        addRow(panel, 2, languageService.text("license.password"), passwordField);
        addFullWidthRow(panel, 3, mutedLabel(languageService.text("license.password_note")));
        return panel;
    }

    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, themeService.spacing("xs")));
        panel.setOpaque(false);
        JLabel label = new JLabel(languageService.text("license.status"));
        label.setFont(themeService.font("default"));
        label.setForeground(themeService.color("text.primary"));
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(themeService.font("default"));
        statusArea.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm")));
        panel.add(label, BorderLayout.NORTH);
        panel.add(statusArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("sm"), 0));
        panel.setOpaque(false);

        JButton saveEndpointButton = button("button.save_license_endpoint");
        saveEndpointButton.addActionListener(event -> saveEndpoint());

        loginButton = button("button.license_login");
        loginButton.addActionListener(event -> login());

        validateButton = button("button.license_validate");
        validateButton.addActionListener(event -> validateOnline());

        logoutButton = button("button.license_logout");
        logoutButton.addActionListener(event -> logout());

        JButton closeButton = button("button.close");
        closeButton.addActionListener(event -> dispose());

        panel.add(saveEndpointButton);
        panel.add(loginButton);
        panel.add(validateButton);
        panel.add(logoutButton);
        panel.add(closeButton);
        return panel;
    }

    private JButton button(String key) {
        JButton button = new JButton(languageService.text(key));
        button.setFont(themeService.font("default"));
        return button;
    }

    private JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(themeService.font("default"));
        label.setForeground(themeService.color("text.muted"));
        return label;
    }

    private void addRow(JPanel panel, int row, String labelText, java.awt.Component component) {
        JLabel label = new JLabel(labelText);
        label.setFont(themeService.font("default"));
        label.setForeground(themeService.color("text.primary"));
        component.setFont(themeService.font("default"));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 0.0;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(0, 0, themeService.spacing("sm"), themeService.spacing("md"));
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, themeService.spacing("sm"), 0);
        panel.add(component, fieldConstraints);
    }

    private void addFullWidthRow(JPanel panel, int row, java.awt.Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(themeService.spacing("xs"), 0, themeService.spacing("sm"), 0);
        panel.add(component, constraints);
    }

    private void saveEndpoint() {
        try {
            licenseService.saveApiBaseUrl(apiBaseUrlField.getText());
            refreshState();
            showInfo(languageService.text("message.license_endpoint_saved"));
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void login() {
        setBusy(true);
        SwingWorker<LicenseState, Void> worker = new SwingWorker<>() {
            @Override
            protected LicenseState doInBackground() throws Exception {
                return licenseService.login(apiBaseUrlField.getText(), emailField.getText(), passwordField.getPassword());
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    passwordField.setText("");
                    refreshState();
                    showInfo(languageService.text("message.license_login_success"));
                } catch (Exception exception) {
                    showError(rootMessage(exception));
                }
            }
        };
        worker.execute();
    }

    private void validateOnline() {
        setBusy(true);
        SwingWorker<LicenseState, Void> worker = new SwingWorker<>() {
            @Override
            protected LicenseState doInBackground() throws Exception {
                return licenseService.validateOnline();
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    refreshState();
                    showInfo(languageService.text("message.license_validate_success"));
                } catch (Exception exception) {
                    refreshState();
                    showError(rootMessage(exception));
                }
            }
        };
        worker.execute();
    }

    private void logout() {
        setBusy(true);
        SwingWorker<LicenseState, Void> worker = new SwingWorker<>() {
            @Override
            protected LicenseState doInBackground() throws Exception {
                return licenseService.logout();
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    refreshState();
                    showInfo(languageService.text("message.license_logout_success"));
                } catch (Exception exception) {
                    showError(rootMessage(exception));
                }
            }
        };
        worker.execute();
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        validateButton.setEnabled(!busy);
        logoutButton.setEnabled(!busy);
    }

    private void refreshState() {
        LicenseState state = licenseService.currentState();
        apiBaseUrlField.setText(state.apiBaseUrl());
        if (!state.customerEmail().isBlank()) {
            emailField.setText(state.customerEmail());
        }
        statusArea.setText(buildStatusText(state));
        validateButton.setEnabled(state.hasToken());
        logoutButton.setEnabled(state.hasToken());
    }

    private String buildStatusText(LicenseState state) {
        StringBuilder builder = new StringBuilder();
        builder.append(languageService.text("license.edition")).append(": ").append(state.edition().displayName()).append(System.lineSeparator());
        builder.append(languageService.text("license.status")).append(": ").append(state.status().name()).append(System.lineSeparator());
        builder.append(languageService.text("license.email")).append(": ").append(emptyDash(state.customerEmail())).append(System.lineSeparator());
        builder.append(languageService.text("license.device_id")).append(": ").append(emptyDash(state.deviceId())).append(System.lineSeparator());
        builder.append(languageService.text("license.last_checked_at")).append(": ").append(formatInstant(state.lastCheckedAt())).append(System.lineSeparator());
        builder.append(languageService.text("license.expires_at")).append(": ").append(formatInstant(state.expiresAt())).append(System.lineSeparator());
        builder.append(languageService.text("license.offline_grace_days")).append(": ").append(state.offlineGraceDays()).append(System.lineSeparator());
        if (!state.message().isBlank()) {
            builder.append(System.lineSeparator()).append(state.message());
        }
        return builder.toString();
    }

    private String formatInstant(java.time.Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    private String emptyDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String rootMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? exception.getMessage() : current.getMessage();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, languageService.text("dialog.license_title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, languageService.text("dialog.license_title"), JOptionPane.ERROR_MESSAGE);
    }
}
