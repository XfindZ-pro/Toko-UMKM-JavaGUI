package projeksmt2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // URL, USER, PASSWORD untuk koneksi database
    static final String URL = "jdbc:mysql://localhost:3306/projeksmt2";
    static final String USER = "root";
    static final String PASSWORD = "";

    // Method untuk mendapatkan koneksi ke database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Memastikan driver JDBC terdaftar
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Membuka koneksi ke database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi ke database dinyatakan aman!");
        } catch (ClassNotFoundException | SQLException e) {
            // Menangani error jika terjadi masalah pada koneksi
            System.err.println("Koneksi gagal \n Dikarenakan:\n" + e.getMessage());
        }
        return connection;
    }

    // Method untuk menutup koneksi
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("Error menutup koneksi: " + e.getMessage());
        }
    }
}
