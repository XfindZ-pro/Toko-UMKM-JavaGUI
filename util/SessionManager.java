package projeksmt2.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private static String currentUser;
    private static String currentRole;
    private static String currentUserId; 
    private static int currentBalance;
    private static ScheduledExecutorService scheduler; // Not final anymore as we'll recreate it

    public static void login(String username, String role, String userId) {
        // Initialize or recreate the scheduler if it was shutdown
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        
        currentUser = username;
        currentRole = role;
        currentUserId = userId;

        // Initial balance fetch
        fetchUserBalance(userId);

        // Schedule periodic balance updates
        scheduler.scheduleAtFixedRate(
            () -> fetchUserBalance(userId),
            0, 3, TimeUnit.SECONDS
        );
    }

    private static void fetchUserBalance(String userId) {
        String query = "SELECT balance FROM emoney WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int newBalance = rs.getInt("balance");
                if (newBalance != currentBalance) {
                    currentBalance = newBalance;
                }
            } else {
                currentBalance = 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getCurrentBalance() {
        return currentBalance;
    }

    public static void updateBalance(int newBalance) {
        currentBalance = newBalance;
        String updateQuery = "UPDATE emoney SET balance = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, newBalance);
            stmt.setString(2, currentUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static boolean isLoggedIn() {
        return currentUser != null && currentRole != null && currentUserId != null;
    }

    public static void logout() {
        // Shutdown the scheduler properly
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        currentUser = null;
        currentRole = null;
        currentUserId = null;
        currentBalance = 0;
    }
}