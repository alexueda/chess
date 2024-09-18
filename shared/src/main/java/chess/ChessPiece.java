package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */




    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMove = new ArrayList<>();
        //return each pieces valid moves
        if (this.type == PieceType.PAWN) {
            PawnMove(board, myPosition, validMove);
        } else if (this.type == PieceType.ROOK) {
            RookMove(board, myPosition, validMove);
        } else if (this.type == PieceType.KNIGHT) {
            KnightMove(board, myPosition, validMove);
        } else if (this.type == PieceType.BISHOP) {
            BishopMove(board, myPosition, validMove);
        } else if (this.type == PieceType.QUEEN) {
            QueenMove(board, myPosition, validMove);
        } else if (this.type == PieceType.KING) {
            KingMove(board, myPosition, validMove);
        }
        return validMove;
    }
    //Bounds check
    private boolean InBound(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    //PAWN Method
    private void PawnMove(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int OneMove; //move one up(WHITE) and down(BLACK)
        if (this.pieceColor == ChessGame.TeamColor.WHITE) {
            OneMove = 1;
        } else {
            OneMove = -1;
        }
        ChessPosition foward = new ChessPosition(myPosition.getRow() + OneMove, myPosition.getColumn());
        //valid check to go foward
        if (InBound(foward) && board.getPiece(foward) == null) ; {
            validMove.add(new ChessMove(myPosition, foward, null));
        }
        ChessPosition leftEnemy = new ChessPosition(myPosition.getRow() + OneMove, myPosition.getColumn()-1);
        ChessPosition rightEnemy = new ChessPosition(myPosition.getRow() + OneMove, myPosition.getColumn()+1);
        //valid check to capture
        if (InBound(leftEnemy) && board.getPiece(leftEnemy) != null && board.getPiece(leftEnemy).getTeamColor() != this.pieceColor); {
            validMove.add(new ChessMove(myPosition, leftEnemy, null));
        }
        if (InBound(rightEnemy) && board.getPiece(leftEnemy) != null && board.getPiece(rightEnemy).getTeamColor() != this.pieceColor); {
            validMove.add(new ChessMove(myPosition, rightEnemy, null));
        }
    }

    private void RookMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        //move to right
        for (int c = myPosition.getColumn() + 1; c <= 8; c++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow(), c);
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left
        for (int c = myPosition.getColumn() - 1; c >= 1; c--) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow(), c);
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move up
        for (int r = myPosition.getColumn() + 1; r <= 8; r++) {
            ChessPosition newPosition = new ChessPosition (r, myPosition.getColumn());
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move down
        for (int r = myPosition.getColumn() - 1; r >= 1; r--) {
            ChessPosition newPosition = new ChessPosition (r, myPosition.getColumn());
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
    }
    private void KnightMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
    //Knight move collection
        int[][] knMove = {
                {2,1}, {2,-1}, {-2,1}, {-2,-1}, {1,2},{-1,2}, {1,-2}, {-1,-2}
        };
        for (int[] move : knMove) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (InBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor))
                validMove.add(new ChessMove(myPosition, newPosition, null));
        }
    }
    private void BishopMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        //move to right up diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() + i, myPosition.getColumn() + i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left up diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() + i, myPosition.getColumn() - i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to right down diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() - i, myPosition.getColumn() + i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
        //move to left down diagnal
        for (int i = 1; i <= 8; i++) {
            ChessPosition newPosition = new ChessPosition (myPosition.getRow() - i, myPosition.getColumn() - i);
            if (!InBound(newPosition)) {
                break;
            }
            if (board.getPiece(newPosition) == null) {
                validMove.add(new ChessMove(myPosition, newPosition, null));
            } else {
                if (board.getPiece(newPosition).getTeamColor() != this.pieceColor) {
                    validMove.add(new ChessMove(myPosition, newPosition, null));
                }
                break;
            }
        }
    }
    private void QueenMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        RookMove(board, myPosition, validMove);
        BishopMove(board, myPosition, validMove);
    }

    private void KingMove (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> validMove) {
        int[][] kiMove = {
                {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
        };
        for (int[] move : kiMove) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + move[0], myPosition.getColumn() + move[1]);
            if (InBound(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.pieceColor))
                validMove.add(new ChessMove(myPosition, newPosition, null));
        }
    }
}

