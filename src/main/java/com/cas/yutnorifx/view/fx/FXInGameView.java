package com.cas.yutnorifx.view.fx;

import com.cas.yutnorifx.model.core.*;
import com.cas.yutnorifx.model.entity.*;
import com.cas.yutnorifx.model.event.*;
import com.cas.yutnorifx.model.request.*;
import com.cas.yutnorifx.view.GameEndChoice;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class FXInGameView implements GameEventObserver {
    private final FXBoardView boardView;
    private final List<Player> players;
    private final VBox statusPanel;

    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    private Runnable onRollYut;
    
    private Consumer<GameEndChoice> onGameEnd;

    private java.util.function.Consumer<BranchSelectionResponse> onBranchSelection;

    private java.util.function.Consumer<TokenSelectionResponse> onTokenSelection;

    private java.util.function.Consumer<YutTestResponse> onYutTestSelection;

    private Runnable onMessageConfirmed;

    private java.util.function.Consumer<ReorderResponse> onReorderSelection;

    private final Queue<MessageInfo> messageQueue = new ConcurrentLinkedQueue<>();
    private boolean isShowingMessage = false;
    
    private final Queue<Runnable> pendingCallbacks = new ConcurrentLinkedQueue<>();
    
    private static class MessageInfo {
        final String message;
        final String title;
        final MessageType type;
        
        MessageInfo(String message, String title, MessageType type) {
            this.message = message;
            this.title = title;
            this.type = type;
        }
    }
    
    private enum MessageType {
        INFO, ERROR
    }

    private Button rollButton;
    private VBox root;
    private Scene scene;
    private Stage stage;
    private Label statusLabel;

    private Queue<String> pendingMessages = new ConcurrentLinkedQueue<>();
    private boolean processingMessage = false;

    public FXInGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new FXBoardView(board, players);

        this.statusPanel = new VBox(10);
        this.statusPanel.setPadding(new Insets(10));
        this.statusPanel.setPrefWidth(200);
        buildStatusPanel();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case YUT_THROW_RESULT:
                    showMessage(event.getMessage(), "윷 결과");
                    refresh();
                    break;
                    
                case MOVE_RESULT:
                    if (!event.getMessage().isEmpty()) {
                        showMessage(event.getMessage(), "이동 결과");
                    }
                    refresh();
                    break;
                    
                case TOKEN_CAUGHT:
                    showMessage(event.getMessage(), "말 잡기!");
                    refresh();
                    break;
                    
                case TURN_CHANGED:
                    showMessage(event.getMessage(), "턴 변경");
                    refresh();
                    break;
                    
                case GAME_ENDED:
                    refresh();
                    Player winner = event.getData(Player.class);
                    GameEndChoice choice = getGameEndChoice(winner.getName() + " 승리!");
                    if (onGameEnd != null) {
                        onGameEnd.accept(choice);
                    }
                    break;
                    
                case ERROR_OCCURRED:
                    showError(event.getMessage());
                    break;
                    
                case TOKEN_SELECTION_NEEDED:
                    TokenSelectionRequest tokenRequest = event.getData(TokenSelectionRequest.class);
                    
                    Platform.runLater(() -> {
                        Token selectedToken = selectToken(tokenRequest.getAvailableTokens(), tokenRequest.getSteps());
                        
                        TokenSelectionResponse tokenResponse;
                        if (selectedToken != null) {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), selectedToken);
                        } else {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), true); // 취소됨
                        }
                        
                        if (onTokenSelection != null) {
                            onTokenSelection.accept(tokenResponse);
                        }
                    });
                    break;
                    
                case YUT_TEST_NEEDED:
                    YutTestRequest yutTestRequest = event.getData(YutTestRequest.class);
                    
                    Platform.runLater(() -> {
                        int selectedYutResult = getTestYutThrow();
                        
                        YutTestResponse yutTestResponse;
                        if (selectedYutResult != -999) {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), selectedYutResult);
                        } else {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), true); // 취소됨
                        }
                        
                        if (onYutTestSelection != null) {
                            onYutTestSelection.accept(yutTestResponse);
                        }
                    });
                    break;
                    
                case BRANCH_SELECTION_NEEDED:
                    BranchSelectionRequest request = event.getData(BranchSelectionRequest.class);
                    
                    Platform.runLater(() -> {
                        BoardNode selectedBranch = selectPath(request.getBranchOptions());
                        
                        BranchSelectionResponse response;
                        if (selectedBranch != null) {
                            response = new BranchSelectionResponse(request.getRequestId(), selectedBranch);
                        } else {
                            response = new BranchSelectionResponse(request.getRequestId(), true); // 취소됨
                        }
                        
                        if (onBranchSelection != null) {
                            onBranchSelection.accept(response);
                        }
                    });
                    break;
                    
                case MESSAGE_CONFIRMED:
                    break;
                    
                case REORDER_NEEDED:
                    ReorderRequest reorderRequest = event.getData(ReorderRequest.class);
                    
                    Platform.runLater(() -> {
                        List<Integer> reorderedResults = requestReorder(reorderRequest);
                        
                        ReorderResponse reorderResponse;
                        if (reorderedResults != null) {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), reorderedResults);
                        } else {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), true); // 취소됨
                        }
                        
                        if (onReorderSelection != null) {
                            onReorderSelection.accept(reorderResponse);
                        }
                    });
                    break;
                    
            }
        });
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
        // 직접 표시하지 않고 큐에 추가
        queueMessage(message, title, MessageType.INFO);
    }

    public String requestInput(String message, String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse(null);
    }

    public void showError(String message) {
        queueMessage(message, "입력 오류", MessageType.ERROR);
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
        alert.setContentText(message + "\n\n다시 시작하시겠습니까?");
        
        ButtonType restartButton = new ButtonType("다시 시작");
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
    
    public void setOnGameEnd(Consumer<GameEndChoice> handler) {
        this.onGameEnd = handler;
    }

    public void setOnBranchSelection(java.util.function.Consumer<BranchSelectionResponse> handler) {
        this.onBranchSelection = handler;
    }

    public void setOnTokenSelection(java.util.function.Consumer<TokenSelectionResponse> handler) {
        this.onTokenSelection = handler;
    }

    public void setOnYutTestSelection(java.util.function.Consumer<YutTestResponse> handler) {
        this.onYutTestSelection = handler;
    }

    public void setOnMessageConfirmed(Runnable handler) {
        this.onMessageConfirmed = handler;
    }

    public void setOnReorderSelection(java.util.function.Consumer<ReorderResponse> handler) {
        this.onReorderSelection = handler;
    }

    private void queueMessage(String message, String title, MessageType type) {
        messageQueue.offer(new MessageInfo(message, title, type));
        processNextMessage();
    }
    
    private void processNextMessage() {
        if (isShowingMessage) {
            return;
        }
        
        MessageInfo nextMessage = messageQueue.poll();
        if (nextMessage == null) {
            processPendingCallbacks();
            return;
        }
        
        isShowingMessage = true;
        
        Platform.runLater(() -> {
            Alert alert;
            if (nextMessage.type == MessageType.ERROR) {
                alert = new Alert(Alert.AlertType.ERROR);
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
            }
            
            alert.setTitle(nextMessage.title);
            alert.setHeaderText(null);
            alert.setContentText(nextMessage.message);
            
            alert.setOnHiding(event -> {
                isShowingMessage = false;
                
                if (onMessageConfirmed != null) {
                    onMessageConfirmed.run();
                }
                
                processNextMessage(); // 다음 메시지 처리
            });
            
            alert.showAndWait();
        });
    }
    
    private void processPendingCallbacks() {
        Runnable callback;
        while ((callback = pendingCallbacks.poll()) != null) {
            callback.run();
        }
    }

    public List<Integer> requestReorder(ReorderRequest request) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("윷 결과 재배열");
        dialog.setHeaderText(null);
        dialog.setContentText(request.getPromptMessage());
        
        while (true) {
            String input = dialog.showAndWait().orElse(null);
            if (input == null) {
                return null;
            }
            
            YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(input, request.getOriginalResults());
            
            if (result.isSuccess()) {
                return result.getReorderedResults();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("입력 오류");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText(result.getErrorMessage());
                errorAlert.showAndWait();
                
                dialog.getEditor().clear();
            }
        }
    }
}
