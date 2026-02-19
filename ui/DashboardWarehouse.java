/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projeksmt2.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import projeksmt2.util.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import projeksmt2.util.DBConnection;

import projeksmt2.panel.warehouse.PanelWarehouse1;
import projeksmt2.panel.warehouse.PanelWarehouse2;

/**
 *
 * @author findo
 */
public class DashboardWarehouse extends javax.swing.JFrame {
    private String currentUser;
 private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Initialize scheduler
    /**
     * Creates new form DashboardWarehouse
     */
    public DashboardWarehouse() {
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

        
           loadWarehouseName();
    } else {
        // Jika tidak ada sesi, arahkan kembali ke login
        dispose();
        new DashboardLogin().setVisible(true);
    }
           // Add ActionListener for btnTopUp
    btnEmoney.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // When Top Up button is clicked, navigate to DashboardTopUp
            new DashboardEmoney().setVisible(true); // Show the DashboardEmoney window
            dispose(); // Close the current DashboardAdmin window
        }
    });
    
     // Add ActionListener for buttons
    btnHome.addActionListener(e -> loadWarehouseName()); // Kembali ke tampilan default
    btnBuy.addActionListener(e -> showWarehousePanel1()); // Tampilkan WarehousePanel1
    btnStock.addActionListener(e -> showWarehousePanel2()); // Tampilkan WarehousePanel2
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

private void loadWarehouseName() {
    String userId = SessionManager.getCurrentUserId();
    String sql = "SELECT warehouse_name FROM warehouse WHERE warehouse_manager = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();

        PanelBoard.removeAll();  // Hapus konten lama
        PanelBoard.setLayout(new BorderLayout());  // Atur layout

        if (rs.next()) {
            String warehouseName = rs.getString("warehouse_name");
            JLabel label = new JLabel("Warehouse: " + warehouseName, SwingConstants.CENTER);
            label.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 24));
            PanelBoard.add(label, BorderLayout.CENTER);  // Tambahkan ke tengah
        } else {
            JLabel label = new JLabel("No Warehouse Assigned", SwingConstants.CENTER);
            PanelBoard.add(label, BorderLayout.CENTER);
        }

        PanelBoard.revalidate();
        PanelBoard.repaint();

    } catch (SQLException ex) {
        JLabel errorLabel = new JLabel("Error loading warehouse", SwingConstants.CENTER);
        PanelBoard.add(errorLabel, BorderLayout.CENTER);
        PanelBoard.revalidate();
    }
}

private void showWarehousePanel1() {
    PanelBoard.removeAll(); // Hapus konten lama
    PanelBoard.add(new PanelWarehouse1()); // Tampilkan WarehousePanel1
    PanelBoard.revalidate();
    PanelBoard.repaint();
}

private void showWarehousePanel2() {
    PanelBoard.removeAll(); // Hapus konten lama
    PanelBoard.add(new PanelWarehouse2()); // Tampilkan WarehousePanel2
    PanelBoard.revalidate();
    PanelBoard.repaint();
}
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        username = new javax.swing.JLabel();
        txtBalance = new javax.swing.JLabel();
        btnEmoney = new javax.swing.JButton();
        logout = new javax.swing.JButton();
        PanelBoard = new javax.swing.JPanel();
        btnHome = new javax.swing.JButton();
        btnBuy = new javax.swing.JButton();
        btnStock = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        username.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        username.setForeground(new java.awt.Color(255, 51, 51));
        username.setText("username");
        username.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usernameMouseClicked(evt);
            }
        });

        txtBalance.setText("Rp.");

        btnEmoney.setText("Emoney");

        logout.setText("Logout");
        logout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        PanelBoard.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout PanelBoardLayout = new javax.swing.GroupLayout(PanelBoard);
        PanelBoard.setLayout(PanelBoardLayout);
        PanelBoardLayout.setHorizontalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        PanelBoardLayout.setVerticalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        btnHome.setText("Home");

        btnBuy.setText("Buy Stock");

        btnStock.setText("Stock");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(btnHome))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(111, 111, 111)
                        .addComponent(btnBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(btnStock, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(logout)
                        .addGap(18, 18, 18)
                        .addComponent(username)
                        .addGap(12, 12, 12))
                    .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmoney))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(224, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(username)
                    .addComponent(logout)
                    .addComponent(btnHome))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBalance)
                    .addComponent(btnBuy)
                    .addComponent(btnStock))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEmoney)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void usernameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usernameMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameMouseClicked

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        SessionManager.logout(); // Reset sesi pengguna
        this.dispose(); // Tutup DashboardUser
        new DashboardLogin().setVisible(true); // Kembali ke halaman login
    }//GEN-LAST:event_logoutActionPerformed

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
            java.util.logging.Logger.getLogger(DashboardWarehouse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardWarehouse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardWarehouse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardWarehouse.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardWarehouse().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelBoard;
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnEmoney;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnStock;
    private javax.swing.JButton logout;
    private javax.swing.JLabel txtBalance;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
