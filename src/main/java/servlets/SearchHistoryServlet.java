package servlets;

import util.DatabaseConnector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Handle the search history
@WebServlet("/getSearchHistory")
public class SearchHistoryServlet extends HttpServlet {
	// default
	private static final long serialVersionUID = 1L;
	
	// Initialize Gson for JSON Parsing
    private final Gson gson = new Gson();
    
    // Handle GET requests to get history
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	 // Set response type as JSON
        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        
        // Get username from request parameters
        String username = request.getParameter("username");
        
        // Validate username parameter
        if (username == null || username.trim().isEmpty()) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Username is required");
            response.getWriter().write(jsonResponse.toString());
            return;
        }
        
        // Initialize database resources
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
        	// Establish database connection
            conn = DatabaseConnector.getConnection();
            
            // SQL query to get the search history
            String sql = "SELECT search_id, user_id, search_query, timestamp " +
                        "FROM searches " +
                        "WHERE user_id = ? " +
                        "ORDER BY timestamp DESC";
            
            // Set up and execute prepared statement
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            	
            // Execute query and process results
            rs = pstmt.executeQuery();
            JsonArray searchHistory = new JsonArray();
            	
            // Build JSON array from result set
            while (rs.next()) {
                JsonObject search = new JsonObject();
                search.addProperty("searchId", rs.getInt("search_id"));
                search.addProperty("userId", rs.getString("user_id"));
                search.addProperty("query", rs.getString("search_query"));
                search.addProperty("timestamp", rs.getTimestamp("timestamp").toString());
                searchHistory.add(search);
            }
            
            // Send success response
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("history", searchHistory);
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
        	// Handle all errors
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error occurred");
            response.getWriter().write(jsonResponse.toString());
        } finally {
        	// Close resources
            DatabaseConnector.closeResources(conn, pstmt, rs);
        }
    }
}
