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

/**
 * JavaFX 기반 인게임 뷰
 */
public class FXInGameView implements GameEventObserver {
    // 보드 뷰
    private final FXBoardView boardView;
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
    
    // 게임 종료 콜백
    private Consumer<GameEndChoice> onGameEnd;

    // 분기 선택 콜백
    private java.util.function.Consumer<BranchSelectionResponse> onBranchSelection;

    // 토큰 선택 콜백
    private java.util.function.Consumer<TokenSelectionResponse> onTokenSelection;

    // 테스트 윷 선택 콜백
    private java.util.function.Consumer<YutTestResponse> onYutTestSelection;

    // 메시지 확인 완료 콜백
    private Runnable onMessageConfirmed;

    // 재배열 선택 콜백
    private java.util.function.Consumer<ReorderResponse> onReorderSelection;

    // 메시지 순차 표시를 위한 큐와 상태 관리
    private final Queue<MessageInfo> messageQueue = new ConcurrentLinkedQueue<>();
    private boolean isShowingMessage = false;
    
    // 메시지 표시 완료 후 실행할 콜백들
    private final Queue<Runnable> pendingCallbacks = new ConcurrentLinkedQueue<>();
    
    // 메시지 정보를 담는 내부 클래스
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
    
    // 메시지 타입 정의
    private enum MessageType {
        INFO, ERROR
    }

    // UI 컴포넌트들
    private Button rollButton;
    private VBox root;
    private Scene scene;
    private Stage stage;
    private Label statusLabel;

    // 메시지 대기열
    private Queue<String> pendingMessages = new ConcurrentLinkedQueue<>();
    private boolean processingMessage = false;

    // FXInGameView 생성자
    public FXInGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new FXBoardView(board, players);

