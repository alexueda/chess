package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.ClearService;
import service.RegisterService;
import model.UserData;
import spark.*;

public class RegisterHandler {
    private final RegisterService registerService;
    private final Gson gson = new Gson();
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterHandler (UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.registerService = new RegisterService(userDAO, authDAO);
    }

    public Object handleRegister(Request req, Response res) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);
            if (userData.username() == null || userData.username().isEmpty() ||
                    userData.password() == null || userData.password().isEmpty() ||
                    userData.email() == null || userData.email().isEmpty()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            var result = registerService.register(userData);
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(403);
            return gson.toJson(new ErrorResponse("Error: Username already taken."));
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
