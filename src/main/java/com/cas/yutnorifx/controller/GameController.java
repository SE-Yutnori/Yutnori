package com.cas.yutnorifx.controller;

import com.cas.yutnorifx.model.BoardNode;
import com.cas.yutnorifx.model.GameState;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.Token;
import com.cas.yutnorifx.model.YutGameRules;
import com.cas.yutnorifx.view.GameEndChoice;
import com.cas.yutnorifx.view.InGameView;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private final GameState gameState;
    private final InGameView view;
    
    // Application 레벨 콜백
    private Runnable onGameRestart;
    private Runnable onGameExit;

    public GameController(GameState gameState, InGameView view) {
        this.gameState = gameState;
        this.view = view;
    }
    
    // Application 콜백 설정
    public void setOnGameRestart(Runnable callback) {
        this.onGameRestart = callback;
    }
    
    public void setOnGameExit(Runnable callback) {
        this.onGameExit = callback;
    }

    // "윷 던지기" 버튼 클릭 시 호출되는 메서드
    public void rollingYut() {
        Player currentPlayer = gameState.getCurrentPlayer();

        // 1. 윷 던지기 (각 모/윷마다 즉시 처리)
        List<Integer> allResults = new ArrayList<>();
        
        if (YutGameRules.isTestMode()) {
            // 테스트 모드: 각 던지기마다 사용자 선택 및 확인
            handleTestModeThrows(currentPlayer, allResults);
        } else {
            // 일반 모드: 각 던지기마다 자동 던지기 및 확인
            handleNormalModeThrows(currentPlayer, allResults);
        }

        if (allResults.isEmpty()) {
            return; // 취소됨
        }

        // 2. 순서 재배열 처리
        List<Integer> orderedResults;
        if (allResults.size() == 1) {
            orderedResults = allResults;
        } else {
            orderedResults = handleReorderResults(allResults, currentPlayer.getName());
            if (orderedResults == null || orderedResults.isEmpty()) {
                return; // 취소됨
            }
        }

        // 3. 이동 처리
        boolean catched = handleMoveExecution(currentPlayer, orderedResults);

        // 4. 승리 조건 확인
        if (gameState.isGameEnded()) {
            handleGameEnd(gameState.getWinner());
            return;
        }

        // 5. 다음 턴 결정
        if (catched) {
            view.showMessage(currentPlayer.getName() + "님이 말을 잡아 추가 턴을 얻었습니다!", "추가 턴");
        } else {
            gameState.nextPlayer();
        }
    }
    
    // 테스트 모드 던지기 처리
    private void handleTestModeThrows(Player currentPlayer, List<Integer> allResults) {
        int throwCount = 0;
        boolean continueThrow = true;
        
        while (continueThrow) {
            int testResult = view.getTestYutThrow();
            if (testResult == -999) { // 취소
                return;
            }
            
            String yutName = getYutName(testResult);
            String message;
            if (throwCount == 0) {
                message = currentPlayer.getName() + ": " + yutName + " (" + testResult + "칸)";
            } else {
                message = "한번 더! " + yutName + " (" + testResult + "칸)";
            }
            
            // 즉시 결과 표시
            view.showMessage(message, "윷 결과");
            allResults.add(testResult);
            throwCount++;
            
            // 윷이나 모가 나오면 추가 기회 메시지
            if (testResult >= 4) {
                view.showMessage(yutName + "가 나왔습니다! 한번 더 기회가 주어집니다!", "추가 기회");
            } else {
                continueThrow = false;
            }
        }
    }
    
    // 일반 모드 던지기 처리
    private void handleNormalModeThrows(Player currentPlayer, List<Integer> allResults) {
        int throwCount = 0;
        boolean continueThrow = true;
        
        while (continueThrow) {
            // 윷 던지기 실행
            int result = YutGameRules.throwSingleYut().getResults().get(0); // 단일 던지기
            
            String yutName = getYutName(result);
            String message;
            if (throwCount == 0) {
                message = currentPlayer.getName() + ": " + yutName + " (" + result + "칸)";
            } else {
                message = "한번 더! " + yutName + " (" + result + "칸)";
            }
            
            // 즉시 결과 표시
            view.showMessage(message, "윷 결과");
            allResults.add(result);
            throwCount++;
            
            // 윷이나 모가 나오면 추가 기회 메시지
            if (result >= 4) {
                view.showMessage(yutName + "가 나왔습니다! 한번 더 기회가 주어집니다!", "추가 기회");
            } else {
                continueThrow = false;
            }
        }
    }

    // 순서 재배열 처리
    private List<Integer> handleReorderResults(List<Integer> results, String playerName) {
        YutGameRules.ReorderRequest request = new YutGameRules.ReorderRequest(results, playerName);
        
        while (true) {
            String input = view.requestInput(request.getPromptMessage(), "윷 순서 재배열");
            if (input == null) {
                return null; // 취소
            }
            
            YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(input, results);
            if (result.isSuccess()) {
                return result.getReorderedResults();
            } else {
                view.showError(result.getErrorMessage());
            }
        }
    }

    // 이동 실행 처리
    private boolean handleMoveExecution(Player player, List<Integer> steps) {
        boolean overallCatched = false;
        
        for (int i = 0; i < steps.size(); i++) {
            int step = steps.get(i);
            
            // 1. 이동 가능한 토큰 계산
            List<Token> availableTokens = gameState.getMovableTokens(step);
            
            if (availableTokens.isEmpty()) {
                if (step < 0) {
                    view.showError("모든 말이 대기 중입니다. 빽도는 적용되지 않습니다.");
                } else {
                    view.showError("이동할 수 있는 말이 없습니다.");
                }
                continue;
            }
            
            // 2. View에서 사용자 토큰 선택
            Token selectedToken = view.selectToken(availableTokens, step);
            if (selectedToken == null) {
                view.showError("말을 선택하지 않아 이동을 중단합니다.");
                return overallCatched;
            }
            
            // 3. 토큰 선택 후 현재 위치가 분기점인지 확인하고 분기 선택
            Token actualToken = selectedToken.getTopMostToken();
            BoardNode currentPosition = gameState.getTokenPositionManager().getTokenPosition(actualToken);
            
            // 빽도가 아니고 현재 위치가 분기점인 경우에만 분기 선택
            if (step > 0 && currentPosition != null && currentPosition.getNextNodes().size() > 1) {
                // 현재 위치가 분기점이면 이동 방향 선택
                BoardNode chosenPath = view.selectPath(currentPosition.getNextNodes());
                actualToken.setNextBranchChoice(chosenPath);
                view.showMessage(getPathDescription(chosenPath) + " 경로를 선택했습니다.", "경로 선택");
            }
            
            // 4. Model에서 이동 실행
            YutGameRules.MoveResult moveResult = gameState.moveToken(selectedToken, step, options -> {
                // 분기점에서 사용자가 경로 선택 (1칸 이동에서만 호출됨)
                return view.selectPath(options);
            });
            
            // 5. View에 결과 반영
            if (!moveResult.isSuccess()) {
                view.showError(moveResult.getMessage());
                continue;
            }
            
            if (moveResult.isCatched()) {
                overallCatched = true;
            }
            
            if (!moveResult.getMessage().isEmpty()) {
                view.showMessage(moveResult.getMessage(), "이동 결과");
            }
            
            view.refresh();
            
            // 6. 각 이동 완료 후 즉시 승리 조건 확인
            Player currentPlayer = gameState.getCurrentPlayer();
            if (gameState.checkVictory(currentPlayer)) {
                handleGameEnd(currentPlayer);
                return overallCatched; // 게임 종료
            }
            
            // 게임 종료 확인 (기존 로직 유지)
            if (gameState.isGameEnded()) {
                break;
            }
        }
        
        return overallCatched;
    }

    // 경로 설명을 위한 헬퍼 메서드
    private String getPathDescription(BoardNode node) {
        if (node.getName().contains("Edge")) {
            return "외곽";
        } else if (node.getName().contains("ToCenter")) {
            return "중앙";
        } else {
            return node.getName();
        }
    }

    // 게임 종료 처리
    private void handleGameEnd(Player winner) {
        GameEndChoice choice = view.getGameEndChoice(winner.getName() + " 승리!");
        
        if (choice == GameEndChoice.RESTART) {
            if (onGameRestart != null) {
                onGameRestart.run();
            }
        } else {
            if (onGameExit != null) {
                onGameExit.run();
            }
        }
    }
    
    // 윷 이름 변환 유틸리티
    private String getYutName(int steps) {
        if (steps < 0) {
            return "빽도";
        } else {
            String[] yutNames = {"도", "개", "걸", "윷", "모"};
            return yutNames[steps - 1];
        }
    }
}
