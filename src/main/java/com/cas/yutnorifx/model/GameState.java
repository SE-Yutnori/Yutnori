package com.cas.yutnorifx.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 게임의 전반적인 상태를 관리하는 클래스
 */
public class GameState {
    private final List<Player> players;
    private final TokenPositionManager tokenPositionManager;
    private final Board board;
    private Player currentPlayer;
    private GamePhase phase;
    private List<Integer> remainingMoves;
    private Player winner;
    
    public GameState(List<Player> players, TokenPositionManager tokenPositionManager, Board board) {
        this.players = new ArrayList<>(players);
        this.tokenPositionManager = tokenPositionManager;
        this.board = board;
        this.phase = GamePhase.NOT_STARTED;
        this.remainingMoves = new ArrayList<>();
        this.winner = null;
        
        if (!players.isEmpty()) {
            this.currentPlayer = players.get(0);
        }
    }

    // 게임 초기화를 위한 생성자
    public GameState(int sides, float radius, List<String> playerNames, List<Integer> tokenCounts) {
        this.board = new Board(sides, radius);
        this.tokenPositionManager = new TokenPositionManager(board);
        this.players = new ArrayList<>();
        this.phase = GamePhase.NOT_STARTED;
        this.remainingMoves = new ArrayList<>();
        this.winner = null;

        // 플레이어 생성
        for (int i = 0; i < playerNames.size(); i++) {
            String playerName = playerNames.get(i);
            int tokenCount = (i < tokenCounts.size()) ? tokenCounts.get(i) : 4;
            players.add(new Player(playerName, tokenCount));
        }

        if (!players.isEmpty()) {
            this.currentPlayer = players.get(0);
        }
    }
    
    public void startGame() {
        if (phase == GamePhase.NOT_STARTED) {
            phase = GamePhase.IN_PROGRESS;
            currentPlayer = players.get(0);
        }
    }

    public YutGameRules.YutThrowResult throwYut() {
        return YutGameRules.accumulateYut(currentPlayer);
    }
    
    public void nextTurn() {
        int currentIndex = players.indexOf(currentPlayer);
        currentIndex = (currentIndex + 1) % players.size();
        currentPlayer = players.get(currentIndex);
        remainingMoves.clear();
    }

    // 기존 nextPlayer() 메서드와 동일
    public void nextPlayer() {
        nextTurn();
    }
    
    public void addMoves(List<Integer> moves) {
        remainingMoves.addAll(moves);
    }
    
    public Integer useNextMove() {
        if (remainingMoves.isEmpty()) {
            return null;
        }
        return remainingMoves.remove(0);
    }
    
    public boolean hasRemainingMoves() {
        return !remainingMoves.isEmpty();
    }

    public List<Token> getMovableTokens(int steps) {
        if (currentPlayer == null) return new ArrayList<>();
        
        if (steps < 0) {
            return currentPlayer.getBackwardMovableTokens();
        } else {
            return currentPlayer.getMovableTokens();
        }
    }

    public YutGameRules.MoveResult moveToken(Token token, int steps, Function<List<BoardNode>, BoardNode> branchSelector) {
        if (phase == GamePhase.FINISHED) {
            return new YutGameRules.MoveResult(false, false, false, "게임이 종료되었습니다.");
        }

        if (steps < 0) {
            if (token.getState() != TokenState.ACTIVE) {
                return new YutGameRules.MoveResult(false, false, false, "대기 중인 말은 빽도로 이동할 수 없습니다.");
            }
            return YutGameRules.moveTokenBackward(token, Math.abs(steps), tokenPositionManager);
        } else {
            if (token.getState() == TokenState.READY) {
                tokenPositionManager.placeTokenAtStart(token);
            }
            return YutGameRules.moveToken(token, steps, tokenPositionManager, branchSelector);
        }
    }
    
    public boolean checkVictory(Player player) {
        if (player.hasFinished()) {
            phase = GamePhase.FINISHED;
            winner = player;
            return true;
        }
        return false;
    }

    public boolean isGameEnded() {
        return phase == GamePhase.FINISHED;
    }
    
    // Getters
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public GamePhase getPhase() {
        return phase;
    }
    
    public List<Integer> getRemainingMoves() {
        return new ArrayList<>(remainingMoves);
    }
    
    public TokenPositionManager getTokenPositionManager() {
        return tokenPositionManager;
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Player getWinner() {
        return winner;
    }
}

enum GamePhase {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED
}