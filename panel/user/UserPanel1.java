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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;
import projeksmt2.util.AutoCompleteComboBox;

/**
 *
 * @author findo
 */
public class UserPanel1 extends javax.swing.JPanel {

    private static final int PRODUCTS_PER_ROW = 3;
    private static final int PRODUCTS_PER_COLUMN = 5;
    private static final int PRODUCTS_PER_PAGE = PRODUCTS_PER_ROW * PRODUCTS_PER_COLUMN;
    private int currentPage = 1;
    private int totalPages = 1;

    private JPanel productsContainer;

    /**
     * Creates new form UserPanel1
     */
    public UserPanel1() {
        initComponents();
        btnChooseOutlet.addActionListener(e -> chooseOutlet()); // Tambahkan listener

        // Initialize products container
        productsContainer = new JPanel(new GridLayout(0, PRODUCTS_PER_ROW, 10, 10));
        productsContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(productsContainer);
        ScrollPane.setViewportView(scrollPane);

        // Load initial data
        checkOutletAndLoadProducts();
    }

    private void checkOutletAndLoadProducts() {
        String userId = SessionManager.getCurrentUserId();

        try (Connection conn = DBConnection.getConnection()) {
            // Check if user has selected outlet
            String sql = "SELECT selected_outlet FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String selectedOutlet = rs.getString("selected_outlet");
                    if (selectedOutlet == null || selectedOutlet.isEmpty()) {
                        showOutletPrompt();
                    } else {
                        loadProducts(selectedOutlet, currentPage);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error checking outlet: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOutletPrompt() {
        productsContainer.removeAll();
        JLabel promptLabel = new JLabel("Please Select Outlet", SwingConstants.CENTER);
        promptLabel.setFont(promptLabel.getFont().deriveFont(18.0f));
        productsContainer.add(promptLabel);
        productsContainer.revalidate();
        productsContainer.repaint();
    }

    private void loadProducts(String outletId, int page) {
        productsContainer.removeAll();

        try (Connection conn = DBConnection.getConnection()) {
            // Count total products for pagination
            String countSql = "SELECT COUNT(*) FROM outlet_product op "
                    + "JOIN product p ON op.product_id = p.product_id "
                    + "WHERE op.outlet_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                pstmt.setString(1, outletId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int totalProducts = rs.getInt(1);
                    totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);
                    PageStatus.setText(page + "/" + totalPages);
                    ((javax.swing.SpinnerNumberModel) PaginationSpinner.getModel())
                            .setMaximum(totalPages);
                }
            }

            // Get products for current page
            String productSql = "SELECT p.product_id, p.product_name, p.product_price, p.product_image "
                    + "FROM outlet_product op "
                    + "JOIN product p ON op.product_id = p.product_id "
                    + "WHERE op.outlet_id = ? "
                    + "LIMIT ? OFFSET ?";
            try (PreparedStatement pstmt = conn.prepareStatement(productSql)) {
                pstmt.setString(1, outletId);
                pstmt.setInt(2, PRODUCTS_PER_PAGE);
                pstmt.setInt(3, (page - 1) * PRODUCTS_PER_PAGE);

                ResultSet rs = pstmt.executeQuery();

                boolean hasProducts = false;
                while (rs.next()) {
                    hasProducts = true;
                    JPanel productPanel = createProductPanel(
                            rs.getString("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("product_price"),
                            rs.getBytes("product_image")
                    );
                    productsContainer.add(productPanel);
                }

                // If no products found, show empty message
                if (!hasProducts) {
                    JLabel emptyLabel = new JLabel("Empty", SwingConstants.CENTER);
                    emptyLabel.setFont(emptyLabel.getFont().deriveFont(18.0f));
                    productsContainer.add(emptyLabel);
                }

                // Add empty panels to maintain layout
                int remainingSlots = PRODUCTS_PER_PAGE - productsContainer.getComponentCount();
                for (int i = 0; i < remainingSlots; i++) {
                    productsContainer.add(createEmptyPanel());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        productsContainer.revalidate();
        productsContainer.repaint();
    }

   private JPanel createProductPanel(String productId, String name, int price, byte[] imageBytes) {
        // Main panel with border layout
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.setPreferredSize(new Dimension(250, 320)); // Increased size
        panel.setBackground(Color.WHITE);

        // Image panel (60% of height)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setPreferredSize(new Dimension(250, 200)); // Fixed height for image
        
        JLabel imageLabel = new JLabel();
        if (imageBytes != null && imageBytes.length > 0) {
            ImageIcon icon = new ImageIcon(imageBytes);
            // Scale image to fit while maintaining aspect ratio
            Image img = icon.getImage();
            double aspectRatio = (double) icon.getIconWidth() / icon.getIconHeight();
            int newWidth = 200;
            int newHeight = (int) (newWidth / aspectRatio);
            if (newHeight > 180) {
                newHeight = 180;
                newWidth = (int) (newHeight * aspectRatio);
            }
            Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // Info panel (40% of height)
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel("<html><div style='text-align:center;'>" + name + "</div></html>");
        nameLabel.setFont(nameLabel.getFont().deriveFont(14f));
        
        JLabel priceLabel = new JLabel("<html><div style='text-align:center; color: #2E7D32; font-weight:bold;'>Rp " + 
                String.format("%,d", price) + "</div></html>");
        priceLabel.setFont(priceLabel.getFont().deriveFont(14f));

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        // Add components to main panel
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        // Add hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                resetPanelBackgrounds();
                panel.setBackground(new Color(230, 245, 243));
                if (e.getClickCount() == 2) {
                    showProductDetail(productId);
                }
            }
        });

        return panel;
    }

    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setPreferredSize(new Dimension(200, 250));
        panel.setBackground(new Color(240, 240, 240));
        return panel;
    }

    private void resetPanelBackgrounds() {
        for (java.awt.Component comp : productsContainer.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(new Color(240, 240, 240));
            }
        }
    }

