package websocket.commands;

public class ResignCommand extends UserGameCommand {
    public ResignCommand(String authToken, Integer gameId) {
        super(CommandType.RESIGN, authToken, gameId);
    }
}
