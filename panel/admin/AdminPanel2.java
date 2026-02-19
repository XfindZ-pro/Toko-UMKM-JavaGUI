/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.admin;

import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.AutoCompleteComboBox;
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class AdminPanel2 extends javax.swing.JPanel {

    private static final int PRODUCTS_PER_PAGE = 3;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = null;

    private JPanel productContainer;
    private JLabel pageStatus;
    private JSpinner paginationSpinner;
    private JPanel productTagsPanel; // New panel for product tags

    /**
     * Creates new form AdminPanel2
     */
    public AdminPanel2() {
        initComponents();
        // Inisialisasi spinner & label pagination
        this.paginationSpinner = PaginationSpinner;
        this.pageStatus = PageStatus;
        customizeUI();
        setupPagination();
        loadProducts(currentPage);
        setupProductTagsTab(); // Initialize product tags tab

        // Add a listener to detect when the tab changes
        jTabbedPane.addChangeListener(e -> {
            int selectedIndex = jTabbedPane.getSelectedIndex();
            // Check if the "Product Tags" tab is selected
            if (selectedIndex == 1) {  // "Product Tags" is the second tab (index 1)
                setProductButtonsVisible(false); // Hide buttons when Product Tags tab is selected
            } else {
                setProductButtonsVisible(true);  // Show buttons when another tab is selected
            }
        });

    }

    private void setProductButtonsVisible(boolean visible) {
        // Set visibility of the buttons when the "Product Tags" tab is selected or not
        btnAdd.setVisible(visible);
        btnEdit.setVisible(visible);
        btnDelete.setVisible(visible);
        btnUploadImage.setVisible(visible);
    }

    private void customizeUI() {
        JScrollPane scrollPane = new JScrollPane();
        productContainer = new JPanel(new GridLayout(1, 3, 10, 10));
        productContainer.setBackground(Color.WHITE);
        scrollPane.setViewportView(productContainer);
        jTabbedPane.addTab("Products", scrollPane);

        btnUploadImage.addActionListener(this::uploadProductImage);
        btnAdd.addActionListener(this::showAddDialog);
        btnEdit.addActionListener(this::showEditDialog);
        btnDelete.addActionListener(this::deleteSelectedProduct);
        btnRefresh.addActionListener(e -> {
            loadProducts(1);
        });
        btnSearch.addActionListener(e -> searchProducts(searchField.getText().trim()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentSearchKeyword = null;
            loadProducts(1);
        });
    }

    private void setupProductTagsTab() {
        productTagsPanel = new JPanel(new BorderLayout());
        JScrollPane tagsScrollPane = new JScrollPane(productTagsPanel);
        jTabbedPane.addTab("Product Tags", tagsScrollPane);

        // Panel for controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // AutoCompleteComboBox for products
        AutoCompleteComboBox productComboBox = new AutoCompleteComboBox();
        loadProductIds(productComboBox);

        // AutoCompleteComboBox for tags
        AutoCompleteComboBox tagComboBox = new AutoCompleteComboBox();
        loadTagIds(tagComboBox);

        // Add button
        JButton btnAddTag = new JButton("Add Tag");
        btnAddTag.addActionListener(e -> {
            String productId = (String) productComboBox.getSelectedItem();
            String tagId = (String) tagComboBox.getSelectedItem();

            if (productId == null || productId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a product", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tagId == null || tagId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a tag", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add product tag and refresh the table
            addProductTag(productId, tagId);
        });

        // Remove button
        JButton btnRemoveTag = new JButton("Remove Selected");
        btnRemoveTag.addActionListener(this::removeSelectedProductTag);

        controlsPanel.add(new JLabel("Product:"));
        controlsPanel.add(productComboBox);
        controlsPanel.add(new JLabel("Tag:"));
        controlsPanel.add(tagComboBox);
        controlsPanel.add(btnAddTag);
        controlsPanel.add(btnRemoveTag);

        productTagsPanel.add(controlsPanel, BorderLayout.NORTH);

        // Adding search feature
        btnSearch.addActionListener(e -> searchProductTags(searchField.getText().trim()));  // Fixed here
        btnReset.addActionListener(e -> {
            searchField.setText("");
            loadProductTags(""); // Refresh with no filter when reset is clicked
        });

        // Load existing product tags
        loadProductTags(""); // Initial load with no filter
    }

    private void loadProductTags(String searchKeyword) {
        // Clear existing content except the controls panel
        if (productTagsPanel.getComponentCount() > 1) {
            productTagsPanel.remove(1); // Remove the center component (table)
        }

        // Prepare SQL query with search filter
        String sql = "SELECT pt.product_id, p.product_name, pt.tag_id "
                + "FROM product_tags pt "
                + "JOIN product p ON pt.product_id = p.product_id "
                + "WHERE LOWER(p.product_name) LIKE ? "
                + "ORDER BY pt.product_id, pt.tag_id";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchKeyword.toLowerCase() + "%");  // Apply the search filter

            try (ResultSet rs = pstmt.executeQuery()) {

                // Create table model
                String[] columnNames = {"Product ID", "Product Name", "Tag ID", "Select"};
                Object[][] data = {};
                DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        return columnIndex == 3 ? Boolean.class : String.class;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 3; // Only the checkbox column is editable
                    }
                };

                // Populate model with data
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("tag_id"),
                        false
                    });
                }

                // Create table
                JTable tagsTable = new JTable(model);
                tagsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                // Add table to scroll pane and to panel
                JScrollPane tableScrollPane = new JScrollPane(tagsTable);
                productTagsPanel.add(tableScrollPane, BorderLayout.CENTER);

                productTagsPanel.revalidate();
                productTagsPanel.repaint();

            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load product tags", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductTag(String productId, String tagId) {
        String checkSql = "SELECT COUNT(*) FROM product_tags WHERE product_id = ? AND tag_id = ?";
        String insertSql = "INSERT INTO product_tags (product_id, tag_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            // First check if the combination already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, productId);
                checkStmt.setString(2, tagId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "This product already has this tag",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // If not exists, insert new record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, productId);
                insertStmt.setString(2, tagId);

                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Tag added successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProductTags(""); // Refresh the table after adding tag
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to add tag: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedProductTag(ActionEvent e) {
        Component centerComponent = productTagsPanel.getComponent(1);
        if (!(centerComponent instanceof JScrollPane)) {
            JOptionPane.showMessageDialog(this,
                    "No tags to remove",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JScrollPane scrollPane = (JScrollPane) centerComponent;
        JTable tagsTable = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) tagsTable.getModel();

        boolean anySelected = false;
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Boolean isSelected = (Boolean) model.getValueAt(i, 3);
            if (isSelected != null && isSelected) {
                anySelected = true;
                String productId = (String) model.getValueAt(i, 0);
                String tagId = (String) model.getValueAt(i, 2);
                deleteProductTag(productId, tagId);
            }
        }

        if (!anySelected) {
            JOptionPane.showMessageDialog(this,
                    "Please select tags to remove by checking the checkboxes",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteProductTag(String productId, String tagId) {
        String sql = "DELETE FROM product_tags WHERE product_id = ? AND tag_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productId);
            pstmt.setString(2, tagId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                loadProductTags(""); // Refresh the table after deleting tag
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to remove tag: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void loadProductIds(AutoCompleteComboBox comboBox) {
    String sql = "SELECT product_id FROM product";
    try (Connection conn = DBConnection.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql); 
         ResultSet rs = pstmt.executeQuery()) {

        List<String> productIds = new ArrayList<>();
        while (rs.next()) {
            productIds.add(rs.getString("product_id"));
        }
        
        comboBox.setItemList(productIds);
        ((JTextField)comboBox.getEditor().getEditorComponent()).setText("");

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Failed to load product IDs", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void loadTagIds(AutoCompleteComboBox comboBox) {
    String sql = "SELECT DISTINCT tag_id FROM tags";
    try (Connection conn = DBConnection.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql); 
         ResultSet rs = pstmt.executeQuery()) {

        List<String> tagIds = new ArrayList<>();
        while (rs.next()) {
            tagIds.add(rs.getString("tag_id"));
        }
        
        comboBox.setItemList(tagIds);
        ((JTextField)comboBox.getEditor().getEditorComponent()).setText("");

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Failed to load tag IDs", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void searchProductTags(String searchKeyword) {
        loadProductTags(searchKeyword);  // Pass the search keyword to loadProductTags
    }

    private void setupPagination() {
        SpinnerNumberModel model = new SpinnerNumberModel(currentPage, 1, 1, 1);
        paginationSpinner.setModel(model);
        paginationSpinner.addChangeListener(e -> {
            currentPage = (Integer) paginationSpinner.getValue();
            loadProducts(currentPage);
        });
    }

    private void loadProducts(int page) {
        productContainer.removeAll();

        String baseQuery = "SELECT product_id, product_name, product_price, product_category, product_image, product_cost FROM product";
        String countQuery = "SELECT COUNT(*) FROM product";

        if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
            baseQuery += " WHERE LOWER(product_name) LIKE ?";
            countQuery += " WHERE LOWER(product_name) LIKE ?";
        }
        baseQuery += " LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection()) {
            // Hitung jumlah total produk untuk pagination
            try (PreparedStatement pstmtCount = conn.prepareStatement(countQuery)) {
                int paramIndex = 1;
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    pstmtCount.setString(paramIndex++, "%" + currentSearchKeyword.toLowerCase() + "%");
                }
                try (ResultSet rs = pstmtCount.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt(1);
                        totalPages = (int) Math.ceil((double) total / PRODUCTS_PER_PAGE);
                        ((SpinnerNumberModel) paginationSpinner.getModel()).setMaximum(totalPages);
                        ((SpinnerNumberModel) paginationSpinner.getModel()).setValue(page);
                        pageStatus.setText(page + "/" + totalPages);
                    }
                }
            }

            // Ambil data produk untuk halaman saat ini
            try (PreparedStatement pstmtData = conn.prepareStatement(baseQuery)) {
                int paramIndex = 1;
                if (currentSearchKeyword != null && !currentSearchKeyword.isEmpty()) {
                    pstmtData.setString(paramIndex++, "%" + currentSearchKeyword.toLowerCase() + "%");
                }
                pstmtData.setInt(paramIndex++, PRODUCTS_PER_PAGE);
                pstmtData.setInt(paramIndex, (page - 1) * PRODUCTS_PER_PAGE);

                try (ResultSet rs = pstmtData.executeQuery()) {
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        JPanel productPanel = createProductPanel(
                                rs.getString("product_id"),
                                rs.getString("product_name"),
                                rs.getInt("product_price"),
                                rs.getObject("product_cost", Integer.class), // Bisa null
                                rs.getBytes("product_image")
                        );
                        productContainer.add(productPanel);
                    }

                    // Tampilkan panel kosong jika tidak ada data
                    if (!hasData) {
                        for (int i = 0; i < PRODUCTS_PER_PAGE; i++) {
                            productContainer.add(createEmptyProductPanel());
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data produk.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        productContainer.revalidate();
        productContainer.repaint();
    }

    private JPanel createEmptyProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setPreferredSize(new Dimension(200, 250));
        panel.setBackground(Color.decode("#f9f9f9"));
        JLabel emptyLabel = new JLabel("Empty", SwingConstants.CENTER);
        panel.add(emptyLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProductPanel(String productId, String name, int price, Integer cost, byte[] imageBytes) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setPreferredSize(new Dimension(200, 300));

        // Simpan product_id di properti panel
        panel.putClientProperty("product_id", productId);

        // Label gambar produk
        JLabel imgLabel = new JLabel();
        if (imageBytes != null && imageBytes.length > 0) {
            ImageIcon icon = new ImageIcon(imageBytes);
            Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imgLabel.setText("No Image");
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // Informasi produk
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        JLabel priceLabel = new JLabel("Price Rp " + price, SwingConstants.CENTER);
        JLabel costLabel = new JLabel("Cost: Rp " + (cost != null ? cost : "N/A"), SwingConstants.CENTER);

        // Panel info
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(costLabel);

        // Layout menggunakan GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        panel.add(imgLabel, gbc);

        gbc.gridy = 1;
        panel.add(infoPanel, gbc);

        // Event klik untuk pilih produk
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Reset semua latar belakang
                for (Component comp : productContainer.getComponents()) {
                    if (comp instanceof JPanel) {
                        comp.setBackground(Color.WHITE);
                    }
                }
                // Highlight panel yang dipilih
                panel.setBackground(Color.YELLOW);
                if (e.getClickCount() == 2) {
                    showProductDetail(productId, name, price, cost); // Detail tambahan
                }
            }
        });

        return panel;
    }

    private void showProductDetail(String id, String name, int price, Integer cost) {
        // Query untuk mendapatkan deskripsi produk berdasarkan product_id
        String sql = "SELECT product_description FROM product WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                String description = rs.next() ? rs.getString("product_description") : "Tidak tersedia";

                // Panel detail
                JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
                panel.add(new JLabel("Product ID: " + id));
                panel.add(new JLabel("Name: " + name));
                panel.add(new JLabel("Price: Rp" + price));
                panel.add(new JLabel("Cost: Rp" + (cost != null ? cost : "N/A")));
                panel.add(new JLabel("Description: " + description));

                JScrollPane scroll = new JScrollPane(panel);
                scroll.setPreferredSize(new Dimension(400, 300));

                // Dialog informasi
                JOptionPane.showMessageDialog(this, scroll, "Detail Produk", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil detail produk: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts(String keyword) {
        currentSearchKeyword = keyword;
        currentPage = 1;
        ((SpinnerNumberModel) paginationSpinner.getModel()).setValue(currentPage);
        loadProducts(currentPage);
    }

    // Changes in the showAddDialog method
    private void showAddDialog(ActionEvent e) {
        // Komponen form
        JTextField tfName = new JTextField();
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1000));
        JTextArea taDesc = new JTextArea(3, 20);
        JTextField tfCategory = new JTextField();
        JSpinner costSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1000));

        // AutoCompleteComboBox untuk supplier
        AutoCompleteComboBox supplierBox = new AutoCompleteComboBox();
        loadSupplierIds(supplierBox); // Ini akan memuat daftar supplier dan mengatur item list

        // Panel form dengan GridLayout
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceSpinner);
        formPanel.add(new JLabel("Cost:"));
        formPanel.add(costSpinner);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(taDesc));
        formPanel.add(new JLabel("Category:"));
        formPanel.add(tfCategory);
        formPanel.add(new JLabel("Supplier ID:"));
        formPanel.add(supplierBox);

        // Dialog setup
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Product", true);
        dialog.setPreferredSize(new Dimension(500, 500)); // Ukuran dialog lebih besar
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        // Action untuk Save button
        btnSave.addActionListener(ev -> {
            String name = tfName.getText().trim();
            int price = (Integer) priceSpinner.getValue();
            int cost = (Integer) costSpinner.getValue(); // Ambil nilai cost
            String desc = taDesc.getText().trim();
            String category = tfCategory.getText().trim(); // Mengambil nilai dari TextField
            String companyId = (String) supplierBox.getSelectedItem();

            // Validasi input
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Product name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                tfName.requestFocus();
                return;
            }
            if (category.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Category cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                tfCategory.requestFocus();
                return;
            }
            if (companyId == null || companyId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a supplier!", "Error", JOptionPane.ERROR_MESSAGE);
                supplierBox.requestFocus();
                return;
            }

            // Generate product_id yang unik
            String productId = generateUniqueProductId();
            if (productId == null) {
                JOptionPane.showMessageDialog(dialog, "Failed to generate unique product ID!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String sql = "INSERT INTO product (product_id, product_name, product_price, "
                        + "product_description, product_category, company_id, product_cost) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, productId);
                    pstmt.setString(2, name);
                    pstmt.setInt(3, price);
                    pstmt.setString(4, desc);
                    pstmt.setString(5, category);
                    pstmt.setString(6, companyId);
                    pstmt.setInt(7, cost); // Simpan product_cost

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "Product added successfully!\nProduct ID: " + productId,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadProducts(currentPage);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to save product: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Action untuk Cancel button
        btnCancel.addActionListener(ev -> dialog.dispose());

        // Layout dialog
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

// Method untuk generate product_id unik
    private String generateUniqueProductId() {
        Random random = new Random();
        String productId;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        do {
            // Generate random 5 digit number
            int randomNum = 10000 + random.nextInt(90000);
            productId = "pd" + randomNum;
            attempts++;

            // Cek apakah ID sudah ada di database
            if (!isProductIdExists(productId)) {
                return productId;
            }

        } while (attempts < MAX_ATTEMPTS);

        return null;
    }

// Method untuk cek apakah product_id sudah ada
    private boolean isProductIdExists(String productId) {
        String sql = "SELECT COUNT(*) FROM product WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return true; // Jika error, anggap ID sudah ada untuk menghindari duplikasi
    }

    private void showEditDialog(ActionEvent e) {
        // Find the selected product panel
        JPanel selectedPanel = null;
        for (Component comp : productContainer.getComponents()) {
            if (comp instanceof JPanel && comp.getBackground() == Color.YELLOW) {
                selectedPanel = (JPanel) comp;
                break;
            }
        }

        if (selectedPanel == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first by clicking on it", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the product_id from the selected panel
        String productId = (String) selectedPanel.getClientProperty("product_id");
        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Could not identify selected product", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        editProduct(productId);
    }

    private void editProduct(String productId) {
        // Query untuk mengambil data produk berdasarkan product_id
        String sql = "SELECT product_name, product_price, product_description, product_category, company_id, product_cost FROM product WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Ambil data produk untuk ditampilkan di form edit
                    String name = rs.getString("product_name");
                    int price = rs.getInt("product_price");
                    String description = rs.getString("product_description");
                    String category = rs.getString("product_category");
                    String companyId = rs.getString("company_id");
                    int cost = rs.getObject("product_cost", Integer.class); // Bisa null

                    // Dialog untuk mengedit produk
                    JTextField tfName = new JTextField(name);
                    JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(price, 0, Integer.MAX_VALUE, 1000));
                    JTextArea taDesc = new JTextArea(description, 3, 20);
                    JTextField tfCategory = new JTextField(category);
                    JSpinner costSpinner = new JSpinner(new SpinnerNumberModel(cost, 0, Integer.MAX_VALUE, 1000)); // Tambahkan product_cost
                    // AutoCompleteComboBox untuk supplier
                    AutoCompleteComboBox supplierBox = new AutoCompleteComboBox();
                    loadSupplierIds(supplierBox);
                    supplierBox.getEditor().setItem(companyId); // Set the current supplier ID

                    // Panel form untuk input data produk yang akan diubah
                    JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
                    formPanel.add(new JLabel("Product ID: " + productId));
                    formPanel.add(new JLabel("Product Name:"));
                    formPanel.add(tfName);
                    formPanel.add(new JLabel("Price:"));
                    formPanel.add(priceSpinner);
                    formPanel.add(new JLabel("Cost:")); // Label untuk Cost
                    formPanel.add(costSpinner); // Input untuk Cost
                    formPanel.add(new JLabel("Description:"));
                    formPanel.add(new JScrollPane(taDesc));
                    formPanel.add(new JLabel("Category:"));
                    formPanel.add(tfCategory);
                    formPanel.add(new JLabel("Supplier ID:"));
                    formPanel.add(supplierBox);

                    // Dialog untuk mengedit produk
                    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Product", true);
                    dialog.setPreferredSize(new Dimension(500, 550));
                    JButton btnSave = new JButton("Save");
                    JButton btnCancel = new JButton("Cancel");

                    btnSave.addActionListener(ev -> {
                        String newName = tfName.getText().trim();
                        int newPrice = (Integer) priceSpinner.getValue();
                        int newCost = (Integer) costSpinner.getValue(); // Ambil nilai cost
                        String newDesc = taDesc.getText().trim();
                        String newCategory = tfCategory.getText().trim();
                        String newCompanyId = (String) supplierBox.getSelectedItem();

                        // Validasi input
                        if (newName.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "Product name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                            tfName.requestFocus();
                            return;
                        }
                        if (newCategory.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "Category cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                            tfCategory.requestFocus();
                            return;
                        }
                        if (newCompanyId == null || newCompanyId.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "Please select a supplier!", "Error", JOptionPane.ERROR_MESSAGE);
                            supplierBox.requestFocus();
                            return;
                        }

                        // Update produk ke database
                        try {
                            String updateSql = "UPDATE product SET product_name = ?, product_price = ?, "
                                    + "product_description = ?, product_category = ?, company_id = ?, product_cost = ? "
                                    + "WHERE product_id = ?";
                            try (Connection conn2 = DBConnection.getConnection(); PreparedStatement pstmt2 = conn2.prepareStatement(updateSql)) {
                                pstmt2.setString(1, newName);
                                pstmt2.setInt(2, newPrice);
                                pstmt2.setString(3, newDesc);
                                pstmt2.setString(4, newCategory);
                                pstmt2.setString(5, newCompanyId);
                                pstmt2.setInt(6, newCost); // Simpan product_cost
                                pstmt2.setString(7, productId);

                                int affectedRows = pstmt2.executeUpdate();
                                if (affectedRows > 0) {
                                    JOptionPane.showMessageDialog(dialog,
                                            "Product updated successfully!",
                                            "Success",
                                            JOptionPane.INFORMATION_MESSAGE);
                                    dialog.dispose();
                                    loadProducts(currentPage);
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Failed to update product: " + ex.getMessage(),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    });

                    btnCancel.addActionListener(ev -> dialog.dispose());

                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.add(btnSave);
                    buttonPanel.add(btnCancel);

                    dialog.setLayout(new BorderLayout());
                    dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
                    dialog.add(buttonPanel, BorderLayout.SOUTH);
                    dialog.pack();
                    dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct(ActionEvent e) {
        JPanel selectedPanel = null;

        // Cari panel yang dipilih dengan warna latar belakang kuning
        for (Component comp : productContainer.getComponents()) {
            if (comp instanceof JPanel && comp.getBackground() == Color.YELLOW) {
                selectedPanel = (JPanel) comp;
                break;
            }
        }

        // Cek apakah ada panel yang dipilih
        if (selectedPanel == null) {
            JOptionPane.showMessageDialog(this, "Please select a product first by clicking on it", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil product_id dari panel yang dipilih
        String productId = (String) selectedPanel.getClientProperty("product_id");
        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Could not identify selected product", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Konfirmasi penghapusan
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            deleteProductFromDB(productId);
        }
    }

    private void deleteProductFromDB(String productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            pstmt.executeUpdate();
            loadProducts(currentPage);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus produk: " + ex.getMessage());
        }
    }

// Add this method to your class:
    private void uploadProductImage(ActionEvent e) {
        // Find the selected product panel
        JPanel selectedPanel = null;
        for (Component comp : productContainer.getComponents()) {
            if (comp instanceof JPanel && comp.getBackground() == Color.YELLOW) {
                selectedPanel = (JPanel) comp;
                break;
            }
        }

        if (selectedPanel == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product first by clicking on it",
                    "No Product Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the product_id from the selected panel
        String productId = (String) selectedPanel.getClientProperty("product_id");
        if (productId == null) {
            JOptionPane.showMessageDialog(this,
                    "Could not identify selected product",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Product Image");

        // Set file filter for images only
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();

            // Verify file extension
            if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
                JOptionPane.showMessageDialog(this,
                        "Only JPG/JPEG/PNG files are allowed",
                        "Invalid File Type",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Read file and update database
            try (FileInputStream fis = new FileInputStream(selectedFile); Connection conn = DBConnection.getConnection()) {

                String sql = "UPDATE product SET product_image = ? WHERE product_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setBinaryStream(1, fis, (int) selectedFile.length());
                    pstmt.setString(2, productId);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Product image updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadProducts(currentPage); // Refresh the view
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to upload image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void loadSupplierIds(AutoCompleteComboBox comboBox) {
        String sql = "SELECT company_id FROM suppliers";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            List<String> suppliers = new ArrayList<>();
            while (rs.next()) {
                suppliers.add(rs.getString("company_id"));
            }

            // Set the item list for autocomplete
            comboBox.setItemList(suppliers);

            // Clear the text field
            JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
            editor.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load suppliers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * private void loadSupplierIds(AutoCompleteComboBox comboBox) { String sql
     * = "SELECT company_id FROM suppliers"; try (Connection conn =
     * DBConnection.getConnection(); PreparedStatement pstmt =
     * conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
     *
     * while (rs.next()) { comboBox.addItem(rs.getString("company_id")); }
     *
     * } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Gagal
     * memuat daftar supplier."); }
    }
     */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelBoard = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        jTabbedPane = new javax.swing.JTabbedPane();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        PaginationSpinner = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        btnUploadImage = new javax.swing.JButton();

        PanelBoard.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane.setViewportView(jTabbedPane);

        javax.swing.GroupLayout PanelBoardLayout = new javax.swing.GroupLayout(PanelBoard);
        PanelBoard.setLayout(PanelBoardLayout);
        PanelBoardLayout.setHorizontalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE)
        );
        PanelBoardLayout.setVerticalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        btnAdd.setText("Add");

        btnEdit.setText("Edit");

        btnDelete.setText("Delete");

        btnRefresh.setText("Refresh");

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        PageStatus.setText("0/0");

        btnUploadImage.setText("Upload Image");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(465, 465, 465)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnSearch)
                .addGap(18, 18, 18)
                .addComponent(btnReset)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 78, Short.MAX_VALUE)
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEdit)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh)
                .addGap(49, 49, 49)
                .addComponent(btnUploadImage, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addComponent(PageStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(107, 107, 107))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnReset))
                .addGap(29, 29, 29)
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAdd)
                        .addComponent(btnEdit)
                        .addComponent(btnDelete)
                        .addComponent(btnRefresh)
                        .addComponent(btnUploadImage))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(PageStatus)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner PaginationSpinner;
    private javax.swing.JPanel PanelBoard;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUploadImage;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
