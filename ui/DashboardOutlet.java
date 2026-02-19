/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projeksmt2.ui;

import projeksmt2.util.SessionManager;

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

import projeksmt2.panel.outlet.OutletPanel1;
import projeksmt2.panel.outlet.OutletPanel2;
import projeksmt2.panel.outlet.OutletPanel3;

/**
 *
 * @author findo
 */
public class DashboardOutlet extends javax.swing.JFrame {

    private String currentUser;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Initialize scheduler

    /**
     * Creates new form DashboardOutlet
     */
    public DashboardOutlet() {
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
        
          // Tambahkan ActionListener untuk tombol
        btnHome.addActionListener(e -> loadOutletName());
        btnManageStock.addActionListener(e -> showOutletPanel1());
        btnStock.addActionListener(e -> showOutletPanel2());
        btnTransaction.addActionListener(e -> showOutletPanel3());
        
        loadOutletName();
        
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

    // Method untuk mengambil outlet name berdasarkan outlet_manager
    private void loadOutletName() {
        String userId = SessionManager.getCurrentUserId();
        String sql = "SELECT outlet_name FROM outlet WHERE outlet_manager = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            PanelBoard.removeAll();  // Hapus konten lama
            PanelBoard.setLayout(new BorderLayout());  // Atur layout

            if (rs.next()) {
                String outletName = rs.getString("outlet_name");
                JLabel label = new JLabel("Outlet: " + outletName, SwingConstants.CENTER);
                label.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 24));
                PanelBoard.add(label, BorderLayout.CENTER);  // Tambahkan ke tengah
            } else {
                JLabel label = new JLabel("No Outlet Assigned", SwingConstants.CENTER);
                PanelBoard.add(label, BorderLayout.CENTER);
            }

            PanelBoard.revalidate();
            PanelBoard.repaint();

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading outlet name", SwingConstants.CENTER);
            PanelBoard.add(errorLabel, BorderLayout.CENTER);
            PanelBoard.revalidate();
        }
    }

    private void showOutletPanel1() {
        PanelBoard.removeAll(); // Hapus konten lama
        PanelBoard.add(new OutletPanel1()); // Tampilkan OutletPanel1
        PanelBoard.revalidate();
        PanelBoard.repaint();
    }

    private void showOutletPanel2() {
        PanelBoard.removeAll(); // Hapus konten lama
        PanelBoard.add(new OutletPanel2()); // Tampilkan OutletPanel2
        PanelBoard.revalidate();
        PanelBoard.repaint();
    }

    private void showOutletPanel3() {
        PanelBoard.removeAll(); // Hapus konten lama
        PanelBoard.add(new OutletPanel3()); // Tampilkan OutletPanel3
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

        PanelBoard = new javax.swing.JPanel();
        username = new javax.swing.JLabel();
        txtBalance = new javax.swing.JLabel();
        btnEmoney = new javax.swing.JButton();
        logout = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();
        btnManageStock = new javax.swing.JButton();
        btnStock = new javax.swing.JButton();
        btnTransaction = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PanelBoard.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout PanelBoardLayout = new javax.swing.GroupLayout(PanelBoard);
        PanelBoard.setLayout(PanelBoardLayout);
        PanelBoardLayout.setHorizontalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 980, Short.MAX_VALUE)
        );
        PanelBoardLayout.setVerticalGroup(
            PanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 440, Short.MAX_VALUE)
        );

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

        btnHome.setText("Home");

        btnManageStock.setText("Manage Stock");

        btnStock.setText("Stocks");

        btnTransaction.setText("Transaction");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(251, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(btnHome))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(153, 153, 153)
                        .addComponent(btnManageStock, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(btnStock, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(btnTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 457, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(logout)
                        .addGap(18, 18, 18)
                        .addComponent(username)
                        .addGap(12, 12, 12))
                    .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmoney))
                .addGap(36, 36, 36))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(username)
                    .addComponent(logout)
                    .addComponent(btnHome))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(txtBalance)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEmoney)
                        .addGap(18, 18, 18)
                        .addComponent(PanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnManageStock)
                            .addComponent(btnStock)
                            .addComponent(btnTransaction))))
                .addContainerGap(52, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(DashboardOutlet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardOutlet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardOutlet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardOutlet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardOutlet().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelBoard;
    private javax.swing.JButton btnEmoney;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnManageStock;
    private javax.swing.JButton btnStock;
    private javax.swing.JButton btnTransaction;
    private javax.swing.JButton logout;
    private javax.swing.JLabel txtBalance;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
