package handler;

import com.google.gson.Gson;
import service.LoginService;
import spark.*;
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
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: Unauthorized. " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private static class LoginRequest {
        String username;
        String password;
    }

    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
