package com.cas.yutnorifx.view;

import com.cas.yutnorifx.controller.GameController;
import com.cas.yutnorifx.model.BoardBuilder;
import com.cas.yutnorifx.model.BoardNode;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.YutGameRules;

import javafx.scene.control.*;
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
            // 보드 커스타미이징 메서드 호출
            int sides = boardCustom();

            // 리스트 형태로 보드판 생성
            List<BoardNode> board = BoardBuilder.buildCustomizingBoard(sides, 2f);

            // 시작노드 저장
            BoardNode startNode = findStartNode(board);

            // 테스트 모드 여부 입력받기
            boolean testMode = getTestMode();
            
            // 테스트 모드 true false에 따라서 설정
            YutGameRules.setTestMode(testMode);

            // 플레이어 수 입력 + 이름 설정 + 사용할 말 갯수 설정
            List<Player> players = getPlayers();

            // 게임 화면 생성
            InGameView inGameView = new InGameView(board, players);

            // 게임 컨트롤러 생성
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
        //JavaFX 텍스트 입력 대화상자 실행
        TextInputDialog dialog = new TextInputDialog("4");
        dialog.setTitle("보드 커스터마이징");
        dialog.setContentText("몇 각형 보드로 커스텀할까요? (권장 4-6)");
        
        while (true) {
            //showAndWait() : 대화상자를 보여주고 사용자가 확인 버튼을 누를 때까지 대기
            //orElse(null) : 사용자가 대화상자를 닫으면 null을 반환
            String result = dialog.showAndWait().orElse(null);
            //사용자가 대화상자를 닫으면 프로그램 종료
            if (result == null) System.exit(0);

            try {
                //입력받은 값을 정수로 변환
                int sides = Integer.parseInt(result);
                //4 이상 6 이하인 경우 반환
                if (sides >= 4 && sides <= 6) return sides;
            } catch (NumberFormatException e) {
                // 4 이상 6 이하가 아닌 경우 무시하고 다시 입력받음
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
            //showAndWait() : 대화상자를 보여주고 사용자가 확인 버튼을 누를 때까지 대기
            //orElse(null) : 사용자가 대화상자를 닫으면 null을 반환
            String result = dialog.showAndWait().orElse(null);
            //사용자가 대화상자를 닫으면 프로그램 종료
            if (result == null) System.exit(0);
            try {
                //입력받은 값을 정수로 변환
                int count = Integer.parseInt(result);
                //2-4명인 경우 반환
                if (count >= 2 && count <= 4) return count;
            } catch (NumberFormatException e) {
                // 2-4명이 아닌 경우 무시하고 다시 입력받음
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
