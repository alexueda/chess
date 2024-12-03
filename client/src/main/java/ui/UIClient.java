package ui;

import chess.ChessBoard;
import model.GameData;
import service.ServerFacade;
import websocket.WebsocketCommunicator;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageObserver;

import java.util.List;
import java.util.Scanner;

public class UIClient implements ServerMessageObserver {
    private final ServerFacade server;
    private WebsocketCommunicator websocket;
    private boolean loggedIn = false;
    private boolean inGame = false;
    private String username;
    private List<GameData> games;
    private ChessBoard chessBoard;

    public UIClient(ServerFacade server) {
        this.server = server;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to 240 Chess. Type 'help' to get started.");
        while (true) {
            if (!loggedIn) {
                System.out.print("[LOGGED_OUT] >>> ");
            } else if (inGame) {
                System.out.print("[IN_GAME] >>> ");
            } else {
                System.out.print("[LOGGED_IN] >>> ");
            }
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");
            String command = parts[0].toLowerCase();

            try {
                if (!loggedIn) {
                    handlePreLogin(command, parts);
                } else if (inGame) {
                    handleInGame(command, parts);
                } else {
                    handlePostLogin(command, parts);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handlePreLogin(String command, String[] parts) throws Exception {
        switch (command) {
            case "help" -> showPreLoginHelp();
            case "quit" -> {
                System.out.println("Exiting Chess Client.");
                System.exit(0);
            }
            case "login" -> handleLogin(parts);
            case "register" -> handleRegister(parts);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void handlePostLogin(String command, String[] parts) throws Exception {
        switch (command) {
            case "help" -> showPostLoginHelp();
            case "logout" -> handleLogout();
            case "create" -> handleCreateGame(parts);
            case "list" -> handleListGames();
            case "join" -> handleJoinGame(parts);
            case "observe" -> handleObserveGame(parts);
            case "quit" -> {
                System.out.println("Exiting Chess Client.");
                System.exit(0);
            }
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void handleInGame(String command, String[] parts) throws Exception {
        switch (command) {
            case "help" -> showInGameHelp();
            case "redraw" -> redrawBoard();
            case "leave" -> handleLeaveGame();
            case "make_move" -> handleMakeMove(parts);
            case "resign" -> handleResign();
            case "highlight_moves" -> handleHighlightLegalMoves(parts);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void handleLogin(String[] parts) throws Exception {
        if (parts.length < 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }
        String user = parts[1];
        String pass = parts[2];
        if (server.login(user, pass)) {
            loggedIn = true;
            this.username = user;
            System.out.println("Logged in as " + user);
        } else {
            System.out.println("Failed to login. Try again.");
        }
    }

    private void handleRegister(String[] parts) throws Exception {
        if (parts.length < 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        String user = parts[1];
        String pass = parts[2];
        String email = parts[3];
        if (server.register(user, pass, email)) {
            loggedIn = true;
            this.username = user;
            System.out.println("Logged in as " + user);
        } else {
            System.out.println("Failed to register.");
        }
    }

    private void handleLogout() throws Exception {
        if (server.logout()) {
            loggedIn = false;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("Failed to logout.");
        }
    }

    private void handleCreateGame(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        String gameName = parts[1];
        if (server.createGame(gameName)) {
            System.out.println("Game '" + gameName + "' created.");
        } else {
            System.out.println("Failed to create game.");
        }
    }

    private void handleListGames() throws Exception {
        games = server.listGames();
        displayGamesList();
    }

    private void handleJoinGame(String[] parts) throws Exception {
        if (parts.length < 3) {
            System.out.println("Usage: join <GAME_ID> <WHITE|BLACK>");
            return;
        }
        int gameIndex = Integer.parseInt(parts[1]) - 1;
        String color = parts[2].toUpperCase();

        if (games == null || gameIndex < 0 || gameIndex >= games.size()) {
            System.out.println("Invalid game ID.");
            return;
        }

        if (server.joinGame(games.get(gameIndex).gameID(), color)) {
            System.out.println("Joined game as " + color);
            inGame = true;

            chessBoard = new ChessBoard();
            chessBoard.resetBoard();
            openWebsocket();
        } else {
            System.out.println("Failed to join game.");
        }
    }

    private void handleObserveGame(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Usage: observe <GAME_ID>");
            return;
        }

        int index = Integer.parseInt(parts[1]) - 1;

        if (games == null || index < 0 || index >= games.size()) {
            System.out.println("Invalid game ID.");
            return;
        }

        System.out.println("Observing game: " + games.get(index).gameName());

        chessBoard = new ChessBoard();
        chessBoard.resetBoard();

        // Display initial board
        UIBoard uiBoard = new UIBoard(chessBoard);
        System.out.println("Initial game state:");
        System.out.println("White at bottom:");
        uiBoard.printBoardWhiteBottom();
        System.out.println("Black at bottom:");
        uiBoard.printBoardBlackBottom();

        // Establish WebSocket connection for observation
        openWebsocket();

        // Notify the server of observation
        websocket.sendMessage(String.format(
                "{\"commandType\": \"CONNECT\", \"authToken\": \"%s\", \"gameID\": \"%d\"}",
                server.getAuthToken(), games.get(index).gameID()
        ));

        System.out.println("You are now observing the game. Use 'redraw' to refresh the board.");
    }


    private void handleMakeMove(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: make_move <START> <END>");
            return;
        }
        String moveMessage = String.format("{\"commandType\": \"MAKE_MOVE\", \"start\": \"%s\", \"end\": \"%s\"}",
                parts[1], parts[2]);
        websocket.sendMessage(moveMessage);
    }

    private void handleResign() {
        websocket.sendMessage("{\"commandType\": \"RESIGN\"}");
        System.out.println("You resigned from the game.");
    }

    private void handleLeaveGame() {
        websocket.sendMessage("{\"commandType\": \"LEAVE\"}");
        closeWebsocket();
        inGame = false;
        System.out.println("You left the game.");
    }

    private void handleHighlightLegalMoves(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight_moves <PIECE_POSITION>");
            return;
        }
        String position = parts[1];
        System.out.printf("Highlighting legal moves for piece at %s.%n", position);
    }

    private void redrawBoard() {
        System.out.println("Redrawing the chess board...");
        // Add the logic to redraw the board using chessBoard.
    }

    private void openWebsocket() {
        try {
            websocket = new WebsocketCommunicator("localhost:8080", this);
        } catch (Exception e) {
            System.out.println("Failed to establish WebSocket connection: " + e.getMessage());
        }
    }

    private void closeWebsocket() {
        if (websocket != null) {
            try {
                websocket.close();
            } catch (Exception e) {
                System.out.println("Failed to close WebSocket connection: " + e.getMessage());
            }
        }
    }

    private void displayGamesList() {
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            System.out.printf("%d: %s | WHITE: %s | BLACK: %s%n",
                    i + 1,
                    game.gameName(),
                    game.whiteUsername() == null ? "null" : game.whiteUsername(),
                    game.blackUsername() == null ? "null" : game.blackUsername());
        }
    }

    private void showPreLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - create an account");
        System.out.println("  login <USERNAME> <PASSWORD> - log in to play");
        System.out.println("  quit - exit the program");
        System.out.println("  help - see available commands");
    }

    private void showPostLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  create <GAME_NAME> - create a new game");
        System.out.println("  list - list all games");
        System.out.println("  join <GAME_ID> <WHITE|BLACK> - join a game");
        System.out.println("  logout - log out of your account");
        System.out.println("  quit - exit the program");
    }

    private void showInGameHelp() {
        System.out.println("Commands:");
        System.out.println("  make_move <START> <END> - make a move");
        System.out.println("  redraw - redraw the chess board");
        System.out.println("  highlight_moves <PIECE_POSITION> - highlight legal moves for a piece");
        System.out.println("  resign - resign the game");
        System.out.println("  leave - leave the game");
        System.out.println("  help - see available commands");
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> System.out.println("Notification: " + message.getMessage());
            case LOAD_GAME -> System.out.println("Game updated: " + message.getGame());
            case ERROR -> System.out.println("Error: " + message.getMessage());
        }
    }
}
