/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projeksmt2.panel.outlet;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;

/**
 *
 * @author findo
 */
public class OutletPanel2 extends javax.swing.JPanel {

    private static final int ITEMS_PER_PAGE = 10;
    private DefaultTableModel tableModel;
    private JTable stockTable;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchTerm = "";

    /**
     * Creates new form OutletPanel2
     */
    public OutletPanel2() {
        initComponents();
        initStockTab();
        loadStockData();
        setupButtonListeners();
    }

    private void initStockTab() {
        // Create table model
        String[] columns = {"Product Name", "Product Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create scroll pane and add table
        JScrollPane scrollPane = new JScrollPane(stockTable);

        // Create panel for tab
        JPanel stockPanel = new JPanel(new BorderLayout());
        stockPanel.add(scrollPane, BorderLayout.CENTER);

        // Add tab
        TabStock.addTab("Stocks", stockPanel);

        // Set up pagination spinner listener
        SpinnerPagination.addChangeListener(e -> {
            currentPage = (int) SpinnerPagination.getValue();
            loadStockData();
        });
    }

    private void setupButtonListeners() {
        btnSearch.addActionListener(e -> {
            currentSearchTerm = searchField.getText().trim();
            currentPage = 1; // Reset to first page when searching
            loadStockData();
        });

        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentSearchTerm = "";
            currentPage = 1; // Reset to first page
            loadStockData();
        });
    }

    private String getOutletIdForCurrentUser() {
        String userId = SessionManager.getCurrentUserId();
        String query = "SELECT outlet_id FROM outlet WHERE outlet_manager = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("outlet_id") : null;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void loadStockData() {
        String outletId = getOutletIdForCurrentUser();
        if (outletId == null) {
            JOptionPane.showMessageDialog(this,
                    "No outlet assigned",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Calculate offset for pagination
        int offset = (currentPage - 1) * ITEMS_PER_PAGE;

        try (Connection conn = DBConnection.getConnection()) {
            // Build base query parts
            String countBaseQuery = "SELECT COUNT(*) FROM outlet_product op "
                    + "JOIN product p ON op.product_id = p.product_id "
                    + "WHERE op.outlet_id = ?";

            String dataBaseQuery = "SELECT p.product_name, p.product_price, op.stock "
                    + "FROM outlet_product op "
                    + "JOIN product p ON op.product_id = p.product_id "
                    + "WHERE op.outlet_id = ?";

            // Add search condition if search term exists
            if (!currentSearchTerm.isEmpty()) {
                String searchCondition = " AND p.product_name LIKE ?";
                countBaseQuery += searchCondition;
                dataBaseQuery += searchCondition;
            }

            // First query to get total count
            int totalItems = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(countBaseQuery)) {
                pstmt.setString(1, outletId);
                if (!currentSearchTerm.isEmpty()) {
                    pstmt.setString(2, "%" + currentSearchTerm + "%");
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totalItems = rs.getInt(1);
                }
            }

            // Calculate total pages
            totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            if (totalPages == 0) {
                totalPages = 1;
            }

            // Update pagination controls
            SpinnerPagination.setModel(new SpinnerNumberModel(
                    currentPage, 1, totalPages, 1));
            PageStatus.setText(currentPage + "/" + totalPages);

            // Clear existing data
            tableModel.setRowCount(0);

            // Main query with pagination
            dataBaseQuery += " LIMIT ? OFFSET ?";

            try (PreparedStatement pstmt = conn.prepareStatement(dataBaseQuery)) {
                int paramIndex = 1;
                pstmt.setString(paramIndex++, outletId);

                if (!currentSearchTerm.isEmpty()) {
                    pstmt.setString(paramIndex++, "%" + currentSearchTerm + "%");
                }

                pstmt.setInt(paramIndex++, ITEMS_PER_PAGE);
                pstmt.setInt(paramIndex, offset);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("product_name"),
                        rs.getInt("product_price"),
                        rs.getInt("stock")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading stock data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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

        SpinnerPagination = new javax.swing.JSpinner();
        PageStatus = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        TabStock = new javax.swing.JTabbedPane();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        PageStatus.setText("0/0");

        jScrollPane1.setViewportView(TabStock);

        btnSearch.setText("Search");

        btnReset.setText("Reset");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(428, 428, 428)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(18, 18, 18)
                .addComponent(btnReset)
                .addContainerGap(145, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(517, 517, 517)
                .addComponent(PageStatus)
                .addGap(18, 18, 18)
                .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 785, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SpinnerPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PageStatus))
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PageStatus;
    private javax.swing.JSpinner SpinnerPagination;
    private javax.swing.JTabbedPane TabStock;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
