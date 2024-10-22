package handler;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {
    private final ClearService clearService = new ClearService();
    private final Gson gson = new Gson();

    public Object handleClear(Request req, Response res) {
        try {
            clearService.clear();  // データベースをクリアするサービスを呼び出す
            res.status(200);
            return gson.toJson(new EmptyResponse());  // 空のJSONオブジェクトを返す
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    // 空のJSONレスポンスを表す内部クラス
    private static class EmptyResponse {
    }

    // エラーレスポンスをJSONで表す内部クラス
    private static class ErrorResponse {
        String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
