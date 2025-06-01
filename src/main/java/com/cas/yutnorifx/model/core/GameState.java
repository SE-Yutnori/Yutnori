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

//게임의 전반적인 상태를 관리하는 클래스
public class GameState {
    private final List<Player> players;
    private final TokenPositionManager tokenPositionManager;
    private final Board board;
    private Player currentPlayer;
    private GamePhase phase;

    // Observer 패턴 추가
    private final GameEventNotifier eventNotifier;
    
    // 분기 선택 관리
    private final Map<String, CompletableFuture<BranchSelectionResponse>> pendingBranchSelections = new ConcurrentHashMap<>();
    
    // 토큰 선택 관리
    private final Map<String, CompletableFuture<TokenSelectionResponse>> pendingTokenSelections = new ConcurrentHashMap<>();
    
    // 테스트 윷 선택 관리
    private final Map<String, CompletableFuture<YutTestResponse>> pendingYutTestSelections = new ConcurrentHashMap<>();
    
    // 재배열 선택 관리
    private final Map<String, CompletableFuture<ReorderResponse>> pendingReorderSelections = new ConcurrentHashMap<>();
    
    private int branchRequestCounter = 0;
    private int tokenRequestCounter = 0;
    private int yutTestRequestCounter = 0;
    private int reorderRequestCounter = 0;
    
    // 메시지 확인 대기 관리
    private CompletableFuture<Void> pendingMessageConfirmation = null;
    
    // 현재 턴의 누적 윷 결과 관리
    private List<Integer> currentTurnResults = new ArrayList<>();
    
    // 추가 턴 관리 (boolean으로 간단하게)
    private boolean hasAdditionalTurn = false;

    // 게임 초기화를 위한 생성자
    public GameState(int sides, float radius, List<String> playerNames, List<Integer> tokenCounts) {
        this.board = new Board(sides, radius);
        this.tokenPositionManager = new TokenPositionManager(board);
        this.players = new ArrayList<>();
        this.phase = GamePhase.NOT_STARTED;
        this.eventNotifier = new GameEventNotifier();

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
    
    // Observer 관리 메서드들
    public void addObserver(GameEventObserver observer) {
        eventNotifier.addObserver(observer);
    }

    // 분기 선택 응답 처리
    public void handleBranchSelection(BranchSelectionResponse response) {
        CompletableFuture<BranchSelectionResponse> future = pendingBranchSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingBranchSelections.remove(response.getRequestId());
        }
    }
    
