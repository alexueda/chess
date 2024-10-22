package handler;

import dataaccess.*;
import service.LogoutService;
import spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class LogoutHandler {
    private final LogoutService logoutService;
    private final Gson gson = new Gson();

    public LogoutHandler (AuthDAO authDAO) {
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
            return gson.toJson(Map.of("message", "Successfully logged out"));
        } catch (IllegalArgumentException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: Unauthorized. " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
