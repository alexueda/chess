package handler;

import com.google.gson.Gson;
import service.RegisterService;
import model.UserData;
import spark.*;

public class RegisterHandler {
    private final RegisterService registerService = new RegisterService();
    private final Gson gson = new Gson();

    public Object handleRegister(Request req, Response res) {
        try {
            // Parse the request body into a UserData object
            UserData userData = gson.fromJson(req.body(), UserData.class);

            // Pass the UserData object to the register method
            var result = registerService.register(userData);

            res.status(200);  // Success status
            return gson.toJson(result);  // Return result in JSON
        } catch (IllegalArgumentException e) {
            // If registration failed due to username already taken
            res.status(403);  // Forbidden status
            return gson.toJson(new ErrorResponse("Error: Username already taken."));
        } catch (Exception e) {
            // Handle any other errors
            res.status(500);  // Internal server error status
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    // A simple class to represent the error response
    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
