package com.cas.yutnorifx.view.fx;

import com.cas.yutnorifx.model.core.*;
import com.cas.yutnorifx.controller.GameController;

//javafx 관련 클래스
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

//java 관련 클래스
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FXGameLauncher {
    public void start() {
        javafx.application.Platform.runLater(() -> {
            int sides = boardCustom();

            boolean testMode = getTestMode();
            YutGameRules.setTestMode(testMode);

            List<String> playerNames = new ArrayList<>();
            List<Integer> tokenCounts = new ArrayList<>();
            
            int numPlayers = getPlayerCount();
            int numTokens = getTokenCount();
            
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

            GameState gameState = new GameState(sides, 2.0f, playerNames, tokenCounts);

            FXInGameView inGameView = new FXInGameView(gameState.getBoard().getNodes(), gameState.getPlayers());

            GameController controller = new GameController(gameState);
            
            gameState.addObserver(inGameView);
            
            inGameView.setOnRollYut(() -> controller.rollingYut());
            
            inGameView.setOnGameEnd(choice -> controller.handleGameEndChoice(choice));
            
            inGameView.setOnBranchSelection(response -> controller.handleBranchSelection(response));
            
            inGameView.setOnTokenSelection(response -> controller.handleTokenSelection(response));
            
            inGameView.setOnYutTestSelection(response -> controller.handleYutTestSelection(response));
            
            inGameView.setOnMessageConfirmed(() -> controller.handleMessageConfirmed());
            
            inGameView.setOnReorderSelection(response -> controller.handleReorderSelection(response));
            
            controller.setOnGameRestart(() -> restartApplication());
            controller.setOnGameExit(() -> exitApplication());

            Stage stage = new Stage();
            stage.setTitle("윷놀이");
            stage.setScene(new Scene(inGameView.getRoot()));
            stage.show();
            
            this.currentStage = stage;
        });
    }

    private Stage currentStage;

    private void restartApplication() {
        if (currentStage != null) {
            currentStage.close();
        }
        start();
    }

    private void exitApplication() {
        System.exit(0);
    }

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
            }
        }
    }

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
            }
        }
    }

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
