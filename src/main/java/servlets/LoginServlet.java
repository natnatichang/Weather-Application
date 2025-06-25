package servlets;

import util.DatabaseConnector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Handle login 
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	// default 
	private static final long serialVersionUID = 1L;
	
	// Initialize Gson for JSON Parsing
    private final Gson gson = new Gson();

    // JSON structure for the request to get the username and password
    private static class LoginRequest {
        String username;
        String password;
    }
    
    // Handle POST requests to log search queries
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	// Response type as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Database resources
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Read the request body into a StringBuilder
            StringBuilder buffer = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            }
            
            // Convert JSON string to LoginRequest object
            LoginRequest loginRequest = gson.fromJson(buffer.toString(), LoginRequest.class);
        
            // Validate input data, otherwise send back a response to day there is an error
            if (loginRequest == null || loginRequest.username == null || loginRequest.password == null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "invalid_input");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Get database connection
            conn = DatabaseConnector.getConnection();
            
            // Check if connection was successful
            if (conn == null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "database_error");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            
            // Prepare the statement to login user
            String checkUser = "SELECT username, password FROM users WHERE username = ?";
            ps = conn.prepareStatement(checkUser);
            ps.setString(1, loginRequest.username);
            rs = ps.executeQuery();
            
            JsonObject jsonResponse = new JsonObject();
            
            // Make sure the user actually exists or not
            if (!rs.next()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "user_not_exist");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            
            // Check to make sure the password actually exists int he database
            String storedPassword = rs.getString("password");
            if (loginRequest.password.equals(storedPassword)) {
            	
            	// If it was correct, then can create a new session
                HttpSession session = request.getSession();
                session.setAttribute("user", loginRequest.username);
                
                // Send a success response back
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("username", loginRequest.username);
            } else {
            	// Send error response for incorrect password
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "incorrect_password");
            }
            response.getWriter().write(gson.toJson(jsonResponse));
            
        } catch (Exception sqle) {
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "There was an error.");
            response.getWriter().write(gson.toJson(jsonResponse));
        } finally {
        	// Close resources
            DatabaseConnector.closeResources(conn, ps, rs);
        }
    }
}
