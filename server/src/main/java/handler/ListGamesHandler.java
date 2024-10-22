package handler;

import service.ListGamesService;
import model.GameData;
import spark.*;
import java.util.*;
import com.google.gson.Gson;


public class ListGamesHandler {
    private final ListGamesService listGamesService = new ListGamesService();
    private final Gson gson = new Gson();

    public Object handleListGames(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: Unauthorized"));
            }
            List<GameData> games = listGamesService.listAllGames();
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("games", games);
            res.status(200);
            return gson.toJson(responseMap);
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
