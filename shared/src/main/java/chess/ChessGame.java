package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame implements Cloneable {

    private ChessBoard board;
    private TeamColor nowTurn;

    public ChessGame() {
        this.board = new ChessBoard ();
        this.nowTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return nowTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.nowTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && nowTurn == chessGame.nowTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, nowTurn);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null || piece.getTeamColor() != nowTurn) {
            return new ArrayList<>();
        }
        Collection<ChessMove> validMoves = new ArrayList<>();
        boolean isKingInCheck = isInCheck(nowTurn);  // Check if the king is in check
        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            ChessGame clonedGame = this.clone();
            clonedGame.executeMove(move);  // Simulate the move on the cloned board
            if (isKingInCheck) {
                if (!clonedGame.isInCheck(nowTurn)) {
                    validMoves.add(move); // If the move blocks or captures the piece attacking the king, add it as a valid move
                }
            } else {
                if (!clonedGame.isInCheck(nowTurn)) {
                    validMoves.add(move); // If the king is not in check, add moves that don't put the king in check
                }
            }
        }
        return validMoves;  // Return all valid moves
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Move is invalid: No piece at that position.");//check piece is there or not
        }
        if (piece.getTeamColor() != nowTurn) {
            throw new InvalidMoveException("Move is invalid: Not this team's turn.");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());//get the array of valid moves
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Move is invalid: Not a legal move.");
        }
        ChessGame clonedGame = this.clone();
        clonedGame.executeMove(move);
        // Check if the move leaves the current team's King in check
        if (clonedGame.isInCheck(nowTurn)) {
            throw new InvalidMoveException("Move is invalid: King would be in check.");
        }
        //modify board
        executeMove(move);
        //turn change
        if (nowTurn == TeamColor.WHITE) {
            nowTurn = TeamColor.BLACK;
        } else {
            nowTurn = TeamColor.WHITE;
        }
    }

    private void executeMove(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

        if (capturedPiece != null && capturedPiece.getTeamColor() == piece.getTeamColor()) {
            throw new RuntimeException("Cannot capture a piece of the same team.");
        }

        // Handle pawn promotion based on the promotion type in ChessMove
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            boolean isPromotion = (piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1);
            if (isPromotion && move.getPromotionPiece() != null) {
                piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
        }
        // Move the piece to the new position
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition (row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }

    private Collection<ChessPosition> getAllPieces() {
        Collection<ChessPosition> pieces = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null) {
                    pieces.add(position);
                }
            }
        }
        return pieces;
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        for (ChessPosition pos : getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() != teamColor) {
                Collection <ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move: moves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false; // not check then not checkmate
        }
        for (ChessPosition pos: getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move: moves) {
                    ChessGame clonedGame = this.clone();
                    clonedGame.executeMove(move);
                    //in the cloned board, move all the own possible piece and if king is not check after it then not checkmate
                    if (!clonedGame.isInCheck(teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (nowTurn != teamColor) {
            return false;  // check it is your turn
        }
        if (isInCheck(teamColor)) {
            return false;//If king in check then not stalemate
        }
        for (ChessPosition pos : getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move : moves) {
                    ChessGame clonedGame = this.clone();
                    clonedGame.executeMove(move);  // Simulate the move on cloned board
                    if (!clonedGame.isInCheck(teamColor)) {
                        return false;  // There's at least one legal move left
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
    //Write the override for clone()
    @Override
    public ChessGame clone() {
        try {
            ChessGame cloned = (ChessGame) super.clone();
            cloned.board = board.clone();  // Ensure the board is cloned
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported");
        }
    }
}
