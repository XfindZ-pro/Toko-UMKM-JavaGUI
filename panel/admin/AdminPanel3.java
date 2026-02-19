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
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class AdminPanel3 extends javax.swing.JPanel {

private DefaultTableModel tableModel;
private JTable userTable;


private static final int ROWS_PER_PAGE = 10;
private int currentPage = 1;
private int totalPages = 1;
private JTable supplierTable;
private String currentSearchKeyword = null;

    /**
     * Creates new form AdminPanel3
     */
    public AdminPanel3() {
        initComponents();
        customizeUI();
        setupPagination();
        loadSuppliers(currentPage);

    }

    //~~
  private void customizeUI() {
    String[] columnNames = {"Company ID", "Company Name", "Contact Name", "Email", "Phone", "Address", "Branch", "Product Count"};
    tableModel = new DefaultTableModel(columnNames, 0);
    supplierTable = new JTable(tableModel); // ✅ Inisialisasi supplierTable
    JScrollPane scrollPane = new JScrollPane(supplierTable);
    tabbedPane.addTab("Suppliers", scrollPane);

    // Setup tombol aksi
    btnAdd.addActionListener(this::showAddDialog);
    btnEdit.addActionListener(this::updateSelectedData); // Pastikan method sesuai signature
    btnDelete.addActionListener(e -> deleteSelectedSupplier(null));
    btnRefresh.addActionListener(e -> loadSuppliers(1));
    btnSearch.addActionListener(e -> searchSuppliers(searchField.getText().trim()));
    btnResetSearch.addActionListener(e -> {
        searchField.setText("");
        currentSearchKeyword = null;
        loadSuppliers(1);
    });
}

    private void setupPagination() {
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(currentPage, 1, 1, 1);
        spinnerPagination.setModel(spinnerModel);
        spinnerPagination.addChangeListener((ChangeEvent e) -> {
            currentPage = (Integer) spinnerPagination.getValue();
            loadSuppliers(currentPage);
        });
    }

  private void loadSuppliers(int page) {
    int offset = (page - 1) * ROWS_PER_PAGE;

    String baseQuery = "SELECT company_id, company_name, contact_name, contact_email, contact_phone, address, company_branch, product_count FROM suppliers";
    String countQuery = "SELECT COUNT(*) FROM suppliers";

    if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
        String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
        baseQuery += " WHERE LOWER(company_name) LIKE ?";
        countQuery += " WHERE LOWER(company_name) LIKE ?";
    }

    baseQuery += " LIMIT ? OFFSET ?";

    try (Connection conn = DBConnection.getConnection()) {

        // Hitung total baris
        try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
            if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
                pstmtCount.setString(1, pattern);
            }
            try (ResultSet rs = pstmtCount.executeQuery()) {
                if (rs.next()) {
                    int totalRows = rs.getInt(1);
                    totalPages = (int) Math.ceil((double) totalRows / ROWS_PER_PAGE);

                    // Update the spinner max value
                    SpinnerNumberModel model = (SpinnerNumberModel) spinnerPagination.getModel();
                    model.setMaximum(totalPages);
                    model.setValue(page);  // Set the current page to the spinner

                    // Update PageStatus to show currentPage/totalPages
                    PageStatus.setText(page + "/" + totalPages);
                }
            }
        }

        // Ambil data halaman saat ini
        try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
            int paramIndex = 1;
            if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
                pstmtData.setString(paramIndex++, pattern);
            }
            pstmtData.setInt(paramIndex++, ROWS_PER_PAGE);
            pstmtData.setInt(paramIndex, offset);

            try (ResultSet rs = pstmtData.executeQuery()) {
                tableModel.setRowCount(0); // Kosongkan tabel

                while (rs.next()) {
                    Object[] row = {
                        rs.getString("company_id"),
                        rs.getString("company_name"),
                        rs.getString("contact_name"),
                        rs.getString("contact_email"),
                        rs.getString("contact_phone"),
                        rs.getString("address"),
                        rs.getString("company_branch"),
                        rs.getInt("product_count")
                    };
                    tableModel.addRow(row);
                }
            }
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data supplier.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void searchSuppliers(String keyword) {
        currentSearchKeyword = keyword;
        currentPage = 1;
        SpinnerNumberModel model = (SpinnerNumberModel) spinnerPagination.getModel();
        model.setValue(currentPage);
        loadSuppliers(currentPage);
    }

    private String generateCompanyId() {
    String companyId;
    boolean isUnique = false;
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        conn = DBConnection.getConnection();
        String checkSql = "SELECT COUNT(*) FROM suppliers WHERE company_id = ?";
        pstmt = conn.prepareStatement(checkSql);

        do {
            // Generate cp + 5 digit random number
            int randomNumber = (int) (Math.random() * 90000) + 10000; // 10000 - 99999
            companyId = "cp" + randomNumber;

            // Cek apakah ID sudah ada di database
            pstmt.setString(1, companyId);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                isUnique = true;
            }
        } while (!isUnique);
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memeriksa ID perusahaan.", "Error", JOptionPane.ERROR_MESSAGE);
        return null;
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    return companyId;
}
    