    // 토큰 선택 응답 처리
    public void handleTokenSelection(TokenSelectionResponse response) {
        CompletableFuture<TokenSelectionResponse> future = pendingTokenSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingTokenSelections.remove(response.getRequestId());
        }
    }
    
    // 테스트 윷 선택 응답 처리
    public void handleYutTestSelection(YutTestResponse response) {
        CompletableFuture<YutTestResponse> future = pendingYutTestSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingYutTestSelections.remove(response.getRequestId());
        }
    }
    
    // Observer 패턴을 통한 분기 선택 필요 이벤트 발생
    public void notifyBranchSelectionNeeded(BranchSelectionRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.BRANCH_SELECTION_NEEDED, request, 
            "분기점 도달: " + request.getToken().getName() + "의 경로를 선택해주세요.");
    }
    
    // Observer 패턴을 통한 토큰 선택 필요 이벤트 발생
    public void notifyTokenSelectionNeeded(TokenSelectionRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.TOKEN_SELECTION_NEEDED, request, 
            request.getSteps() + "칸 이동할 토큰을 선택해주세요.");
    }
    
    // Observer 패턴을 통한 테스트 윷 선택 필요 이벤트 발생
    public void notifyYutTestNeeded(YutTestRequest request) {
        eventNotifier.notifyEvent(GameEvent.Type.YUT_TEST_NEEDED, request, 
            "테스트 모드: " + request.getPlayerName() + "의 윷 결과를 선택해주세요.");
    }
    
    // 비동기 분기 선택을 위한 Future 등록
    public void registerBranchSelectionFuture(String requestId, CompletableFuture<BranchSelectionResponse> future) {
        pendingBranchSelections.put(requestId, future);
    }
    
    // 비동기 토큰 선택을 위한 Future 등록
    public void registerTokenSelectionFuture(String requestId, CompletableFuture<TokenSelectionResponse> future) {
        pendingTokenSelections.put(requestId, future);
    }
    
    // 비동기 테스트 윷 선택을 위한 Future 등록
    public void registerYutTestSelectionFuture(String requestId, CompletableFuture<YutTestResponse> future) {
        pendingYutTestSelections.put(requestId, future);
    }
    
    // 비동기 재배열 선택을 위한 Future 등록
    public void registerReorderSelectionFuture(String requestId, CompletableFuture<ReorderResponse> future) {
        pendingReorderSelections.put(requestId, future);
    }
    
    // 현재 플레이어의 활성 토큰 중 하나 반환
    private Token getCurrentPlayerActiveToken() {
        if (currentPlayer == null) return null;
        
        for (Token token : currentPlayer.getTokens()) {
            if (token.getState() == TokenState.ACTIVE) {
                return token;
            }
        }
        return null;
    }
    
    // 윷 이름 변환 유틸리티
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
            // 테스트 모드: Observer 패턴으로 View에서 선택받기
            String requestId = "yutTest_" + (++yutTestRequestCounter);
            YutTestRequest request = new YutTestRequest(currentPlayer.getName(), requestId);
            
            try {
                // 1. CompletableFuture 생성하고 등록
                CompletableFuture<YutTestResponse> future = new CompletableFuture<>();
                registerYutTestSelectionFuture(requestId, future);
                
                // 2. Observer 이벤트 발생
                notifyYutTestNeeded(request);
                
                // 3. 결과가 올 때까지 AWAIT (최대 30분)
                YutTestResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
                
                if (response != null && !response.isCancelled()) {
                    int testResult = response.getSelectedYutResult();
                    String yutName = getYutName(testResult);
                    String message = currentPlayer.getName() + ": " + yutName + " (" + testResult + "칸)";
                    
                    YutGameRules.YutThrowResult result = new YutGameRules.YutThrowResult(
                        List.of(testResult), 
                        List.of(message)
                    );
                    
                    // 윷 결과 이벤트 발생
                    eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, result, message);
                    
                    return result;
                }
                
            } catch (Exception e) {
                System.err.println("테스트 윷 선택 대기 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "테스트 윷 선택에 실패했습니다.");
            }
            
            // 실패시 기본값
            return new YutGameRules.YutThrowResult(List.of(1), List.of(currentPlayer.getName() + ": 도 (1칸)"));
        } else {
            // 일반 모드 - 기존 로직 유지
            YutGameRules.YutThrowResult result = YutGameRules.accumulateYut(currentPlayer);
            
            // 윷 결과를 Observer들에게 통지
            for (String message : result.getResultMessages()) {
                eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, result, message);
            }
            
            return result;
        }
    }
    
    public void nextTurn() {
        // 일반 턴 변경만 수행 (추가 턴은 startYutProcess에서 처리)
        int currentIndex = players.indexOf(currentPlayer);
        currentIndex = (currentIndex + 1) % players.size();
        Player previousPlayer = currentPlayer;
        currentPlayer = players.get(currentIndex);
        
        // 턴 변경 이벤트 통지
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
            // Controller에서 받은 branchSelector 사용
            result = YutGameRules.moveToken(token, steps, tokenPositionManager, branchSelector);
        }
        
        // 이동 결과 이벤트 통지
        if (result.isSuccess()) {
            // 이동이 성공했으면 메시지와 상관없이 항상 이벤트 발생 (View refresh 용)
            eventNotifier.notifyEvent(GameEvent.Type.MOVE_RESULT, result, result.getMessage());
            
            // 말 잡기 이벤트는 processAccumulatedResults()에서만 처리하도록 제거
            // (중복 추가 턴 방지)
        } else {
            eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, result.getMessage());
        }
        
        return result;
    }
    
    public boolean checkVictory(Player player) {
        if (player.hasFinished()) {
            phase = GamePhase.FINISHED;

            // 게임 종료 이벤트 통지
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

    // 한 번의 이동 처리 (Observer 패턴)
    private boolean processOneMove(int steps) {
        try {
            // 1. 이동 가능한 토큰 계산
            List<Token> availableTokens = getMovableTokens(steps);
            
            if (availableTokens.isEmpty()) {
                String errorMsg = steps < 0 ? 
                    "모든 말이 대기 중입니다. 빽도는 적용되지 않습니다." : 
                    "이동할 수 있는 말이 없습니다.";
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, errorMsg);
                return false;
            }
            
            // 2. Observer 패턴으로 토큰 선택 요청
            String requestId = "token_" + (++tokenRequestCounter);
            TokenSelectionRequest request = new TokenSelectionRequest(availableTokens, steps, requestId);
            
            // CompletableFuture로 토큰 선택 대기
            CompletableFuture<TokenSelectionResponse> future = new CompletableFuture<>();
            registerTokenSelectionFuture(requestId, future);
            
            // Observer 이벤트 발생
            notifyTokenSelectionNeeded(request);
            
            // 결과 대기 (최대 30분)
            TokenSelectionResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
            
            if (response == null || response.isCancelled()) {
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "토큰 선택이 취소되어 이동을 중단합니다.");
                return false;
            }
            
            Token selectedToken = response.getSelectedToken();
            
            // 3. 분기 선택 함수 생성 (Observer 패턴)
            Function<List<BoardNode>, BoardNode> branchSelector = this::handleBranchSelection;
            
            // 4. Model에서 이동 실행
            YutGameRules.MoveResult moveResult = moveToken(selectedToken, steps, branchSelector);
            
            return moveResult.isCatched();
            
        } catch (Exception e) {
            System.err.println("단일 이동 처리 중 오류: " + e.getMessage());
            eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "이동 처리 중 오류: " + e.getMessage());
            return false;
        }
    }
    
    // 분기 선택 처리 (Observer 패턴)
    private BoardNode handleBranchSelection(List<BoardNode> nextNodes) {
        if (nextNodes.size() <= 1) {
            return nextNodes.get(0);
        }
        
        try {
            // Observer 패턴으로 분기 선택 요청
            String requestId = "branch_" + (++branchRequestCounter);
            Token currentToken = getCurrentPlayerActiveToken();
            BranchSelectionRequest request = new BranchSelectionRequest(currentToken, nextNodes, requestId);
            
            // CompletableFuture로 분기 선택 대기
            CompletableFuture<BranchSelectionResponse> future = new CompletableFuture<>();
            registerBranchSelectionFuture(requestId, future);
            
            // Observer 이벤트 발생
            notifyBranchSelectionNeeded(request);
            
            // 결과 대기 (최대 30분)
            BranchSelectionResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
            
            if (response != null && !response.isCancelled()) {
                return response.getSelectedBranch();
            }
            
        } catch (Exception e) {
            System.err.println("분기 선택 대기 중 오류: " + e.getMessage());
        }
        
        // 실패시 첫 번째 옵션
        return nextNodes.get(0);
    }

    // 윷 프로세스 시작 (Controller에서 호출)
    public void startYutProcess() {
        // 새 턴 시작시 누적 결과 초기화
        currentTurnResults.clear();
        
        // 추가 턴을 사용하는 경우 플래그 미리 초기화 (중복 추가 턴 방지)
        if (hasAdditionalTurn) {
            hasAdditionalTurn = false;
            // 메시지 없이 조용히 추가 턴 시작
        }
        
        if (YutGameRules.isTestMode()) {
            // 테스트 모드: 사용자가 하나씩 선택
            startYutProcessInternal();
        } else {
            // 일반 모드: accumulateYut이 이미 연속 던지기를 처리함
            startNormalModeYutProcess();
        }
    }
    
    // 일반 모드 윷 프로세스 (accumulateYut 사용)
    private void startNormalModeYutProcess() {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. accumulateYut으로 한 번에 연속 던지기 처리
                YutGameRules.YutThrowResult throwResult = YutGameRules.accumulateYut(currentPlayer);
                
                // 2. 결과를 누적 리스트에 추가
                currentTurnResults.addAll(throwResult.getResults());
                
                // 3. 모든 윷 결과 이벤트를 순차적으로 발생
                for (String message : throwResult.getResultMessages()) {
                    eventNotifier.notifyEvent(GameEvent.Type.YUT_THROW_RESULT, throwResult, message);
                    
                    // 각 메시지마다 사용자 확인 대기
                    if (!waitForMessageConfirmation()) {
                        return; // 확인 실패시 중단
                    }
                }
                
                // 4. 모든 메시지 표시 완료 후 이동 처리
                processAccumulatedResults();
                
            } catch (Exception e) {
                System.err.println("일반 모드 윷 프로세스 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "윷 프로세스 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }
    
    // 내부 윷 프로세스 (테스트 모드 전용 - 재귀용)
    private void startYutProcessInternal() {
        // UI 스레드 블로킹 방지를 위해 백그라운드 스레드에서 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 윷 던지기 (테스트 모드에서는 단일 결과만 반환)
                YutGameRules.YutThrowResult throwResult = throwYut();
                
                // 2. 결과를 누적 리스트에 추가
                currentTurnResults.addAll(throwResult.getResults());
                
                // 3. 윷 결과 이벤트 발생 (이미 throwYut에서 발생됨)
                // View에서 메시지를 표시하고 사용자가 확인하면 자동으로 handleMessageConfirmed()가 호출됨
                
                // 4. View에서 사용자 확인을 기다림 - CompletableFuture로 확인 대기
                if (waitForMessageConfirmation()) {
                    // 5. 윷/모로 인한 연속 던지기 확인 (말 잡기 추가 턴과 구분)
                    boolean hasYutMoContinue = throwResult.getResults().stream()
                            .anyMatch(step -> step == 4 || step == 5); // 윷(4) 또는 모(5)
                    
                    if (hasYutMoContinue) {
                        // 윷/모로 인한 연속 던지기 (재귀)
                        startYutProcessInternal();
                    } else {
                        // 연속 던지기 없으면 전체 누적 결과로 재배열 + 이동 처리
                        processAccumulatedResults();
                    }
                }
                
            } catch (Exception e) {
                System.err.println("테스트 모드 윷 프로세스 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "윷 프로세스 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }
    
    // 메시지 확인 완료 처리 (Controller에서 호출)
    public void handleMessageConfirmed() {
        if (pendingMessageConfirmation != null) {
            pendingMessageConfirmation.complete(null);
            pendingMessageConfirmation = null;
        }
    }
    
    // 메시지 확인 대기
    private boolean waitForMessageConfirmation() {
        try {
            pendingMessageConfirmation = new CompletableFuture<>();
            
            // View에서 자동으로 메시지 표시 후 확인 대기
            // 별도 이벤트 발생 불필요 - 이미 throwYut()에서 결과 이벤트 발생됨
            
            // 최대 30분 대기
            pendingMessageConfirmation.get(30, java.util.concurrent.TimeUnit.MINUTES);
            return true;
            
        } catch (Exception e) {
            System.err.println("메시지 확인 대기 중 오류: " + e.getMessage());
            return false;
        }
    }

    // 전체 누적 결과로 재배열 + 이동 처리
    private void processAccumulatedResults() {
        // UI 스레드 블로킹 방지를 위해 백그라운드 스레드에서 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 전체 누적 결과로 재배열 (결과가 2개 이상일 때만)
                List<Integer> orderedResults;
                if (currentTurnResults.size() <= 1) {
                    orderedResults = new ArrayList<>(currentTurnResults);
                } else {
                    // Observer 패턴으로 재배열 요청
                    String requestId = "reorder_" + (++reorderRequestCounter);
                    ReorderRequest request = new ReorderRequest(currentTurnResults, currentPlayer.getName(), requestId);
                    
                    try {
                        // CompletableFuture로 재배열 선택 대기
                        CompletableFuture<ReorderResponse> future = new CompletableFuture<>();
                        registerReorderSelectionFuture(requestId, future);
                        
                        // Observer 이벤트 발생
                        notifyReorderNeeded(request);
                        
                        // 결과 대기 (최대 30분)
                        ReorderResponse response = future.get(30, java.util.concurrent.TimeUnit.MINUTES);
                        
                        if (response != null && !response.isCancelled()) {
                            orderedResults = response.getReorderedResults();
                        } else {
                            // 취소된 경우 원래 순서 사용
                            orderedResults = new ArrayList<>(currentTurnResults);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("재배열 대기 중 오류: " + e.getMessage());
                        orderedResults = new ArrayList<>(currentTurnResults);
                    }
                }
                
                // 2. 각 결과별로 이동 처리
                boolean anyCaught = false; // 한 턴에 말을 잡았는지 여부 (개수 무관)
                for (int step : orderedResults) {
                    boolean stepCaught = processOneMove(step);
                    if (stepCaught) {
                        anyCaught = true; // 말을 잡았음을 표시
                    }
                    
                    // 게임 종료 확인
                    if (checkVictory(currentPlayer)) {
                        return; // 승리시 이후 이동 중단
                    }
                }
                
                // 3. 턴 변경 결정 - 말을 잡았으면 1번의 추가 턴만 부여
                if (!anyCaught) {
                    nextTurn(); // 말을 잡지 않았으면 턴 변경
                } else {
                    // 말을 잡았으면 1번의 추가 턴만 부여
                    hasAdditionalTurn = true;
                    String additionalTurnMessage = "상대방의 말을 잡아서 1턴을 더 얻습니다!";
                    eventNotifier.notifyEvent(GameEvent.Type.TOKEN_CAUGHT, additionalTurnMessage);
                    // 현재 플레이어 유지하고 추가 턴 준비 완료
                }
                
            } catch (Exception e) {
                System.err.println("이동 처리 중 오류: " + e.getMessage());
                eventNotifier.notifyEvent(GameEvent.Type.ERROR_OCCURRED, "이동 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    // 재배열 선택 응답 처리
    public void handleReorderSelection(ReorderResponse response) {
        CompletableFuture<ReorderResponse> future = pendingReorderSelections.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
            pendingReorderSelections.remove(response.getRequestId());
        }
    }
    
    // Observer 패턴을 통한 재배열 필요 이벤트 발생
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