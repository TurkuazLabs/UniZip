/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/ArchiveListPanel.java
# 📌 Amac: Arsiv icerigini ikon izgara veya detay tablo gorunumunde gostermek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Liste secimi, F2 rename, sag tik context menu ve global arama yol gosterimini yonetir

Bagimli Oldugu Katman: View
*/
package com.unizip.desktop.views;

import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.ThemeService;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ArchiveListPanel extends JPanel {
    public enum ViewMode {
        GRID,
        DETAILS
    }

    private static final int GRID_CELL_WIDTH = 118;
    private static final int GRID_CELL_HEIGHT = 94;
    private static final int GRID_ICON_SIZE = 42;
    private static final int DETAILS_ICON_SIZE = 18;
    private static final String CARD_GRID = "grid";
    private static final String CARD_DETAILS = "details";

    private final ThemeService themeService;
    private final LanguageService languageService;
    private final DefaultListModel<ArchiveEntryModel> listModel;
    private final ArchiveDetailsTableModel tableModel;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JList<ArchiveEntryModel> gridList;
    private final JTable detailsTable;
    private List<ArchiveEntryModel> entries;
    private ViewMode viewMode;
    private boolean syncingSelection;
    private Consumer<ArchiveEntryModel> selectionListener;
    private Consumer<ArchiveEntryModel> openListener;
    private Runnable renameShortcutHandler;
    private Runnable extractSelectedHandler;
    private Runnable deleteHandler;
    private Runnable addHandler;
    private Runnable copyPathHandler;
    private boolean detailsSelectFullRow;
    private boolean showFullEntryPath;

    public ArchiveListPanel(ThemeService themeService, LanguageService languageService) {
        super(new BorderLayout(0, 0));
        this.themeService = themeService;
        this.languageService = languageService;
        this.listModel = new DefaultListModel<>();
        this.tableModel = new ArchiveDetailsTableModel();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.gridList = new JList<>(listModel);
        this.detailsTable = new JTable(tableModel);
        this.entries = new ArrayList<>();
        this.viewMode = ViewMode.DETAILS;
        this.selectionListener = entry -> { };
        this.openListener = entry -> { };
        this.renameShortcutHandler = () -> { };
        this.extractSelectedHandler = () -> { };
        this.deleteHandler = () -> { };
        this.addHandler = () -> { };
        this.copyPathHandler = () -> { };
        this.detailsSelectFullRow = true;
        this.showFullEntryPath = false;
        build();
    }

    public void setEntries(List<ArchiveEntryModel> nextEntries) {
        setEntries(nextEntries, selectedEntryName());
    }

    public void setEntries(List<ArchiveEntryModel> nextEntries, String preferredEntryName) {
        String fallbackEntryName = preferredEntryName == null || preferredEntryName.isBlank()
                ? selectedEntryName()
                : preferredEntryName;

        syncingSelection = true;
        entries = new ArrayList<>(nextEntries);
        listModel.clear();
        for (ArchiveEntryModel entry : entries) {
            listModel.addElement(entry);
        }
        tableModel.fireTableDataChanged();
        gridList.clearSelection();
        detailsTable.clearSelection();
        detailsTable.repaint();
        syncingSelection = false;

        int preferredIndex = indexOfEntry(fallbackEntryName);
        if (preferredIndex >= 0) {
            setSelectedIndex(preferredIndex);
            notifySelectedIndex(preferredIndex);
        } else if (!entries.isEmpty()) {
            setSelectedIndex(0);
            notifySelectedIndex(0);
        } else {
            clearSelection();
        }
    }

    public void setSelectionListener(Consumer<ArchiveEntryModel> listener) {
        selectionListener = listener == null ? entry -> { } : listener;
    }

    public void setOpenListener(Consumer<ArchiveEntryModel> listener) {
        openListener = listener == null ? entry -> { } : listener;
    }


    public void setDetailsSelectFullRow(boolean selectFullRow) {
        detailsSelectFullRow = selectFullRow;
        applyDetailsSelectionMode();
        detailsTable.repaint();
    }

    public void setShowFullEntryPath(boolean showFullEntryPath) {
        this.showFullEntryPath = showFullEntryPath;
        tableModel.fireTableDataChanged();
        gridList.repaint();
    }

    public void setViewMode(ViewMode nextViewMode) {
        if (nextViewMode == null || nextViewMode == viewMode) {
            return;
        }
        int selectedIndex = selectedIndex();
        viewMode = nextViewMode;
        cardLayout.show(cardPanel, viewMode == ViewMode.GRID ? CARD_GRID : CARD_DETAILS);
        setSelectedIndex(selectedIndex);
        revalidate();
        repaint();
    }

    public ViewMode viewMode() {
        return viewMode;
    }

    public ArchiveEntryModel selectedEntry() {
        int index = selectedIndex();
        return entryAt(index);
    }

    public String selectedEntryName() {
        ArchiveEntryModel entry = selectedEntry();
        return entry == null ? null : entry.name();
    }

    public boolean selectEntryByName(String entryName) {
        int index = indexOfEntry(entryName);
        if (index < 0) {
            return false;
        }
        setSelectedIndex(index);
        selectionListener.accept(entryAt(index));
        return true;
    }

    public void installDragExportHandler(TransferHandler transferHandler) {
        setTransferHandler(transferHandler);
        gridList.setTransferHandler(transferHandler);
        gridList.setDragEnabled(true);
        detailsTable.setTransferHandler(transferHandler);
        detailsTable.setDragEnabled(true);
    }

    public void installRenameShortcut(Runnable handler) {
        renameShortcutHandler = handler == null ? () -> { } : handler;
        bindRenameShortcut(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        bindRenameShortcut(gridList, JComponent.WHEN_FOCUSED);
        bindRenameShortcut(gridList, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        bindRenameShortcut(detailsTable, JComponent.WHEN_FOCUSED);
        bindRenameShortcut(detailsTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void installContextMenu(
            Runnable addHandler,
            Runnable extractSelectedHandler,
            Runnable renameHandler,
            Runnable deleteHandler,
            Runnable copyPathHandler
    ) {
        this.addHandler = addHandler == null ? () -> { } : addHandler;
        this.extractSelectedHandler = extractSelectedHandler == null ? () -> { } : extractSelectedHandler;
        this.renameShortcutHandler = renameHandler == null ? renameShortcutHandler : renameHandler;
        this.deleteHandler = deleteHandler == null ? () -> { } : deleteHandler;
        this.copyPathHandler = copyPathHandler == null ? () -> { } : copyPathHandler;
        installPopupMouseHandler(gridList);
        installPopupMouseHandler(detailsTable);
        installPopupMouseHandler(this);
    }

    private void bindRenameShortcut(JComponent component, int condition) {
        if (component == null) {
            return;
        }
        InputMap inputMap = component.getInputMap(condition);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "unizip.rename.selected.entry");
        actionMap.put("unizip.rename.selected.entry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                renameShortcutHandler.run();
            }
        });
    }

    private void installPopupMouseHandler(Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                maybeShowContextMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                maybeShowContextMenu(event);
            }
        });
    }

    private void maybeShowContextMenu(MouseEvent event) {
        if (event == null || !event.isPopupTrigger()) {
            return;
        }
        selectEntryAtPopupPoint(event);
        JPopupMenu menu = buildContextMenu();
        menu.show(event.getComponent(), event.getX(), event.getY());
    }

    private void selectEntryAtPopupPoint(MouseEvent event) {
        if (event.getComponent() == detailsTable) {
            int row = detailsTable.rowAtPoint(event.getPoint());
            if (row >= 0 && row < entries.size()) {
                setSelectedIndex(row);
                notifySelectedIndex(row);
            }
            return;
        }
        if (event.getComponent() == gridList) {
            int index = gridList.locationToIndex(event.getPoint());
            if (index >= 0 && index < entries.size()) {
                setSelectedIndex(index);
                notifySelectedIndex(index);
            }
        }
    }

    private JPopupMenu buildContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        ArchiveEntryModel selectedEntry = selectedEntry();
        boolean hasEntry = selectedEntry != null && !selectedEntry.name().equals("..");
        JMenuItem openItem = contextMenuItem("context.open", () -> {
            ArchiveEntryModel entry = selectedEntry();
            if (entry != null) {
                openListener.accept(entry);
            }
        });
        openItem.setEnabled(hasEntry);
        menu.add(openItem);
        JMenuItem extractItem = contextMenuItem("context.extract_selected", extractSelectedHandler);
        extractItem.setEnabled(hasEntry);
        menu.add(extractItem);
        menu.addSeparator();
        menu.add(contextMenuItem("context.add", addHandler));
        JMenuItem renameItem = contextMenuItem("context.rename", renameShortcutHandler);
        renameItem.setEnabled(hasEntry);
        menu.add(renameItem);
        JMenuItem deleteItem = contextMenuItem("context.delete", deleteHandler);
        deleteItem.setEnabled(hasEntry);
        menu.add(deleteItem);
        menu.addSeparator();
        JMenuItem copyPathItem = contextMenuItem("context.copy_path", copyPathHandler);
        copyPathItem.setEnabled(hasEntry);
        menu.add(copyPathItem);
        return menu;
    }

    private JMenuItem contextMenuItem(String key, Runnable action) {
        JMenuItem item = new JMenuItem(languageService.text(key));
        item.setFont(themeService.font("menu"));
        item.addActionListener(event -> action.run());
        return item;
    }

    public int entryCount() {
        return entries.size();
    }

    public long totalSize() {
        return entries.stream()
                .filter(entry -> !entry.directory())
                .mapToLong(entry -> Math.max(entry.size(), 0L))
                .sum();
    }

    public int directoryCount() {
        return (int) entries.stream().filter(ArchiveEntryModel::directory).count();
    }

    public int fileCount() {
        return entries.size() - directoryCount();
    }

    private void build() {
        setBackground(themeService.color("grid.background"));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, themeService.color("panel.divider")));
        cardPanel.setBackground(themeService.color("grid.background"));
        buildGridList();
        buildDetailsTable();
        cardPanel.add(buildGridScrollPane(), CARD_GRID);
        cardPanel.add(buildDetailsScrollPane(), CARD_DETAILS);
        add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, CARD_DETAILS);
    }

    private void buildGridList() {
        gridList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        gridList.setVisibleRowCount(-1);
        gridList.setFixedCellWidth(GRID_CELL_WIDTH);
        gridList.setFixedCellHeight(GRID_CELL_HEIGHT);
        gridList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gridList.setBackground(themeService.color("grid.background"));
        gridList.setForeground(themeService.color("text.primary"));
        gridList.setFont(themeService.font("grid"));
        gridList.setCellRenderer(new ArchiveGridCellRenderer());
        gridList.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("md"), themeService.spacing("lg"), themeService.spacing("md"), themeService.spacing("lg")));
        gridList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && viewMode == ViewMode.GRID && !syncingSelection) {
                int index = gridList.getSelectedIndex();
                syncTableSelection(index);
                selectionListener.accept(entryAt(index));
            }
        });
        gridList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && gridList.getSelectedValue() != null) {
                    openListener.accept(gridList.getSelectedValue());
                }
            }
        });
    }

    private JScrollPane buildGridScrollPane() {
        JScrollPane scrollPane = new JScrollPane(gridList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(themeService.color("grid.background"));
        return scrollPane;
    }

    private void applyDetailsSelectionMode() {
        detailsTable.setRowSelectionAllowed(true);
        detailsTable.setColumnSelectionAllowed(false);
        detailsTable.setCellSelectionEnabled(false);
    }

    private void buildDetailsTable() {
        detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applyDetailsSelectionMode();
        detailsTable.setSelectionBackground(themeService.color("table.selection.background"));
        detailsTable.setSelectionForeground(themeService.color("table.selection.foreground"));
        detailsTable.setRowHeight(28);
        detailsTable.setShowGrid(false);
        detailsTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        detailsTable.setBackground(themeService.color("table.background"));
        detailsTable.setForeground(themeService.color("table.foreground"));
        detailsTable.setFont(themeService.font("default"));
        detailsTable.setDefaultRenderer(Object.class, new ArchiveDetailsCellRenderer());
        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(320);
        detailsTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        detailsTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JTableHeader header = detailsTable.getTableHeader();
        header.setBackground(themeService.color("table.header.background"));
        header.setForeground(themeService.color("table.header.foreground"));
        header.setFont(themeService.font("default"));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, themeService.color("panel.divider")));

        detailsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && viewMode == ViewMode.DETAILS && !syncingSelection) {
                int index = detailsTable.getSelectedRow();
                syncGridSelection(index);
                detailsTable.repaint();
                selectionListener.accept(entryAt(index));
            }
        });
        detailsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && detailsTable.getSelectedRow() >= 0) {
                    openListener.accept(entryAt(detailsTable.getSelectedRow()));
                }
            }
        });
    }

    private JScrollPane buildDetailsScrollPane() {
        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(themeService.color("table.background"));
        return scrollPane;
    }

    private int selectedIndex() {
        return viewMode == ViewMode.GRID ? gridList.getSelectedIndex() : detailsTable.getSelectedRow();
    }

    private void setSelectedIndex(int index) {
        if (index < 0 || index >= entries.size()) {
            clearSelection();
            return;
        }
        syncingSelection = true;
        gridList.setSelectedIndex(index);
        gridList.ensureIndexIsVisible(index);
        detailsTable.getSelectionModel().setSelectionInterval(index, index);
        detailsTable.scrollRectToVisible(detailsTable.getCellRect(index, 0, true));
        detailsTable.repaint();
        syncingSelection = false;
    }

    private void clearSelection() {
        syncingSelection = true;
        gridList.clearSelection();
        detailsTable.clearSelection();
        detailsTable.repaint();
        syncingSelection = false;
        selectionListener.accept(null);
    }

    private void notifySelectedIndex(int index) {
        selectionListener.accept(entryAt(index));
    }

    private void syncTableSelection(int index) {
        if (index < 0 || index >= entries.size()) {
            return;
        }
        syncingSelection = true;
        detailsTable.getSelectionModel().setSelectionInterval(index, index);
        detailsTable.repaint();
        syncingSelection = false;
    }

    private void syncGridSelection(int index) {
        if (index < 0 || index >= entries.size()) {
            return;
        }
        syncingSelection = true;
        gridList.setSelectedIndex(index);
        gridList.ensureIndexIsVisible(index);
        syncingSelection = false;
    }

    private int indexOfEntry(String entryName) {
        if (entryName == null || entryName.isBlank()) {
            return -1;
        }
        for (int index = 0; index < entries.size(); index++) {
            ArchiveEntryModel entry = entries.get(index);
            if (entry != null && entryName.equals(entry.name())) {
                return index;
            }
        }
        return -1;
    }

    private ArchiveEntryModel entryAt(int index) {
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index);
    }

    private String formatSize(long bytes) {
        if (bytes < 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.0f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }

    private String entryType(ArchiveEntryModel entry) {
        if (entry == null) {
            return "";
        }
        if (entry.name().equals("..")) {
            return languageService.text("label.parent_folder");
        }
        if (entry.directory()) {
            return languageService.text("entry.directory");
        }
        String extension = extension(entry.name()).toUpperCase();
        return extension.isBlank() ? languageService.text("entry.file") : extension + " " + languageService.text("entry.file");
    }

    private String displayName(String path) {
        if (!showFullEntryPath || path == null || path.equals("..")) {
            return baseName(path);
        }
        String normalized = path.replace('\\', '/');
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String baseName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.equals("..")) {
            return path;
        }
        String normalized = path.replace('\\', '/');
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }

    private String extension(String name) {
        if (name == null) {
            return "";
        }
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "";
        }
        return name.substring(index + 1).toLowerCase();
    }

    private final class ArchiveDetailsTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> languageService.text("column.name");
                case 1 -> languageService.text("column.size");
                case 2 -> languageService.text("column.type");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ArchiveEntryModel entry = entryAt(rowIndex);
            if (entry == null) {
                return "";
            }
            return switch (columnIndex) {
                case 0 -> displayName(entry.name());
                case 1 -> entry.directory() ? "-" : formatSize(entry.size());
                case 2 -> entryType(entry);
                default -> "";
            };
        }
    }

    private final class ArchiveDetailsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean hasFocus,
                int row,
                int column
        ) {
            boolean rowSelected = table.getSelectionModel().isSelectedIndex(row);
            boolean paintSelected = rowSelected;
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, paintSelected, hasFocus, row, column);
            label.setFont(themeService.font("default"));
            label.setBorder(BorderFactory.createEmptyBorder(0, themeService.spacing("md"), 0, themeService.spacing("md")));
            label.setOpaque(true);
            label.setForeground(paintSelected ? themeService.color("table.selection.foreground") : themeService.color("table.foreground"));
            label.setBackground(paintSelected ? themeService.color("table.selection.background") : themeService.color("table.background"));
            if (column == 0) {
                label.setIcon(new ArchiveEntryIcon(entryAt(row), themeService, DETAILS_ICON_SIZE));
                label.setIconTextGap(themeService.spacing("md"));
            } else {
                label.setIcon(null);
            }
            return label;
        }
    }

    private final class ArchiveGridCellRenderer implements ListCellRenderer<ArchiveEntryModel> {
        private final JPanel panel;
        private final JLabel iconLabel;
        private final JLabel nameLabel;
        private final DefaultListCellRenderer fallbackRenderer;

        private ArchiveGridCellRenderer() {
            this.panel = new JPanel(new BorderLayout(0, themeService.spacing("sm")));
            this.iconLabel = new JLabel("", SwingConstants.CENTER);
            this.nameLabel = new JLabel("", SwingConstants.CENTER);
            this.fallbackRenderer = new DefaultListCellRenderer();
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm")));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setVerticalAlignment(SwingConstants.TOP);
            nameLabel.setFont(themeService.font("grid"));
            panel.add(iconLabel, BorderLayout.CENTER);
            panel.add(nameLabel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ArchiveEntryModel> list,
                ArchiveEntryModel value,
                int index,
                boolean selected,
                boolean cellHasFocus
        ) {
            if (value == null) {
                return fallbackRenderer.getListCellRendererComponent(list, "", index, selected, cellHasFocus);
            }
            panel.setBackground(selected ? themeService.color("grid.selection.background") : themeService.color("grid.background"));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? themeService.color("grid.selection.border") : themeService.color("grid.background")),
                    BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"))
            ));
            iconLabel.setIcon(new ArchiveEntryIcon(value, themeService, GRID_ICON_SIZE));
            nameLabel.setText(toHtml(multilineName(displayName(value.name()))));
            nameLabel.setForeground(themeService.color("text.primary"));
            return panel;
        }

        private String toHtml(String text) {
            return "<html><div style='text-align:center;'>" + text + "</div></html>";
        }

        private String multilineName(String name) {
            String safeName = name == null || name.isBlank() ? languageService.text("label.parent_folder") : name;
            if (safeName.length() <= 14) {
                return safeName;
            }
            return safeName.substring(0, 12) + "...";
        }
    }

    private static final class ArchiveEntryIcon implements Icon {
        private final ArchiveEntryModel entry;
        private final ThemeService themeService;
        private final int size;

        private ArchiveEntryIcon(ArchiveEntryModel entry, ThemeService themeService, int size) {
            this.entry = entry;
            this.themeService = themeService;
            this.size = size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = size / 42.0;
            g.translate(x, y);
            g.scale(scale, scale);
            if (entry == null) {
                paintDocument(g, 0, 0, "");
            } else if (entry.name().equals("..")) {
                paintParentFolder(g, 0, 0);
            } else if (entry.directory()) {
                paintFolder(g, 0, 0);
            } else if (isImage(entry.name())) {
                paintImage(g, 0, 0);
            } else {
                paintDocument(g, 0, 0, extension(entry.name()));
            }
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        private void paintParentFolder(Graphics2D g, int x, int y) {
            paintFolder(g, x, y);
            g.setColor(new Color(90, 90, 90));
            g.drawString("..", x + 15, y + 28);
        }

        private void paintFolder(Graphics2D g, int x, int y) {
            g.setColor(new Color(255, 212, 78));
            g.fillRoundRect(x + 4, y + 11, 34, 25, 3, 3);
            g.setColor(new Color(255, 226, 111));
            g.fillRoundRect(x + 4, y + 8, 18, 10, 3, 3);
            g.setColor(new Color(224, 168, 40));
            g.drawRoundRect(x + 4, y + 11, 34, 25, 3, 3);
        }

        private void paintImage(Graphics2D g, int x, int y) {
            g.setColor(new Color(232, 238, 246));
            g.fillRect(x + 6, y + 6, 30, 30);
            g.setColor(new Color(156, 172, 194));
            g.drawRect(x + 6, y + 6, 30, 30);
            g.setColor(new Color(84, 135, 190));
            g.fillRect(x + 9, y + 22, 24, 11);
            g.setColor(new Color(106, 166, 86));
            g.fillOval(x + 22, y + 11, 7, 7);
        }

        private void paintDocument(Graphics2D g, int x, int y, String extension) {
            Color fill = switch (extension) {
                case "pdf" -> new Color(229, 57, 53);
                case "xlsx", "xls", "csv" -> new Color(33, 163, 102);
                case "doc", "docx" -> new Color(43, 124, 222);
                default -> new Color(230, 235, 243);
            };
            g.setColor(fill);
            g.fillRect(x + 10, y + 5, 24, 32);
            g.setColor(new Color(135, 145, 160));
            g.drawRect(x + 10, y + 5, 24, 32);
            g.setColor(Color.WHITE);
            FontMetrics metrics = g.getFontMetrics();
            String text = extension == null || extension.isBlank() ? "FILE" : extension.toUpperCase();
            if (text.length() > 4) {
                text = text.substring(0, 4);
            }
            int textWidth = metrics.stringWidth(text);
            g.drawString(text, x + 22 - textWidth / 2, y + 25);
        }

        private boolean isImage(String name) {
            String extension = extension(name);
            return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("gif") || extension.equals("webp");
        }

        private String extension(String name) {
            if (name == null) {
                return "";
            }
            int index = name.lastIndexOf('.');
            if (index < 0 || index == name.length() - 1) {
                return "";
            }
            return name.substring(index + 1).toLowerCase();
        }
    }
}