private void showAddDialog(ActionEvent e) {
    String companyId = generateCompanyId();
    if (companyId == null) {
        JOptionPane.showMessageDialog(this, "Gagal menghasilkan ID perusahaan.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    JTextField tfName = new JTextField();
    JTextField tfContact = new JTextField();
    JTextField tfEmail = new JTextField();
    JTextField tfPhone = new JTextField();
    JTextArea taAddress = new JTextArea(3, 20);
    JTextField tfBranch = new JTextField();
    JTextField tfProductCount = new JTextField("0");

    JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
    panel.add(new JLabel("Company ID:"));
    panel.add(new JLabel(companyId)); // Hanya menampilkan ID
    panel.add(new JLabel("Company Name:"));
    panel.add(tfName);
    panel.add(new JLabel("Contact Name:"));
    panel.add(tfContact);
    panel.add(new JLabel("Email:"));
    panel.add(tfEmail);
    panel.add(new JLabel("Phone:"));
    panel.add(tfPhone);
    panel.add(new JLabel("Address:"));
    panel.add(new JScrollPane(taAddress)); // ScrollPane untuk textarea panjang
    panel.add(new JLabel("Branch:"));
    panel.add(tfBranch);
    panel.add(new JLabel("Product Count:"));
    panel.add(tfProductCount);

    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(new Dimension(400, 300));

    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Supplier", true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JButton btnOk = new JButton("OK");
    JButton btnCancel = new JButton("Cancel");

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(btnOk);
    buttonPanel.add(btnCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(scrollPane, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Aksi tombol OK
    btnOk.addActionListener(evt -> {
        try {
            String name = tfName.getText().trim();
            String contact = tfContact.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String address = taAddress.getText().trim();
            String branch = tfBranch.getText().trim();
            int productCount = Integer.parseInt(tfProductCount.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Company Name harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO suppliers (company_id, company_name, contact_name, contact_email, contact_phone, address, company_branch, product_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, companyId);
                pstmt.setString(2, name);
                pstmt.setString(3, contact);
                pstmt.setString(4, email);
                pstmt.setString(5, phone);
                pstmt.setString(6, address);
                pstmt.setString(7, branch);
                pstmt.setInt(8, productCount);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Supplier berhasil ditambahkan.");
                dialog.dispose();
                loadSuppliers(currentPage); // Muat ulang tabel

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan supplier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Product Count harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    btnCancel.addActionListener(evt -> dialog.dispose());

    dialog.pack();
    dialog.setLocationRelativeTo(null); // Posisi di tengah layar
    dialog.setVisible(true);
}

 private void updateSelectedData(ActionEvent e) {
    int selectedRow = supplierTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih salah satu baris untuk diedit.");
        return;
    }

    String companyId = tableModel.getValueAt(selectedRow, 0).toString();

    // Ambil nilai dari tabel langsung
    String newName = tableModel.getValueAt(selectedRow, 1).toString();
    String newContact = tableModel.getValueAt(selectedRow, 2).toString();
    String newEmail = tableModel.getValueAt(selectedRow, 3).toString();
    String newPhone = tableModel.getValueAt(selectedRow, 4).toString();
    String newAddress = tableModel.getValueAt(selectedRow, 5).toString();
    String newBranch = tableModel.getValueAt(selectedRow, 6).toString();
    Object productCountObj = tableModel.getValueAt(selectedRow, 7);
    int newProductCount;

    try {
        newProductCount = Integer.parseInt(productCountObj.toString());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Product Count harus berupa angka valid.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "Simpan perubahan?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        String sql = "UPDATE suppliers SET company_name = ?, contact_name = ?, contact_email = ?, contact_phone = ?, address = ?, company_branch = ?, product_count = ? WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, newContact);
            pstmt.setString(3, newEmail);
            pstmt.setString(4, newPhone);
            pstmt.setString(5, newAddress);
            pstmt.setString(6, newBranch);
            pstmt.setInt(7, newProductCount);
            pstmt.setString(8, companyId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil diperbarui.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void deleteSelectedSupplier(ActionEvent e) {
    int selectedRow = supplierTable.getSelectedRow(); // ✅ Sekarang aman
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Pilih salah satu baris untuk dihapus.");
        return;
    }

    String companyId = tableModel.getValueAt(selectedRow, 0).toString();
    int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus supplier ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        String sql = "DELETE FROM suppliers WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, companyId);
            pstmt.executeUpdate();
            loadSuppliers(currentPage);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    
    private void loadUsers(int page) {
    int offset = (page - 1) * ROWS_PER_PAGE;

    String baseQuery = "SELECT id, username, email, role, terminated_reason FROM users";
    String countQuery = "SELECT COUNT(*) FROM users";

    // Jika ada pencarian, tambahkan kondisi WHERE
    if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
        String pattern = "%" + currentSearchKeyword.toLowerCase() + "%";
        baseQuery += " WHERE LOWER(username) LIKE ? OR LOWER(email) LIKE ?";
        countQuery += " WHERE LOWER(username) LIKE ? OR LOWER(email) LIKE ?";
    }

    baseQuery += " LIMIT ? OFFSET ?";

    try (Connection conn = DBConnection.getConnection()) {

        // Hitung total halaman
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
                    SpinnerNumberModel model = (SpinnerNumberModel) spinnerPagination.getModel();
                    model.setMaximum(totalPages);
                    model.setValue(page);
                    PageStatus.setText(page + "/" + totalPages);
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
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelList = new javax.swing.JPanel();
        ScrollPane = new javax.swing.JScrollPane();
        tabbedPane = new javax.swing.JTabbedPane();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnResetSearch = new javax.swing.JButton();
        spinnerPagination = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();

        panelList.setBackground(new java.awt.Color(255, 255, 255));

        ScrollPane.setViewportView(tabbedPane);

        javax.swing.GroupLayout panelListLayout = new javax.swing.GroupLayout(panelList);
        panelList.setLayout(panelListLayout);
        panelListLayout.setHorizontalGroup(
            panelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 872, Short.MAX_VALUE)
        );
        panelListLayout.setVerticalGroup(
            panelListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );

        btnAdd.setText("Add");

        btnEdit.setText("Edit");

        btnDelete.setText("Delete");

        btnRefresh.setText("Refresh");

        searchField.setBackground(new java.awt.Color(204, 204, 204));

        btnSearch.setText("Search");

        btnResetSearch.setText("Reset");

        PageStatus.setText("0/0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(panelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(221, 221, 221)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch)
                        .addGap(18, 18, 18)
                        .addComponent(btnResetSearch)))
                .addContainerGap(39, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addComponent(btnAdd)
                .addGap(28, 28, 28)
                .addComponent(btnEdit)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PageStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(spinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnResetSearch))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(panelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnEdit)
                            .addComponent(btnDelete)
                            .addComponent(btnRefresh))
                        .addContainerGap(72, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(PageStatus))
                        .addGap(64, 64, 64))))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnResetSearch;
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel panelList;
    private javax.swing.JTextField searchField;
    private javax.swing.JSpinner spinnerPagination;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

 
}