package handler;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;
import dataaccess.*;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearHandler (UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    public Object handleClear(Request req, Response res) {
        try {
            clearService.clear();
            res.status(200);
            return gson.toJson(new EmptyResponse());
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private static class EmptyResponse {
    }

    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
