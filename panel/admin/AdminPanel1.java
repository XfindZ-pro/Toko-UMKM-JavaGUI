/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.admin;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.DBConnection;
/**
 *
 * @author findo
 */
public class AdminPanel1 extends javax.swing.JPanel {

    private JTable userTable; // 👈 Ditambahkan di sini
     private DefaultTableModel tableModel;
    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = null;
    /**
     * Creates new form AdminPanel1
     */
    public AdminPanel1() {
        initComponents();
         customizeUI();
        setupPagination();
        loadUsers(currentPage);
    }

    //UI Custom
private void customizeUI() {
    String[] columnNames = {"ID", "Username", "Email", "Role", "Terminated Reason"};
    tableModel = new DefaultTableModel(columnNames, 0);

    userTable = new JTable(tableModel); // ✅ Sudah terhubung ke variabel kelas
    JScrollPane scrollPane = new JScrollPane(userTable);

    jTabbedPane.addTab("Daftar Pengguna", scrollPane);

    refresh.addActionListener(e -> updateSelectedUser());
    
    btnDelete.addActionListener(e -> deleteSelectedUser());
    
    btnSearch.addActionListener(e -> {
    String keyword = searchField.getText().trim();
    searchUsers(keyword);
});

btnReset.addActionListener(e -> {
    searchField.setText("");
    currentSearchKeyword = null;
    currentPage = 1;
    SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
    model.setValue(currentPage);
    loadUsers(currentPage);
});
}
    //Setup pagination
     private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        SpinnerPagination.setModel(spinnerModel);

        SpinnerPagination.addChangeListener((ChangeEvent e) -> {
            currentPage = (Integer) SpinnerPagination.getValue();
            loadUsers(currentPage);
        });
    }
        //Load users
private void loadUsers(int page) {
    String baseQuery = "SELECT id, username, email, role, terminated_reason FROM users";
    String countQuery = "SELECT COUNT(*) FROM users";

    if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
        String searchPattern = "%" + currentSearchKeyword.toLowerCase() + "%";
        baseQuery += " WHERE LOWER(username) LIKE ? OR LOWER(email) LIKE ?";
        countQuery += " WHERE LOWER(username) LIKE ? OR LOWER(email) LIKE ?";
    }

    baseQuery += " LIMIT ? OFFSET ?";
    
    int offset = (page - 1) * ROWS_PER_PAGE;

    try (Connection conn = DBConnection.getConnection()) {

        // Hitung total baris
        try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
            if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
                pstmtCount.setString(1, pattern);
                pstmtCount.setString(2, pattern);
            }

            try (ResultSet rs = pstmtCount.executeQuery()) {
                if (rs.next()) {
                    int totalRows = rs.getInt(1);
                    totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);
                    SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
                    model.setMaximum(totalPages);
                    model.setValue(page);
                    numberPage.setText(page + "/" + totalPages);
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
                tableModel.setRowCount(0); // Kosongkan tabel

                while (rs.next()) {
                    Object[] row = {
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getObject("role", String.class),
                        rs.getObject("terminated_reason", String.class)
                    };
                    tableModel.addRow(row);
                }
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data pengguna.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

//search users
private void searchUsers(String keyword) {
    currentSearchKeyword = keyword;
    currentPage = 1;
    SpinnerNumberModel model = (SpinnerNumberModel) SpinnerPagination.getModel();
    model.setValue(currentPage);
    loadUsers(currentPage);
}
//update selected
private void updateSelectedUser() {
    int selectedRow = userTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang akan diperbarui.");
        return;
    }

    String id = tableModel.getValueAt(selectedRow, 0).toString();
    String newUsername = tableModel.getValueAt(selectedRow, 1).toString();
    String newEmail = tableModel.getValueAt(selectedRow, 2).toString();
    String newRole = tableModel.getValueAt(selectedRow, 3).toString();
    String newTerminatedReason = tableModel.getValueAt(selectedRow, 4) != null ?
                                  tableModel.getValueAt(selectedRow, 4).toString() : null;

    String sql = "UPDATE users SET username = ?, email = ?, role = ?, terminated_reason = ? WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, newUsername);
        pstmt.setString(2, newEmail);
        pstmt.setString(3, newRole);
        pstmt.setString(4, newTerminatedReason);
        pstmt.setString(5, id);

        pstmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui.");

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memperbarui data pengguna: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
//delete selected
private void deleteSelectedUser() {
    int selectedRow = userTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih baris yang akan dihapus.");
        return;
    }

    String id = tableModel.getValueAt(selectedRow, 0).toString();

    // Konfirmasi penghapusan
    int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus pengguna ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");

                // Muat ulang tabel setelah penghapusan
                loadUsers(currentPage);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data pengguna: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database.", "Error", JOptionPane.ERROR_MESSAGE);
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

        jScrollPane = new javax.swing.JScrollPane();
        jTabbedPane = new javax.swing.JTabbedPane();
        refresh = new javax.swing.JButton();
        SpinnerPagination = new javax.swing.JSpinner();
        numberPage = new javax.swing.JLabel();
        btnDelete = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        jScrollPane.setViewportView(jTabbedPane);

        refresh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        refresh.setText("Refresh (Update)");

        numberPage.setText("0/0");

        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 51, 51));
        btnDelete.setText("Delete");

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(refresh)
                                .addGap(53, 53, 53)
                                .addComponent(btnDelete)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(numberPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 815, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(172, 172, 172)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReset)))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnReset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numberPage)
                            .addComponent(btnDelete))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JLabel numberPage;
    private javax.swing.JButton refresh;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
