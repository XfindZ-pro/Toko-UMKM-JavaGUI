/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projeksmt2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.SwingUtilities;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import projeksmt2.util.SessionManager;
import projeksmt2.util.DBConnection;
//
import projeksmt2.panel.admin.AdminPanel1;
import projeksmt2.panel.admin.AdminPanel2;
import projeksmt2.panel.admin.AdminPanel3;
import projeksmt2.panel.admin.AdminPanel4;
import projeksmt2.panel.admin.AdminPanel5;
import projeksmt2.panel.admin.AdminPanel6;
import projeksmt2.panel.admin.AdminPanel7;

/**
 *
 * @author findo
 */
public class DashboardAdmin extends javax.swing.JFrame {

    private JLabel labelTotalUsers;
    private JLabel labelTotalStaff;
    private JLabel labelTotalWarehouse;
    private JLabel labelTotalOutlet;

    private JTable tableTags;
    private JTextField inputTagName;
    private JButton btnCreateTag, btnEditTag, btnDeleteTag, btnRefreshTag;
    private DefaultTableModel modelTags;

    private JScrollPane ScrollContent;
    private JTabbedPane TabContent;

    private String currentUser;
    private JPanel homePanel; // Untuk menyimpan konten Home
   private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Initialize scheduler
    /**
     * Creates new form DashboardAdmin
     */
  public DashboardAdmin() {
      
      
    initComponents();
    if (SessionManager.isLoggedIn()) {
       String user = SessionManager.getCurrentUser();
        this.currentUser = user;
        this.username.setText(user);

      // Ambil balance dan setel ke txtBalance
        updateBalanceDisplay();  // Menampilkan saldo awal

        // Jadwalkan pembaruan balance setiap 3 detik
        scheduler.scheduleAtFixedRate(() -> {
            // Update balance secara periodik di thread UI
            SwingUtilities.invokeLater(() -> {
                updateBalanceDisplay();
            });
        }, 0, 3, TimeUnit.SECONDS);  // Pembaruan setiap 3 detik

    } else {
        // Jika tidak ada sesi, arahkan kembali ke login
        dispose();
        new DashboardLogin().setVisible(true);
    }
    // Set warna untuk panel
    Panel1.setForeground(Color.BLUE);
    Panel2.setForeground(Color.BLUE);
    Panel3.setForeground(Color.BLUE);
    Panel4.setForeground(Color.BLUE);
    Panel5.setForeground(Color.BLUE);
    Panel6.setForeground(Color.BLUE);
    Panel7.setForeground(Color.BLUE);

    // Inisialisasi ScrollContent dan TabContent sekali saja
    ScrollContent = new JScrollPane();
    TabContent = new JTabbedPane();

    // Isi tab sekali saja
    initStatisticsTab();
    initTagTab();
    ScrollContent.setViewportView(TabContent);

    // Buat homePanel dengan ScrollContent yang sudah siap
    createHomePanel();

    setupButtonActions();
    
      // Add ActionListener for btnTopUp
    btnEmoney.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // When Top Up button is clicked, navigate to DashboardEmoney
            new DashboardEmoney().setVisible(true); // Show the DashboardEmoney window
            dispose(); // Close the current DashboardAdmin window
        }
    });
}

