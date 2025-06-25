package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import util.DatabaseConnector;

// Handles logging user search queries to database
@WebServlet("/logSearch")
public class SearchLogger extends HttpServlet {
	// default
	private static final long serialVersionUID = 1L;
	
	// Initialize Gson for JSON Parsing
    private final Gson gson = new Gson();
    
    // JSON structure for the request to get the id and query
    static class SearchRequest {
        String user_id;
        String search_query;
    }
    
    
    // Handle POST requests to log search queries
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	// Response type as JSON
        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        
        // Database resources
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
        	// Parse incoming JSON request into a SearchRequest object
            SearchRequest searchRequest = gson.fromJson(request.getReader(), SearchRequest.class);
            conn = DatabaseConnector.getConnection();
            

            // Make sure user exists 
            String checkUser = "SELECT username FROM users WHERE username = ?";
            
            try (PreparedStatement checker = conn.prepareStatement(checkUser)) {
            	checker.setString(1, searchRequest.user_id);
            	
                ResultSet rs = checker.executeQuery();
                
                // User doesn't exist, then return error
                if (!rs.next()) {
                    jsonResponse.addProperty("status", "error");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }
            }
            
            // Insert the search into the searches table
            String sql = "INSERT INTO searches (user_id, search_query) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            
            // Set the user id
            pstmt.setString(1, searchRequest.user_id);
            
            // Set the search query type
            pstmt.setString(2, searchRequest.search_query);
            pstmt.executeUpdate();
            
            // Return a success response
            jsonResponse.addProperty("status", "success");
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
        	// Handle exceptions
            jsonResponse.addProperty("status", "error");
            response.getWriter().write(jsonResponse.toString());
        } finally {
        	// Close resources
            DatabaseConnector.closeResources(conn, pstmt);
        }
    }
}
