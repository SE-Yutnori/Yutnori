package com.cas.yutnorifx.view;

import com.cas.yutnorifx.controller.GameController;
import com.cas.yutnorifx.model.BoardBuilder;
import com.cas.yutnorifx.model.BoardNode;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.YutGameRules;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//게임을 시작하는 class
public class GameLauncher {
    /**
     * 게임에 필요한 정보 수집 이후 플레이
     * 1. 보드 각형
     * 2. 테스트 모드 여부
     * 3. 플레이어 수
     * 4. 플레이어가 사용할 말 개수
     */
    public void start() {
        javafx.application.Platform.runLater(() -> {
            int sides = boardCustom();
            List<BoardNode> board = BoardBuilder.buildCustomizingBoard(sides, 2f);
            BoardNode startNode = findStartNode(board);
            boolean testMode = getTestMode();
            YutGameRules.setTestMode(testMode);
            List<Player> players = getPlayers();

            InGameView inGameView = new InGameView(board, players);
            GameController controller = new GameController(players, inGameView, startNode);
            inGameView.setOnRollYut(() -> controller.rollingYut());

            Stage stage = new Stage();
            stage.setTitle("윷놀이");
            stage.setScene(new Scene(inGameView.getRoot()));
            stage.show();
        });
    }

    /**
     * 사용자에게 n각형 커스터마이징을 입력받는 메서드
     * @return : sides(입력받은 n값 반환)
     */
    private int boardCustom() {
        TextInputDialog dialog = new TextInputDialog("4");
        dialog.setTitle("보드 커스터마이징");
        dialog.setContentText("몇 각형 보드로 커스텀할까요? (권장 4-6)");
        
        while (true) {
            String result = dialog.showAndWait().orElse(null);
            if (result == null) System.exit(0);
            try {
                int sides = Integer.parseInt(result);
                if (sides >= 3) return sides;
            } catch (NumberFormatException e) {
                // 무시하고 다시 입력받음
            }
        }
    }

    // 보드에서 이름이 "Edge0-0" 인 노드를 시작 노드로 찾는 메서드
    private BoardNode findStartNode(List<BoardNode> board) {
        for (BoardNode node : board) {
            if (node.getName().equals("Edge0-0")) {
                return node;
            }
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("에러");
        alert.setContentText("시작 노드를 찾을 수 없습니다.");
        alert.showAndWait();
        System.exit(1);
        return null;
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

    /**
     * 플레이어 정보를 입력 받는 메서드 (플레이어 수 , 플레이어 이름, 사용할 말의 갯수)
     * @return : List[Player]
     */
    private List<Player> getPlayers() {
        int numPlayers = getPlayerCount();
        int numTokens = getTokenCount();
        List<Player> players = new ArrayList<>();
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
                    players.add(new Player(name, numTokens));
                    break;
                }
            }
        }
        return players;
    }

    /**
     * 플레이어 수를 입력받는 메서드 (2-4명)
     * @return : numPlayers
     */
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
                // 무시하고 다시 입력받음
            }
        }
    }

    /**
     * 사용할 말의 갯수를 입력 받는 메서드 (2-5명)
     * @return : tokenCount
     */
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
                // 무시하고 다시 입력받음
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