        //상태패널 VBox 생성
        this.statusPanel = new VBox(10);
        //패널 패딩 설정
        this.statusPanel.setPadding(new Insets(10));
        //패널 너비 설정
        this.statusPanel.setPrefWidth(200);
        //상태패널 빌드
        buildStatusPanel();
    }

    // Observer 패턴 구현: 게임 이벤트 자동 처리
    @Override
    public void onGameEvent(GameEvent event) {
        // UI 업데이트는 JavaFX Application Thread에서 실행되어야 함
        Platform.runLater(() -> {
            switch (event.getType()) {
                case YUT_THROW_RESULT:
                    // 윷 결과 자동 표시
                    showMessage(event.getMessage(), "윷 결과");
                    refresh(); // 보드 상태 자동 새로고침
                    break;
                    
                case MOVE_RESULT:
                    // 이동 결과 자동 표시
                    if (!event.getMessage().isEmpty()) {
                        showMessage(event.getMessage(), "이동 결과");
                    }
                    refresh(); // 보드 상태 항상 새로고침 (이동 성공시 화면 업데이트)
                    break;
                    
                case TOKEN_CAUGHT:
                    // 말 잡기 결과 자동 표시
                    showMessage(event.getMessage(), "말 잡기!");
                    refresh();
                    break;
                    
                case TURN_CHANGED:
                    // 턴 변경 자동 표시
                    showMessage(event.getMessage(), "턴 변경");
                    refresh();
                    break;
                    
                case GAME_ENDED:
                    // 게임 종료시 자동으로 승리 메시지와 선택 다이얼로그 표시
                    refresh();
                    Player winner = event.getData(Player.class);
                    GameEndChoice choice = getGameEndChoice(winner.getName() + " 승리!");
                    if (onGameEnd != null) {
                        onGameEnd.accept(choice);
                    }
                    break;
                    
                case ERROR_OCCURRED:
                    // 오류 자동 표시
                    showError(event.getMessage());
                    break;
                    
                case TOKEN_SELECTION_NEEDED:
                    // 토큰 선택 요청을 비동기로 처리
                    TokenSelectionRequest tokenRequest = event.getData(TokenSelectionRequest.class);
                    
                    Platform.runLater(() -> {
                        Token selectedToken = selectToken(tokenRequest.getAvailableTokens(), tokenRequest.getSteps());
                        
                        TokenSelectionResponse tokenResponse;
                        if (selectedToken != null) {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), selectedToken);
                        } else {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), true); // 취소됨
                        }
                        
                        // Controller를 통해 Model에 응답 전달 (비동기)
                        if (onTokenSelection != null) {
                            onTokenSelection.accept(tokenResponse);
                        }
                    });
                    break;
                    
                case YUT_TEST_NEEDED:
                    // 테스트 윷 선택 요청을 비동기로 처리
                    YutTestRequest yutTestRequest = event.getData(YutTestRequest.class);
                    
                    Platform.runLater(() -> {
                        int selectedYutResult = getTestYutThrow();
                        
                        YutTestResponse yutTestResponse;
                        if (selectedYutResult != -999) {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), selectedYutResult);
                        } else {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), true); // 취소됨
                        }
                        
                        // Controller를 통해 Model에 응답 전달 (비동기)
                        if (onYutTestSelection != null) {
                            onYutTestSelection.accept(yutTestResponse);
                        }
                    });
                    break;
                    
                case BRANCH_SELECTION_NEEDED:
                    // 분기 선택 요청을 비동기로 처리
                    BranchSelectionRequest request = event.getData(BranchSelectionRequest.class);
                    
                    // 백그라운드 스레드에서 분기 선택 다이얼로그 표시
                    Platform.runLater(() -> {
                        BoardNode selectedBranch = selectPath(request.getBranchOptions());
                        
                        BranchSelectionResponse response;
                        if (selectedBranch != null) {
                            response = new BranchSelectionResponse(request.getRequestId(), selectedBranch);
                        } else {
                            response = new BranchSelectionResponse(request.getRequestId(), true); // 취소됨
                        }
                        
                        // Controller를 통해 Model에 응답 전달 (비동기)
                        if (onBranchSelection != null) {
                            onBranchSelection.accept(response);
                        }
                    });
                    break;
                    
                case MESSAGE_CONFIRMED:
                    // 메시지 확인 대기 상태 - 특별 처리 없음 (Model이 대기 중)
                    break;
                    
                case REORDER_NEEDED:
                    // 재배열 요청을 비동기로 처리
                    ReorderRequest reorderRequest = event.getData(ReorderRequest.class);
                    
                    Platform.runLater(() -> {
                        List<Integer> reorderedResults = requestReorder(reorderRequest);
                        
                        ReorderResponse reorderResponse;
                        if (reorderedResults != null) {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), reorderedResults);
                        } else {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), true); // 취소됨
                        }
                        
                        // Controller를 통해 Model에 응답 전달 (비동기)
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
        // 직접 표시하지 않고 큐에 추가
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
    
    // 게임 종료 콜백 설정 메서드 추가
    public void setOnGameEnd(Consumer<GameEndChoice> handler) {
        this.onGameEnd = handler;
    }

    // 분기 선택 콜백 설정 메서드 추가
    public void setOnBranchSelection(java.util.function.Consumer<BranchSelectionResponse> handler) {
        this.onBranchSelection = handler;
    }

    // 토큰 선택 콜백 설정 메서드 추가
    public void setOnTokenSelection(java.util.function.Consumer<TokenSelectionResponse> handler) {
        this.onTokenSelection = handler;
    }

    // 테스트 윷 선택 콜백 설정 메서드 추가
    public void setOnYutTestSelection(java.util.function.Consumer<YutTestResponse> handler) {
        this.onYutTestSelection = handler;
    }

    // 메시지 확인 완료 콜백 설정 메서드 추가
    public void setOnMessageConfirmed(Runnable handler) {
        this.onMessageConfirmed = handler;
    }

    // 재배열 선택 콜백 설정 메서드 추가
    public void setOnReorderSelection(java.util.function.Consumer<ReorderResponse> handler) {
        this.onReorderSelection = handler;
    }

    // 메시지를 큐에 추가하고 순차 처리 시작
    private void queueMessage(String message, String title, MessageType type) {
        messageQueue.offer(new MessageInfo(message, title, type));
        processNextMessage();
    }
    
    // 다음 메시지를 순차적으로 처리
    private void processNextMessage() {
        // 이미 메시지를 표시 중이면 대기
        if (isShowingMessage) {
            return;
        }
        
        MessageInfo nextMessage = messageQueue.poll();
        if (nextMessage == null) {
            // 더 이상 메시지 없음 - 대기 중인 콜백들 실행
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
            
            // 사용자가 확인 버튼을 누르면 다음 메시지 처리
            alert.setOnHiding(event -> {
                isShowingMessage = false;
                
                // Model에게 메시지 확인 완료 신호 전송
                if (onMessageConfirmed != null) {
                    onMessageConfirmed.run();
                }
                
                processNextMessage(); // 다음 메시지 처리
            });
            
            alert.showAndWait();
        });
    }
    
    // 대기 중인 콜백들 실행
    private void processPendingCallbacks() {
        Runnable callback;
        while ((callback = pendingCallbacks.poll()) != null) {
            callback.run();
        }
    }
    
    // 메시지 표시 완료 후 실행할 콜백 등록
    public void executeAfterMessages(Runnable callback) {
        pendingCallbacks.offer(callback);
        // 현재 메시지가 없으면 즉시 실행
        if (messageQueue.isEmpty() && !isShowingMessage) {
            processPendingCallbacks();
        }
    }

    // 재배열 요청 다이얼로그
    public List<Integer> requestReorder(ReorderRequest request) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("윷 결과 재배열");
        dialog.setHeaderText(null);
        dialog.setContentText(request.getPromptMessage());
        
        while (true) {
            String input = dialog.showAndWait().orElse(null);
            if (input == null) {
                return null; // 취소됨
            }
            
            // YutGameRules의 검증 로직 사용
            YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(input, request.getOriginalResults());
            
            if (result.isSuccess()) {
                return result.getReorderedResults();
            } else {
                // 오류 메시지 표시하고 다시 입력받기
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("입력 오류");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText(result.getErrorMessage());
                errorAlert.showAndWait();
                
                // 다이얼로그를 다시 표시하기 위해 계속 진행
                dialog.getEditor().clear();
            }
        }
    }
}
