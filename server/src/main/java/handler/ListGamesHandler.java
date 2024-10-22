package handler;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.ClearService;
import service.ListGamesService;
import model.GameData;
import spark.*;
import java.util.*;
import com.google.gson.Gson;


public class ListGamesHandler {
    private final ListGamesService listGamesService;
    private final Gson gson = new Gson();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ListGamesHandler (AuthDAO authDAO, GameDAO gameDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
        this.listGamesService = new ListGamesService(authDAO, gameDAO);
    }

    public Object handleListGames(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: Unauthorized"));
            }
            List<GameData> games = listGamesService.listAllGames(authToken);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("games", games);
            res.status(200);
            return gson.toJson(responseMap);
        }
        catch (IllegalArgumentException e) {
            res.status(401);
            return gson.toJson(createErrorResponse("Error: Unauthorized"));
        }
        catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return errorResponse;
    }
}
