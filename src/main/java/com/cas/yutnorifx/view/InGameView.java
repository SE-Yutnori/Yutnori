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
    // 보드 뷰
    private final BoardView boardView;
    // 플레이어 리스트 (이름, Token 리스트 보유)
    private final List<Player> players;
    // 오른쪽에 나타나는 상태 패널
    // VBox : 수직 레이아웃 컨테이너
    private final VBox statusPanel;

    // 플레이어 색상 배열
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    //runnable 타입의 onRollYut 변수 선언
    private Runnable onRollYut;

    // InGameView 생성자
    public InGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new BoardView(board, players);

        //상태패널 VBox 생성
        this.statusPanel = new VBox(10);
        //패널 패딩 설정
        this.statusPanel.setPadding(new Insets(10));
        //패널 너비 설정
        this.statusPanel.setPrefWidth(200);
        //상태패널 빌드
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
        //상태패널 자식 노드 제거
        statusPanel.getChildren().clear();

        //플레이어 수만큼 반복
        for (int i = 0; i < players.size(); i++) {
            //플레이어 객체 가져오기
            Player player = players.get(i);

            //플레이어 이름 레이블 생성
            Label playerLabel = new Label("<< " + player.getName() + " >>");
            //플레이어 이름 레이블 폰트 설정
            playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            //상태패널에 플레이어 이름 레이블 추가
            statusPanel.getChildren().add(playerLabel);
            
            //플레이어 토큰 수만큼 반복
            for (Token token : player.getTokens()) {
                //토큰 상태가 READY인 경우
                if (token.getState() == TokenState.READY) {
                    //HBox : 수평 레이아웃 컨테이너
                    HBox tokenBox = new HBox(5);
                    //Circle : 원형 모양 노드
                    Circle circle = new Circle(5, playerColors[i % playerColors.length]);
                    //Label : 텍스트 레이블
                    Label tokenLabel = new Label(token.getName());
                    //HBox에 원형 모양 노드와 텍스트 레이블 추가
                    tokenBox.getChildren().addAll(circle, tokenLabel);
                    //상태패널에 HBox 추가
                    statusPanel.getChildren().add(tokenBox);
                }
            }

            //Separator : 구분선
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
