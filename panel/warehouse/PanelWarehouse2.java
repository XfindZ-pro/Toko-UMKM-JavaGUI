/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.warehouse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.AutoCompleteComboBox;

/**
 *
 * @author findo
 */
public class PanelWarehouse2 extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    /**
     * Creates new form PanelWarehouse2
     */
    public PanelWarehouse2() {
        initComponents();
        customizeUI();
        loadProductStock(currentPage);
    }

    private String getWarehouseIdForCurrentUser() {
        String userId = SessionManager.getCurrentUserId();
        String query = "SELECT warehouse_id FROM warehouse WHERE warehouse_manager = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("warehouse_id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void deleteSelectedProduct(JTable stockTable) {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        String productId = tableModel.getValueAt(selectedRow, 0).toString();
        String warehouseId = getWarehouseIdForCurrentUser();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM warehouse_product WHERE warehouse_id = ? AND product_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, warehouseId);
                pstmt.setString(2, productId);
                pstmt.executeUpdate();
                loadProductStock(currentPage);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete product from stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadProductStock(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        String warehouseId = getWarehouseIdForCurrentUser();
        String baseQuery = "SELECT wp.product_id, p.product_name, wp.stock FROM warehouse_product wp "
                + "JOIN product p ON wp.product_id = p.product_id "
                + "WHERE wp.warehouse_id = ?";

        String countQuery = "SELECT COUNT(*) FROM warehouse_product wp WHERE wp.warehouse_id = ?";

        baseQuery += " LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection()) {
            // Count total rows
            try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
                pstmtCount.setString(1, warehouseId);
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

            // Fetch product stock data for the current page
            try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
                pstmtData.setString(1, warehouseId);
                pstmtData.setInt(2, ROWS_PER_PAGE);
                pstmtData.setInt(3, offset);
                try (ResultSet rs = pstmtData.executeQuery()) {
                    tableModel.setRowCount(0); // Clear the table
                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("stock")
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load product stock data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void customizeUI() {
        String[] columnNames = {"Product ID", "Product Name", "Stock"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable stockTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        TabPane.addTab("Stock", scrollPane);

        // Setup action buttons
        btnEdit.addActionListener(this::btnEditActionPerformed);
        btnDelete.addActionListener(e -> deleteSelectedProduct(stockTable));
        btnRefresh.addActionListener(e -> loadProductStock(currentPage));
    }

    private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        SpinnerPagination.setModel(spinnerModel);
        SpinnerPagination.addChangeListener((ChangeEvent e) -> {
            currentPage = (Integer) SpinnerPagination.getValue();
            loadProductStock(currentPage);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        TabPane = new javax.swing.JTabbedPane();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        SpinnerPagination = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();

        jScrollPane.setViewportView(TabPane);

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 921, Short.MAX_VALUE))
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
        );

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        PageStatus.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(147, 147, 147)
                        .addComponent(jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(317, 317, 317)
                        .addComponent(btnEdit)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete)
                        .addGap(18, 18, 18)
                        .addComponent(btnRefresh)))
                .addContainerGap(156, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(PageStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(258, 258, 258))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEdit)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh))
                .addGap(46, 46, 46)
                .addComponent(jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PageStatus))
                .addContainerGap(55, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // Access the JTable directly from the selected tab
        JScrollPane scrollPane = (JScrollPane) TabPane.getSelectedComponent(); // Get selected tab
        JTable stockTable = (JTable) scrollPane.getViewport().getView(); // Get JTable from the scroll pane
        int selectedRow = stockTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        String productId = tableModel.getValueAt(selectedRow, 0).toString();
        int currentStock = (Integer) tableModel.getValueAt(selectedRow, 2);

        // Create a dialog to edit stock
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(currentStock, 0, Integer.MAX_VALUE, 1));

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Stock:"));
        panel.add(stockSpinner);

        JScrollPane scrollPaneDialog = new JScrollPane(panel);
        scrollPaneDialog.setPreferredSize(new Dimension(400, 200));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Stock", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPaneDialog, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(evt1 -> {
            try {
                int newStock = (Integer) stockSpinner.getValue();
                String sql = "UPDATE warehouse_product SET stock = ? WHERE warehouse_id = ? AND product_id = ?";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, newStock);
                    pstmt.setString(2, getWarehouseIdForCurrentUser());
                    pstmt.setString(3, productId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Stock updated successfully.");
                    dialog.dispose();
                    loadProductStock(currentPage); // Reload the stock table
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to update stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(evt1 -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRefreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JTabbedPane TabPane;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel jPanel;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables
}
