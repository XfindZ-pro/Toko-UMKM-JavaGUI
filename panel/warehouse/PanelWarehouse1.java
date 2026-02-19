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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import projeksmt2.util.AutoCompleteComboBox;

/**
 *
 * @author findo
 */
public class PanelWarehouse1 extends javax.swing.JPanel {
   private List<String> originalItems;
    private boolean isAdjusting = false;
    /**
     * Creates new form PanelWarehouse1
     */
    public PanelWarehouse1() {
        initComponents();
    }

         private int checkUserBalance(String userId) {
        // Query to get the user's balance from emoney table based on user_id
        String query = "SELECT balance FROM emoney WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("balance");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0; // Return 0 if no balance found
    }

    private int getProductCostByName(String productName) {
        // Query to get the product cost from product table by product name
        String query = "SELECT product_cost FROM product WHERE product_name = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_cost");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private String getWarehouseIdForCurrentUser() {
        // Query to get the warehouse_id for the current user
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

    private String getProductIdByName(String productName) {
        // Query to get the product_id from product table by product name
        String query = "SELECT product_id FROM product WHERE product_name = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("product_id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

private void addProductToWarehouse(String warehouseProductId, String warehouseId, String productId, int quantity) {
    if (warehouseProductId.length() > 10) { // Sesuaikan dengan panjang kolom
        JOptionPane.showMessageDialog(null, "ID terlalu panjang!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    String query = "INSERT INTO warehouse_product (id, warehouse_id, product_id, stock) VALUES (?, ?, ?, ?)";
    try (Connection conn = DBConnection.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, warehouseProductId);
        pstmt.setString(2, warehouseId);
        pstmt.setString(3, productId);
        pstmt.setInt(4, quantity);
        pstmt.executeUpdate();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

    private void updateUserBalance(String userId, int newBalance) {
        String query = "UPDATE emoney SET balance = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, newBalance);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

private String generateWarehouseProductId() {
    // Sesuaikan dengan panjang kolom 'id' di database (misal: varchar(8))
    int randomNumber = (int) (Math.random() * 900) + 100; // Generate 3 digit angka
    return "wrp" + randomNumber; // Total 6 karakter ("wrp" + 3 digit)
}

 private void loadProductNames(AutoCompleteComboBox productComboBox) {
    List<String> productNames = new ArrayList<>();
    String query = "SELECT product_name FROM product";
    try (Connection conn = DBConnection.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(query); 
         ResultSet rs = pstmt.executeQuery()) {
        
        while (rs.next()) {
            productNames.add(rs.getString("product_name"));
        }
        productComboBox.setItemList(productNames);
        
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, 
            "Error loading products: " + ex.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
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

        jPanel = new javax.swing.JPanel();
        btnBuy = new javax.swing.JButton();
        label1 = new javax.swing.JLabel();
        FilterProduct = new javax.swing.JComboBox<>();
        jScrollPane = new javax.swing.JScrollPane();
        TabInfo = new javax.swing.JTabbedPane();

        jPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1403, Short.MAX_VALUE)
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );

        btnBuy.setText("Buy");
        btnBuy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyActionPerformed(evt);
            }
        });

        label1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        label1.setText("Click Buy to restock");

        FilterProduct.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        FilterProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterProductActionPerformed(evt);
            }
        });

        jScrollPane.setViewportView(TabInfo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(299, 299, 299)
                        .addComponent(btnBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(309, 309, 309)
                        .addComponent(FilterProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 934, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(193, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(FilterProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(56, 56, 56)
                .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(190, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void FilterProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterProductActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FilterProductActionPerformed

    private void btnBuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuyActionPerformed
   // Create a dialog to input product name and quantity
    JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
    panel.add(new JLabel("Product Name:"));
    
    // Create AutoCompleteComboBox with product names
    AutoCompleteComboBox productComboBox = new AutoCompleteComboBox();
    loadProductNames(productComboBox);
    
    // Ensure the text field is empty initially
    JTextField textField = (JTextField) productComboBox.getEditor().getEditorComponent();
    textField.setText("");
    
    JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    
    panel.add(productComboBox);
    panel.add(new JLabel("Quantity:"));
    panel.add(quantitySpinner);

    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(new Dimension(400, 200));

    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Restock Product", true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JButton btnBuy = new JButton("Buy");
    JButton btnCancel = new JButton("Cancel");
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(btnBuy);
    buttonPanel.add(btnCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(scrollPane, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    btnBuy.addActionListener(e -> {
        try {
            String productName = (String) productComboBox.getSelectedItem();
            if (productName == null || productName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a product", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verify product exists
            String productId = getProductIdByName(productName);
            if (productId == null) {
                JOptionPane.showMessageDialog(dialog, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity = (Integer) quantitySpinner.getValue();

            // Check if the user has enough balance
            String userId = SessionManager.getCurrentUserId();
            int userBalance = checkUserBalance(userId);
            int productCost = getProductCostByName(productName);

            if (userBalance >= productCost * quantity) {
                // Proceed with the purchase
                String warehouseId = getWarehouseIdForCurrentUser();

                // Check if product already exists in warehouse_product
                String checkProductQuery = "SELECT stock FROM warehouse_product WHERE warehouse_id = ? AND product_id = ?";
                try (Connection conn = DBConnection.getConnection(); 
                     PreparedStatement pstmt = conn.prepareStatement(checkProductQuery)) {
                    pstmt.setString(1, warehouseId);
                    pstmt.setString(2, productId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            // Product exists, update stock
                            int currentStock = rs.getInt("stock");
                            int newStock = currentStock + quantity;

                            String updateStockQuery = "UPDATE warehouse_product SET stock = ? WHERE warehouse_id = ? AND product_id = ?";
                            try (PreparedStatement updatePstmt = conn.prepareStatement(updateStockQuery)) {
                                updatePstmt.setInt(1, newStock);
                                updatePstmt.setString(2, warehouseId);
                                updatePstmt.setString(3, productId);
                                updatePstmt.executeUpdate();
                            }
                        } else {
                            // Product does not exist, insert new record
                            String warehouseProductId = generateWarehouseProductId();
                            addProductToWarehouse(warehouseProductId, warehouseId, productId, quantity);
                        }
                    }
                }

                // Deduct balance
                updateUserBalance(userId, userBalance - (productCost * quantity));

                JOptionPane.showMessageDialog(dialog, 
                    "Purchase successful!\n" +
                    "Product: " + productName + "\n" +
                    "Quantity: " + quantity + "\n" +
                    "Total Cost: " + (productCost * quantity),
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Insufficient balance!\n" +
                    "Your balance: " + userBalance + "\n" +
                    "Required: " + (productCost * quantity),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }

            dialog.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, 
                "An error occurred: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    });

    btnCancel.addActionListener(evt1 -> dialog.dispose());

    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    }//GEN-LAST:event_btnBuyActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> FilterProduct;
    private javax.swing.JTabbedPane TabInfo;
    private javax.swing.JButton btnBuy;
    private javax.swing.JPanel jPanel;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JLabel label1;
    // End of variables declaration//GEN-END:variables
}
