package com.cas.yutnorifx.controller;

import com.cas.yutnorifx.model.core.GameState;
import com.cas.yutnorifx.model.request.*;
import com.cas.yutnorifx.view.GameEndChoice;
//test
//Observer 패턴 적용을 간소화된 Controller <- 기존에는 Controller의 비중이 큰 전통적인 MVC 패턴이었음.
//Model의 상태 변화는 Observer를 통해 View에 자동 전달되게 함.
//Controller는 단순히 사용자 액션을 Model에 전달하는 역할만 수행
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
    // Observer 패턴 적용 후 rollingYut 간소화 -> Model이 모든 처리를 담당
    public void rollingYut() {
        // Model에게 "윷 던지기" 신호만 전달
        // Model이 스스로 순차적으로 모든 처리를 담당
        gameState.startYutProcess();
    }

    // 게임 종료 처리 - View에서 콜백을 통해 호출
    public void handleGameEndChoice(GameEndChoice choice) {
        //재시작
        if (choice == GameEndChoice.RESTART) {
            if (onGameRestart != null) {
                onGameRestart.run();
            }
        } else {//종료
            if (onGameExit != null) {
                onGameExit.run();
            }
        }
    }
    
    // 분기 선택 - View에서 콜백을 통해 호출
    public void handleBranchSelection(BranchSelectionResponse response) {
        gameState.handleBranchSelection(response);
    }
    
    // 토큰 선택 - View에서 콜백을 통해 호출
    public void handleTokenSelection(TokenSelectionResponse response) {
        gameState.handleTokenSelection(response);
    }

    // 테스트 모드 선택  - View에서 콜백을 통해 호출
    public void handleYutTestSelection(YutTestResponse response) {
        gameState.handleYutTestSelection(response);
    }

    // 메시지 확인 완료 - View에서 콜백을 통해 호출
    public void handleMessageConfirmed() {
        // Model에게 사용자가 메시지 확인했음을 알림
        gameState.handleMessageConfirmed();
    }

    // 재배열 선택 - View에서 콜백을 통해 호출
    public void handleReorderSelection(ReorderResponse response) {
        gameState.handleReorderSelection(response);
    }
}
