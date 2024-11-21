package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public LeaveCommand(String authToken, Integer gameId) {
        super(CommandType.LEAVE, authToken, gameId);
    }
}
