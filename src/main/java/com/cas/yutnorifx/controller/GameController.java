package com.cas.yutnorifx.controller;

import com.cas.yutnorifx.model.GameState;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.Token;
import com.cas.yutnorifx.model.YutGameRules;
import com.cas.yutnorifx.view.GameEndChoice;
import com.cas.yutnorifx.view.InGameView;

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

        // 1. Model에서 윷 결과 계산
        YutGameRules.YutThrowResult throwResult;
        if (YutGameRules.isTestMode()) {
            // 테스트 모드인 경우 View에서 선택받기
            int testResult = view.getTestYutThrow();
            if (testResult == -999) { // 취소
                return;
            }
            // 테스트 결과로 YutThrowResult 생성
            String yutName = getYutName(testResult);
            String message = currentPlayer.getName() + ": " + yutName + " (" + testResult + "칸)";
            throwResult = new YutGameRules.YutThrowResult(
                List.of(testResult), 
                List.of(message)
            );
        } else {
            // 일반 모드
            throwResult = gameState.throwYut();
        }

        // 2. View에 윷 결과 표시
        for (String message : throwResult.getResultMessages()) {
            view.showMessage(message, "윷 결과");
        }

        // 3. 순서 재배열 처리
        List<Integer> orderedResults;
        if (throwResult.getResults().size() == 1) {
            orderedResults = throwResult.getResults();
        } else {
            orderedResults = handleReorderResults(throwResult.getResults(), currentPlayer.getName());
            if (orderedResults == null || orderedResults.isEmpty()) {
                return; // 취소됨
            }
        }

        // 4. 이동 처리
        boolean catched = handleMoveExecution(currentPlayer, orderedResults);

        // 5. 승리 조건 확인
        if (gameState.isGameEnded()) {
            handleGameEnd(gameState.getWinner());
            return;
        }

        // 6. 다음 턴 결정
        if (catched) {
            view.showMessage(currentPlayer.getName() + "님이 말을 잡아 추가 턴을 얻었습니다!", "추가 턴");
        } else {
            gameState.nextPlayer();
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
        
        for (int step : steps) {
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
            
            // 3. Model에서 이동 실행
            YutGameRules.MoveResult moveResult = gameState.moveToken(selectedToken, step, options -> options.get(0));
            
            // 4. View에 결과 반영
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
            
            // 게임 종료 확인
            if (gameState.isGameEnded()) {
                break;
            }
        }
        
        return overallCatched;
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
