package handler;

import com.google.gson.Gson;
import service.LoginService;
import spark.Request;
import spark.Response;
import model.AuthData;

public class LoginHandler {
    private final LoginService loginService = new LoginService();
    private final Gson gson = new Gson();

    public Object handleLogin(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            AuthData result = loginService.login(loginRequest.username, loginRequest.password);

            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            // If login failed due to incorrect username or password
            res.status(401);
            return gson.toJson("Error: Unauthorized. " + e.getMessage());
        } catch (Exception e) {
            // Handle any other errors
            res.status(500);
            return gson.toJson("Error: " + e.getMessage());
        }
    }

    // A simple class to represent the login request
    private static class LoginRequest {
        String username;
        String password;
    }
}
