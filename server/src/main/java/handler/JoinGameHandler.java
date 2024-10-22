package handler;

import service.JoinGameService;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import spark.*;
import com.google.gson.Gson;
import java.util.*;

public class JoinGameHandler {

    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public JoinGameHandler() {
        this.joinGameService = new JoinGameService(new GameDAO(), new AuthDAO());
    }

    public Object handleJoinGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return gson.toJson(createErrorResponse("Error: Unauthorized"));
            }
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("gameID") || !body.containsKey("playerColor")) {
                res.status(400);
                return gson.toJson(createErrorResponse("Error: bad request"));
            }
            int gameID = ((Double) body.get("gameID")).intValue();
            String playerColor = (String) body.get("playerColor");
            joinGameService.joinGame(gameID, playerColor, authToken);
            res.status(200);
            return gson.toJson(new HashMap<>());
        } catch (IllegalArgumentException e) {
            res.status(403);
            return gson.toJson(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return errorResponse;
    }
}
