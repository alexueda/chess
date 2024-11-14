package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.GameData;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
            System.out.println("Auth token set: " + responseMap.get("authToken"));
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
            System.out.println("Auth token set: " + responseMap.get("authToken"));
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean logout() throws Exception {
        String response = communicator.sendDeleteRequest("/session");

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("message") && "Successfully logged out".equals(responseMap.get("message"))) {
            communicator.clearAuthToken();
            System.out.println("Logged out successfully.");
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public boolean createGame(String gameName) throws Exception {
        Map<String, String> gameData = Map.of("gameName", gameName, "gameID", gameName); // Set gameID and gameName to be the same
        String response = communicator.sendPostRequest("/game", gson.toJson(gameData));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("gameID")) {
            System.out.println("Game created with ID: " + responseMap.get("gameID"));
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public List<GameData> listGames() throws Exception {
        String response = communicator.sendGetRequest("/game");

        // Parse the "games" list from the response JSON
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("games")) {
            Type gameListType = new TypeToken<List<GameData>>() {}.getType();
            return gson.fromJson(gson.toJson(responseMap.get("games")), gameListType);
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return null;
        }
    }

    public boolean joinGame(String gameId, String color) throws Exception {
        Map<String, Object> gameData = Map.of("gameID", gameId, "playerColor", color);
        String response = communicator.sendPutRequest("/game", gson.toJson(gameData));

        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        if (responseMap.containsKey("success") && (Boolean) responseMap.get("success")) {
            System.out.println("Joined game successfully.");
            return true;
        } else {
            System.out.println("Error: " + responseMap.get("message"));
            return false;
        }
    }

    public String observeGame(String gameId) throws Exception {
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
