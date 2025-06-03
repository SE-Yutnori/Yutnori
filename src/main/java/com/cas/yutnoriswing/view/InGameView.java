package com.cas.yutnoriswing.view;

import com.cas.yutnoriswing.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InGameView {
    //mainPanel에 보이는 요소들 - 보드, 플레이어, 플레이어 말 상태창
    private final BoardView boardView;
    private final List<Player> players;
    private final JPanel statusPanel;
    private final JPanel mainPanel;

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

        //상태패널 생성
        this.statusPanel = new JPanel();
        this.statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        // 정사각형 보드에 맞게 높이 조정
        this.statusPanel.setPreferredSize(new Dimension(200, 720));
        this.statusPanel.setBorder(BorderFactory.createTitledBorder("플레이어 상태"));
        
        //메인 패널 생성
        this.mainPanel = new JPanel(new BorderLayout());
        
        //상태패널
        buildStatusPanel();
        setupMainPanel();
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

    private void setupMainPanel() {
        // 보드를 중앙에 배치
        mainPanel.add(boardView, BorderLayout.CENTER);
        
        // 상태패널을 오른쪽에 배치
        mainPanel.add(statusPanel, BorderLayout.EAST);
        
        // 윷 던지기 버튼을 아래쪽에 배치
        JButton rollButton = new JButton("윷 던지기");
        rollButton.setPreferredSize(new Dimension(200, 50));
        rollButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(rollButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        rollButton.addActionListener(e -> {
            if (onRollYut != null) onRollYut.run();
        });
    }


    public JPanel getRoot() {
        return mainPanel;
    }

    //말 이동 변경을 컨트롤러가 알려주면 view에서 갱신해주는 메서드
    public void refresh() {
        boardView.refresh();
        buildStatusPanel();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(mainPanel, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public String requestInput(String message, String title) {
        return JOptionPane.showInputDialog(mainPanel, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(mainPanel, message, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }

    //게임 내 사용자가 개입해야 하는 요소들에 대한 것들을 정의
    //테스트모드 윷 던지기, 말 선택하기, 분기 선택하기, 게임 종료/재시작 여부, 순서 재배열

    public int getTestYutThrow() {
        String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
        String result = (String) JOptionPane.showInputDialog(
            mainPanel,
            "테스트 모드: 윷 결과를 선택하세요.",
            "테스트 윷 던지기",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );

        if (result == null) return -999; // 취소
        
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
            mainPanel,
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
        String result = (String) JOptionPane.showInputDialog(
            mainPanel,
            "어느 방향으로 진행하시겠습니까?",
            "분기 선택",
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );

        if (result == null) return first;
        return result.equals(firstLabel) ? first : last;
    }

    private void buildStatusPanel() {
        statusPanel.removeAll();

        //플레이어 수만큼 반복해서
        for (int i = 0; i < players.size(); i++) {
            //각 플레이어의 객체 가져오기
            Player player = players.get(i);

            JLabel playerLabel = new JLabel("<< " + player.getName() + " >>");
            playerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusPanel.add(playerLabel);
            
            //플레이어 토큰 수만큼 반복하여
            for (Token token : player.getTokens()) {
                //토큰 상태가져와서 보여주기
                if (token.getState() == TokenState.READY) {
                    JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    JPanel colorPanel = new JPanel();
                    colorPanel.setBackground(playerColors[i % playerColors.length]);
                    colorPanel.setPreferredSize(new Dimension(10, 10));
                    
                    JLabel tokenLabel = new JLabel(token.getName());
                    tokenLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                    
                    tokenPanel.add(colorPanel);
                    tokenPanel.add(tokenLabel);
                    statusPanel.add(tokenPanel);
                }
            }

            //플레이어 구분선
            if (i < players.size() - 1) {
                statusPanel.add(Box.createVerticalStrut(10));
                statusPanel.add(new JSeparator());
                statusPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    public GameEndChoice getGameEndChoice(String message) {
        int result = JOptionPane.showConfirmDialog(
            mainPanel,
            message + "\n다시 시작하시겠습니까?",
            "게임 종료",
            JOptionPane.YES_NO_OPTION
        );
        
        return result == JOptionPane.YES_OPTION ? GameEndChoice.RESTART : GameEndChoice.EXIT;
    }

    public void setOnRollYut(Runnable handler) {
        this.onRollYut = handler;
    }
} 