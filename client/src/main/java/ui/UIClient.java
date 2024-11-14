package ui;

import java.util.List;
import java.util.Scanner;

import model.GameData;
import service.ServerFacade;

public class UIClient {
    private final ServerFacade server;
    private boolean loggedIn = false;
    private String username;
    private List<GameData> games; // Store game IDs and names

    public UIClient(ServerFacade server) {
        this.server = server;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to 240 Chess. Type 'help' to get started.");
        while (true) {
            System.out.print(loggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");
            String command = parts[0].toLowerCase();
            try {
                if (!loggedIn) {
                    handlePreLogin(command, parts);
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
            case "help":
                showPreLoginHelp();
                break;
            case "quit":
                System.out.println("Exiting Chess Client.");
                System.exit(0);
                break;
            case "login":
                if (parts.length < 3) {
                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                } else {
                    String user = parts[1];
                    String pass = parts[2];
                    if (server.login(user, pass)) {
                        loggedIn = true;
                        this.username = user;
                        System.out.println("Logged in as " + user);
                    } else {
                        System.out.println("Failed to login.");
                    }
                }
                break;
            case "register":
                if (parts.length < 4) {
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                } else {
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
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void showPreLoginHelp() {
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - create an account");
        System.out.println("  login <USERNAME> <PASSWORD> - log in to play");
        System.out.println("  quit - exit the program");
        System.out.println("  help - see available commands");
    }

    private void handlePostLogin(String command, String[] parts) throws Exception {
        switch (command) {
            case "help":
                showPostLoginHelp();
                break;
            case "logout":
                if (server.logout()) {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                } else {
                    System.out.println("Failed to logout.");
                }
                break;
            case "create":
                if (parts.length < 2) {
                    System.out.println("Usage: create <GAME_NAME>");
                } else {
                    String gameName = parts[1];
                    if (server.createGame(gameName)) {
                        System.out.println("Game '" + gameName + "' created.");
                    } else {
                        System.out.println("Failed to create game.");
                    }
                }
                break;
            case "list":
                games = server.listGames();
                for (int i = 0; i < games.size(); i++) {
                    GameData game = games.get(i);
                    String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "null";
                    String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "null";
                    System.out.println((i + 1) + " " + game.gameName() + " WHITE: " + whitePlayer + " BLACK: " + blackPlayer);
                }
                break;
            case "join":
                if (parts.length < 3) {
                    System.out.println("Usage: join <GAME_ID> <WHITE|BLACK>");
                } else {
                    int index = Integer.parseInt(parts[1]) - 1;
                    String color = parts[2].toUpperCase();
                    if (games == null) {
                        games = server.listGames();
                    }
                    if (server.joinGame(games.get(index).gameID(), color)) {
                        System.out.println("Joined game " + index + " as " + color);
                    } else {
                        System.out.println("Failed to join game. Please check if the game ID or color is valid, or if the color is already taken.");
                    }
                }
                break;
            case "observe":
                if (parts.length < 2) {
                    System.out.println("Usage: observe <GAME_ID>");
                } else {
                    int index= Integer.parseInt(parts[1])- 1;
                    String gameState = server.observeGame(index);
                    if (gameState != null) {
                        System.out.println("Observing game " + index + ": " + gameState);
                    } else {
                        System.out.println("Failed to observe game.");
                    }
                }
                break;
            case "quit":
                System.out.println("Exiting Chess Client.");
                System.exit(0);
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void showPostLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  create <GAME_NAME> - create a new game");
        System.out.println("  list - list all games");
        System.out.println("  join <GAME_ID> <WHITE|BLACK> - join a game");
        System.out.println("  observe <GAME_ID> - observe a game");
        System.out.println("  logout - log out of your account");
        System.out.println("  quit - exit the program");
        System.out.println("  help - see available commands");
    }
}
