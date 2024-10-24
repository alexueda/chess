package handler;

import com.google.gson.Gson;
import dataaccess.*;
import service.LoginService;
import spark.Request;
import spark.Response;
import model.AuthData;
import model.UserData;

public class LoginHandler {
    private final LoginService loginService;
    private final Gson gson = new Gson();
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginHandler (UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.loginService = new LoginService(userDAO, authDAO);
    }

    public Object handleLogin(Request req, Response res) {
        try {
            UserData user = gson.fromJson(req.body(), UserData.class);
            if (user.username() == null || user.password() == null) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }
            AuthData result = loginService.login(user.username(), user.password());
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
