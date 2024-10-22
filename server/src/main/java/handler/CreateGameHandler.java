package handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import service.CreateGameService;
import spark.*;

public class CreateGameHandler {
    private final CreateGameService createGameService;
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGameHandler (AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.createGameService = new CreateGameService(authDAO, gameDAO);
    }

    public Object handleCreateGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: Unauthorized, missing auth token."));
            }
            CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            if (createGameRequest.gameName == null || createGameRequest.gameName.isEmpty()) {
                res.status(400);  // Bad Request
                return gson.toJson(new ErrorResponse("Error: Bad request, missing game name."));
            }
            GameData createdGame = createGameService.createGame(createGameRequest.gameName, authToken);
            res.status(200);
            return gson.toJson(new SuccessResponse(createdGame.gameID()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private static class CreateGameRequest {
        String gameName;
    }

    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    private static class SuccessResponse {
        int gameID;

        public SuccessResponse(int gameID) {
            this.gameID = gameID;
        }
    }
}
