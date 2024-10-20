package handler;

import service.ClearService;
import com.google.gson.Gson;
import spark.*;


public class ClearHandler {
    private final ClearService clearService = new ClearService();
    private final Gson gson = new Gson();

    public Object handleClear(Request req, Response res) {
        //understand there is no need for req for right now. keep it for later possible usage.
        try {
            clearService.clear();
            res.status(200);
            return gson.toJson("Success");
        } catch (Exception e) {
            res.status(500);
            return gson.toJson("Error: " + e.getMessage());
        }
    }
}