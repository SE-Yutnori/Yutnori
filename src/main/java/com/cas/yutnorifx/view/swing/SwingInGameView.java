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

public class SwingInGameView implements GameEventObserver {
    private final SwingBoardView boardView;
    private final List<Player> players;
    private final JPanel statusPanel;

    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    private Runnable onRollYut;
    private Consumer<GameEndChoice> onGameEnd;
    private Consumer<BranchSelectionResponse> onBranchSelection;
    private Consumer<TokenSelectionResponse> onTokenSelection;
    private Consumer<YutTestResponse> onYutTestSelection;
    private Runnable onMessageConfirmed;
    private Consumer<ReorderResponse> onReorderSelection;

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

    private JButton rollButton;
    private JPanel root;

    public SwingInGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new SwingBoardView(board, players);

        this.statusPanel = new JPanel();
        this.statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        this.statusPanel.setPreferredSize(new Dimension(200, 600));
        this.statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        buildStatusPanel();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        SwingUtilities.invokeLater(() -> {
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
                    
                    SwingUtilities.invokeLater(() -> {
                        Token selectedToken = selectToken(tokenRequest.getAvailableTokens(), tokenRequest.getSteps());
                        
                        TokenSelectionResponse tokenResponse;
                        if (selectedToken != null) {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), selectedToken);
                        } else {
                            tokenResponse = new TokenSelectionResponse(tokenRequest.getRequestId(), true);
                        }
                        
                        if (onTokenSelection != null) {
                            onTokenSelection.accept(tokenResponse);
                        }
                    });
                    break;
                    
                case YUT_TEST_NEEDED:
                    YutTestRequest yutTestRequest = event.getData(YutTestRequest.class);
                    
                    SwingUtilities.invokeLater(() -> {
                        int selectedYutResult = getTestYutThrow();
                        
                        YutTestResponse yutTestResponse;
                        if (selectedYutResult != -999) {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), selectedYutResult);
                        } else {
                            yutTestResponse = new YutTestResponse(yutTestRequest.getRequestId(), true);
                        }
                        
                        if (onYutTestSelection != null) {
                            onYutTestSelection.accept(yutTestResponse);
                        }
                    });
                    break;
                    
                case BRANCH_SELECTION_NEEDED:
                    BranchSelectionRequest request = event.getData(BranchSelectionRequest.class);
                    
                    SwingUtilities.invokeLater(() -> {
                        BoardNode selectedBranch = selectPath(request.getBranchOptions());
                        
                        BranchSelectionResponse response;
                        if (selectedBranch != null) {
                            response = new BranchSelectionResponse(request.getRequestId(), selectedBranch);
                        } else {
                            response = new BranchSelectionResponse(request.getRequestId(), true);
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
                    
                    SwingUtilities.invokeLater(() -> {
                        List<Integer> reorderedResults = requestReorder(reorderRequest);
                        
                        ReorderResponse reorderResponse;
                        if (reorderedResults != null) {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), reorderedResults);
                        } else {
                            reorderResponse = new ReorderResponse(reorderRequest.getRequestId(), true);
                        }
                        
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
        queueMessage(message, title, MessageType.INFO);
    }

    public void showError(String message) {
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

        if (result == null) return -999;
        
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
        statusPanel.removeAll();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            final int playerIndex = i; // final 변수 생성

            JLabel playerLabel = new JLabel("<< " + player.getName() + " >>");
            playerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            statusPanel.add(playerLabel);
            
            for (Token token : player.getTokens()) {
                if (token.getState() == TokenState.READY) {
                    JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    
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
            
            if (onMessageConfirmed != null) {
                onMessageConfirmed.run();
            }
            
            processNextMessage();
        });
    }
    
    private void processPendingCallbacks() {
        Runnable callback;
        while ((callback = pendingCallbacks.poll()) != null) {
            callback.run();
        }
    }

    public List<Integer> requestReorder(ReorderRequest request) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                null,
                request.getPromptMessage(),
                "윷 결과 재배열",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (input == null) {
                return null;
            }
            
            YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(input, request.getOriginalResults());
            
            if (result.isSuccess()) {
                return result.getReorderedResults();
            } else {
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