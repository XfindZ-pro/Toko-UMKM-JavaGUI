/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.admin;

import projeksmt2.util.AutoCompleteComboBox;
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
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class AdminPanel4 extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private DefaultTableModel partnerModel;
    private JTable outletTable;
    private JTable partnerTable;

    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = null;

    /**
     * Creates new form AdminPanel4
     */
    public AdminPanel4() {
        initComponents();
        customizeUI();
        setupPagination();
        loadOutlets(currentPage);

    }

    // Customize UI components
    private void customizeUI() {
        // Tabel "User Balance"
        String[] outletColumnNames = {"Outlet ID", "Outlet Name", "Location", "Branch", "Manager ID", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(outletColumnNames, 0);
        outletTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(outletTable);
        jTabbedPane1.addTab("Outlets", scrollPane);

        // Tabel "Partner"
        String[] partnerColumnNames = {"Outlet Name", "Outlet ID", "Warehouse ID"};
        partnerModel = new DefaultTableModel(partnerColumnNames, 0);
        partnerTable = new JTable(partnerModel);
        JScrollPane partnerScrollPane = new JScrollPane(partnerTable);
        jTabbedPane1.addTab("Partner", partnerScrollPane);

        // Setup action buttons
        btnAdd.addActionListener(this::showAddDialog);
        btnEdit.addActionListener(this::updateSelectedData);
        btnDelete.addActionListener(e -> deleteSelectedOutlet(null));
        btnRefresh.addActionListener(e -> loadOutlets(1));
        btnSearch.addActionListener(e -> searchOutlets(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentSearchKeyword = null;
            loadOutlets(1);
        });

        jTabbedPane1.addChangeListener(e -> {
            int selectedTab = jTabbedPane1.getSelectedIndex();
            if (selectedTab == 1) {  // Tab Partner
                loadPartners(currentPage);  // Load data partner
                btnEdit.setVisible(false);
            } else {
                loadOutlets(currentPage);  // Load data outlet
                btnEdit.setVisible(true);
            }
        });
    }

    // Method to handle the update for selected data in the outlet table
private void updateSelectedData(ActionEvent e) {
    int selectedRow = outletTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang akan diedit.");
        return;
    }

    // Ambil data dari baris yang dipilih
    String outletId = tableModel.getValueAt(selectedRow, 0).toString();
    String currentName = tableModel.getValueAt(selectedRow, 1).toString();
    String currentLocation = tableModel.getValueAt(selectedRow, 2).toString();
    String currentBranch = tableModel.getValueAt(selectedRow, 3).toString();
    String currentManager = tableModel.getValueAt(selectedRow, 4).toString();

    // Buat form edit dengan AutoCompleteComboBox untuk manager
    JPanel panel = new JPanel(new GridLayout(0, 1));
    JTextField nameField = new JTextField(currentName);
    JTextField locationField = new JTextField(currentLocation);
    JTextField branchField = new JTextField(currentBranch);
    
    AutoCompleteComboBox managerComboBox = new AutoCompleteComboBox();
    loadManagerIds(managerComboBox); // Method untuk load data manager
    managerComboBox.setSelectedItem(currentManager);

    panel.add(new JLabel("Nama Outlet:"));
    panel.add(nameField);
    panel.add(new JLabel("Lokasi:"));
    panel.add(locationField);
    panel.add(new JLabel("Cabang:"));
    panel.add(branchField);
    panel.add(new JLabel("Manager:"));
    panel.add(managerComboBox);

    int result = JOptionPane.showConfirmDialog(
        this, 
        panel, 
        "Edit Outlet", 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        // Update database
        updateOutletInDatabase(
            outletId,
            nameField.getText(),
            locationField.getText(),
            branchField.getText(),
            managerComboBox.getSelectedItem().toString()
        );
        
        // Refresh tabel
        loadOutlets(currentPage);
    }
}

