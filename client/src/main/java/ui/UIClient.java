package ui;

import chess.ChessBoard;
import chess.ChessPosition;
import chess.ChessMove;
import chess.ChessGame;
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
    private boolean isObserver = false;
    private boolean isWhitePlayer = false; // Track if the player is White
    private String username;
    private List<GameData> games;
    private ChessGame chessGame;

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
            System.out.println("Registered and logged in as " + user);
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
            isObserver = false;
            isWhitePlayer = "WHITE".equals(color); // Set the player role

            chessGame = new ChessGame();
            openWebsocket();
            redrawBoard(); // Display the board immediately after joining
        } else {
            System.out.println("Failed to join game.");
        }
    }

    private void redrawBoard() {
        if (chessGame == null) {
            System.out.println("No game is currently loaded.");
            return;
        }

        UIBoard uiBoard = new UIBoard(chessGame.getBoard());
        if (isObserver || isWhitePlayer) {
            uiBoard.printBoardWhiteBottom(); // White perspective for observers or White player
        } else {
            uiBoard.printBoardBlackBottom(); // Black perspective for Black player
        }
    }

    private void handleObserveGame(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Usage: observe <GAME_ID>");
            return;
        }

        int gameIndex = Integer.parseInt(parts[1]) - 1;

        if (games == null || gameIndex < 0 || gameIndex >= games.size()) {
            System.out.println("Invalid game ID.");
            return;
        }

        System.out.println("Observing game: " + games.get(gameIndex).gameName());
        inGame = true;
        isObserver = true;

        chessGame = new ChessGame();
        openWebsocket();
        redrawBoard(); // Observers see the board immediately
    }

    private void handleLeaveGame() {
        try {
            websocket.sendMessage(String.format(
                    "{\"commandType\": \"LEAVE\", \"authToken\": \"%s\", \"gameID\": \"%d\"}",
                    server.getAuthToken(), getCurrentGameID()
            ));
            inGame = false;
            chessGame = null;
            closeWebsocket();
            System.out.println("You left the game.");
        } catch (Exception e) {
            System.out.println("Failed to leave the game: " + e.getMessage());
        }
    }

    private int getCurrentGameID() {
        return games.stream()
                .filter(game -> username.equals(game.whiteUsername()) || username.equals(game.blackUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No game is currently active."))
                .gameID();
    }

    private void handleMakeMove(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: make_move <START> <END>");
            return;
        }
        String start = parts[1];
        String end = parts[2];

        try {
            // Send the move command to the server
            websocket.sendMessage(String.format(
                    "{\"commandType\": \"MAKE_MOVE\", \"authToken\": \"%s\", \"gameID\": \"%d\", \"start\": \"%s\", \"end\": \"%s\"}",
                    server.getAuthToken(), getCurrentGameID(), start, end
            ));
        } catch (Exception e) {
            System.out.println("Failed to make move: " + e.getMessage());
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

    private void handleResign() {
        try {
            websocket.sendMessage(String.format(
                    "{\"commandType\": \"RESIGN\", \"authToken\": \"%s\", \"gameID\": \"%d\"}",
                    server.getAuthToken(), getCurrentGameID()
            ));
            System.out.println("You resigned from the game.");
        } catch (Exception e) {
            System.out.println("Failed to resign: " + e.getMessage());
        }
    }

    private void handleHighlightLegalMoves(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight_moves <PIECE_POSITION>");
            return;
        }

        String positionString = parts[1];
        try {
            ChessPosition position = new ChessPosition(
                    Character.getNumericValue(positionString.charAt(1)),
                    positionString.charAt(0) - 'a' + 1
            );

            List<ChessPosition> legalMoves = chessGame.validMoves(position).stream()
                    .map(ChessMove::getEndPosition).toList();

            UIBoard uiBoard = new UIBoard(chessGame.getBoard());
            if (isObserver || isWhitePlayer) {
                uiBoard.printBoardWhiteHL(legalMoves); // White perspective
            } else {
                uiBoard.printBoardBlackHL(legalMoves); // Black perspective
            }
        } catch (Exception e) {
            System.out.println("Failed to highlight legal moves: " + e.getMessage());
        }
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
        System.out.println("  observe <GAME_ID> - observe a game");
        System.out.println("  logout - log out of your account");
        System.out.println("  quit - exit the program");
    }

    private void showInGameHelp() {
        System.out.println("Commands:");
        System.out.println("  make_move <START> <END> - make a move");
        System.out.println("  redraw - redraw the chess board");
        System.out.println("  leave - leave the game");
        System.out.println("  resign - resign the game");
        System.out.println("  highlight_moves <PIECE_POSITION> - highlight legal moves for a piece");
        System.out.println("  help - see available commands");
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> System.out.println("Notification: " + message.getMessage());
            case LOAD_GAME -> {
                chessGame.getBoard().resetBoard(); // Update game state
                redrawBoard(); // Refresh board after loading
            }
            case ERROR -> System.out.println("Error: " + message.getMessage());
        }
    }
}