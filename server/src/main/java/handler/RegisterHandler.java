package handler;

import com.google.gson.Gson;
import service.RegisterService;
import model.UserData;
import model.AuthData;
import spark.*;

public class RegisterHandler {
    private final RegisterService registerService = new RegisterService();
    private final Gson gson = new Gson();

    public Object handleRegister(Request req, Response res) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);
            AuthData registerResult = registerService.register(userData);
            //registration successful
            res.status(200);
            return gson.toJson(registerResult);
        } catch (IllegalArgumentException e) {
            // "username already taken" error
            res.status(403);
            return gson.toJson("Error: " + e.getMessage());
        } catch (Exception e) {
            // Handle other bad request errors
            res.status(400);
            return gson.toJson("Error: " + e.getMessage());
        }
    }
}
