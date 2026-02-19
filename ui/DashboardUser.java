package projeksmt2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import projeksmt2.util.DBConnection;
import projeksmt2.util.SessionManager;

import projeksmt2.panel.user.UserPanel1;
import projeksmt2.panel.user.UserPanel2;
import projeksmt2.panel.user.UserPanel3;

/**
 *
 * @author findo
 */
public class DashboardUser extends javax.swing.JFrame {

    private String currentUser;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Initialize scheduler
    private JPanel defaultPanel; // Panel untuk tampilan default

    public DashboardUser() {
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
                    checkPendingOrders(); // Check for pending orders periodically
                });
            }, 0, 3, TimeUnit.SECONDS);  // Pembaruan setiap 3 detik
        } else {
            // Jika tidak ada sesi, arahkan kembali ke login
            dispose();
            new DashboardLogin().setVisible(true);
        }

        // Tambahkan ActionListener untuk tombol
        btnHome.addActionListener(e -> loadPanelHome());
        btnProductList.addActionListener(e -> showUserPanel1());
        btnCart.addActionListener(e -> showUserPanel2());
        btnCheckout.addActionListener(e -> showUserPanel3());

        loadPanelHome();       // Buat panel default

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

    private void loadPanelHome() {
        PanelContent.removeAll();  // Hapus konten lama
        PanelContent.setLayout(new BorderLayout());  // Gunakan layout BorderLayout

        JLabel label = new JLabel("Guaranteed Product Quality, Easy Shopping, Go Home Happy!", SwingConstants.CENTER);
        label.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 24));

        PanelContent.add(label, BorderLayout.CENTER);  // Tambahkan ke tengah
        PanelContent.revalidate();
        PanelContent.repaint();
    }

    private void showUserPanel1() {
        PanelContent.removeAll(); // Hapus konten lama
        PanelContent.add(new UserPanel1()); // Tampilkan OutletPanel1
        PanelContent.revalidate();
        PanelContent.repaint();
    }

    private void showUserPanel2() {
        PanelContent.removeAll(); // Hapus konten lama
        PanelContent.add(new UserPanel2()); // Tampilkan OutletPanel2
        PanelContent.revalidate();
        PanelContent.repaint();
    }

    private void showUserPanel3() {
        PanelContent.removeAll(); // Hapus konten lama
        PanelContent.add(new UserPanel3()); // Tampilkan OutletPanel3
        PanelContent.revalidate();
        PanelContent.repaint();
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

   private void checkPendingOrders() {
    String userId = SessionManager.getCurrentUserId();
    boolean hasActiveOrder = false;

    try (Connection conn = DBConnection.getConnection()) {
        // Check for both pending and completed orders
        String sql = "SELECT COUNT(*) FROM order_transactions WHERE customer_id = ? AND (order_status = 'pending' OR order_status = 'completed')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                hasActiveOrder = rs.getInt(1) > 0;
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    // Update button visibility based on order status
    btnProductList.setVisible(!hasActiveOrder);
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
        logout = new javax.swing.JButton();
        txtBalance = new javax.swing.JLabel();
        btnEmoney = new javax.swing.JButton();
        PanelContent = new javax.swing.JPanel();
        btnProductList = new javax.swing.JButton();
        btnCart = new javax.swing.JButton();
        btnCheckout = new javax.swing.JButton();
        btnHome = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        username.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        username.setText("username");
        username.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usernameMouseClicked(evt);
            }
        });

        logout.setText("Logout");
        logout.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        txtBalance.setText("Rp.");

        btnEmoney.setText("Emoney");

        PanelContent.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout PanelContentLayout = new javax.swing.GroupLayout(PanelContent);
        PanelContent.setLayout(PanelContentLayout);
        PanelContentLayout.setHorizontalGroup(
            PanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1066, Short.MAX_VALUE)
        );
        PanelContentLayout.setVerticalGroup(
            PanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 543, Short.MAX_VALUE)
        );

        btnProductList.setText("Product List");

        btnCart.setText("Cart");

        btnCheckout.setText("Checkout");

        btnHome.setText("Home");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(PanelContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                        .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(btnEmoney)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(192, 192, 192)
                .addComponent(btnProductList, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(76, 76, 76)
                .addComponent(btnCart, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(103, 103, 103)
                .addComponent(btnCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 360, Short.MAX_VALUE)
                .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(btnHome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(275, 275, 275))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnProductList)
                            .addComponent(btnCart)
                            .addComponent(btnCheckout)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnHome))
                            .addComponent(logout))
                        .addGap(10, 10, 10)
                        .addComponent(txtBalance)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(PanelContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEmoney)))
                .addContainerGap(28, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(DashboardUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardUser().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelContent;
    private javax.swing.JButton btnCart;
    private javax.swing.JButton btnCheckout;
    private javax.swing.JButton btnEmoney;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnProductList;
    private javax.swing.JButton logout;
    private javax.swing.JLabel txtBalance;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