    private String getSelectedOutletId(String customerId) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "SELECT selected_outlet FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("selected_outlet");
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return null;
}
    
    private String generateCartId() {
    Random random = new Random();
    String cartId;
    int attempts = 0;
    final int MAX_ATTEMPTS = 10;

    try (Connection conn = DBConnection.getConnection()) {
        do {
            // Generate random 5 digit number
            int randomNum = 10000 + random.nextInt(90000);
            cartId = "shp" + randomNum;
            attempts++;

            // Check if ID already exists
            String checkSql = "SELECT COUNT(*) FROM shopping_cart WHERE cart_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, cartId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return cartId;
                    }
                }
            }
        } while (attempts < MAX_ATTEMPTS);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return "shp" + System.currentTimeMillis(); // Fallback if random generation fails
}
    
   private void addToCart(String productId, String customerId, int quantity) {
    // Don't proceed if quantity is 0 for new items
    if (quantity <= 0) {
        JOptionPane.showMessageDialog(this,
            "Quantity must be at least 1",
            "Invalid Quantity",
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    String outletId = getSelectedOutletId(customerId);
    
    if (outletId == null) {
        JOptionPane.showMessageDialog(this,
                "Please select an outlet first",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DBConnection.getConnection()) {
        // Check if item already exists in cart
        String checkSql = "SELECT cart_id, quantity FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, productId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String existingCartId = rs.getString("cart_id");
                int existingQuantity = rs.getInt("quantity");
                
                if (quantity <= 0) {
                    // Remove item from cart if quantity is 0
                    String deleteSql = "DELETE FROM shopping_cart WHERE cart_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setString(1, existingCartId);
                        int affected = deleteStmt.executeUpdate();
                        if (affected > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Item removed from cart!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    // Update existing item quantity
                    String updateSql = "UPDATE shopping_cart SET quantity = ?, updated_at = CURRENT_TIMESTAMP() " +
                                     "WHERE cart_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setString(2, existingCartId);
                        
                        int affected = updateStmt.executeUpdate();
                        if (affected > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Cart updated successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } else {
                // Only add new item if quantity > 0
                if (quantity > 0) {
                    String cartId = generateCartId();
                    String insertSql = "INSERT INTO shopping_cart (cart_id, customer_id, product_id, quantity, outlet_id) " +
                                     "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, cartId);
                        insertStmt.setString(2, customerId);
                        insertStmt.setString(3, productId);
                        insertStmt.setInt(4, quantity);
                        insertStmt.setString(5, outletId);
                        
                        int affected = insertStmt.executeUpdate();
                        if (affected > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Item added to cart!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error updating cart: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

private void showProductDetail(String productId) {
    try (Connection conn = DBConnection.getConnection()) {
        // Get product details
        String productSql = "SELECT product_name, product_description, product_price FROM product WHERE product_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(productSql);
        pstmt.setString(1, productId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String productName = rs.getString("product_name");
            String productDesc = rs.getString("product_description");
            int productPrice = rs.getInt("product_price");

            // Create dialog
            JDialog dialog = new JDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), "Product Details", true);
            dialog.setLayout(new BorderLayout());

            // Main panel
            JPanel mainPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Product info
            JLabel nameLabel = new JLabel("<html><b>" + productName + "</b></html>");
            nameLabel.setFont(nameLabel.getFont().deriveFont(16f));
            
            JLabel descLabel = new JLabel("<html>" + (productDesc != null ? productDesc : "No description available") + "</html>");
            JLabel priceLabel = new JLabel("<html>Price: <b>Rp " + String.format("%,d", productPrice) + "</b></html>");

            // Quantity spinner
            JPanel quantityPanel = new JPanel(new BorderLayout());
            JLabel quantityLabel = new JLabel("Quantity:");
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
            JSpinner quantitySpinner = new JSpinner(spinnerModel);
            
            // Check if product already in cart
            String userId = SessionManager.getCurrentUserId();
            String checkCartSql = "SELECT quantity FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
            pstmt = conn.prepareStatement(checkCartSql);
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            ResultSet cartRs = pstmt.executeQuery();
            
            boolean isInCart = cartRs.next();
            if (isInCart) {
                spinnerModel.setValue(cartRs.getInt("quantity"));
            }

            quantityPanel.add(quantityLabel, BorderLayout.WEST);
            quantityPanel.add(quantitySpinner, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelButton = new JButton("Cancel");
            JButton addButton = new JButton(isInCart ? "Update Cart" : "Add to Cart");
            JButton removeButton = null;
            
            if (isInCart) {
                removeButton = new JButton("Remove from Cart");
                removeButton.setForeground(Color.RED);
                
                removeButton.addActionListener(e -> {
                    removeFromCart(productId, userId);
                    dialog.dispose();
                });
            }

            // Add components
            mainPanel.add(nameLabel);
            mainPanel.add(descLabel);
            mainPanel.add(priceLabel);
            mainPanel.add(quantityPanel);

            buttonPanel.add(cancelButton);
            if (removeButton != null) {
                buttonPanel.add(removeButton);
            }
            buttonPanel.add(addButton);

            dialog.add(mainPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Button actions
            cancelButton.addActionListener(e -> dialog.dispose());
            
            addButton.addActionListener(e -> {
                int quantity = (Integer) quantitySpinner.getValue();
                addToCart(productId, userId, quantity);
                dialog.dispose();
            });

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error loading product details: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

private void removeFromCart(String productId, String customerId) {
    try (Connection conn = DBConnection.getConnection()) {
        String deleteSql = "DELETE FROM shopping_cart WHERE customer_id = ? AND product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, productId);
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                JOptionPane.showMessageDialog(this,
                        "Item removed from cart!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Item not found in cart",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error removing from cart: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


    private void chooseOutlet() {
        try {
            String userId = SessionManager.getCurrentUserId();

            // 1. Cek apakah ada item di shopping_cart
            boolean hasItemsInCart = false;
            String checkCartSQL = "SELECT COUNT(*) FROM shopping_cart WHERE customer_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(checkCartSQL)) {

                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    hasItemsInCart = rs.getInt(1) > 0;
                }
            }

            // 2. Jika ada item di cart, tampilkan konfirmasi
            if (hasItemsInCart) {
                int option = JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to change outlet? This will clear your current cart.",
                        "Change Outlet",
                        JOptionPane.YES_NO_OPTION
                );

                if (option != JOptionPane.YES_OPTION) {
                    return; // User batal
                }

                // Kosongkan shopping_cart jika user setuju ganti outlet
                String clearCartSQL = "DELETE FROM shopping_cart WHERE customer_id = ?";
                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(clearCartSQL)) {

                    pstmt.setString(1, userId);
                    pstmt.executeUpdate();
                }
            }

            // 3. Buat dialog pencarian outlet
            JDialog dialog = new JDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), "Select Outlet", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JLabel label = new JLabel("Search Outlet:");
            AutoCompleteComboBox outletComboBox = new AutoCompleteComboBox();
            JButton selectButton = new JButton("Select");

            // Isi combo box dengan nama outlet
            String loadOutletsSQL = "SELECT outlet_name FROM outlet";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(loadOutletsSQL); ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    outletComboBox.addItem(rs.getString("outlet_name"));
                }
            }

            // Setup layout dialog
            GroupLayout layout = new GroupLayout(dialog.getContentPane());
            dialog.getContentPane().setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(label)
                            .addComponent(outletComboBox, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectButton)
            );
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addComponent(label)
                            .addComponent(outletComboBox)
                            .addComponent(selectButton)
            );

            // Aksi tombol Select
            selectButton.addActionListener(e -> {
                String selectedOutletName = (String) outletComboBox.getSelectedItem();
                if (selectedOutletName == null || selectedOutletName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select an outlet.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Ambil outlet_id berdasarkan outlet_name
                String getOutletIdSQL = "SELECT outlet_id FROM outlet WHERE outlet_name = ?";
                String outletId = null;

                try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(getOutletIdSQL)) {

                    pstmt.setString(1, selectedOutletName);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        outletId = rs.getString("outlet_id");
                    }

                    if (outletId == null || outletId.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Outlet ID not found!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Update selected_outlet di tabel users dengan outlet_id
                    String updateSQL = "UPDATE users SET selected_outlet = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                        updateStmt.setString(1, outletId); // Simpan outlet_id
                        updateStmt.setString(2, userId);
                        updateStmt.executeUpdate();

                        JOptionPane.showMessageDialog(dialog, "Outlet changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error retrieving outlet data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        ScrollPane = new javax.swing.JScrollPane();
        searchProductField = new javax.swing.JTextField();
        btnChooseOutlet = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        PaginationSpinner = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();

        jPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );

        btnChooseOutlet.setText("Choose Outlet");

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        PageStatus.setText("0/0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addComponent(searchProductField, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(18, 18, 18)
                .addComponent(btnReset)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnChooseOutlet, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PageStatus)
                .addGap(18, 18, 18)
                .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(searchProductField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSearch)
                        .addComponent(btnReset))
                    .addComponent(btnChooseOutlet, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PaginationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PageStatus))
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner PaginationSpinner;
    private javax.swing.JScrollPane ScrollPane;
    private javax.swing.JButton btnChooseOutlet;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel jPanel;
    private javax.swing.JTextField searchProductField;
    // End of variables declaration//GEN-END:variables
}
