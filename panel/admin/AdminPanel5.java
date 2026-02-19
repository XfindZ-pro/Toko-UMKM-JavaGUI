/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.admin;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.AutoCompleteComboBox;
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class AdminPanel5 extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable warehouseTable;
    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = null;

    /**
     * Creates new form AdminPanel5
     */
    public AdminPanel5() {
        initComponents();
        customizeUI();
        setupPagination();
        loadWarehouses(currentPage);

    }

    // Customize UI components
    private void customizeUI() {
        String[] columnNames = {"Warehouse ID", "Warehouse Name", "Location", "Branch", "Manager ID", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columnNames, 0);
        warehouseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(warehouseTable);
        jTabbedPane.addTab("Warehouses", scrollPane);

        // Setup action buttons
        btnAdd.addActionListener(this::showAddDialog);
        btnEdit.addActionListener(this::updateSelectedData);
        btnDelete.addActionListener(e -> deleteSelectedWarehouse(null));
        btnRefresh.addActionListener(e -> loadWarehouses(1));
        btnSearch.addActionListener(e -> searchWarehouses(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentSearchKeyword = null;
            loadWarehouses(1);
        });
    }

    // Setup pagination
    private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        PaginationSpinner.setModel(spinnerModel);
        PaginationSpinner.addChangeListener((ChangeEvent e) -> {
            currentPage = (Integer) PaginationSpinner.getValue();
            loadWarehouses(currentPage);
        });
    }

    // Load warehouses with pagination
    private void loadWarehouses(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        String baseQuery = "SELECT warehouse_id, warehouse_name, warehouse_location, warehouse_branch, warehouse_manager, created_at, updated_at FROM warehouse";
        String countQuery = "SELECT COUNT(*) FROM warehouse";

        if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
            String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
            baseQuery += " WHERE LOWER(warehouse_name) LIKE ?";
            countQuery += " WHERE LOWER(warehouse_name) LIKE ?";
        }
        baseQuery += " LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection()) {
            // Count total rows
            try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    pstmtCount.setString(1, "%" + currentSearchKeyword.toLowerCase() + "%");
                }
                try (ResultSet rs = pstmtCount.executeQuery()) {
                    if (rs.next()) {
                        int totalRows = rs.getInt(1);
                        totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);
                        SpinnerNumberModel model = (SpinnerNumberModel) PaginationSpinner.getModel();
                        model.setMaximum(totalPages);
                        model.setValue(page);
                        PageStatus.setText(page + "/" + totalPages);
                    }
                }
            }

            // Fetch data for the current page
            try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
                int paramIndex = 1;
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    pstmtData.setString(paramIndex++, "%" + currentSearchKeyword.toLowerCase() + "%");
                }
                pstmtData.setInt(paramIndex++, ROWS_PER_PAGE);
                pstmtData.setInt(paramIndex, offset);
                try (ResultSet rs = pstmtData.executeQuery()) {
                    tableModel.setRowCount(0); // Clear the table
                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("warehouse_id"),
                            rs.getString("warehouse_name"),
                            rs.getString("warehouse_location"),
                            rs.getString("warehouse_branch"),
                            rs.getString("warehouse_manager"), // Include manager ID
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load warehouse data.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Search warehouses
    private void searchWarehouses(String keyword) {
        currentSearchKeyword = keyword;
        currentPage = 1;
        SpinnerNumberModel model = (SpinnerNumberModel) PaginationSpinner.getModel();
        model.setValue(currentPage);
        loadWarehouses(currentPage);
    }

    // Generate unique warehouse_id
    private String generateWarehouseId() {
        String warehouseId;
        boolean isUnique = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String checkSql = "SELECT COUNT(*) FROM warehouse WHERE warehouse_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            do {
                // Generate wh + 5-digit random number
                int randomNumber = (int) (Math.random() * 90000) + 10000; // 10000 - 99999
                warehouseId = "wh" + randomNumber;
                // Check if ID already exists in the database
                pstmt.setString(1, warehouseId);
                rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    isUnique = true;
                }
            } while (!isUnique);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to check warehouse ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return warehouseId;
    }

    // Show add warehouse dialog
    private void showAddDialog(ActionEvent e) {
        String warehouseId = generateWarehouseId();
        if (warehouseId == null) {
            JOptionPane.showMessageDialog(this, "Failed to generate warehouse ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField tfName = new JTextField();
        JTextArea taLocation = new JTextArea(3, 20);
        JTextField tfBranch = new JTextField();

        AutoCompleteComboBox managerComboBox = new AutoCompleteComboBox();
        loadUserIds(managerComboBox);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Warehouse ID:"));
        panel.add(new JLabel(warehouseId)); // Display ID only
        panel.add(new JLabel("Warehouse Name:"));
        panel.add(tfName);
        panel.add(new JLabel("Location:"));
        panel.add(new JScrollPane(taLocation));
        panel.add(new JLabel("Branch:"));
        panel.add(tfBranch);
        panel.add(new JLabel("Warehouse Manager (User ID):"));
        panel.add(managerComboBox);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Warehouse", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);

        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        btnOk.addActionListener(evt -> {
            try {
                String name = tfName.getText().trim();
                String location = taLocation.getText().trim();
                String branch = tfBranch.getText().trim();
                String managerId = (String) managerComboBox.getSelectedItem();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Warehouse Name must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO warehouse (warehouse_id, warehouse_name, warehouse_location, warehouse_branch, warehouse_manager) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, warehouseId);
                    pstmt.setString(2, name);
                    pstmt.setString(3, location);
                    pstmt.setString(4, branch);
                    pstmt.setString(5, managerId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Warehouse added successfully.");
                    dialog.dispose();
                    loadWarehouses(currentPage);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to save warehouse: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(evt -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // Update selected warehouse
    private void updateSelectedData(ActionEvent e) {
        int selectedRow = warehouseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        String warehouseId = tableModel.getValueAt(selectedRow, 0).toString();
        String newName = tableModel.getValueAt(selectedRow, 1).toString();
        String newLocation = tableModel.getValueAt(selectedRow, 2).toString();
        String newBranch = tableModel.getValueAt(selectedRow, 3).toString();
        String currentManagerId = tableModel.getValueAt(selectedRow, 4).toString();

        JTextField tfName = new JTextField(newName);
        JTextArea taLocation = new JTextArea(newLocation, 3, 20);
        JTextField tfBranch = new JTextField(newBranch);
        AutoCompleteComboBox managerComboBox = new AutoCompleteComboBox();
        loadUserIds(managerComboBox);
        managerComboBox.setSelectedItem(currentManagerId);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Warehouse Name:"));
        panel.add(tfName);
        panel.add(new JLabel("Location:"));
        panel.add(new JScrollPane(taLocation));
        panel.add(new JLabel("Branch:"));
        panel.add(tfBranch);
        panel.add(new JLabel("Warehouse Manager (User ID):"));
        panel.add(managerComboBox);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Warehouse", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);

        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        btnOk.addActionListener(evt -> {
            try {
                String updatedName = tfName.getText().trim();
                String updatedLocation = taLocation.getText().trim();
                String updatedBranch = tfBranch.getText().trim();
                String updatedManager = (String) managerComboBox.getSelectedItem();

                String sql = "UPDATE warehouse SET warehouse_name = ?, warehouse_location = ?, warehouse_branch = ?, warehouse_manager = ? WHERE warehouse_id = ?";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, updatedName);
                    pstmt.setString(2, updatedLocation);
                    pstmt.setString(3, updatedBranch);
                    pstmt.setString(4, updatedManager);
                    pstmt.setString(5, warehouseId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Warehouse updated successfully.");
                    dialog.dispose();
                    loadWarehouses(currentPage);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to update warehouse: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(evt -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // Delete selected warehouse
    private void deleteSelectedWarehouse(ActionEvent e) {
        int selectedRow = warehouseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }
        String warehouseId = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this warehouse?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM warehouse WHERE warehouse_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, warehouseId);
                pstmt.executeUpdate();
                loadWarehouses(currentPage);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete warehouse.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadUserIds(AutoCompleteComboBox comboBox) {
        String sql = "SELECT id FROM users";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                items.add(rs.getString("id"));
            }
            comboBox.setItemList(items);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load user IDs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBoard = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        jTabbedPane = new javax.swing.JTabbedPane();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        PageStatus = new javax.swing.JLabel();
        PaginationSpinner = new javax.swing.JSpinner();
        searchField = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        panelBoard.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane.setViewportView(jTabbedPane);

        javax.swing.GroupLayout panelBoardLayout = new javax.swing.GroupLayout(panelBoard);
        panelBoard.setLayout(panelBoardLayout);
        panelBoardLayout.setHorizontalGroup(
            panelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 915, Short.MAX_VALUE)
        );
        panelBoardLayout.setVerticalGroup(
            panelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
        );

        btnAdd.setText("Add");

        btnEdit.setText("Edit");

        btnDelete.setText("Delete");

        btnRefresh.setText("Refresh");

        PageStatus.setText("0/0");

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(btnAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnEdit)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete)
                        .addGap(18, 18, 18)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PageStatus)
                        .addGap(61, 61, 61)
                        .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(panelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(btnSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReset)
                .addGap(213, 213, 213))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnReset))
                .addGap(18, 18, 18)
                .addComponent(panelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnEdit)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh)
                    .addComponent(PageStatus)
                    .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner PaginationSpinner;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JPanel panelBoard;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
