package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class UIBoard {
    private final String[][] board;
    private static final String RESET = "\u001b[0m";
    private static final String RED_TEXT = "\u001b[31m";
    private static final String BLUE_TEXT = "\u001b[34m";
    private static final String BG_LIGHT = "\u001b[47m";
    private static final String BG_DARK = "\u001b[100m";

    public UIBoard() {
        board = new String[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        board[0] = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
        board[1] = new String[]{"P", "P", "P", "P", "P", "P", "P", "P"};
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = " ";
            }
        }
        board[7] = new String[]{"r", "n", "b", "q", "k", "b", "n", "r"};
        board[6] = new String[]{"p", "p", "p", "p", "p", "p", "p", "p"};
    }

    public void printBoard() {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print("   ");
        for (char col = 'h'; col >= 'a'; col--) {
            out.print(" " + col + " ");
        }
        out.println();
        for (int i = 0; i < 8; i++) {
            out.print((8 - i) + " ");
            for (int j = 7; j >= 0; j--) {
                boolean isLightSquare = (i + j) % 2 == 0;
                String bgColor = isLightSquare ? BG_LIGHT : BG_DARK;
                String textColor = board[i][j].equals(board[i][j].toLowerCase()) ? BLUE_TEXT : RED_TEXT;
                out.print(bgColor + " " + textColor + board[i][j] + " " + RESET);
            }
            out.println(" " + (8 - i));
        }
        out.print("   ");
        for (char col = 'h'; col >= 'a'; col--) {
            out.print(" " + col + " ");
        }
        out.println();
    }
}
