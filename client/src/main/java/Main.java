import service.ServerFacade;
import ui.UIClient;
import dataaccess.SQLGameDAO;

public class Main {
    public static void main(String[] args) {
        String baseUrl = "http://localhost:8080";
        ServerFacade serverFacade = new ServerFacade(baseUrl);
        UIClient client = new UIClient(serverFacade);
        client.start();
    }
}
