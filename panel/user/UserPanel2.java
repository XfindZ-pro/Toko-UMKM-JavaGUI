/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;
import projeksmt2.util.AutoCompleteComboBox;

/**
 *
 * @author findo
 */
public class UserPanel2 extends javax.swing.JPanel {

      private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private double totalAmount = 0;
    private boolean hasPendingOrder = false;

    /**
     * Creates new form UserPanel2
     */
    public UserPanel2() {
        initComponents();

        loadCartItems();
        setupPagination();

        btnPay.addActionListener(e -> showPaymentDialog());
    }

    
    private void checkPendingOrder() {
    String userId = SessionManager.getCurrentUserId();
    
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "SELECT COUNT(*) FROM order_transactions WHERE customer_id = ? AND order_status = 'pending'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                hasPendingOrder = rs.getInt(1) > 0;
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error checking pending orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
    
       private void showPendingOrderMessage() {
        TabPane.removeAll();
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("<html><center>You have a pending order.<br>Please complete or cancel it before making a new order.</center></html>", 
                                       SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(16f));
        
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        TabPane.add("Pending Order", messagePanel);
        
        // Hide payment-related components
        btnPay.setVisible(false);
        PageStatus.setVisible(false);
        SpinnerPagination.setVisible(false);
    }
       
     private void loadCartItems() {
    String userId = SessionManager.getCurrentUserId();
    totalAmount = 0;

    try (Connection conn = DBConnection.getConnection()) {
        // Count total items for pagination
        String countSql = "SELECT COUNT(*) FROM shopping_cart WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalItems = rs.getInt(1);
                totalPages = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
                
                // Ensure currentPage is within valid range
                currentPage = Math.min(currentPage, totalPages);
                currentPage = Math.max(currentPage, 1);
                
                PageStatus.setText(currentPage + "/" + totalPages);
                ((SpinnerNumberModel) SpinnerPagination.getModel()).setMaximum(totalPages);
            }
        }

        // Calculate offset safely
        int offset = Math.max(0, (currentPage - 1) * ITEMS_PER_PAGE);

        String sql = "SELECT p.product_name, p.product_price, sc.quantity "
                + "FROM shopping_cart sc "
                + "JOIN product p ON sc.product_id = p.product_id "
                + "WHERE sc.customer_id = ? "
                + "LIMIT ? OFFSET ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, ITEMS_PER_PAGE);
            pstmt.setInt(3, offset);

            ResultSet rs = pstmt.executeQuery();

            // Create table model
            String[] columnNames = {"Product Name", "Price", "Quantity", "Subtotal"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

            while (rs.next()) {
                String name = rs.getString("product_name");
                double price = rs.getDouble("product_price");
                int quantity = rs.getInt("quantity");
                double subtotal = price * quantity;
                totalAmount += subtotal;

                model.addRow(new Object[]{
                    name,
                    "Rp " + String.format("%,.0f", price),
                    quantity,
                    "Rp " + String.format("%,.0f", subtotal)
                });
            }

            // If cart is empty, show empty message
            if (model.getRowCount() == 0) {
                showEmptyCartMessage();
                return;
            }

            // Create table and add to tab
            JTable cartTable = new JTable(model);
            cartTable.setEnabled(false);

            JScrollPane scrollPane = new JScrollPane(cartTable);
            scrollPane.setPreferredSize(new Dimension(800, 250));

            TabPane.removeAll();
            
            // Add total label
            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            totalPanel.add(new JLabel("Total: Rp " + String.format("%,.0f", totalAmount)));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(totalPanel, BorderLayout.SOUTH);

            TabPane.add("Shopping Cart", mainPanel);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error loading cart items: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

private void showEmptyCartMessage() {
    TabPane.removeAll();
    
    JPanel emptyPanel = new JPanel(new BorderLayout());
    JLabel emptyLabel = new JLabel("<html><center>Your cart is empty</center></html>", 
                                   SwingConstants.CENTER);
    emptyLabel.setFont(emptyLabel.getFont().deriveFont(16f));
    emptyPanel.add(emptyLabel, BorderLayout.CENTER);
    TabPane.add("Empty Cart", emptyPanel);
    
    // Hide payment-related components
    btnPay.setVisible(false);
    PageStatus.setVisible(false);
    SpinnerPagination.setVisible(false);
}

   private void setupPagination() {
        SpinnerPagination.addChangeListener(e -> {
            currentPage = (Integer) SpinnerPagination.getValue();
            loadCartItems();
        });
    }


    private void showPaymentDialog() {
        if (totalAmount <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Your cart is empty",
                    "Empty Cart",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), "Payment", true);
        dialog.setLayout(new BorderLayout());
        dialog.setPreferredSize(new Dimension(400, 200));

        JPanel mainPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel totalLabel = new JLabel("Total Amount: Rp " + String.format("%,.0f", totalAmount));
        totalLabel.setFont(totalLabel.getFont().deriveFont(16f));

        // Check user balance
        String userId = SessionManager.getCurrentUserId();
        double userBalance = getUserBalance(userId);

        JLabel balanceLabel = new JLabel("Your Balance: Rp " + String.format("%,.0f", userBalance));
        balanceLabel.setFont(balanceLabel.getFont().deriveFont(14f));

        JLabel statusLabel = new JLabel();
        if (userBalance >= totalAmount) {
            statusLabel.setText("Balance sufficient");
            statusLabel.setForeground(new Color(0, 150, 0));
        } else {
            statusLabel.setText("Insufficient balance");
            statusLabel.setForeground(Color.RED);
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton payButton = new JButton("Pay Now");
        payButton.setEnabled(userBalance >= totalAmount);

        // Add components
        mainPanel.add(totalLabel);
        mainPanel.add(balanceLabel);
        mainPanel.add(statusLabel);

        buttonPanel.add(cancelButton);
        buttonPanel.add(payButton);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        cancelButton.addActionListener(e -> dialog.dispose());

        payButton.addActionListener(e -> {
            if (processPayment(userId)) {
                JOptionPane.showMessageDialog(dialog,
                        "Payment successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadCartItems(); // Refresh cart display
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private double getUserBalance(String userId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT balance FROM emoney WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

private boolean processPayment(String userId) {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // Start transaction

        // 1. First validate stock availability for all items in cart
        if (!validateStockAvailability(conn, userId)) {
            conn.rollback();
            return false;
        }

        // 2. Get outlet_id from users table
        String outletId = getOutletIdFromUser(userId);
        if (outletId == null || outletId.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No outlet selected. Please select an outlet first.", 
                "Outlet Required", 
                JOptionPane.ERROR_MESSAGE);
            conn.rollback();
            return false;
        }

        // 3. Verify outlet exists
        if (!outletExists(conn, outletId)) {
            JOptionPane.showMessageDialog(this, 
                "Selected outlet does not exist", 
                "Invalid Outlet", 
                JOptionPane.ERROR_MESSAGE);
            conn.rollback();
            return false;
        }

        // 4. Generate unique IDs
        String orderId = generateOrderId();
        String orderDetailId = generateOrderDetailId();
        
        // 5. Create order transaction with outlet_id
        String insertOrderSql = "INSERT INTO order_transactions " +
                              "(order_id, customer_id, outlet_id, order_status, payment_status, delivery_method) " +
                              "VALUES (?, ?, ?, 'pending', 'paid', 'on_point')";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {
            pstmt.setString(1, orderId);
            pstmt.setString(2, userId);
            pstmt.setString(3, outletId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected != 1) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Failed to create order transaction", 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // 6. Move items from shopping_cart to order_details with outlet_id
        String moveItemsSql = "INSERT INTO order_details " +
                             "(order_detail_id, order_id, product_id, quantity, price, outlet_id) " +
                             "SELECT ?, ?, sc.product_id, sc.quantity, " +
                             "(SELECT product_price FROM product WHERE product_id = sc.product_id), ? " +
                             "FROM shopping_cart sc WHERE sc.customer_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(moveItemsSql)) {
            pstmt.setString(1, orderDetailId);
            pstmt.setString(2, orderId);
            pstmt.setString(3, outletId);
            pstmt.setString(4, userId);
            
            int itemsMoved = pstmt.executeUpdate();
            if (itemsMoved == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Failed to move items to order details", 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // 7. Update stock in outlet_product table
        String updateStockSql = "UPDATE outlet_product op " +
                              "JOIN shopping_cart sc ON op.product_id = sc.product_id AND op.outlet_id = ? " +
                              "SET op.stock = op.stock - sc.quantity " +
                              "WHERE sc.customer_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
            pstmt.setString(1, outletId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        }

        // 8. Deduct from user balance
        String updateBalanceSql = "UPDATE emoney SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateBalanceSql)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setString(2, userId);
            pstmt.setDouble(3, totalAmount);
            
            int balanceUpdated = pstmt.executeUpdate();
            if (balanceUpdated == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Insufficient balance or user not found", 
                    "Payment Failed", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // 9. Clear shopping cart
        String clearCartSql = "DELETE FROM shopping_cart WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(clearCartSql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }

        // If all steps succeeded, commit transaction
        conn.commit();
        
        // Update UI after successful payment
        SwingUtilities.invokeLater(() -> {
            TabPane.removeAll();
            JPanel successPanel = new JPanel(new BorderLayout());
            JLabel successLabel = new JLabel("<html><center>Payment Successful!<br>Order ID: " + orderId + 
                                           "<br>Outlet: " + outletId + "</center></html>", 
                                           SwingConstants.CENTER);
            successLabel.setFont(successLabel.getFont().deriveFont(16f));
            successPanel.add(successLabel, BorderLayout.CENTER);
            TabPane.add("Payment Success", successPanel);
            
            btnPay.setVisible(false);
            PageStatus.setVisible(false);
            SpinnerPagination.setVisible(false);
            
            currentPage = 1;
            totalPages = 1;
            totalAmount = 0;
        });
        
        return true;

    } catch (SQLException ex) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this,
                "Payment failed: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        return false;
    } finally {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Helper methods
private String getOutletIdFromUser(String userId) throws SQLException {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "SELECT selected_outlet FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("selected_outlet");
            }
        }
    }
    return null;
}

private boolean outletExists(Connection conn, String outletId) throws SQLException {
    String sql = "SELECT 1 FROM outlet WHERE outlet_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, outletId);
        return pstmt.executeQuery().next();
    }
}

private boolean validateStockAvailability(Connection conn, String userId) throws SQLException {
    // Check if all items in cart have sufficient stock
    String checkStockSql = "SELECT p.product_name, op.stock, sc.quantity, op.outlet_id " +
            "FROM shopping_cart sc " +
            "JOIN outlet_product op ON sc.product_id = op.product_id AND sc.outlet_id = op.outlet_id " +
            "JOIN product p ON sc.product_id = p.product_id " +
            "WHERE sc.customer_id = ? AND op.stock < sc.quantity";
    
    try (PreparedStatement pstmt = conn.prepareStatement(checkStockSql)) {
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            // Build list of out-of-stock items
            StringBuilder errorMessage = new StringBuilder("Insufficient stock for:\n");
            do {
                errorMessage.append("- ")
                          .append(rs.getString("product_name"))
                          .append(" (Available: ")
                          .append(rs.getInt("stock"))
                          .append(", Requested: ")
                          .append(rs.getInt("quantity"))
                          .append(")\n");
            } while (rs.next());
            
            JOptionPane.showMessageDialog(this,
                    errorMessage.toString(),
                    "Insufficient Stock",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    return true;
}


  private String generateOrderId() {
    // Changed from "odt" to "ord" to save 1 character
    return generateUniqueId("ord", "order_transactions", "order_id", 12); // 3 prefix + 9 random = 12 total
}

private String generateOrderDetailId() {
    return generateUniqueId("odt", "order_details", "order_detail_id", 12); // 3 prefix + 9 random = 12 total
}

private String generateUniqueId(String prefix, String table, String column, int maxLength) {
    Random random = new Random();
    String id;
    int attempts = 0;
    final int MAX_ATTEMPTS = 10;
    int randomDigits = maxLength - prefix.length();

    try (Connection conn = DBConnection.getConnection()) {
        do {
            // Generate random number with appropriate length
            int randomNum = (int) (Math.pow(10, randomDigits-1) + random.nextInt(9 * (int) Math.pow(10, randomDigits-1)));
            id = prefix + randomNum;
            attempts++;

            String checkSql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return id;
                    }
                }
            }
        } while (attempts < MAX_ATTEMPTS);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    // Fallback with timestamp (ensuring it fits within maxLength)
    return prefix + System.currentTimeMillis() % (long) Math.pow(10, maxLength - prefix.length());
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ScrollPane = new javax.swing.JScrollPane();
        TabPane = new javax.swing.JTabbedPane();
        SpinnerPagination = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();
        btnPay = new javax.swing.JButton();

        ScrollPane.setViewportView(TabPane);

        PageStatus.setText("0/0");

        btnPay.setText("Pay");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PageStatus)
                        .addGap(18, 18, 18)
                        .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 868, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPay, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(322, 322, 322))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(btnPay)
                .addGap(18, 18, 18)
                .addComponent(ScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PageStatus))
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JTabbedPane TabPane;
    private javax.swing.JButton btnPay;
    // End of variables declaration//GEN-END:variables
}
