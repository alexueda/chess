package handler;

import dataaccess.*;
import service.ClearService;
import service.LogoutService;
import spark.*;
import com.google.gson.Gson;

import java.util.Map;

public class LogoutHandler {
    private final LogoutService logoutService;
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;

    public LogoutHandler (AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.logoutService = new LogoutService(authDAO);
    }

    public Object handleLogout(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                res.status(401); // Unauthorized
                return gson.toJson(Map.of("message", "Error: Unauthorized"));
            }
            logoutService.logout(authToken);
            res.status(200);
            return gson.toJson("{}");
        } catch (IllegalArgumentException e) {
            res.status(401);
            return gson.toJson("Error: Unauthorized. " + e.getMessage());
        } catch (Exception e) {
            res.status(500);
            return gson.toJson("Error: " + e.getMessage());
        }
    }
}