// Method untuk load data manager ke combo box
private void loadManagerIds(AutoCompleteComboBox comboBox) {
    String sql = "SELECT id FROM users WHERE role = 'outlet_manager'"; // Sesuaikan query dengan struktur tabel Anda
    try (Connection conn = DBConnection.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql); 
         ResultSet rs = pstmt.executeQuery()) {
        
        List<String> managers = new ArrayList<>();
        while (rs.next()) {
            managers.add(rs.getString("id"));
        }
        comboBox.setItemList(managers);
        
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Gagal memuat daftar manager.", "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

// Method untuk update database
private void updateOutletInDatabase(String outletId, String name, String location, String branch, String manager) {
    String sql = "UPDATE outlet SET outlet_name = ?, outlet_location = ?, outlet_branch = ?, outlet_manager = ? WHERE outlet_id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, name);
        pstmt.setString(2, location);
        pstmt.setString(3, branch);
        pstmt.setString(4, manager);
        pstmt.setString(5, outletId);
        
        int rowsAffected = pstmt.executeUpdate();
        
        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            // Update juga data di table model
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(outletId)) {
                    tableModel.setValueAt(name, i, 1);
                    tableModel.setValueAt(location, i, 2);
                    tableModel.setValueAt(branch, i, 3);
                    tableModel.setValueAt(manager, i, 4);
                    break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

// Method to search outlets by outlet name
    private void searchOutlets(String keyword) {
        currentSearchKeyword = keyword;
        currentPage = 1;
        SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
        model.setValue(currentPage);
        loadOutlets(currentPage);  // Reload outlets with the new search keyword
    }

    // Setup pagination
    private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        SpinnerPagination.setModel(spinnerModel);
        SpinnerPagination.addChangeListener((ChangeEvent e) -> {
            currentPage = (Integer) SpinnerPagination.getValue();
            if (jTabbedPane1.getSelectedIndex() == 1) {
                loadPartners(currentPage);
            } else {
                loadOutlets(currentPage);
            }
        });
    }

    // Load outlets with pagination
    private void loadOutlets(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        String baseQuery = "SELECT outlet_id, outlet_name, outlet_location, outlet_branch, outlet_manager, created_at, updated_at FROM outlet";
        String countQuery = "SELECT COUNT(*) FROM outlet";

        if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
            String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
            baseQuery += " WHERE LOWER(outlet_name) LIKE ?";
            countQuery += " WHERE LOWER(outlet_name) LIKE ?";
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
                        SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
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
                            rs.getString("outlet_id"),
                            rs.getString("outlet_name"),
                            rs.getString("outlet_location"),
                            rs.getString("outlet_branch"),
                            rs.getString("outlet_manager"),
                            rs.getString("created_at"),
                            rs.getString("updated_at")
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load outlet data.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

  private void loadPartners(int page) {
    int offset = (page - 1) * ROWS_PER_PAGE;
    String baseQuery = """
        SELECT o.outlet_name, o.outlet_id, w.warehouse_id 
        FROM outlet_warehouse ow
        JOIN outlet o ON ow.outlet_id = o.outlet_id
        JOIN warehouse w ON ow.warehouse_id = w.warehouse_id
        LIMIT ? OFFSET ?""";

    String countQuery = "SELECT COUNT(*) FROM outlet_warehouse";

    try (Connection conn = DBConnection.getConnection()) {
        // Hitung total data
        try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery);
             ResultSet rsCount = pstmtCount.executeQuery()) {
            if (rsCount.next()) {
                int totalRows = rsCount.getInt(1);
                totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);
                updatePaginationUI(page);  // Method untuk update spinner & label
            }
        }

        // Ambil data
        try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
            pstmtData.setInt(1, ROWS_PER_PAGE);
            pstmtData.setInt(2, offset);
            ResultSet rsData = pstmtData.executeQuery();

            partnerModel.setRowCount(0);  // Bersihkan tabel
            while (rsData.next()) {
                partnerModel.addRow(new Object[]{
                    rsData.getString("outlet_name"),
                    rsData.getString("outlet_id"),
                    rsData.getString("warehouse_id")
                });
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error loading partner data:\n" + ex.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

  private void updatePaginationUI(int page) {
    SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
    model.setMaximum(totalPages);
    model.setValue(page);
    PageStatus.setText(page + "/" + totalPages);
}
  
    // Show add partner dialog
    private void showAddDialog(ActionEvent e) {
        AutoCompleteComboBox outletComboBox = new AutoCompleteComboBox();
        AutoCompleteComboBox warehouseComboBox = new AutoCompleteComboBox();
        loadOutletIds(outletComboBox);
        loadWarehouseIds(warehouseComboBox);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Select Outlet:"));
        panel.add(outletComboBox);
        panel.add(new JLabel("Select Warehouse:"));
        panel.add(warehouseComboBox);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add Partner", true);
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
                String selectedOutletId = (String) outletComboBox.getSelectedItem();
                String selectedWarehouseId = (String) warehouseComboBox.getSelectedItem();

                String sql = "INSERT INTO outlet_warehouse (outlet_id, warehouse_id) VALUES (?, ?)";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, selectedOutletId);
                    pstmt.setString(2, selectedWarehouseId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Partner added successfully.");
                    dialog.dispose();
                    loadPartners(currentPage); // Reload the partner data
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to add partner: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void loadOutletIds(AutoCompleteComboBox comboBox) {
        String sql = "SELECT outlet_id FROM outlet";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                items.add(rs.getString("outlet_id"));
            }
            comboBox.setItemList(items);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load outlet IDs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWarehouseIds(AutoCompleteComboBox comboBox) {
        String sql = "SELECT warehouse_id FROM warehouse";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            List<String> items = new ArrayList<>();
            while (rs.next()) {
                items.add(rs.getString("warehouse_id"));
            }
            comboBox.setItemList(items);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load warehouse IDs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete selected partner
    private void deleteSelectedOutlet(ActionEvent e) {
        int selectedRow = partnerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        String outletId = partnerModel.getValueAt(selectedRow, 1).toString();
        String warehouseId = partnerModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this partner?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM outlet_warehouse WHERE outlet_id = ? AND warehouse_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, outletId);
                pstmt.setString(2, warehouseId);
                pstmt.executeUpdate();
                loadPartners(currentPage);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete partner.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        SpinnerPagination = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setViewportView(jTabbedPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 889, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
        );

        btnAdd.setText("Add");

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");

        btnRefresh.setText("Refresh");

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        PageStatus.setText("0/0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReset)))
                .addContainerGap(30, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnEdit)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PageStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnReset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnEdit)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh)
                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PageStatus))
                .addContainerGap(26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
