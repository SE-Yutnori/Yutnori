package com.cas.yutnorifx.view;

import com.cas.yutnorifx.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InGameView {
    //보드, 플레이어, 플레이어 말 상태창
    private final BoardView boardView;
    private final List<Player> players;
    private final VBox statusPanel;

    // 플레이어 색상은 임의 할당 (순서 임의 변경 불가)
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    //onRollYut(윷을 던지는) 변수 선언
    private Runnable onRollYut;

    // InGameView 생성자
    public InGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new BoardView(board, players);

        //상태패널 VBox 생성
        this.statusPanel = new VBox(10);
        //패널 설정
        this.statusPanel.setPadding(new Insets(10));
        this.statusPanel.setPrefWidth(200);
        buildStatusPanel();
    }

    //순서 재배열 결과를 담는 클래스
    public static class ReorderResult {
        private final boolean success;
        private final List<Integer> reorderedResults;
        private final String errorMessage;

        public static ReorderResult success(List<Integer> results) {
            return new ReorderResult(true, results, null);
        }

        public static ReorderResult error(String message) {
            return new ReorderResult(false, null, message);
        }

        private ReorderResult(boolean success, List<Integer> reorderedResults, String errorMessage) {
            this.success = success;
            this.reorderedResults = reorderedResults != null ? new ArrayList<>(reorderedResults) : null;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public List<Integer> getReorderedResults() {
            return reorderedResults != null ? new ArrayList<>(reorderedResults) : null;
        }
        public String getErrorMessage() { return errorMessage; }
    }

    public Pane getRoot() {
        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setRight(statusPanel);
        
        Button rollButton = new Button("윷 던지기");
        rollButton.setMaxWidth(Double.MAX_VALUE);
        rollButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        
        VBox bottomBox = new VBox(10, rollButton);
        bottomBox.setPadding(new Insets(10));
        root.setBottom(bottomBox);
        
        rollButton.setOnAction(e -> {
            if (onRollYut != null) onRollYut.run();
        });
        
        return root;
    }

    public void refresh() {
        boardView.refresh();
        buildStatusPanel();
    }

    public void showMessage(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String requestInput(String message, String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse(null);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("입력 오류");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public int getTestYutThrow() {
        String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options[1], options);
        dialog.setTitle("테스트 윷 던지기");
        dialog.setHeaderText(null);
        dialog.setContentText("테스트 모드: 윷 결과를 선택하세요.");

        String result = dialog.showAndWait().orElse(options[1]);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(result)) {
                return i == 0 ? -1 : i;
            }
        }
        return 1;
    }

    public Token selectToken(List<Token> tokens, int steps) {
        List<Token> activeTokens = tokens.stream()
                .filter(t -> t.getState() != TokenState.FINISHED)
                .toList();

        if (activeTokens.isEmpty()) {
            showError("모든 말이 도착했습니다.");
            return null;
        }

        String[] names = activeTokens.stream()
                .map(Token::getName)
                .toArray(String[]::new);

        ChoiceDialog<String> dialog = new ChoiceDialog<>(names[0], names);
        dialog.setTitle("말 선택");
        dialog.setHeaderText(null);
        dialog.setContentText("어떤 말을 " + steps + "칸 이동하시겠습니까?");

        String selected = dialog.showAndWait().orElse(null);
        if (selected == null) return null;

        return activeTokens.stream()
                .filter(t -> t.getName().equals(selected))
                .findFirst()
                .orElse(null);
    }

    public static InGameView.ReorderResult validateReorderInput(String input, List<Integer> originalResults) {
        if (input == null || input.trim().isEmpty()) {
            return InGameView.ReorderResult.error("입력이 비어있습니다.");
        }

        String[] parts = input.split(",");
        if (parts.length != originalResults.size()) {
            return InGameView.ReorderResult.error("입력 개수가 실제 개수와 다릅니다!");
        }

        List<Integer> reordered = new ArrayList<>();
        try {
            for (String p : parts) {
                int val = Integer.parseInt(p.trim());
                if ((val < -1 || val > 5) || val == 0) {
                    return InGameView.ReorderResult.error("윷 값 (-1,1~5)을 벗어났습니다: " + val);
                }
                reordered.add(val);
            }
        } catch (NumberFormatException e) {
            return InGameView.ReorderResult.error("숫자가 아닌 값이 포함되어 있습니다.");
        }

        List<Integer> sortedOrig = new ArrayList<>(originalResults);
        List<Integer> sortedReorder = new ArrayList<>(reordered);
        Collections.sort(sortedOrig);
        Collections.sort(sortedReorder);

        if (!sortedOrig.equals(sortedReorder)) {
            return InGameView.ReorderResult.error("입력한 값이 실제 윷 결과와 다릅니다.");
        }

        return InGameView.ReorderResult.success(reordered);
    }

    public BoardNode selectPath(List<BoardNode> options) {
        if (options.size() <= 1) {
            return options.get(0);
        }

        BoardNode first = options.get(0);
        BoardNode last = options.get(options.size() - 1);

        String firstLabel = first.getName().contains("Edge")
                ? "1"
                : first.getName().replace("ToCenter", "");
        String lastLabel = last.getName().contains("Edge")
                ? "1"
                : last.getName().replace("ToCenter", "");

        String[] choices = {firstLabel, lastLabel};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices[0], choices);
        dialog.setTitle("분기 선택");
        dialog.setHeaderText(null);
        dialog.setContentText("어느 방향으로 진행하시겠습니까?");

        String result = dialog.showAndWait().orElse(choices[0]);
        return result.equals(firstLabel) ? first : last;
    }

    private void buildStatusPanel() {
        statusPanel.getChildren().clear();

        //플레이어 수만큼 반복해서
        for (int i = 0; i < players.size(); i++) {
            //각 플레이어의 객체 가져오기
            Player player = players.get(i);

            Label playerLabel = new Label("<< " + player.getName() + " >>");
            playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            statusPanel.getChildren().add(playerLabel);

            //플레이어 토큰 수만큼 반복하여
            for (Token token : player.getTokens()) {
                //토큰 상태가져와서 보여주기
                if (token.getState() == TokenState.READY) {
                    HBox tokenBox = new HBox(5);
                    Circle circle = new Circle(5, playerColors[i % playerColors.length]);
                    Label tokenLabel = new Label(token.getName());
                    tokenBox.getChildren().addAll(circle, tokenLabel);
                    statusPanel.getChildren().add(tokenBox);
                }
            }

            //플레이어 구분선
            statusPanel.getChildren().add(new Separator());
        }
    }

    public GameEndChoice getGameEndChoice(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("게임 종료");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n재시작하시겠습니까?");
        
        ButtonType restartButton = new ButtonType("재시작");
        ButtonType exitButton = new ButtonType("종료");
        alert.getButtonTypes().setAll(restartButton, exitButton);
        
        return alert.showAndWait()
                .filter(buttonType -> buttonType == restartButton)
                .map(buttonType -> GameEndChoice.RESTART)
                .orElse(GameEndChoice.EXIT);
    }

    public void setOnRollYut(Runnable handler) {
        this.onRollYut = handler;
    }
}
