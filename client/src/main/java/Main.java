import service.ServerFacade;
import ui.UIClient;

public class Main {
    public static void main(String[] args) {
        ServerFacade serverFacade = new ServerFacade();
        UIClient client = new UIClient(serverFacade);
        client.start();
    }
}
