package com.cas.yutnorifx.view;

import com.cas.yutnorifx.model.BoardNode;
import com.cas.yutnorifx.model.Player;
import com.cas.yutnorifx.model.Token;
import com.cas.yutnorifx.model.TokenState;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class InGameView {
    private final BoardView boardView;
    private final List<Player> players;
    private final VBox statusPanel;
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };
    private Runnable onRollYut;

    public InGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new BoardView(board, players);
        this.statusPanel = new VBox(10);
        this.statusPanel.setPadding(new Insets(10));
        this.statusPanel.setPrefWidth(200);
        buildStatusPanel();
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
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            Label playerLabel = new Label("<< " + player.getName() + " >>");
            playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            statusPanel.getChildren().add(playerLabel);
            
            for (Token token : player.getTokens()) {
                if (token.getState() == TokenState.READY) {
                    HBox tokenBox = new HBox(5);
                    Circle circle = new Circle(5, playerColors[i % playerColors.length]);
                    Label tokenLabel = new Label(token.getName());
                    tokenBox.getChildren().addAll(circle, tokenLabel);
                    statusPanel.getChildren().add(tokenBox);
                }
            }
            
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

    public void exitGame() {
        System.exit(0);
    }

    public void restartGame() {
        // JavaFX에서는 Platform.runLater를 사용하여 UI 스레드에서 실행
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) statusPanel.getScene().getWindow();
            stage.close();
            try {
                new com.cas.yutnorifx.YutnoriGameFX().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void setOnRollYut(Runnable handler) {
        this.onRollYut = handler;
    }
}
