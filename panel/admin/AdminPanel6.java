/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.admin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class AdminPanel6 extends javax.swing.JPanel {

    private DefaultTableModel userBalanceModel;
    private DefaultTableModel topUpModel;
    private JTable userBalanceTable;
    private JTable topUpTable;

    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = null;

    /**
     * Creates new form AdminPanel6
     */
    public AdminPanel6() {
        initComponents();
        customizeUI();
        setupPagination();
        loadUserBalance(currentPage);
        loadTopUp(currentPage);
    }

    //~~
   private void customizeUI() {
        // Tabel "User Balance"
        userBalanceModel = new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Balance", "Coin", "Top-Up Status"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Username tidak bisa diedit
            }
        };

        userBalanceTable = new JTable(userBalanceModel);
        JScrollPane scrollPaneBalance = new JScrollPane(userBalanceTable);
        TabPane.addTab("User Balance", scrollPaneBalance);

        // Tabel "Top Up"
        topUpModel = new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Balance", "Top-Up Status", "Top-Up Time"});
        topUpTable = new JTable(topUpModel);
        JScrollPane scrollPaneTopUp = new JScrollPane(topUpTable);
        TabPane.addTab("Top Up", scrollPaneTopUp);

        // Listener untuk menentukan fungsi btnEdit sesuai tab
        TabPane.addChangeListener(e -> {
            int selectedIndex = TabPane.getSelectedIndex();
            if (selectedIndex == 1) {
                // Tab Top Up
                btnEdit.setText("Confirm");
            } else {
                // Tab User Balance
                btnEdit.setText("Save Changes");
            }
        });

        // Aksi tombol Edit
        btnEdit.addActionListener(e -> {
            int selectedIndex = TabPane.getSelectedIndex();
            if (selectedIndex == 0) {  // Jika tab User Balance yang aktif
                int selectedRow = userBalanceTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih salah satu baris untuk mengedit.");
                    return;
                }

                String username = (String) userBalanceModel.getValueAt(selectedRow, 0);
                int balance = (Integer) userBalanceModel.getValueAt(selectedRow, 1);
                int coin = (Integer) userBalanceModel.getValueAt(selectedRow, 2);
                String topUpStatus = (String) userBalanceModel.getValueAt(selectedRow, 3);

                // Memanggil dialog untuk mengedit data
                showEditDialog(username, balance, coin, topUpStatus);
            } else if (selectedIndex == 1) {  // Jika tab Top Up yang aktif
                showTopUpConfirmation(); // Munculkan dialog konfirmasi top-up
            }
        });

        // Aksi tombol Delete
        btnDelete.addActionListener(e -> deleteSelectedData());

        // Aksi pencarian
        btnSearch.addActionListener(e -> searchUsers(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentSearchKeyword = null;
            loadUserBalance(1);
            loadTopUp(1);
        });
    }

    private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        SpinnerPagination.setModel(spinnerModel);

        SpinnerPagination.addChangeListener(e -> {
            currentPage = (Integer) SpinnerPagination.getValue();
            int selectedIndex = TabPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadUserBalance(currentPage);
            } else {
                loadTopUp(currentPage);
            }
        });
    }

    private void loadUserBalance(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;

        String baseQuery = """
            SELECT u.username, e.balance, e.coin, e.topup_status 
            FROM users u JOIN emoney e ON u.id = e.user_id""";

        String countQuery = "SELECT COUNT(*) FROM users u JOIN emoney e ON u.id = e.user_id";

        if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
            String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
            baseQuery += " WHERE LOWER(u.username) LIKE ? OR LOWER(u.email) LIKE ?";
            countQuery += " WHERE LOWER(u.username) LIKE ? OR LOWER(u.email) LIKE ?";
        }

        baseQuery += " LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection()) {

            // Hitung total halaman
            try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
                int paramIndex = 1;
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    pstmtCount.setString(paramIndex++, "%" + currentSearchKeyword.toLowerCase() + "%");
                    pstmtCount.setString(paramIndex++, "%" + currentSearchKeyword.toLowerCase() + "%");
                }

                try (ResultSet rs = pstmtCount.executeQuery()) {
                    if (rs.next()) {
                        int totalRows = rs.getInt(1);
                        totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);
                        ((SpinnerNumberModel) SpinnerPagination.getModel()).setMaximum(totalPages);
                        ((SpinnerNumberModel) SpinnerPagination.getModel()).setValue(page);
                        StatusPage.setText(page + "/" + totalPages);
                    }
                }
            }

            // Ambil data halaman saat ini
            try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
                int paramIndex = 1;
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
                    pstmtData.setString(paramIndex++, pattern);
                    pstmtData.setString(paramIndex++, pattern);
                }
                pstmtData.setInt(paramIndex++, ROWS_PER_PAGE);
                pstmtData.setInt(paramIndex, offset);

                try (ResultSet rs = pstmtData.executeQuery()) {
                    userBalanceModel.setRowCount(0); // Kosongkan tabel

                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("username"),
                            rs.getObject("balance", Integer.class),
                            rs.getObject("coin", Integer.class),
                            rs.getObject("topup_status", String.class)
                        };
                        userBalanceModel.addRow(row);
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data pengguna.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTopUp(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;

        String baseQuery = """
            SELECT u.username, e.balance, e.topup_status, e.topup_time 
            FROM users u 
            JOIN emoney e ON u.id = e.user_id 
            WHERE e.topup_status IN ('pending', 'checking') 
            AND e.topup_time > NOW() - INTERVAL 5 MINUTE
            """;

        String countQuery = """
            SELECT COUNT(*) FROM users u 
            JOIN emoney e ON u.id = e.user_id 
            WHERE e.topup_status IN ('pending', 'checking') 
            AND e.topup_time > NOW() - INTERVAL 5 MINUTE
            """;

        baseQuery += " LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection()) {

            // Hitung total halaman
            try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery); ResultSet rs = pstmtCount.executeQuery()) {
                if (rs.next()) {
                    int totalRows = rs.getInt(1);
                    totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);
                    ((SpinnerNumberModel) SpinnerPagination.getModel()).setMaximum(totalPages);
                    ((SpinnerNumberModel) SpinnerPagination.getModel()).setValue(page);
                    StatusPage.setText(page + "/" + totalPages);
                }
            }

            // Ambil data halaman saat ini
            try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
                pstmtData.setInt(1, ROWS_PER_PAGE);
                pstmtData.setInt(2, offset);

                try (ResultSet rs = pstmtData.executeQuery()) {
                    topUpModel.setRowCount(0); // Kosongkan tabel

                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("username"),
                            rs.getObject("balance", Integer.class),
                            rs.getObject("topup_status", String.class),
                            rs.getObject("topup_time", String.class)
                        };
                        topUpModel.addRow(row);
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data top-up.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTopUpConfirmation() {
        int selectedRow = topUpTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu baris untuk dikonfirmasi.");
            return;
        }

        String username = (String) topUpModel.getValueAt(selectedRow, 0);
        int balance = (int) topUpModel.getValueAt(selectedRow, 1);
        String currentStatus = (String) topUpModel.getValueAt(selectedRow, 2);
        String topUpTime = (String) topUpModel.getValueAt(selectedRow, 3);

        // Panel utama dialog
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Username: " + username));
        panel.add(new JLabel("Balance: " + balance));
        panel.add(new JLabel("Status Saat Ini: " + currentStatus));
        panel.add(new JLabel("Top-Up Time: " + topUpTime));

        JButton btnAccept = new JButton("Accept");
        JButton btnDecline = new JButton("Decline");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnAccept);
        buttonPanel.add(btnDecline);

        // Dialog pop-up
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Konfirmasi Top-Up", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Event Accept
        btnAccept.addActionListener(evt -> {
            updateTopUpStatus(dialog, username, "completed", selectedRow);
            dialog.dispose();
        });

        // Event Decline
        btnDecline.addActionListener(evt -> {
            updateTopUpStatus(dialog, username, "failed", selectedRow);
            dialog.dispose();
        });

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setPreferredSize(new Dimension(400, 300));
        dialog.pack();
        dialog.setLocationRelativeTo(null); // Di tengah layar
        dialog.setVisible(true);
    }

