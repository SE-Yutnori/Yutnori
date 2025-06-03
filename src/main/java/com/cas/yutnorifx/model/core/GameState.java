package com.cas.yutnorifx.model.core;

import com.cas.yutnorifx.model.entity.*;
import com.cas.yutnorifx.model.event.*;
import com.cas.yutnorifx.model.request.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GameState {
    private final List<Player> players;
    private final TokenPositionManager tokenPositionManager;
    private final Board board;
    private Player currentPlayer;
    private GamePhase phase;

    private final GameEventNotifier eventNotifier;
    
    private final Map<String, CompletableFuture<BranchSelectionResponse>> pendingBranchSelections = new ConcurrentHashMap<>();
    
    private final Map<String, CompletableFuture<TokenSelectionResponse>> pendingTokenSelections = new ConcurrentHashMap<>();
    
    private final Map<String, CompletableFuture<YutTestResponse>> pendingYutTestSelections = new ConcurrentHashMap<>();
    
    private final Map<String, CompletableFuture<ReorderResponse>> pendingReorderSelections = new ConcurrentHashMap<>();
    
    private int branchRequestCounter = 0;
    private int tokenRequestCounter = 0;
    private int yutTestRequestCounter = 0;
    private int reorderRequestCounter = 0;
    
    private CompletableFuture<Void> pendingMessageConfirmation = null;
    
    private List<Integer> currentTurnResults = new ArrayList<>();
    
    private boolean hasAdditionalTurn = false;

    public GameState(int sides, float radius, List<String> playerNames, List<Integer> tokenCounts) {
        this.board = new Board(sides, radius);
        this.tokenPositionManager = new TokenPositionManager(board);
        this.players = new ArrayList<>();
        this.phase = GamePhase.NOT_STARTED;
        this.eventNotifier = new GameEventNotifier();

        for (int i = 0; i < playerNames.size(); i++) {
            String playerName = playerNames.get(i);
            int tokenCount = (i < tokenCounts.size()) ? tokenCounts.get(i) : 4;
            players.add(new Player(playerName, tokenCount));
        }

        if (!players.isEmpty()) {
            this.currentPlayer = players.get(0);
        }
    }
    
    public void addObserver(GameEventObserver observer) {
        eventNotifier.addObserver(observer);
    }

    public void handleBranchSelection(BranchSelectionResponse response) {
        CompletableFuture<BranchSelectionResponse> future = pendingBranchSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingBranchSelections.remove(response.getRequestId());
        }
    }
    
    public void handleTokenSelection(TokenSelectionResponse response) {
        CompletableFuture<TokenSelectionResponse> future = pendingTokenSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingTokenSelections.remove(response.getRequestId());
        }
    }
    
    public void handleYutTestSelection(YutTestResponse response) {
        CompletableFuture<YutTestResponse> future = pendingYutTestSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingYutTestSelections.remove(response.getRequestId());
        }
    }
    
    public void notifyBranchSelectionNeeded(BranchSelectionRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.BRANCH_SELECTION_NEEDED, request, 
            "분기점 도달: " + request.getToken().getName() + "의 경로를 선택해주세요.");
    }
    
    public void notifyTokenSelectionNeeded(TokenSelectionRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.TOKEN_SELECTION_NEEDED, request, 
            request.getSteps() + "칸 이동할 토큰을 선택해주세요.");
    }
    
    public void notifyYutTestNeeded(YutTestRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.YUT_TEST_NEEDED, request, 
            "테스트 모드: " + request.getPlayerName() + "의 윷 결과를 선택해주세요.");
    }
    
    public void registerBranchSelectionFuture(String requestId, CompletableFuture<BranchSelectionResponse> future) {
        pendingBranchSelections.put(requestId, future);
    }
    
    public void registerTokenSelectionFuture(String requestId, CompletableFuture<TokenSelectionResponse> future) {
        pendingTokenSelections.put(requestId, future);
    }
    
    public void registerYutTestSelectionFuture(String requestId, CompletableFuture<YutTestResponse> future) {
        pendingYutTestSelections.put(requestId, future);
    }
    
    public void registerReorderSelectionFuture(String requestId, CompletableFuture<ReorderResponse> future) {
        pendingReorderSelections.put(requestId, future);
    }
    
    private Token getCurrentPlayerActiveToken() {
        if (currentPlayer == null) return null;
        
        for (Token token : currentPlayer.getTokens()) {
            if (token.getState() == TokenState.ACTIVE) {
                return token;
            }
        }
        return null;
    }
    
    private String getYutName(int steps) {
        if (steps < 0) {
            return "빽도";
        } else {
            String[] yutNames = {"도", "개", "걸", "윷", "모"};
            return yutNames[Math.min(steps - 1, yutNames.length - 1)];
        }
    }

    public YutGameRules.YutThrowResult throwYut() {
        if (YutGameRules.isTestMode()) {
            String requestId = "yutTest_" + (++yutTestRequestCounter);
            YutTestRequest request = new YutTestRequest(currentPlayer.getName(), requestId);
            
            try {
                CompletableFuture<YutTestResponse> future = new CompletableFuture<>();
                registerYutTestSelectionFuture(requestId, future);
                
                notifyYutTestNeeded(request);
                
                YutTestResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
                
                if (response != null && !response.isCancelled()) {
                    int testResult = response.getSelectedYutResult();
                    String yutName = getYutName(testResult);
                    String message = currentPlayer.getName() + ": " + yutName + " (" + testResult + "칸)";
                    
                    YutGameRules.YutThrowResult result = new YutGameRules.YutThrowResult(
                        List.of(testResult), 
                        List.of(message)
                    );
                    
                    eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, result, message);
                    
                    return result;
                }
                
            } catch (Exception e) {
                System.err.println("테스트 윷 선택 대기 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "테스트 윷 선택에 실패했습니다.");
            }
            
            return new YutGameRules.YutThrowResult(List.of(1), List.of(currentPlayer.getName() + ": 도 (1칸)"));
        } else {
            YutGameRules.YutThrowResult result = YutGameRules.accumulateYut(currentPlayer);
            
            for (String message : result.getResultMessages()) {
                eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, result, message);
            }
            
            return result;
        }
    }
    
    public void nextTurn() {
        int currentIndex = players.indexOf(currentPlayer);
        currentIndex = (currentIndex + 1) % players.size();
        Player previousPlayer = currentPlayer;
        currentPlayer = players.get(currentIndex);
        
        eventNotifier.notifyEvent(GameEvent.Type.TURN_CHANGED,
            previousPlayer.getName() + " → " + currentPlayer.getName() + " 차례입니다.");
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
            YutGameRules.MoveResult errorResult = new YutGameRules.MoveResult(false, false, false, "게임이 종료되었습니다.");
            eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, errorResult.getMessage());
            return errorResult;
        }

        YutGameRules.MoveResult result;
        if (steps < 0) {
            if (token.getState() != TokenState.ACTIVE) {
                result = new YutGameRules.MoveResult(false, false, false, "대기 중인 말은 빽도로 이동할 수 없습니다.");
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, result.getMessage());
                return result;
            }
            result = YutGameRules.moveTokenBackward(token, Math.abs(steps), tokenPositionManager);
        } else {
            if (token.getState() == TokenState.READY) {
                tokenPositionManager.placeTokenAtStart(token);
            }
            result = YutGameRules.moveToken(token, steps, tokenPositionManager, branchSelector);
        }
        
        if (result.isSuccess()) {
            eventNotifier.notifyEvent(GameEvent.Type.MOVE_RESULT, result, result.getMessage());
            
        } else {
            eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, result.getMessage());
        }
        
        return result;
    }
    
    public boolean checkVictory(Player player) {
        if (player.hasFinished()) {
            phase = GamePhase.FINISHED;

            eventNotifier.notifyEvent(GameEvent.Type.GAME_ENDED, player,
                player.getName() + "님이 승리했습니다!");
            return true;
        }
        return false;
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public TokenPositionManager getTokenPositionManager() {
        return tokenPositionManager;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void startGame() {
        this.phase = GamePhase.IN_PROGRESS;
    }

    private boolean processOneMove(int steps) {
        try {
            List<Token> availableTokens = getMovableTokens(steps);
            
            if (availableTokens.isEmpty()) {
                String errorMsg = steps < 0 ? 
                    "모든 말이 대기 중입니다. 빽도는 적용되지 않습니다." : 
                    "이동할 수 있는 말이 없습니다.";
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, errorMsg);
                return false;
            }
            
            String requestId = "token_" + (++tokenRequestCounter);
            TokenSelectionRequest request = new TokenSelectionRequest(availableTokens, steps, requestId);
            
            CompletableFuture<TokenSelectionResponse> future = new CompletableFuture<>();
            registerTokenSelectionFuture(requestId, future);
            
            notifyTokenSelectionNeeded(request);
            
            TokenSelectionResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
            
            if (response == null || response.isCancelled()) {
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "토큰 선택이 취소되어 이동을 중단합니다.");
                return false;
            }
            
            Token selectedToken = response.getSelectedToken();
            
            Function<List<BoardNode>, BoardNode> branchSelector = this::handleBranchSelection;
            
            YutGameRules.MoveResult moveResult = moveToken(selectedToken, steps, branchSelector);
            
            return moveResult.isCatched();
            
        } catch (Exception e) {
            System.err.println("단일 이동 처리 중 오류: " + e.getMessage());
            eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "이동 처리 중 오류: " + e.getMessage());
            return false;
        }
    }
    
    private BoardNode handleBranchSelection(List<BoardNode> nextNodes) {
        if (nextNodes.size() <= 1) {
            return nextNodes.get(0);
        }
        
        try {
            String requestId = "branch_" + (++branchRequestCounter);
            Token currentToken = getCurrentPlayerActiveToken();
            BranchSelectionRequest request = new BranchSelectionRequest(currentToken, nextNodes, requestId);
            
            CompletableFuture<BranchSelectionResponse> future = new CompletableFuture<>();
            registerBranchSelectionFuture(requestId, future);
            
            notifyBranchSelectionNeeded(request);
            
            BranchSelectionResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
            
            if (response != null && !response.isCancelled()) {
                return response.getSelectedBranch();
            }
            
        } catch (Exception e) {
            System.err.println("분기 선택 대기 중 오류: " + e.getMessage());
        }
        
        return nextNodes.get(0);
    }

    public void startYutProcess() {
        currentTurnResults.clear();
        
        if (hasAdditionalTurn) {
            hasAdditionalTurn = false;
        }
        
        if (YutGameRules.isTestMode()) {
            startYutProcessInternal();
        } else {
            startNormalModeYutProcess();
        }
    }
    
    private void startNormalModeYutProcess() {
        CompletableFuture.runAsync(() -> {
            try {
                YutGameRules.YutThrowResult throwResult = YutGameRules.accumulateYut(currentPlayer);
                
                currentTurnResults.addAll(throwResult.getResults());
                
                for (String message : throwResult.getResultMessages()) {
                    eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, throwResult, message);
                    
                    if (!waitForMessageConfirmation()) {
                        return;
                    }
                }
                
                processAccumulatedResults();
                
            } catch (Exception e) {
                System.err.println("일반 모드 윷 프로세스 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "윷 프로세스 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }
    
    private void startYutProcessInternal() {
        CompletableFuture.runAsync(() -> {
            try {
                YutGameRules.YutThrowResult throwResult = throwYut();
                
                currentTurnResults.addAll(throwResult.getResults());
                
                if (waitForMessageConfirmation()) {
                    boolean hasYutMoContinue = throwResult.getResults().stream()
                            .anyMatch(step -> step == 4 || step == 5);
                    
                    if (hasYutMoContinue) {
                        startYutProcessInternal();
                    } else {
                        processAccumulatedResults();
                    }
                }
                
            } catch (Exception e) {
                System.err.println("테스트 모드 윷 프로세스 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "윷 프로세스 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }
    
    public void handleMessageConfirmed() {
        if (pendingMessageConfirmation != null) {
            pendingMessageConfirmation.complete(null);
            pendingMessageConfirmation = null;
        }
    }
    
    private boolean waitForMessageConfirmation() {
        try {
            pendingMessageConfirmation = new CompletableFuture<>();
            pendingMessageConfirmation.get(30, java.util.concurrent.TimeUnit.MINUTES);
            return true;
            
        } catch (Exception e) {
            System.err.println("메시지 확인 대기 중 오류: " + e.getMessage());
            return false;
        }
    }

    private void processAccumulatedResults() {
        CompletableFuture.runAsync(() -> {
            try {
                List<Integer> orderedResults;
                if (currentTurnResults.size() <= 1) {
                    orderedResults = new ArrayList<>(currentTurnResults);
                } else {
                    String requestId = "reorder_" + (++reorderRequestCounter);
                    ReorderRequest request = new ReorderRequest(currentTurnResults, currentPlayer.getName(), requestId);
                    
                    try {
                        CompletableFuture<ReorderResponse> future = new CompletableFuture<>();
                        registerReorderSelectionFuture(requestId, future);
                        
                        notifyReorderNeeded(request);
                        
                        ReorderResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
                        
                        if (response != null && !response.isCancelled()) {
                            orderedResults = response.getReorderedResults();
                        } else {
                            orderedResults = new ArrayList<>(currentTurnResults);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("재배열 대기 중 오류: " + e.getMessage());
                        orderedResults = new ArrayList<>(currentTurnResults);
                    }
                }
                
                boolean anyCaught = false; // 한 턴에 말을 잡았는지 여부 (개수 무관)
                for (int step : orderedResults) {
                    boolean stepCaught = processOneMove(step);
                    if (stepCaught) {
                        anyCaught = true; // 말을 잡았음을 표시
                    }
                    
                    if (checkVictory(currentPlayer)) {
                        return;
                    }
                }
                
                if (!anyCaught) {
                    nextTurn();
                } else {
                    hasAdditionalTurn = true;
                    String additionalTurnMessage = "상대방의 말을 잡아서 1턴을 더 얻습니다!";
                    eventNotifier.notifyEvent(GameEvent.Type.TOKEN_CAUGHT, additionalTurnMessage);
                }
                
            } catch (Exception e) {
                System.err.println("이동 처리 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "이동 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    public void handleReorderSelection(ReorderResponse response) {
        CompletableFuture<ReorderResponse> future = pendingReorderSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingReorderSelections.remove(response.getRequestId());
        }
    }
    
    public void notifyReorderNeeded(ReorderRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.REORDER_NEEDED, request, 
            "윷 결과 재배열이 필요합니다: " + request.getPromptMessage());
    }
}

enum GamePhase {
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED
}