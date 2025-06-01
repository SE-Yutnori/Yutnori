package com.cas.yutnorifx.controller;

import com.cas.yutnorifx.model.core.GameState;
import com.cas.yutnorifx.model.request.*;
import com.cas.yutnorifx.view.GameEndChoice;

/**
 * Observer 패턴이 적용된 간소화된 Controller
 * Model의 상태 변화는 Observer를 통해 View에 자동 전달되므로
 * Controller는 단순히 사용자 액션을 Model에 전달하는 역할만 함
 */
public class GameController {
    private final GameState gameState;
    
    // Application 레벨 콜백
    private Runnable onGameRestart;
    private Runnable onGameExit;

    public GameController(GameState gameState) {
        this.gameState = gameState;
    }
    
    // Application 콜백 설정
    public void setOnGameRestart(Runnable callback) {
        this.onGameRestart = callback;
    }
    
    public void setOnGameExit(Runnable callback) {
        this.onGameExit = callback;
    }

    // "윷 던지기" 버튼 클릭 시 호출되는 메서드
    // Observer 패턴으로 대폭 간소화됨 - Model이 모든 처리를 담당
    public void rollingYut() {
        // Model에게 "윷 던지기!" 신호만 전달
        // Model이 스스로 순차적으로 모든 처리를 담당
        gameState.startYutProcess();
    }

    // 게임 종료 처리 - View에서 콜백을 통해 호출됨
    public void handleGameEndChoice(GameEndChoice choice) {
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
    
    // 분기 선택 처리 - View에서 콜백을 통해 호출됨
    public void handleBranchSelection(BranchSelectionResponse response) {
        // CompletableFuture를 통해 대기 중인 스레드에게 결과 전달
        gameState.handleBranchSelection(response);
    }
    
    // 토큰 선택 처리 - View에서 콜백을 통해 호출됨
    public void handleTokenSelection(TokenSelectionResponse response) {
        // CompletableFuture를 통해 대기 중인 스레드에게 결과 전달
        gameState.handleTokenSelection(response);
    }

    // 테스트 윷 선택 처리 - View에서 콜백을 통해 호출됨
    public void handleYutTestSelection(YutTestResponse response) {
        // CompletableFuture를 통해 대기 중인 스레드에게 결과 전달
        gameState.handleYutTestSelection(response);
    }

    // 메시지 확인 완료 처리 - View에서 콜백을 통해 호출됨
    public void handleMessageConfirmed() {
        // Model에게 사용자가 메시지 확인했음을 알림
        gameState.handleMessageConfirmed();
    }

    // 재배열 선택 처리 - View에서 콜백을 통해 호출됨
    public void handleReorderSelection(ReorderResponse response) {
        // CompletableFuture를 통해 대기 중인 스레드에게 결과 전달
        gameState.handleReorderSelection(response);
    }
}
