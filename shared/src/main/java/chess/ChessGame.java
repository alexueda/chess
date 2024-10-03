package chess;

import java.util.ArrayList;
import java.util.Collection;

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
        if (piece == null) {
            return null;
        }
        if (piece.getTeamColor() != nowTurn) {
            return new ArrayList<>();
            //This will return empty valid move array to none turn player's pieces
        }
        return piece.pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        //check move is not invalid
        if (piece == null) {
            throw new InvalidMoveException("move is invalid: No piece at that position");
        }
        if (piece.getTeamColor() != nowTurn) {
            throw  new InvalidMoveException("move is invalid: Not this team turn");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
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
            throw new RuntimeException("Can not capture the piece");
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition (row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KNIGHT && piece.getTeamColor() == teamColor) {
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
        if (isInCheck(teamColor)) {
            return false;
        }
        for (ChessPosition pos: getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                if (!moves.isEmpty()) {
                    return false;
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
