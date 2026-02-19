/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.outlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;

/**
 *
 * @author findo
 */
public class OutletPanel3 extends javax.swing.JPanel {

    private JTable transactionTable;
    private DefaultTableModel transactionModel;

    /**
     * Creates new form OutletPanel3
     */
    public OutletPanel3() {
        initComponents();
        initializeTable();
        loadPendingTransactions();
        setupButtonListener();

    }

    private void initializeTable() {
        String[] columnNames = {"Order ID"};
        transactionModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        transactionTable = new JTable(transactionModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        TabPane.removeAll();
        TabPane.add("Transactions", scrollPane);
    }

    private void loadPendingTransactions() {
        String outletId = getOutletIdForManager();
        if (outletId == null) {
            JOptionPane.showMessageDialog(this,
                    "Outlet not found for this manager",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Loading transactions for outlet: " + outletId); // Debug log

        try (Connection conn = DBConnection.getConnection()) {
            // First verify the outlet exists
            if (!outletExists(conn, outletId)) {
                JOptionPane.showMessageDialog(this,
                        "Outlet ID " + outletId + " not found in database",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT order_id FROM order_transactions "
                    + "WHERE outlet_id = ? AND order_status = 'pending' AND payment_status = 'paid'";

            System.out.println("Executing query: " + sql); // Debug log

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, outletId);
                ResultSet rs = pstmt.executeQuery();

                // Clear existing data
                transactionModel.setRowCount(0);

                int count = 0;
                while (rs.next()) {
                    String orderId = rs.getString("order_id");
                    transactionModel.addRow(new Object[]{orderId});
                    count++;
                    System.out.println("Found order: " + orderId); // Debug log
                }

                if (count == 0) {
                    System.out.println("No pending orders found for outlet " + outletId); // Debug log
                }
            }
        } catch (SQLException ex) {
            System.err.println("Database error: " + ex.getMessage()); // Debug log
            JOptionPane.showMessageDialog(this,
                    "Error loading transactions: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean outletExists(Connection conn, String outletId) throws SQLException {
        String sql = "SELECT 1 FROM outlet WHERE outlet_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, outletId);
            return pstmt.executeQuery().next();
        }
    }

    private String getOutletIdForManager() {
        String userId = SessionManager.getCurrentUserId();
        System.out.println("Getting outlet for manager: " + userId); // Debug log

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT outlet_id FROM outlet WHERE outlet_manager = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String outletId = rs.getString("outlet_id");
                    System.out.println("Found outlet: " + outletId); // Debug log
                    return outletId;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error getting outlet: " + ex.getMessage()); // Debug log
            ex.printStackTrace();
        }

        System.out.println("No outlet found for manager: " + userId); // Debug log
        return null;
    }

    private void setupButtonListener() {
        btnAccept.addActionListener(e -> {
            int selectedRow = transactionTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a transaction first",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String orderId = (String) transactionModel.getValueAt(selectedRow, 0);
            processOrderAcceptance(orderId);
        });
    }

private void processOrderAcceptance(String orderId) {
    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);

        try {
            String outletId = getOutletIdForManager();
            if (outletId == null) {
                JOptionPane.showMessageDialog(this,
                    "Outlet not found for this manager",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Generate unique conveyor number for this outlet
            int conveyorNumber;
            try {
                conveyorNumber = generateConveyorNumber(conn, outletId);
            } catch (SQLException e) {
                // Rollback transaction if conveyor is full
                conn.rollback();
                
                // Show user-friendly message
                JOptionPane.showMessageDialog(this,
                    "All conveyor numbers (1-10) are currently in use.\n" +
                    "Please wait until a conveyor becomes available or\n" +
                    "contact staff to resolve completed orders.",
                    "Conveyor Full",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 2. Generate conveyor ID
            String conveyorId = generateConveyorId();
            
            // 3. Insert new conveyor record
            String insertConveyorSql = "INSERT INTO conveyor "
                    + "(conveyor_id, conveyor_number, order_id, status, outlet_id) "
                    + "VALUES (?, ?, ?, 'in_progress', ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertConveyorSql)) {
                pstmt.setString(1, conveyorId);
                pstmt.setInt(2, conveyorNumber);
                pstmt.setString(3, orderId);
                pstmt.setString(4, outletId);
                int rowsInserted = pstmt.executeUpdate();
                
                if (rowsInserted != 1) {
                    throw new SQLException("Failed to insert conveyor record");
                }
            }

            // 4. Update order status
            String updateOrderSql = "UPDATE order_transactions "
                    + "SET order_status = 'completed' "
                    + "WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderSql)) {
                pstmt.setString(1, orderId);
                int rowsUpdated = pstmt.executeUpdate();
                
                if (rowsUpdated != 1) {
                    throw new SQLException("Failed to update order status");
                }
            }

            // Commit transaction if everything succeeded
            conn.commit();
            
            // Show success message with conveyor number
            JOptionPane.showMessageDialog(this,
                "Order completed successfully!\n" +
                "Conveyor Number: " + conveyorNumber + "\n" +
                "Please inform the customer to pick up their order.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            // Refresh the pending transactions list
            SwingUtilities.invokeLater(this::loadPendingTransactions);

        } catch (SQLException ex) {
            // Rollback transaction if any error occurs
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            
            // Show error message
            JOptionPane.showMessageDialog(this,
                "Error completing order: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Database connection error: " + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

private int generateConveyorNumber(Connection conn, String outletId) throws SQLException {
    // 1. Cari semua conveyor_number yang sedang digunakan oleh outlet ini
    String usedNumbersSql = "SELECT conveyor_number FROM conveyor WHERE outlet_id = ? AND status = 'in_progress'";
    Set<Integer> usedNumbers = new HashSet<>();
    
    try (PreparedStatement pstmt = conn.prepareStatement(usedNumbersSql)) {
        pstmt.setString(1, outletId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            usedNumbers.add(rs.getInt("conveyor_number"));
        }
    }
    
    // 2. Cari nomor yang tersedia antara 1-10
    for (int i = 1; i <= 10; i++) {
        if (!usedNumbers.contains(i)) {
            return i; // Kembalikan nomor pertama yang tersedia
        }
    }
    
    // 3. Jika semua nomor 1-10 sudah digunakan
    throw new SQLException("All conveyor numbers (1-10) are currently in use for this outlet");
}

private String generateConveyorId() {
    Random random = new Random();
    final int MAX_ATTEMPTS = 10;
    int attempts = 0;

    try (Connection conn = DBConnection.getConnection()) {
        while (attempts < MAX_ATTEMPTS) {
            int randomNum = 100 + random.nextInt(900);
            String conveyorId = "cnv" + randomNum;

            String checkSql = "SELECT 1 FROM conveyor WHERE conveyor_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, conveyorId);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    return conveyorId;
                }
            }
            attempts++;
        }
        return "cnv" + (100 + random.nextInt(900)) + System.currentTimeMillis() % 10;
    } catch (SQLException ex) {
        return "cnv" + (100 + random.nextInt(900));
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

        ScrollPane = new javax.swing.JScrollPane();
        TabPane = new javax.swing.JTabbedPane();
        btnAccept = new javax.swing.JButton();

        ScrollPane.setViewportView(TabPane);

        btnAccept.setText("Accept");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(ScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 855, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(387, 387, 387)
                        .addComponent(btnAccept, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(97, 97, 97)
                .addComponent(ScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAccept)
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JTabbedPane TabPane;
    private javax.swing.JButton btnAccept;
    // End of variables declaration//GEN-END:variables
}
