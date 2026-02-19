/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projeksmt2.ui;

import java.awt.BorderLayout;
import projeksmt2.util.SessionManager;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import projeksmt2.util.DBConnection;

/**
 *
 * @author findo
 */
public class DashboardEmoney extends javax.swing.JFrame {
        private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Initialize scheduler
    /**
     * Creates new form Frame
     */
    public DashboardEmoney() {
        initComponents();
            if (SessionManager.isLoggedIn()) {
    

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
        
    btnSend.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        showSendBalanceDialog();
    }
});
            
    }
    
    private void showSendBalanceDialog() {
    // Komponen input
    JTextField txtUsername = new JTextField(20);
    JTextField txtAmount = new JTextField(10);

    // Panel form
    JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
    panel.add(new JLabel("Enter Username:"));
    panel.add(txtUsername);
    panel.add(new JLabel("Enter Amount to Send:"));
    panel.add(txtAmount);

    // Dialog
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Send Balance", true);
    JButton btnConfirm = new JButton("Confirm");
    JButton btnCancel = new JButton("Cancel");

    // Event Confirm
    btnConfirm.addActionListener(e -> {
        String recipientUsername = txtUsername.getText().trim();
        int amountToSend;

        try {
            amountToSend = Integer.parseInt(txtAmount.getText().trim());
            if (amountToSend <= 0) {
                JOptionPane.showMessageDialog(dialog, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Please enter a valid number for the amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = SessionManager.getCurrentUserId(); // ID pengirim
        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String getRecipientIdQuery = "SELECT id FROM users WHERE username = ?";
        String getSenderBalanceQuery = "SELECT balance FROM emoney WHERE user_id = ?";
        String deductBalanceQuery = "UPDATE emoney SET balance = balance - ? WHERE user_id = ?";
        String addBalanceQuery = "UPDATE emoney SET balance = balance + ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            String recipientId = null;

            // Cari user ID penerima
            try (PreparedStatement pstmtRecipient = conn.prepareStatement(getRecipientIdQuery)) {
                pstmtRecipient.setString(1, recipientUsername);
                ResultSet rs = pstmtRecipient.executeQuery();
                if (rs.next()) {
                    recipientId = rs.getString("id");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Username tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Dapatkan saldo pengirim
            int senderBalance = 0;
            try (PreparedStatement pstmtSender = conn.prepareStatement(getSenderBalanceQuery)) {
                pstmtSender.setString(1, userId);
                ResultSet rs = pstmtSender.executeQuery();
                if (rs.next()) {
                    senderBalance = rs.getInt("balance");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Data pengirim tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Validasi saldo
            if (senderBalance < amountToSend) {
                JOptionPane.showMessageDialog(dialog, "Saldo tidak mencukupi.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update saldo pengirim
            try (PreparedStatement pstmtDeduct = conn.prepareStatement(deductBalanceQuery)) {
                pstmtDeduct.setInt(1, amountToSend);
                pstmtDeduct.setString(2, userId);
                pstmtDeduct.executeUpdate();
            }

            // Update saldo penerima
            try (PreparedStatement pstmtAdd = conn.prepareStatement(addBalanceQuery)) {
                pstmtAdd.setInt(1, amountToSend);
                pstmtAdd.setString(2, recipientId);
                pstmtAdd.executeUpdate();
            }

            // Update tampilan UI
            updateBalanceDisplay();

            JOptionPane.showMessageDialog(dialog, "Berhasil mengirim " + amountToSend + " ke " + recipientUsername);
            dialog.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Gagal mengirim saldo: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    });

    // Event Cancel
    btnCancel.addActionListener(e -> dialog.dispose());

    // Layout dialog
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.add(btnConfirm);
    buttonPanel.add(btnCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(panel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
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
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnTopUp = new javax.swing.JButton();
        btnStatus = new javax.swing.JButton();
        btnConfirmation = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        TextPaneInfo = new javax.swing.JTextPane();
        btnBack = new javax.swing.JButton();
        LabelStatus = new javax.swing.JLabel();
        jPanelBoard = new javax.swing.JPanel();
        txtBalance = new javax.swing.JLabel();
        timeRemainingTxt = new javax.swing.JLabel();
        btnSend = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnTopUp.setText("Top Up");
        btnTopUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTopUpActionPerformed(evt);
            }
        });

        btnStatus.setText("Status (Refresh)");
        btnStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatusActionPerformed(evt);
            }
        });

        btnConfirmation.setText("Confirmation");
        btnConfirmation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmationActionPerformed(evt);
            }
        });

        jScrollPane.setViewportView(TextPaneInfo);

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        LabelStatus.setBackground(new java.awt.Color(255, 255, 255));

        jPanelBoard.setBackground(new java.awt.Color(255, 255, 255));
        jPanelBoard.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txtBalance.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        txtBalance.setText("Rp.");

        javax.swing.GroupLayout jPanelBoardLayout = new javax.swing.GroupLayout(jPanelBoard);
        jPanelBoard.setLayout(jPanelBoardLayout);
        jPanelBoardLayout.setHorizontalGroup(
            jPanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBoardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(112, Short.MAX_VALUE))
        );
        jPanelBoardLayout.setVerticalGroup(
            jPanelBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBoardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(174, Short.MAX_VALUE))
        );

        timeRemainingTxt.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        btnSend.setText("Send Money");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(153, 153, 153)
                        .addComponent(timeRemainingTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(btnBack))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(147, 147, 147)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnTopUp)
                                .addGap(61, 61, 61)
                                .addComponent(btnStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(btnConfirmation, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(447, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStatus)
                    .addComponent(btnTopUp)
                    .addComponent(btnConfirmation))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(LabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(timeRemainingTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23)
                        .addComponent(btnSend)
                        .addGap(18, 18, 18)
                        .addComponent(jPanelBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
       // Ambil role pengguna saat ini dari SessionManager
    String role = SessionManager.getCurrentRole();
    
    // Tutup form DashboardTopUp
    this.dispose();

    // Arahkan ke dashboard sesuai dengan role pengguna
    if ("admin".equalsIgnoreCase(role)) {
        new DashboardAdmin().setVisible(true); // Arahkan ke DashboardAdmin
    } else if ("warehouse_manager".equalsIgnoreCase(role)) {
        new DashboardWarehouse().setVisible(true); // Arahkan ke DashboardWarehouse
    } else if ("outlet_manager".equalsIgnoreCase(role)) {
        new DashboardOutlet().setVisible(true); // Arahkan ke DashboardOutlet
    } else if ("user".equalsIgnoreCase(role)) {
        new DashboardUser().setVisible(true); // Arahkan ke DashboardUser
    }
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnTopUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTopUpActionPerformed
      
    // Fetch topup_status from the database
    String userId = SessionManager.getCurrentUserId();
    String query = "SELECT topup_status FROM emoney WHERE user_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String topUpStatus = rs.getString("topup_status");

            // Only allow top-up when status is "idle"
            if ("idle".equalsIgnoreCase(topUpStatus)) {
                // Show input dialog for top-up amount
                String amountInput = JOptionPane.showInputDialog(this, "Enter Top Up Amount:", "Top Up", JOptionPane.PLAIN_MESSAGE);

                if (amountInput != null && !amountInput.isEmpty()) {
                    try {
                        int topUpAmount = Integer.parseInt(amountInput);

                        // Ensure it's a valid integer and no duplicate top-up amount
                        if (topUpAmount <= 0) {
                            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Add a random amount between 1 and 500
                        Random rand = new Random();
                        int randomAddition = rand.nextInt(500) + 1;
                        int finalAmount = topUpAmount + randomAddition;

                        // Check if the top-up amount already exists in the table
                        if (isTopUpAmountExists(finalAmount)) {
                            JOptionPane.showMessageDialog(this, "Top-up amount already exists, please try again.", "Duplicate Amount", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Proceed with updating the emoney table
                        updateTopUp(finalAmount);
                        JOptionPane.showMessageDialog(this, "Check Button Status for procedure", "Top Up Success", JOptionPane.INFORMATION_MESSAGE);

                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                // If top-up status is not "idle", show an error message
                JOptionPane.showMessageDialog(this, "Top-up is not allowed because previous activity still exists.", "Invalid Action", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "An error occurred while fetching top-up status.", "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnTopUpActionPerformed

        private boolean isTopUpAmountExists(int amount) {
        String query = "SELECT * FROM emoney WHERE topup_amount = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, amount);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // If any row is found, the amount already exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
        
          private void updateTopUp(int amount) {
        String userId = SessionManager.getCurrentUserId();
        String updateQuery = "UPDATE emoney SET topup_status = 'pending', topup_amount = ?, topup_time = DATE_ADD(NOW(), INTERVAL 5 MINUTE) WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, amount);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void btnStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatusActionPerformed
     String userId = SessionManager.getCurrentUserId();
    String query = "SELECT * FROM emoney WHERE user_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String topUpStatus = rs.getString("topup_status");
            String topUpAmount = rs.getString("topup_amount");
            String topUpTime = rs.getString("topup_time");

            // Check if the top-up has exceeded 5 minutes from the current time
            long timeRemaining = getTimeRemaining(topUpTime);

            if ("idle".equalsIgnoreCase(topUpStatus) || timeRemaining <= 0) {
                // Show "No Activity" if topup_status is idle or if time has passed 5 minutes
                showNoActivityDialog();
                LabelStatus.setText("Click the top up button to top up your balance");
            } else {
                // If topup_status is "pending", display top-up details
                if ("pending".equalsIgnoreCase(topUpStatus)) {
                  
                    LabelStatus.setText("Top-Up Amount: " + topUpAmount );

                    // Open a pop-up dialog to show the QR code image
                    showImageDialog();

                    // Add timer logic here to countdown until the time remaining
                    startCountdownTimer(timeRemaining);

                    // Display message in TextPaneInfo
                    TextPaneInfo.setText("Please click button confirmation if already paid");
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    }//GEN-LAST:event_btnStatusActionPerformed

    // Method to show the "No Activity" dialog
private void showNoActivityDialog() {
    JDialog noActivityDialog = new JDialog(this, "No Activity", true);
    noActivityDialog.setSize(300, 200);
    noActivityDialog.setLayout(new BorderLayout());

    JLabel label = new JLabel("No Activity", JLabel.CENTER);
    label.setFont(new java.awt.Font("Segoe UI", 0, 18)); // Set font size
    label.setForeground(Color.RED);
    
    noActivityDialog.add(label, BorderLayout.CENTER);
    
    // Close button for the dialog
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> noActivityDialog.dispose());
    noActivityDialog.add(closeButton, BorderLayout.SOUTH);

    // Center the dialog and make it visible
    noActivityDialog.setLocationRelativeTo(this);
    noActivityDialog.setVisible(true);
}
// Method to show the QR code image in a pop-up JDialog with a JScrollPane
private void showImageDialog() {
    // Create the JDialog with updated size (smaller dialog)
    JDialog imageDialog = new JDialog(this, "QR Code Image", true);
    
    // Set the size of the dialog (appropriate size, not too large)
    imageDialog.setSize(600, 800); // Adjusted dialog size

    // Set the layout of the dialog
    imageDialog.setLayout(new BorderLayout());
    
    // Create a label to display the QR code image
    JLabel imageLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/projeksmt2/images/feature/Qris_XfindZ.jpg")));
    
    // Adjust the size of the image if it is too large for the dialog
    ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();
    Image image = imageIcon.getImage();
    Image scaledImage = image.getScaledInstance(600, 800, Image.SCALE_SMOOTH); // Scale to fit the dialog
    imageLabel.setIcon(new ImageIcon(scaledImage));
    
    // Add the image label to a JScrollPane for scrolling if the image exceeds the dialog size
    JScrollPane imageScrollPane = new JScrollPane(imageLabel);
    imageScrollPane.setPreferredSize(new java.awt.Dimension(780, 900)); // Scroll pane size

    // Add the JScrollPane to the dialog
    imageDialog.add(imageScrollPane, BorderLayout.CENTER);
    
    // Add a close button to the dialog
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> imageDialog.dispose()); // Ensure dispose is called only once
    imageDialog.add(closeButton, BorderLayout.SOUTH);
    
    // Center the dialog on the screen
    imageDialog.setLocationRelativeTo(this); // Center dialog relative to the parent window
    
    // Make the dialog visible
    imageDialog.setVisible(true);
}


// Method to calculate the remaining time until the top-up time
private long getTimeRemaining(String topUpTime) {
    // Parse topUpTime into a Date object
    try {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date topUpDate = sdf.parse(topUpTime);
        java.util.Date currentDate = new java.util.Date();

        // Returns time remaining in milliseconds
        return topUpDate.getTime() - currentDate.getTime();
    } catch (java.text.ParseException e) {
        e.printStackTrace();
    }
    return 0;
}

private void startCountdownTimer(long timeRemaining) {
    final int oneSecond = 1000; // 1 second in milliseconds
    
    // Use an array to wrap timeRemaining so it can be updated in the inner class
    final long[] remainingTime = {timeRemaining};

    // Create a timer to update the remaining time
    javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (remainingTime[0] <= 0) {
                ((javax.swing.Timer) e.getSource()).stop(); // Stop the timer when time is up
                TextPaneInfo.setText("Top-up process completed.");
                timeRemainingTxt.setText("");  // Clear the timeRemainingTxt when the countdown is finished
            } else {
                long secondsRemaining = remainingTime[0] / 1000; // Convert from milliseconds to seconds
                long minutesRemaining = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                timeRemainingTxt.setText("Time Remaining: " + minutesRemaining + "m " + seconds + "s");
                remainingTime[0] -= oneSecond; // Decrease remaining time by 1 second
            }
        }
    });
    timer.start(); // Start the countdown
}



    
    private void btnConfirmationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmationActionPerformed
      String userId = SessionManager.getCurrentUserId();

    // Query untuk mendapatkan topup_status dari emoney berdasarkan user_id
    String query = "SELECT topup_status FROM emoney WHERE user_id = ?";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String topUpStatus = rs.getString("topup_status");

            // Validasi apakah status adalah 'pending'
            if ("pending".equalsIgnoreCase(topUpStatus)) {
                // Lanjutkan proses update ke 'checking'
                String updateQuery = "UPDATE emoney SET topup_status = 'checking' WHERE user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, userId);
                    updateStmt.executeUpdate();

                    // Tampilkan pesan sukses
                    TextPaneInfo.setText("Your top-up will be processed shortly.");
                }
            } else {
                // Tampilkan pesan error jika status bukan 'pending'
                JOptionPane.showMessageDialog(this,
                        "Top-up confirmation can only be done when status is 'pending'.",
                        "Invalid Status",
                        JOptionPane.WARNING_MESSAGE);
                TextPaneInfo.setText("Confirmation failed: Status is not 'pending'.");
            }
        } else {
            // Jika tidak ada data ditemukan
            JOptionPane.showMessageDialog(this,
                    "No e-money account found for this user.",
                    "Data Not Found",
                    JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "An error occurred while checking top-up status: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        TextPaneInfo.setText("No Activity Yet");
    }
    }//GEN-LAST:event_btnConfirmationActionPerformed

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
            java.util.logging.Logger.getLogger(DashboardEmoney.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardEmoney.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardEmoney.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardEmoney.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardEmoney().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelStatus;
    private javax.swing.JTextPane TextPaneInfo;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnConfirmation;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnStatus;
    private javax.swing.JButton btnTopUp;
    private javax.swing.JPanel jPanelBoard;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JLabel timeRemainingTxt;
    private javax.swing.JLabel txtBalance;
    // End of variables declaration//GEN-END:variables
}
