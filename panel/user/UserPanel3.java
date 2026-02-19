/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.user;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;

/**
 *
 * @author findo
 */
public class UserPanel3 extends javax.swing.JPanel {

    private String orderId;
    private String orderDate; // ← Tambahkan ini di bagian atas class
    private List<OrderItem> orderItems = new ArrayList<>();
    private double totalPrice = 0.0;

    // Class to hold order item data
    private class OrderItem {

        String productName;
        int quantity;
        double price;
        double subtotal;

        public OrderItem(String productName, int quantity, double price) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = price * quantity;
        }
    }

    /**
     * Creates new form UserPanel3
     */
    public UserPanel3() {
        initComponents();
        loadOrderData();
        setupButtonListener();
    }

    private void loadOrderData() {
        String customerId = SessionManager.getCurrentUserId();

        try (Connection conn = DBConnection.getConnection()) {
            // Periksa apakah tabel yang dibutuhkan ada
            if (!tableExists(conn, "order_transactions")
                    || !tableExists(conn, "order_details")
                    || !tableExists(conn, "product")) {
                JOptionPane.showMessageDialog(this,
                        "Database tables tidak lengkap",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String orderSql = "SELECT order_id, order_status, order_date FROM order_transactions "
                    + "WHERE customer_id = ? "
                    + "ORDER BY order_date DESC LIMIT 1";

            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setString(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    orderId = rs.getString("order_id");
                    orderDate = rs.getString("order_date"); // ← Ambil tanggal order

                    String orderStatus = rs.getString("order_status");

                    if ("completed".equals(orderStatus)) {
                        // Jika order completed, tampilkan conveyor number
                        String conveyorSql = "SELECT conveyor_number FROM conveyor WHERE order_id = ?";
                        try (PreparedStatement conveyorStmt = conn.prepareStatement(conveyorSql)) {
                            conveyorStmt.setString(1, orderId);
                            ResultSet conveyorRs = conveyorStmt.executeQuery();
                            if (conveyorRs.next()) {
                                int conveyorNum = conveyorRs.getInt("conveyor_number");
                                conveyorNumber.setText("Pesanan Anda siap di conveyor nomor " + conveyorNum);
                            } else {
                                conveyorNumber.setText("Pesanan Anda sudah selesai");
                            }
                        }

                        // Ambil detail produk
                        loadOrderDetails(conn);
                    } else if ("pending".equals(orderStatus)) {
                        conveyorNumber.setText("Menunggu Konfirmasi Staff");
                        btnDone.setEnabled(false);
                    }
                } else {
                    conveyorNumber.setText("Tidak ada pesanan ditemukan");
                    btnDone.setEnabled(false);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadOrderDetails(Connection conn) throws SQLException {
        String detailsSql = "SELECT p.product_name, od.quantity, p.product_price "
                + "FROM order_details od "
                + "JOIN product p ON od.product_id = p.product_id "
                + "WHERE od.order_id = ?";

        try (PreparedStatement detailsStmt = conn.prepareStatement(detailsSql)) {
            detailsStmt.setString(1, orderId);
            ResultSet detailsRs = detailsStmt.executeQuery();

            orderItems.clear();
            totalPrice = 0;

            while (detailsRs.next()) {
                String name = detailsRs.getString("product_name");
                int quantity = detailsRs.getInt("quantity");
                int price = detailsRs.getInt("product_price");

                OrderItem item = new OrderItem(name, quantity, price);
                orderItems.add(item);
                totalPrice += item.subtotal;
            }
        }
    }

// Method untuk cek keberadaan tabel
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private void setupButtonListener() {
        btnDone.addActionListener(e -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Order Pickup Confirmation");
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("<html><center>Make sure you already picked up your product<br><br>Do you want to download the receipt?</center></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnYes = new JButton("Done");
        btnYes.addActionListener(e -> {
            generateTextReceipt();
            dialog.dispose();
        });

        JButton btnNo = new JButton("Not yet");
        btnNo.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnYes);
        buttonPanel.add(btnNo);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

private void generateTextReceipt() {
    try {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMaximumFractionDigits(0); // Remove decimal places for cleaner look
        
        // Get all necessary order info before processing payment
        OrderInfo orderInfo = getOrderInfoBeforeProcessing();
        
        // Create receipt content
        StringBuilder receiptContent = new StringBuilder();
        receiptContent.append("=============================================\n");
        receiptContent.append("               ORDER RECEIPT                 \n");
        receiptContent.append("=============================================\n\n");
        receiptContent.append("Order ID   : ").append(orderInfo.orderId).append("\n");
        receiptContent.append("Order Date : ").append(orderInfo.orderDate).append("\n");
        receiptContent.append("Outlet     : ").append(orderInfo.outletName).append("\n");
        receiptContent.append("Customer   : ").append(SessionManager.getCurrentUser()).append("\n\n");
        
        // Header with borders
        receiptContent.append("+------------------------------------------+\n");
        receiptContent.append(String.format("| %-20s | %4s | %10s | %10s |\n", 
            "Product Name", "Qty", "Price", "Subtotal"));
        receiptContent.append("+----------------------+------+------------+------------+\n");
        
        // Product rows
        for (OrderItem item : orderItems) {
            receiptContent.append(String.format("| %-20s | %4d | %10s | %10s |\n",
                item.productName,
                item.quantity,
                currencyFormat.format(item.price),
                currencyFormat.format(item.subtotal)));
        }
        
        // Footer
        receiptContent.append("+----------------------+------+------------+------------+\n");
        receiptContent.append(String.format("| %38s %10s |\n", "TOTAL:", currencyFormat.format(totalPrice)));
        receiptContent.append("+------------------------------------------+\n");
        receiptContent.append("|           THANK YOU FOR SHOPPING!        |\n");
        receiptContent.append("=============================================\n");

        // Save to file
        String downloadsPath = System.getProperty("user.home") + "/Downloads/Receipt_" + orderId + ".txt";
        try (FileWriter writer = new FileWriter(downloadsPath)) {
            writer.write(receiptContent.toString());
        }

        // Process payment to outlet manager (this will delete the order)
        processOutletManagerPayment();

        // Open the file
        Desktop.getDesktop().open(new File(downloadsPath));

        JOptionPane.showMessageDialog(this,
            "Receipt downloaded to: " + downloadsPath,
            "Receipt Generated",
            JOptionPane.INFORMATION_MESSAGE);

    } catch (IOException | SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error generating receipt: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

// Helper class to store order info
private class OrderInfo {
    String orderId;
    String orderDate;
    String outletName;
    
    public OrderInfo(String orderId, String orderDate, String outletName) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.outletName = outletName;
    }
}

// Get all order info before processing payment
private OrderInfo getOrderInfoBeforeProcessing() throws SQLException {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "SELECT ot.order_id, ot.order_date, o.outlet_name " +
                     "FROM order_transactions ot " +
                     "JOIN outlet o ON ot.outlet_id = o.outlet_id " +
                     "WHERE ot.order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new OrderInfo(
                    rs.getString("order_id"),
                    rs.getString("order_date"),
                    rs.getString("outlet_name")
                );
            }
        }
    }
    throw new SQLException("Order information not found for ID: " + orderId);
}


private void processOutletManagerPayment() throws SQLException {
    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);
        
        try {
            // 1. Get outlet manager from order
            String outletManager = getOutletManagerForOrder(conn, orderId);
            
            if (outletManager == null) {
                throw new SQLException("Outlet manager not found for this order");
            }
            
            // 2. Update emoney balance for outlet manager
            String updateEmoneySql = "UPDATE emoney SET balance = balance + ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateEmoneySql)) {
                pstmt.setDouble(1, totalPrice);
                pstmt.setString(2, outletManager);
                int updated = pstmt.executeUpdate();
                
                if (updated == 0) {
                    // If no emoney record exists, create one
                    String insertEmoneySql = "INSERT INTO emoney (user_id, balance) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertEmoneySql)) {
                        insertStmt.setString(1, outletManager);
                        insertStmt.setDouble(2, totalPrice);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            // 3. Delete conveyor record first to avoid foreign key constraint violation
            String deleteConveyorSql = "DELETE FROM conveyor WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteConveyorSql)) {
                pstmt.setString(1, orderId);
                int rowsDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + rowsDeleted + " conveyor records for order " + orderId);
            }
            
            // 4. Now delete the order
            String deleteOrderSql = "DELETE FROM order_transactions WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOrderSql)) {
                pstmt.setString(1, orderId);
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted != 1) {
                    throw new SQLException("Failed to delete order transaction");
                }
                System.out.println("Deleted order transaction for order " + orderId);
            }
            
            conn.commit();
            
            // 5. Update UI after successful transaction
            SwingUtilities.invokeLater(() -> {
                conveyorNumber.setText("No transaction yet");
                btnDone.setEnabled(false);
            });
            
        } catch (SQLException ex) {
            conn.rollback();
            JOptionPane.showMessageDialog(this,
                "Error processing payment: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
    }
}

private String getOutletManagerForOrder(Connection conn, String orderId) throws SQLException {
    String sql = "SELECT o.outlet_manager " +
                 "FROM outlet o " +
                 "JOIN order_transactions ot ON o.outlet_id = ot.outlet_id " +
                 "WHERE ot.order_id = ?";
    
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, orderId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("outlet_manager");
        }
    }
    return null;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        conveyorNumber = new javax.swing.JLabel();
        btnDone = new javax.swing.JButton();

        conveyorNumber.setBackground(new java.awt.Color(255, 255, 255));
        conveyorNumber.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        btnDone.setText("Done");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDone)
                .addGap(234, 234, 234))
            .addGroup(layout.createSequentialGroup()
                .addGap(147, 147, 147)
                .addComponent(conveyorNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(364, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(183, Short.MAX_VALUE)
                .addComponent(conveyorNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(75, 75, 75)
                .addComponent(btnDone)
                .addGap(116, 116, 116))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDone;
    private javax.swing.JLabel conveyorNumber;
    // End of variables declaration//GEN-END:variables
}
