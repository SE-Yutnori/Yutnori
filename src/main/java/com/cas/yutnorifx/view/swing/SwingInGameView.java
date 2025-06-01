package com.cas.yutnorifx.view.swing;

import com.cas.yutnorifx.model.core.*;
import com.cas.yutnorifx.model.entity.*;
import com.cas.yutnorifx.model.event.*;
import com.cas.yutnorifx.model.request.*;
import com.cas.yutnorifx.view.GameEndChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Swing 기반 인게임 뷰
 */
public class SwingInGameView implements GameEventObserver {
    // 보드 뷰
    private final SwingBoardView boardView;
    // 플레이어 리스트
    private final List<Player> players;
    // 오른쪽에 나타나는 상태 패널
    private final JPanel statusPanel;

    // 플레이어 색상 배열
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    // 콜백들
    private Runnable onRollYut;
    private Consumer<GameEndChoice> onGameEnd;
    private Consumer<BranchSelectionResponse> onBranchSelection;
    private Consumer<TokenSelectionResponse> onTokenSelection;
    private Consumer<YutTestResponse> onYutTestSelection;
    private Runnable onMessageConfirmed;
    private Consumer<ReorderResponse> onReorderSelection;

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
    private JButton rollButton;
    private JPanel root;

    // SwingInGameView 생성자
    public SwingInGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new SwingBoardView(board, players);

        // 상태패널 JPanel 생성
        this.statusPanel = new JPanel();
        this.statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        this.statusPanel.setPreferredSize(new Dimension(200, 600));
        this.statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상태패널 빌드
        buildStatusPanel();
    }

    // Observer 패턴 구현: 게임 이벤트 자동 처리
    @Override
    public void onGameEvent(GameEvent event) {
        // UI 업데이트는 Swing Event Dispatch Thread에서 실행되어야 함
        SwingUtilities.invokeLater(() -> {
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
                    
                    SwingUtilities.invokeLater(() -> {
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
                    
                    SwingUtilities.invokeLater(() -> {
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
                    
                    SwingUtilities.invokeLater(() -> {
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
                    
                    SwingUtilities.invokeLater(() -> {
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

    public JPanel getRoot() {
        if (root == null) {
            root = new JPanel(new BorderLayout());
            root.add(boardView, BorderLayout.CENTER);
            root.add(statusPanel, BorderLayout.EAST);
            
            rollButton = new JButton("윷 던지기");
            rollButton.setFont(new Font("Arial", Font.BOLD, 16));
            rollButton.setPreferredSize(new Dimension(200, 50));
            
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            bottomPanel.add(rollButton);
            root.add(bottomPanel, BorderLayout.SOUTH);
            
            rollButton.addActionListener(e -> {
                if (onRollYut != null) onRollYut.run();
            });
        }
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

    public void showError(String message) {
        // 직접 표시하지 않고 큐에 추가
        queueMessage(message, "입력 오류", MessageType.ERROR);
    }

    public int getTestYutThrow() {
        String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
        String result = (String) JOptionPane.showInputDialog(
            null,
            "테스트 모드: 윷 결과를 선택하세요.",
            "테스트 윷 던지기",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (result == null) return -999; // 취소됨
        
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

        String selected = (String) JOptionPane.showInputDialog(
            null,
            "어떤 말을 " + steps + "칸 이동하시겠습니까?",
            "말 선택",
            JOptionPane.QUESTION_MESSAGE,
            null,
            names,
            names[0]
        );

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

        String[] choices = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            BoardNode option = options.get(i);
            if (option.getName().contains("Edge")) {
                choices[i] = "외곽 경로 " + (i + 1);
            } else {
                choices[i] = option.getName().replace("ToCenter", "중앙 경로 ");
            }
        }

        String result = (String) JOptionPane.showInputDialog(
            null,
            "어느 방향으로 진행하시겠습니까?",
            "분기 선택",
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );

        if (result == null) return options.get(0);

        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(result)) {
                return options.get(i);
            }
        }
        return options.get(0);
    }

    private void buildStatusPanel() {
        // 상태패널 자식 노드 제거
        statusPanel.removeAll();

        // 플레이어 수만큼 반복
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            final int playerIndex = i; // final 변수 생성

            // 플레이어 이름 레이블 생성
            JLabel playerLabel = new JLabel("<< " + player.getName() + " >>");
            playerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            statusPanel.add(playerLabel);
            
            // 플레이어 토큰 수만큼 반복
            for (Token token : player.getTokens()) {
                if (token.getState() == TokenState.READY) {
                    JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    
                    // 색상 원 만들기
                    JPanel colorCircle = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(playerColors[playerIndex % playerColors.length]);
                            g.fillOval(2, 2, 10, 10);
                        }
                    };
                    colorCircle.setPreferredSize(new Dimension(14, 14));
                    
                    JLabel tokenLabel = new JLabel(token.getName());
                    tokenPanel.add(colorCircle);
                    tokenPanel.add(tokenLabel);
                    statusPanel.add(tokenPanel);
                }
            }

            // 구분선
            statusPanel.add(new JSeparator());
        }
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    public GameEndChoice getGameEndChoice(String message) {
        int result = JOptionPane.showConfirmDialog(
            null,
            message + "\n\n다시 시작하시겠습니까?",
            "게임 종료",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION ? GameEndChoice.RESTART : GameEndChoice.EXIT;
    }

    // 콜백 설정 메서드들
    public void setOnRollYut(Runnable handler) {
        this.onRollYut = handler;
    }
    
    public void setOnGameEnd(Consumer<GameEndChoice> handler) {
        this.onGameEnd = handler;
    }

    public void setOnBranchSelection(Consumer<BranchSelectionResponse> handler) {
        this.onBranchSelection = handler;
    }

    public void setOnTokenSelection(Consumer<TokenSelectionResponse> handler) {
        this.onTokenSelection = handler;
    }

    public void setOnYutTestSelection(Consumer<YutTestResponse> handler) {
        this.onYutTestSelection = handler;
    }

    public void setOnMessageConfirmed(Runnable handler) {
        this.onMessageConfirmed = handler;
    }

    public void setOnReorderSelection(Consumer<ReorderResponse> handler) {
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
        
        SwingUtilities.invokeLater(() -> {
            int messageType = nextMessage.type == MessageType.ERROR ? 
                JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            
            JOptionPane.showMessageDialog(
                null,
                nextMessage.message,
                nextMessage.title,
                messageType
            );
            
            isShowingMessage = false;
            
            // Model에게 메시지 확인 완료 신호 전송
            if (onMessageConfirmed != null) {
                onMessageConfirmed.run();
            }
            
            processNextMessage(); // 다음 메시지 처리
        });
    }
    
    // 대기 중인 콜백들 실행
    private void processPendingCallbacks() {
        Runnable callback;
        while ((callback = pendingCallbacks.poll()) != null) {
            callback.run();
        }
    }

    // 재배열 요청 다이얼로그
    public List<Integer> requestReorder(ReorderRequest request) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                null,
                request.getPromptMessage(),
                "윷 결과 재배열",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (input == null) {
                return null; // 취소됨
            }
            
            // YutGameRules의 검증 로직 사용
            YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(input, request.getOriginalResults());
            
            if (result.isSuccess()) {
                return result.getReorderedResults();
            } else {
                // 오류 메시지 표시하고 다시 입력받기
                JOptionPane.showMessageDialog(
                    null,
                    result.getErrorMessage(),
                    "입력 오류",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
} 