package com.cas.yutnorifx.view;

import com.cas.yutnorifx.controller.GameController;
import com.cas.yutnorifx.model.YutGameRules;
import com.cas.yutnorifx.model.GameState;

import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//게임을 시작하며 사용자가 게임 관련 초기설정을 하는 class
public class GameLauncher {
    /**
     * 게임에 필요한 정보 수집 이후 플레이
     * 1. 보드 각형
     * 2. 테스트 모드 여부
     * 3. 플레이어 수
     * 4. 플레이어가 사용할 말 개수
     * 5. 플레이어 이름
     */
    public void start() {
        javafx.application.Platform.runLater(() -> {
            // 보드 커스터마이징하기
            int sides = boardCustom();

            // 테스트 모드 여부 입력받기
            boolean testMode = getTestMode();
            YutGameRules.setTestMode(testMode);

            // 플레이어 정보 설정 후 플레이어 수와 토큰 수 입력받기
            List<String> playerNames = new ArrayList<>();
            List<Integer> tokenCounts = new ArrayList<>();
            int numPlayers = getPlayerCount();
            int numTokens = getTokenCount();

            // 플레이어 이름 입력받기
            Set<String> usedNames = new HashSet<>();
            for (int i = 1; i <= numPlayers; i++) {
                while (true) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("플레이어 이름 입력");
                    dialog.setContentText("플레이어 " + i + "의 이름을 입력하세요:");
                    
                    String name = dialog.showAndWait().orElse(null);
                    if (name == null) System.exit(0);
                    name = name.trim();
                    
                    if (name.isEmpty()) {
                        showError("이름은 필수입니다.");
                    } else if (usedNames.contains(name)) {
                        showError("중복 이름입니다.");
                    } else {
                        usedNames.add(name);
                        playerNames.add(name);
                        tokenCounts.add(numTokens);
                        break;
                    }
                }
            }

            // GameState 생성 (이때, Board와 Player들이 자동으로 생성됨)
            GameState gameState = new GameState(sides, 2.0f, playerNames, tokenCounts);

            // 게임 화면 생성
            InGameView inGameView = new InGameView(gameState.getBoard().getNodes(), gameState.getPlayers());

            // 게임 컨트롤러 생성
            GameController controller = new GameController(gameState, inGameView);
            
            // Controller와 View 연결
            inGameView.setOnRollYut(() -> controller.rollingYut());
            
            // Application 레벨 콜백 설정
            controller.setOnGameRestart(() -> restartApplication());
            controller.setOnGameExit(() -> exitApplication());

            // 메인 프레임 생성
            Stage stage = new Stage();
            stage.setTitle("윷놀이");
            stage.setScene(new Scene(inGameView.getRoot()));
            stage.show();
            
            this.currentStage = stage;
        });
    }

    // 현재 스테이지
    private Stage currentStage;

    // 게임 재시작 처리
    private void restartApplication() {
        if (currentStage != null) {
            currentStage.close();
        }
        start(); // 새 게임 시작
    }

    // 게임 종료 처리
    private void exitApplication() {
        System.exit(0);
    }

    //사용자에게 n각형 커스터마이징을 입력받는 메서드
    private int boardCustom() {
        TextInputDialog dialog = new TextInputDialog("4");
        dialog.setTitle("보드 커스터마이징");
        dialog.setContentText("몇 각형 보드로 커스텀할까요? (권장 4-6)");
        
        while (true) {
            String result = dialog.showAndWait().orElse(null);
            if (result == null) System.exit(0);

            try {
                int sides = Integer.parseInt(result);
                if (sides >= 4 && sides <= 6) return sides;
            } catch (NumberFormatException e) {
                // 4 이상 6 이하가 아닌 경우 무시하고 다시 입력받음 (이 설정 없을 시 6 이상의 n각형도 생성 후 플레이 가능)
            }
        }
    }

    // 테스트 모드 여부를 사용자에게 물어보고 반환하는 메서드
    private boolean getTestMode() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("게임 모드 선택");
        alert.setContentText("테스트 모드로 진행하시겠습니까?");
        
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        return alert.showAndWait()
                .filter(buttonType -> buttonType == yesButton)
                .isPresent();
    }

    //플레이어 수를 입력받는 메서드 (2-4명)
    private int getPlayerCount() {
        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setTitle("플레이어 수 입력");
        dialog.setContentText("플레이어 수를 입력하세요 (2 - 4명)");
        
        while (true) {
            String result = dialog.showAndWait().orElse(null);
            if (result == null) System.exit(0);
            try {
                int count = Integer.parseInt(result);
                if (count >= 2 && count <= 4) return count;
            } catch (NumberFormatException e) {
                // 2-4명이 아닌 경우 무시하고 다시 입력받음
            }
        }
    }

    //사용할 말의 갯수를 입력 받는 메서드 (2-5명)
    private int getTokenCount() {
        TextInputDialog dialog = new TextInputDialog("4");
        dialog.setTitle("말 갯수 설정");
        dialog.setContentText("플레이어가 사용할 말의 갯수를 입력하세요. (2 - 5개)");
        
        while (true) {
            String result = dialog.showAndWait().orElse(null);
            if (result == null) System.exit(0);
            try {
                int count = Integer.parseInt(result.trim());
                if (count >= 2 && count <= 5) return count;
            } catch (NumberFormatException e) {
                // 2-5개가 아닌 경우 무시하고 다시 입력받음
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("입력 오류");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
