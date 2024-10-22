package handler;

import com.google.gson.Gson;
import service.LoginService;
import spark.*;
import model.UserData;
import model.AuthData;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;

public class LoginHandler {
    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();
    private final LoginService loginService = new LoginService(userDAO, authDAO);
    private final Gson gson = new Gson();

    public Object handleLogin(Request req, Response res) {
        try {
            UserData user = gson.fromJson(req.body(), UserData.class);
            AuthData result = loginService.login(user.username(), user.password());
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Missing required fields")) {
                res.status(400);
            } else if (e.getMessage().contains("Username already taken")) {
                res.status(400);
            } else {
                res.status(401);
            }
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
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
