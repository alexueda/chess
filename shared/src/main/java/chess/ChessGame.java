package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame implements Cloneable {

    private ChessBoard board;
    private TeamColor nowTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
        this.nowTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return nowTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.nowTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && nowTurn == chessGame.nowTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, nowTurn);
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece without leaving the king in check.
     *
     * @param startPosition the starting position of the piece
     * @return a collection of valid moves, or an empty list if no moves are valid
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return new ArrayList<>();
        }

        return filterValidMoves(piece.pieceMoves(board, startPosition), piece.getTeamColor());
    }

    /**
     * Filters the moves that do not leave the king in check.
     */
    private Collection<ChessMove> filterValidMoves(Collection<ChessMove> moves, TeamColor teamColor) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessGame clonedGame = this.clone();
            clonedGame.executeMove(move);
            if (!clonedGame.isInCheck(teamColor)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Move is invalid: No piece at that position.");
        }
        if (piece.getTeamColor() != nowTurn) {
            throw new InvalidMoveException("Move is invalid: Not this team's turn.");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Move is invalid: Not a legal move.");
        }

        ChessGame clonedGame = this.clone();
        clonedGame.executeMove(move);
        if (clonedGame.isInCheck(nowTurn)) {
            throw new InvalidMoveException("Move is invalid: King would be in check.");
        }

        executeMove(move);
        nowTurn = (nowTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private void executeMove(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

        if (capturedPiece != null && capturedPiece.getTeamColor() == piece.getTeamColor()) {
            throw new RuntimeException("Cannot capture a piece of the same team.");
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            boolean isPromotion = (piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1);
            if (isPromotion && move.getPromotionPiece() != null) {
                piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            }
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
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

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        return checkForCheck(kingPosition, teamColor);
    }

    private boolean checkForCheck(ChessPosition kingPosition, TeamColor teamColor) {
        for (ChessPosition pos : getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() != teamColor) {
                for (ChessMove move : piece.pieceMoves(board, pos)) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return noLegalMovesLeft(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (nowTurn != teamColor || isInCheck(teamColor)) {
            return false;
        }
        return noLegalMovesLeft(teamColor);
    }

    private boolean noLegalMovesLeft(TeamColor teamColor) {
        for (ChessPosition pos : getAllPieces()) {
            ChessPiece piece = board.getPiece(pos);
            if (piece.getTeamColor() == teamColor) {
                for (ChessMove move : piece.pieceMoves(board, pos)) {
                    ChessGame clonedGame = this.clone();
                    clonedGame.executeMove(move);
                    if (!clonedGame.isInCheck(teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public ChessGame clone() {
        try {
            ChessGame cloned = (ChessGame) super.clone();
            cloned.board = board.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported");
        }
    }
}