private void updateTopUpStatus(Component parent, String username, String newStatus, int row) {
    String getBalanceQuery = """
        SELECT e.balance, e.topup_amount 
        FROM emoney e 
        JOIN users u ON e.user_id = u.id 
        WHERE u.username = ?
    """;

    String updateStatusQuery = """
        UPDATE emoney e 
        JOIN users u ON e.user_id = u.id 
        SET e.topup_status = ?, 
            e.topup_amount = 0,
            e.topup_time = NULL 
        WHERE u.username = ?
    """;

    try (Connection conn = DBConnection.getConnection()) {
        // Step 1: Dapatkan balance dan topup_amount
        try (PreparedStatement pstmtGet = conn.prepareStatement(getBalanceQuery)) {
            pstmtGet.setString(1, username);
            try (ResultSet rs = pstmtGet.executeQuery()) {
                if (rs.next()) {
                    int currentBalance = rs.getInt("balance");
                    int topUpAmount = rs.getInt("topup_amount");

                    // Step 2: Update status dan reset topup_amount + topup_time
                    try (PreparedStatement pstmtUpdateStatus = conn.prepareStatement(updateStatusQuery)) {
                        pstmtUpdateStatus.setString(1, newStatus); // Set status baru
                        pstmtUpdateStatus.setString(2, username);
                        pstmtUpdateStatus.executeUpdate();
                    }

                    // Step 3: Tambahkan top-up amount ke balance hanya jika status completed
                    if ("completed".equalsIgnoreCase(newStatus)) {
                        currentBalance += topUpAmount;

                        String updateBalanceQuery = """
                            UPDATE emoney e 
                            JOIN users u ON e.user_id = u.id 
                            SET e.balance = ? 
                            WHERE u.username = ?
                        """;
                        try (PreparedStatement pstmtUpdateBalance = conn.prepareStatement(updateBalanceQuery)) {
                            pstmtUpdateBalance.setInt(1, currentBalance);
                            pstmtUpdateBalance.setString(2, username);
                            pstmtUpdateBalance.executeUpdate();
                        }
                    }

                    // Update tabel UI
                    topUpModel.setValueAt(newStatus.equalsIgnoreCase("completed") ? "idle" : newStatus, row, 2);
                    topUpModel.setValueAt(null, row, 3); // topup_time jadi null

                    JOptionPane.showMessageDialog(parent, 
                        "Status top-up berhasil diubah ke: " + newStatus);
                } else {
                    JOptionPane.showMessageDialog(parent, 
                        "Data pengguna tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(parent, 
            "Gagal mengubah status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}


    private void deleteSelectedData() {
        int selectedRow = userBalanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu baris untuk dihapus.");
            return;
        }

        String username = (String) userBalanceModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus akun ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE u, e FROM users u LEFT JOIN emoney e ON u.id = e.user_id WHERE u.username = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Akun berhasil dihapus.");
                loadUserBalance(currentPage);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus akun.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchUsers(String keyword) {
        currentSearchKeyword = keyword;
        currentPage = 1;
        loadUserBalance(currentPage);
        loadTopUp(currentPage);
    }

    
      private void showEditDialog(String username, int currentBalance, int currentCoin, String currentStatus) {
    // Create a dialog window for editing
    JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User Balance", true);
    editDialog.setLayout(new GridLayout(4, 2, 10, 10));

    // Add labels and text fields for balance, coin, and status
    JLabel lblBalance = new JLabel("Balance:");
    JTextField txtBalance = new JTextField(String.valueOf(currentBalance));
    JLabel lblCoin = new JLabel("Coin:");
    JTextField txtCoin = new JTextField(String.valueOf(currentCoin));
    JLabel lblStatus = new JLabel("Top-Up Status:");
    JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"idle", "pending", "completed", "failed"});
    cmbStatus.setSelectedItem(currentStatus);

    // Add components to the dialog
    editDialog.add(lblBalance);
    editDialog.add(txtBalance);
    editDialog.add(lblCoin);
    editDialog.add(txtCoin);
    editDialog.add(lblStatus);
    editDialog.add(cmbStatus);

    // Add save and cancel buttons
    JButton btnSave = new JButton("Save");
    JButton btnCancel = new JButton("Cancel");

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.add(btnSave);
    buttonPanel.add(btnCancel);

    // Add buttons to dialog
    editDialog.add(buttonPanel);

    // Add actions to the buttons
    btnSave.addActionListener(e -> {
        try {
            int balance = Integer.parseInt(txtBalance.getText());
            int coin = Integer.parseInt(txtCoin.getText());
            String status = (String) cmbStatus.getSelectedItem();

            // Cek apakah nilai yang dimasukkan valid
            System.out.println("Username: " + username);
            System.out.println("Balance: " + balance);
            System.out.println("Coin: " + coin);
            System.out.println("Status: " + status);

            if (balance < 0 || coin < 0) {
                JOptionPane.showMessageDialog(editDialog, "Balance and Coin cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Query untuk update data
            String sql = "UPDATE emoney e JOIN users u ON e.user_id = u.id SET e.balance = ?, e.coin = ?, e.topup_status = ? WHERE u.username = ?";

            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, balance);
                pstmt.setInt(2, coin);
                pstmt.setString(3, status);
                pstmt.setString(4, username);
                int rowsAffected = pstmt.executeUpdate(); // Mengeksekusi query dan mendapatkan jumlah baris yang terpengaruh

                // Cek apakah update berhasil
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(editDialog, "Data successfully updated.");
                    loadUserBalance(currentPage);
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Data failed to update. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(editDialog, "Error updating data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(editDialog, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    btnCancel.addActionListener(e -> editDialog.dispose()); // Close dialog on cancel

    // Set dialog properties
    editDialog.setSize(300, 200);
    editDialog.setLocationRelativeTo(null);
    editDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    editDialog.setVisible(true);
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelBoard = new javax.swing.JPanel();
        ScrollPane = new javax.swing.JScrollPane();
        TabPane = new javax.swing.JTabbedPane();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        SpinnerPagination = new javax.swing.JSpinner();
        StatusPage = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();

        PanelBoard.setBackground(new java.awt.Color(255, 255, 255));

        ScrollPane.setViewportView(TabPane);

        javax.swing.GroupLayout PanelBoardLayout = new javax.swing.GroupLayout(PanelBoard);
        PanelBoard.setLayout(PanelBoardLayout);
        PanelBoardLayout.setHorizontalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        PanelBoardLayout.setVerticalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
        );

        btnEdit.setText("Edit");

        btnDelete.setText("Delete");

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        StatusPage.setText("0/0");

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnReset)))
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(192, 192, 192)
                .addComponent(btnEdit)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addGap(38, 38, 38)
                .addComponent(btnRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(StatusPage, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnReset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEdit)
                    .addComponent(btnDelete)
                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StatusPage)
                    .addComponent(btnRefresh))
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            currentSearchKeyword = keyword;
        } else {
            currentSearchKeyword = null;
        }
        currentPage = 1;
        loadUserBalance(currentPage);
    }//GEN-LAST:event_searchFieldActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
          // Update top-up status from "completed" to "idle"
    String updateStatusQuery = "UPDATE emoney e JOIN users u ON e.user_id = u.id SET e.topup_status = 'idle' WHERE e.topup_status = 'completed'";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(updateStatusQuery)) {

        int rowsUpdated = pstmt.executeUpdate();

        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Top-up status updated to idle.");
        }

        // Refresh data for User Balance and Top-Up tables
        loadUserBalance(currentPage);  // Memuat ulang data User Balance
        loadTopUp(currentPage);         // Memuat ulang data Top-Up
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Gagal memperbarui status top-up.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnRefreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelBoard;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JLabel StatusPage;
    private javax.swing.JTabbedPane TabPane;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables



}
