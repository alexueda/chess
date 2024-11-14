package service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dataaccess.ClientCommunicator;
import model.GameData;
import java.lang.reflect.Type;
import java.util.*;

public class ServerFacade {
    private final ClientCommunicator communicator;
    private final Gson gson = new Gson();

    public ServerFacade() {
        this.communicator = new ClientCommunicator();
    }

    public boolean login(String username, String password) throws Exception {
        Map<String, String> credentials = Map.of("username", username, "password", password);
        String response = communicator.sendPostRequest("/session", gson.toJson(credentials));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("authToken")) {
            communicator.setAuthToken((String) responseMap.get("authToken"));
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean register(String username, String password, String email) throws Exception {
        Map<String, String> credentials = Map.of("username", username, "password", password, "email", email);
        String response = communicator.sendPostRequest("/user", gson.toJson(credentials));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("authToken")) {
            communicator.setAuthToken((String) responseMap.get("authToken"));
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean logout() throws Exception {
        String response = communicator.sendDeleteRequest("/session");
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("message") && responseMap.get("message").equals("Successfully logged out")) {
            communicator.clearAuthToken();
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean createGame(String gameName) throws Exception {
        Map<String, String> gameData = Map.of("gameName", gameName, "gameID", gameName);
        String response = communicator.sendPostRequest("/game", gson.toJson(gameData));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap.containsKey("gameID");
    }

    public Map<Integer, String> listGames() throws Exception {
        String response = communicator.sendGetRequest("/game");

        // Parse the response as a map with a "games" key containing the list of games
        Type responseType = new TypeToken<Map<String, List<GameData>>>(){}.getType();
        Map<String, List<GameData>> responseMap = gson.fromJson(response, responseType);

        // Extract the list of games from the response map
        List<GameData> games = responseMap.get("games");

        // Create a map to store game ID and game name pairs
        Map<Integer, String> gameMap = new HashMap<>();
        for (GameData game : games) {
            gameMap.put(game.gameID(), game.gameName());
        }
        return gameMap;
    }

    public boolean joinGame(int gameId, String color) throws Exception {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("gameID", gameId);
        gameData.put("playerColor", color);
        String response = communicator.sendPutRequest("/game", gson.toJson(gameData));
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        return responseMap.containsKey("success") && (Boolean) responseMap.get("success");
    }

    public String observeGame(int gameId) throws Exception {
        String response = communicator.sendGetRequest("/game/observe/" + gameId);
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("gameState")) {
            return (String) responseMap.get("gameState");
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return null;
        }
    }
}
