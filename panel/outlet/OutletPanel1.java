/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.outlet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;
import projeksmt2.util.AutoCompleteComboBox;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author findo
 */
public class OutletPanel1 extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable warehouseTable;

    /**
     * Creates new form OutletPanel1
     */
    public OutletPanel1() {
        initComponents();
        loadWarehouseData();
    }

   private void loadWarehouseData() {
        String userId = SessionManager.getCurrentUserId();
        
        // 1. Dapatkan outlet_id berdasarkan user (outlet_manager)
        String outletId = getOutletIdForCurrentUser();
        if (outletId == null) {
            JOptionPane.showMessageDialog(this, 
                "No outlet assigned to this manager", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Dapatkan warehouse_id dari outlet_warehouse
        String query = "SELECT warehouse_id FROM outlet_warehouse WHERE outlet_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, outletId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String warehouseId = rs.getString("warehouse_id");
                System.out.println("Loading data for warehouse ID: " + warehouseId);
                loadWarehouseProducts(warehouseId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No warehouse associated with this outlet", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getOutletIdForCurrentUser() {
        String userId = SessionManager.getCurrentUserId();
        String query = "SELECT outlet_id FROM outlet WHERE outlet_manager = ?";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("outlet_id") : null;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private void refreshWarehouseData() {
    String outletId = getOutletIdForCurrentUser();
    if (outletId != null) {
        String warehouseId = getWarehouseIdForOutlet(outletId);
        if (warehouseId != null) {
            loadWarehouseProducts(warehouseId);
        }
    }
}
    
         private void loadWarehouseProducts(String warehouseId) {
        String query = """
            SELECT 
                w.warehouse_name, 
                w.warehouse_branch, 
                COUNT(wp.product_id) AS product_count,
                SUM(wp.stock) AS total_stock
            FROM warehouse w
            LEFT JOIN warehouse_product wp ON w.warehouse_id = wp.warehouse_id
            WHERE w.warehouse_id = ?
            GROUP BY w.warehouse_name, w.warehouse_branch""";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, warehouseId);
            ResultSet rs = pstmt.executeQuery();

            String[] columns = {"Warehouse Name", "Branch", "Product Count", "Total Stock"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            warehouseTable = new JTable(tableModel);
            warehouseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("warehouse_name"),
                    rs.getString("warehouse_branch"),
                    rs.getInt("product_count"),
                    rs.getInt("total_stock")
                });
            }

            // Update UI
            TabPanel.removeAll();
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(new JScrollPane(warehouseTable), BorderLayout.CENTER);
            TabPanel.addTab("Warehouse Stock", tablePanel);
            TabPanel.revalidate();
            TabPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading warehouse data:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
     }




    private String getProductIdByName(String productName) {
        String query = "SELECT product_id FROM product WHERE product_name = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("product_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private int getProductStockInWarehouse(String productId, String warehouseName) {
        String query = "SELECT stock FROM warehouse_product WHERE product_id = ? AND warehouse_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productId);
            pstmt.setString(2, warehouseName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("stock");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

   

private String generateUniqueOutletProductId() {
    String outletProductId = "otl000"; // Nilai default
    boolean isUnique = false;
    int attempt = 0;
    final int MAX_ATTEMPTS = 10;

    while (!isUnique && attempt < MAX_ATTEMPTS) {
        attempt++;
        
        // Generate 3 digit random number
        int randomNumber = (int) (Math.random() * 900) + 100; // 100-999
        outletProductId = "otl" + randomNumber; // Format: "otlXXX" (6 karakter)

        // Pastikan panjang ID sesuai dengan kolom database (7 karakter)
        if (outletProductId.length() > 7) {
            outletProductId = outletProductId.substring(0, 7);
        }

        // Cek keunikan ID
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM outlet_product WHERE id = ?")) {
            pstmt.setString(1, outletProductId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                isUnique = true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            continue; // Coba lagi jika ada error
        }
    }

    // Fallback mechanism jika tidak dapat menghasilkan ID unik
    if (!isUnique) {
        long timestamp = System.currentTimeMillis() % 10000; // 4 digit terakhir
        outletProductId = "otl" + timestamp;
        if (outletProductId.length() > 7) {
            outletProductId = outletProductId.substring(0, 7);
        }
    }

    return outletProductId;
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnRequest = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        PanelList = new javax.swing.JPanel();
        ScrollPane = new javax.swing.JScrollPane();
        TabPanel = new javax.swing.JTabbedPane();

        btnRequest.setText("Request");
        btnRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRequestActionPerformed(evt);
            }
        });

        btnSend.setText("Send");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        PanelList.setBackground(new java.awt.Color(255, 255, 255));

        ScrollPane.setViewportView(TabPanel);

        javax.swing.GroupLayout PanelListLayout = new javax.swing.GroupLayout(PanelList);
        PanelList.setLayout(PanelListLayout);
        PanelListLayout.setHorizontalGroup(
            PanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 896, Short.MAX_VALUE)
        );
        PanelListLayout.setVerticalGroup(
            PanelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(btnRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(PanelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(PanelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRequest)
                    .addComponent(btnSend))
                .addGap(23, 23, 23))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRequestActionPerformed
        int selectedRow = warehouseTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a warehouse first!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String warehouseId = getWarehouseIdFromSelectedRow(selectedRow);
    if (warehouseId == null) {
        JOptionPane.showMessageDialog(this, "Invalid warehouse selection", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Buat dialog request
    showProductRequestDialog(warehouseId);
    }//GEN-LAST:event_btnRequestActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
          int selectedRow = warehouseTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a warehouse first!", 
            "Warning", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    String warehouseId = getWarehouseIdFromSelectedRow(selectedRow);
    if (warehouseId == null) {
        JOptionPane.showMessageDialog(this, 
            "Invalid warehouse selection", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Buat dialog untuk mengirim stok kembali ke warehouse
    showReturnProductDialog(warehouseId);
    }//GEN-LAST:event_btnSendActionPerformed

    private void showReturnProductDialog(String warehouseId) {
    String outletId = getOutletIdForCurrentUser();
    if (outletId == null) {
        JOptionPane.showMessageDialog(this, 
            "No outlet found for current user", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Buat komponen dialog
    AutoCompleteComboBox productComboBox = new AutoCompleteComboBox();
    JTextField quantityField = new JTextField(10);
    JLabel stockLabel = new JLabel("Available in Outlet: 0");
    
    // Load produk yang tersedia di outlet
    loadOutletProducts(outletId, productComboBox, stockLabel);
    
    // Tambahkan listener untuk update stock saat produk dipilih
    productComboBox.addActionListener(e -> {
        String productName = (String) productComboBox.getSelectedItem();
        if (productName != null) {
            updateOutletStockLabel(productName, outletId, stockLabel);
        }
    });
    
    // Panel input
    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    inputPanel.add(new JLabel("Product:"));
    inputPanel.add(productComboBox);
    inputPanel.add(new JLabel("Quantity:"));
    inputPanel.add(quantityField);
    inputPanel.add(new JLabel("Available Stock:"));
    inputPanel.add(stockLabel);
    
    // Dialog
    JDialog dialog = new JDialog();
    dialog.setTitle("Return Product to Warehouse");
    dialog.setModal(true);
    
    // Button panel
    JButton btnReturn = new JButton("Return");
    JButton btnCancel = new JButton("Cancel");
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(btnReturn);
    buttonPanel.add(btnCancel);
    
    // Action listeners
    btnReturn.addActionListener(e -> {
        processProductReturn(
            (String) productComboBox.getSelectedItem(),
            quantityField.getText(),
            outletId,
            warehouseId,
            dialog
        );
    });
    
    btnCancel.addActionListener(e -> dialog.dispose());
    
    // Layout dialog
    dialog.setLayout(new BorderLayout());
    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}
    
private void loadAvailableProducts(String warehouseId, AutoCompleteComboBox comboBox, JLabel stockLabel) {
    String query = """
        SELECT p.product_name, wp.stock 
        FROM warehouse_product wp
        JOIN product p ON wp.product_id = p.product_id
        WHERE wp.warehouse_id = ? AND wp.stock > 0""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, warehouseId);
        ResultSet rs = pstmt.executeQuery();
        
        List<String> products = new ArrayList<>();
        while (rs.next()) {
            products.add(rs.getString("product_name"));
        }
        
        comboBox.setItemList(products);
        
        // Hanya update label jika ada produk
        if (!products.isEmpty()) {
            updateStockLabel(products.get(0), warehouseId, stockLabel);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

private void loadOutletProducts(String outletId, AutoCompleteComboBox comboBox, JLabel stockLabel) {
    String query = """
        SELECT p.product_name, op.stock 
        FROM outlet_product op
        JOIN product p ON op.product_id = p.product_id
        WHERE op.outlet_id = ? AND op.stock > 0""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, outletId);
        ResultSet rs = pstmt.executeQuery();
        
        List<String> products = new ArrayList<>();
        while (rs.next()) {
            products.add(rs.getString("product_name"));
        }
        
        comboBox.setItemList(products);
        
        // Hanya update label jika ada produk
        if (!products.isEmpty()) {
            updateOutletStockLabel(products.get(0), outletId, stockLabel);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

private void updateOutletStockLabel(String productName, String outletId, JLabel stockLabel) {
    String query = """
        SELECT op.stock 
        FROM outlet_product op
        JOIN product p ON op.product_id = p.product_id
        WHERE p.product_name = ? AND op.outlet_id = ?""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, productName);
        pstmt.setString(2, outletId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            stockLabel.setText("Available in Outlet: " + rs.getInt("stock"));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

private void processProductReturn(String productName, String quantityText, 
                                String outletId, String warehouseId, JDialog dialog) {
    // Validasi input
    if (productName == null || productName.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, 
            "Please select a product", 
            "Warning", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int quantity;
    try {
        quantity = Integer.parseInt(quantityText);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(dialog, 
            "Invalid quantity format", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Dapatkan product_id
    String productId = getProductIdByName(productName);
    if (productId == null) {
        JOptionPane.showMessageDialog(dialog, 
            "Product not found", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Cek stok tersedia di outlet
    int availableStock = getProductStockInOutlet(productId, outletId);
    if (quantity > availableStock) {
        JOptionPane.showMessageDialog(dialog, 
            "Requested quantity exceeds available stock in outlet", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Proses pengembalian
    if (returnProductToWarehouse(productId, quantity, outletId, warehouseId)) {
        JOptionPane.showMessageDialog(dialog, 
            "Product returned to warehouse successfully", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh data warehouse setelah berhasil return
        refreshWarehouseData();
        
        dialog.dispose();
    } else {
        JOptionPane.showMessageDialog(dialog, 
            "Failed to return product", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

private int getProductStockInOutlet(String productId, String outletId) {
    String query = "SELECT stock FROM outlet_product WHERE product_id = ? AND outlet_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, productId);
        pstmt.setString(2, outletId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("stock") : 0;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return 0;
    }
}

private boolean returnProductToWarehouse(String productId, int quantity, 
                                       String outletId, String warehouseId) {
    // Kurangi stok di outlet
    String updateOutletQuery = "UPDATE outlet_product SET stock = stock - ? " +
                             "WHERE product_id = ? AND outlet_id = ? AND stock >= ?";
    
    // Tambah stok di warehouse
    String updateWarehouseQuery = "UPDATE warehouse_product SET stock = stock + ? " +
                                "WHERE product_id = ? AND warehouse_id = ?";
    
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // Mulai transaksi
        
        // 1. Kurangi stok di outlet
        try (PreparedStatement pstmt = conn.prepareStatement(updateOutletQuery)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.setString(3, outletId);
            pstmt.setInt(4, quantity);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }
        }
        
        // 2. Tambah stok di warehouse
        try (PreparedStatement pstmt = conn.prepareStatement(updateWarehouseQuery)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.setString(3, warehouseId);
            
            pstmt.executeUpdate();
        }
        
        conn.commit();
        return true;
    } catch (SQLException ex) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, 
            "Database error: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        return false;
    } finally {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
    
    private String getWarehouseIdFromSelectedRow(int selectedRow) {
    String warehouseName = (String) tableModel.getValueAt(selectedRow, 0);
    String warehouseBranch = (String) tableModel.getValueAt(selectedRow, 1);
    
    String query = "SELECT warehouse_id FROM warehouse WHERE warehouse_name = ? AND warehouse_branch = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, warehouseName);
        pstmt.setString(2, warehouseBranch);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getString("warehouse_id") : null;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return null;
    }
}

private void showProductRequestDialog(String warehouseId) {
    // Buat komponen dialog
    AutoCompleteComboBox productComboBox = new AutoCompleteComboBox();
    JTextField quantityField = new JTextField(10);
    JLabel stockLabel = new JLabel("Available: 0");
    
    // Load produk yang tersedia di warehouse
    loadAvailableProducts(warehouseId, productComboBox, stockLabel);
    
    // Tambahkan listener untuk update stock saat produk dipilih
    productComboBox.addActionListener(e -> {
        String productName = (String) productComboBox.getSelectedItem();
        if (productName != null) {
            updateStockLabel(productName, warehouseId, stockLabel);
        }
    });
    
    // Panel input
    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    inputPanel.add(new JLabel("Product:"));
    inputPanel.add(productComboBox);
    inputPanel.add(new JLabel("Quantity:"));
    inputPanel.add(quantityField);
    inputPanel.add(new JLabel("Available Stock:"));
    inputPanel.add(stockLabel);
    
    // Dialog
    JDialog dialog = new JDialog();
    dialog.setTitle("Request Product from Warehouse");
    dialog.setModal(true);
    
    // Button panel
    JButton btnDelivery = new JButton("Delivery");
    JButton btnCancel = new JButton("Cancel");
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(btnDelivery);
    buttonPanel.add(btnCancel);
    
    // Action listeners
    btnDelivery.addActionListener(e -> {
        processProductDelivery(
            (String) productComboBox.getSelectedItem(),
            quantityField.getText(),
            warehouseId,
            dialog
        );
    });
    
    btnCancel.addActionListener(e -> dialog.dispose());
    
    // Layout dialog
    dialog.setLayout(new BorderLayout());
    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}



private void updateStockLabel(String productName, String warehouseId, JLabel stockLabel) {
    String query = """
        SELECT wp.stock 
        FROM warehouse_product wp
        JOIN product p ON wp.product_id = p.product_id
        WHERE p.product_name = ? AND wp.warehouse_id = ?""";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, productName);
        pstmt.setString(2, warehouseId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            stockLabel.setText("Available: " + rs.getInt("stock"));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

private void processProductDelivery(String productName, String quantityText, String warehouseId, JDialog dialog) {
    // Validasi input
    if (productName == null || productName.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please select a product", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int quantity;
    try {
        quantity = Integer.parseInt(quantityText);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(dialog, "Invalid quantity format", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Dapatkan product_id
    String productId = getProductIdByName(productName);
    if (productId == null) {
        JOptionPane.showMessageDialog(dialog, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Cek stok tersedia
    int availableStock = getProductStockInWarehouse(productId, warehouseId);
    if (quantity > availableStock) {
        JOptionPane.showMessageDialog(dialog, 
            "Requested quantity exceeds available stock", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Proses pengiriman
    if (deliverProductToOutlet(productId, quantity)) {
        JOptionPane.showMessageDialog(dialog, 
            "Product delivered successfully", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh data warehouse setelah berhasil delivery
        refreshWarehouseData();
        
        dialog.dispose();
    } else {
        JOptionPane.showMessageDialog(dialog, 
            "Failed to deliver product", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

private boolean deliverProductToOutlet(String productId, int quantity) {
    String outletId = getOutletIdForCurrentUser();
    if (outletId == null) {
        JOptionPane.showMessageDialog(null, 
            "No outlet found for current user", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return false;
    }

    String warehouseId = getWarehouseIdForOutlet(outletId);
    if (warehouseId == null) {
        JOptionPane.showMessageDialog(null, 
            "No warehouse associated with this outlet", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return false;
    }

    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // Mulai transaksi

        // 1. Cek apakah produk sudah ada di outlet
        String checkQuery = "SELECT id FROM outlet_product WHERE outlet_id = ? AND product_id = ?";
        String outletProductId = null;
        
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, outletId);
            pstmt.setString(2, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                outletProductId = rs.getString("id");
            }
        }

        // 2. Jika belum ada, generate ID baru
        if (outletProductId == null) {
            outletProductId = generateUniqueOutletProductId();
        }

        // 3. Update atau insert ke outlet_product
        String upsertQuery = "INSERT INTO outlet_product (id, outlet_id, product_id, stock) " +
                           "VALUES (?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE stock = stock + VALUES(stock)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(upsertQuery)) {
            pstmt.setString(1, outletProductId);
            pstmt.setString(2, outletId);
            pstmt.setString(3, productId);
            pstmt.setInt(4, quantity);
            pstmt.executeUpdate();
        }

        // 4. Kurangi stok di warehouse
        String updateWarehouseQuery = "UPDATE warehouse_product SET stock = stock - ? " +
                                    "WHERE product_id = ? AND warehouse_id = ? AND stock >= ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(updateWarehouseQuery)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.setString(3, warehouseId);
            pstmt.setInt(4, quantity);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, 
                    "Not enough stock in warehouse", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        conn.commit();
        return true;
    } catch (SQLException ex) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, 
            "Database error: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        return false;
    } finally {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

// Helper method untuk update stock di warehouse
private void updateWarehouseStock(String productId, String warehouseId, int quantity) {
    if (warehouseId == null) return;
    
    String query = "UPDATE warehouse_product SET stock = stock - ? " +
                   "WHERE product_id = ? AND warehouse_id = ? AND stock >= ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, quantity);
        pstmt.setString(2, productId);
        pstmt.setString(3, warehouseId);
        pstmt.setInt(4, quantity);
        pstmt.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

// Helper method untuk mendapatkan warehouse_id berdasarkan outlet_id
private String getWarehouseIdForOutlet(String outletId) {
    String query = "SELECT warehouse_id FROM outlet_warehouse WHERE outlet_id = ? LIMIT 1";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, outletId);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getString("warehouse_id") : null;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return null;
    }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelList;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JTabbedPane TabPanel;
    private javax.swing.JButton btnRequest;
    private javax.swing.JButton btnSend;
    // End of variables declaration//GEN-END:variables
}