// Method untuk memperbarui dan menampilkan saldo di txtBalance
private void updateBalanceDisplay() {
    int balance = SessionManager.getCurrentBalance();  // Ambil saldo dari SessionManager

    // Format balance sebagai mata uang Indonesia
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();  // Membuat format mata uang
    String formattedBalance = currencyFormat.format(balance);  // Format angka sebagai mata uang

    // Menampilkan balance yang sudah diformat pada komponen txtBalance
    this.txtBalance.setText(formattedBalance);
}


    private void createHomePanel() {
        if (ScrollContent == null || TabContent == null) {
            System.err.println("ScrollContent atau TabContent belum diinisialisasi!");
            return;
        }

        homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);

        TabContent.removeAll();
        initStatisticsTab();
        initTagTab();

        ScrollContent.setViewportView(TabContent);

        PanelContent.setLayout(new BorderLayout());
        PanelContent.add(ScrollContent, BorderLayout.CENTER);
    }

    /// Listener Menu
    private void setupButtonActions() {
        home.addActionListener(e -> {
            loadContentPane(homePanel);
        });

        Panel1.addActionListener(e -> {
            AdminPanel1 adminPanel = new AdminPanel1();
            loadContentPane(adminPanel);
        });

        Panel2.addActionListener(e -> {
            AdminPanel2 adminPanel2 = new AdminPanel2();
            loadContentPane(adminPanel2);
        });

        Panel3.addActionListener(e -> {
            AdminPanel3 adminPanel3 = new AdminPanel3();
            loadContentPane(adminPanel3);
        });

        Panel4.addActionListener(e -> {
            AdminPanel4 adminPanel4 = new AdminPanel4();
            loadContentPane(adminPanel4);
        });

        Panel5.addActionListener(e -> {
            AdminPanel5 adminPanel5 = new AdminPanel5();
            loadContentPane(adminPanel5);
        });

        Panel6.addActionListener(e -> {
            AdminPanel6 adminPanel6 = new AdminPanel6();
            loadContentPane(adminPanel6);
        });

        Panel7.addActionListener(e -> {
            AdminPanel7 adminPanel7 = new AdminPanel7();
            loadContentPane(adminPanel7);
        });
    }

    private void loadContentPane(JPanel panel) {
        if (PanelContent == null) {
            System.err.println("PanelContent belum diinisialisasi!");
            return;
        }

        PanelContent.removeAll();

        if (panel == homePanel) {
            // Jika kembali ke home, pastikan TabContent di-refresh
            initTabContent();
            PanelContent.add(ScrollContent, BorderLayout.CENTER);
        } else {
            // Jika panel lain, tambahkan langsung
            PanelContent.add(panel, BorderLayout.CENTER);
        }

        PanelContent.revalidate();
        PanelContent.repaint();
    }

    private void initTabContent() {
        TabContent.removeAll();
        initStatisticsTab();
        initTagTab();
        ScrollContent.setViewportView(TabContent);
    }

    //~~~
    private void initStatisticsTab() {
        JPanel statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new BoxLayout(statisticsPanel, BoxLayout.Y_AXIS));
        statisticsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statisticsPanel.setBackground(Color.WHITE);

        labelTotalUsers = new JLabel("Total Users: ");
        labelTotalStaff = new JLabel("Total Staff: ");
        labelTotalWarehouse = new JLabel("Total Warehouse: ");
        labelTotalOutlet = new JLabel("Total Outlet: ");

        statisticsPanel.add(labelTotalUsers);
        statisticsPanel.add(labelTotalStaff);
        statisticsPanel.add(labelTotalWarehouse);
        statisticsPanel.add(labelTotalOutlet);

        updateStatistics();

        TabContent.addTab("Statistics", statisticsPanel);
    }

    private void updateStatistics() {
        try (Connection conn = DBConnection.getConnection()) {
            String queryUser = "SELECT COUNT(*) FROM users WHERE role IN ('admin', 'warehouse_manager', 'outlet_manager')";
            String queryStaff = "SELECT COUNT(*) FROM users WHERE role IN ('warehouse_manager', 'outlet_manager')";
            String queryWarehouse = "SELECT COUNT(*) FROM warehouse";
            String queryOutlet = "SELECT COUNT(*) FROM outlet";

            Statement stmt = conn.createStatement();
            ResultSet rs;

            rs = stmt.executeQuery(queryUser);
            if (rs.next()) {
                labelTotalUsers.setText("Total Users: " + rs.getInt(1));
            }

            rs = stmt.executeQuery(queryStaff);
            if (rs.next()) {
                labelTotalStaff.setText("Total Staff: " + rs.getInt(1));
            }

            rs = stmt.executeQuery(queryWarehouse);
            if (rs.next()) {
                labelTotalWarehouse.setText("Total Warehouse: " + rs.getInt(1));
            }

            rs = stmt.executeQuery(queryOutlet);
            if (rs.next()) {
                labelTotalOutlet.setText("Total Outlet: " + rs.getInt(1));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initTagTab() {
        JPanel tagPanel = new JPanel(new BorderLayout());
        tagPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tagPanel.setBackground(Color.WHITE);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnReset = new JButton("Reset");

        searchPanel.add(new JLabel("Search Tag: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        // Form Input
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputTagName = new JTextField(20);
        btnCreateTag = new JButton("Create");
        btnEditTag = new JButton("Edit");
        btnDeleteTag = new JButton("Delete");
        btnRefreshTag = new JButton("Refresh");

        formPanel.add(new JLabel("Tag Name: "));
        formPanel.add(inputTagName);
        formPanel.add(btnCreateTag);
        formPanel.add(btnEditTag);
        formPanel.add(btnDeleteTag);
        formPanel.add(btnRefreshTag);

        // Combine search and form panels
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(searchPanel);
        topPanel.add(formPanel);

        // Tabel Tags
        String[] columns = {"Tag ID", "Tag Name"};
        modelTags = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        tableTags = new JTable(modelTags);
        JScrollPane scrollPane = new JScrollPane(tableTags);

        tagPanel.add(topPanel, BorderLayout.NORTH);
        tagPanel.add(scrollPane, BorderLayout.CENTER);

        TabContent.addTab("Tag", tagPanel);

        loadTags();

        // Action Listeners
        btnCreateTag.addActionListener(e -> createTag());
        btnEditTag.addActionListener(e -> editTag());
        btnDeleteTag.addActionListener(e -> deleteTag());
        btnRefreshTag.addActionListener(e -> loadTags());

        // Search functionality
        btnSearch.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchTags(searchTerm);
            } else {
                loadTags();
            }
        });

        btnReset.addActionListener(e -> {
            searchField.setText("");
            loadTags();
        });
    }

    private void searchTags(String searchTerm) {
        modelTags.setRowCount(0);  // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT tag_id, tag_name FROM tags WHERE tag_name LIKE ?");
            ps.setString(1, "%" + searchTerm + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelTags.addRow(new Object[]{
                    rs.getString("tag_id"),
                    rs.getString("tag_name")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to search tags",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateTagId() {
        String id;
        do {
            int random = (int) (Math.random() * 90000) + 10000;
            id = "tg" + random;
        } while (isTagIdExists(id));
        return id;
    }

    private boolean isTagIdExists(String id) {
        for (int i = 0; i < modelTags.getRowCount(); i++) {
            if (modelTags.getValueAt(i, 0).equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void loadTags() {
        modelTags.setRowCount(0);  // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT tag_id, tag_name FROM tags");
            while (rs.next()) {
                modelTags.addRow(new Object[]{rs.getString("tag_id"), rs.getString("tag_name")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load tags", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTag() {
        String tagName = inputTagName.getText().trim();
        if (tagName.isEmpty()) {
            return;
        }

        String tagId = generateTagId();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO tags (tag_id, tag_name) VALUES (?, ?)");
            ps.setString(1, tagId);
            ps.setString(2, tagName);
            ps.executeUpdate();
            loadTags();
            inputTagName.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void editTag() {
        int row = tableTags.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a tag to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tagId = (String) modelTags.getValueAt(row, 0);
        String oldName = (String) modelTags.getValueAt(row, 1);

        // Buat field input di dialog
        JTextField txtTagName = new JTextField(oldName);
        Object[] message = {
            "Tag Name:", txtTagName
        };

        // Tampilkan dialog
        int option = JOptionPane.showConfirmDialog(this, message, "Edit Tag", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = txtTagName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tag name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Simpan ke database
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE tags SET tag_name = ? WHERE tag_id = ?");
                ps.setString(1, newName);
                ps.setString(2, tagId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Tag updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTags(); // Refresh tabel
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update tag.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTag() {
        int row = tableTags.getSelectedRow();
        if (row == -1) {
            return;
        }

        String tagId = (String) modelTags.getValueAt(row, 0);

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM tags WHERE tag_id = ?");
            ps.setString(1, tagId);
            ps.executeUpdate();
            loadTags();
        } catch (Exception ex) {
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

        logout = new javax.swing.JButton();
        Panel1 = new javax.swing.JButton();
        Panel2 = new javax.swing.JButton();
        Panel3 = new javax.swing.JButton();
        Panel4 = new javax.swing.JButton();
        Panel5 = new javax.swing.JButton();
        Panel6 = new javax.swing.JButton();
        Panel7 = new javax.swing.JButton();
        home = new javax.swing.JButton();
        username = new javax.swing.JLabel();
        PanelContent = new javax.swing.JPanel();
        txtBalance = new javax.swing.JLabel();
        btnEmoney = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        logout.setText("Logout");
        logout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        Panel1.setText("Manage Users");
        Panel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Panel2.setText("Manage Products");
        Panel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Panel3.setText("Manage Suppliers");
        Panel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        Panel3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Panel3ActionPerformed(evt);
            }
        });

        Panel4.setText("Manage Outlet");
        Panel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        Panel4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Panel4ActionPerformed(evt);
            }
        });

        Panel5.setText("Manage Warehouse");
        Panel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Panel6.setText("E-money");
        Panel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Panel7.setText("Log Activity");
        Panel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        Panel7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Panel7ActionPerformed(evt);
            }
        });

        home.setText("home");

        username.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        username.setForeground(new java.awt.Color(255, 51, 51));
        username.setText("username");
        username.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usernameMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout PanelContentLayout = new javax.swing.GroupLayout(PanelContent);
        PanelContent.setLayout(PanelContentLayout);
        PanelContentLayout.setHorizontalGroup(
            PanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 971, Short.MAX_VALUE)
        );
        PanelContentLayout.setVerticalGroup(
            PanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 453, Short.MAX_VALUE)
        );

        txtBalance.setText("Rp.");

        btnEmoney.setText("Emoney");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logout)
                .addGap(18, 18, 18)
                .addComponent(username)
                .addGap(257, 257, 257))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(home)
                        .addGap(739, 739, 739)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEmoney)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Panel4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Panel5, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(Panel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Panel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Panel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Panel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Panel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(40, 40, 40)
                        .addComponent(PanelContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(89, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(username)
                    .addComponent(logout))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(home))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(txtBalance)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEmoney)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(Panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Panel2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Panel3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Panel4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Panel5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Panel6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Panel7, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(PanelContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(107, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        SessionManager.logout(); // Reset sesi pengguna
        this.dispose(); // Tutup DashboardUser
        new DashboardLogin().setVisible(true); // Kembali ke halaman login
    }//GEN-LAST:event_logoutActionPerformed

    private void usernameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usernameMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameMouseClicked

    private void Panel7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Panel7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Panel7ActionPerformed

    private void Panel3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Panel3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Panel3ActionPerformed

    private void Panel4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Panel4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Panel4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DashboardAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardAdmin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Panel1;
    private javax.swing.JButton Panel2;
    private javax.swing.JButton Panel3;
    private javax.swing.JButton Panel4;
    private javax.swing.JButton Panel5;
    private javax.swing.JButton Panel6;
    private javax.swing.JButton Panel7;
    private javax.swing.JPanel PanelContent;
    private javax.swing.JButton btnEmoney;
    private javax.swing.JButton home;
    private javax.swing.JButton logout;
    private javax.swing.JLabel txtBalance;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
