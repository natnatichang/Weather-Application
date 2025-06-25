package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Manage database connection + resources
public class DatabaseConnector {
    
	// Create a connection to the MySQL database on local
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Uses JDBC Driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Return connection based on the following stuff
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/weather_app", 
            "root", 
            "root"
        );
    }

    // Close databases without the resultset
    public static void closeResources(Connection conn, PreparedStatement ps) {
        try {
        	// Close the following resources 
            if (conn != null) {
                conn.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Close databases with the resultset
    public static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
        	// Close the following resources 
        	if (conn != null) {
                conn.close();
            }
        	
            if (ps != null) {
                ps.close();
            }
            
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
