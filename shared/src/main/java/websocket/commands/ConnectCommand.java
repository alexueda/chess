package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    public ConnectCommand(String authToken, Integer gameId) {
        super(CommandType.CONNECT, authToken, gameId);
    }
}
