package com.cas.yutnoriswing.view;

import com.cas.yutnoriswing.model.BoardNode;
import com.cas.yutnoriswing.model.Player;
import com.cas.yutnoriswing.model.Token;
import com.cas.yutnoriswing.model.TokenState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InGameView {
    // 보드 뷰
    private final BoardView boardView;
    // 플레이어 리스트 (이름, Token 리스트 보유)
    private final List<Player> players;
    // 오른쪽에 나타나는 상태 패널
    private final JPanel statusPanel;
    // 메인 패널
    private final JPanel mainPanel;

    // 플레이어 색상 배열
    private final Color[] playerColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA
    };

    //Runnable 타입의 onRollYut 변수 선언
    private Runnable onRollYut;

    // InGameView 생성자
    public InGameView(List<BoardNode> board, List<Player> players) {
        this.players = players;
        this.boardView = new BoardView(board, players);

        //상태패널 생성
        this.statusPanel = new JPanel();
        this.statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        // 정사각형 보드(720x720)에 맞게 높이 조정
        this.statusPanel.setPreferredSize(new Dimension(200, 720));
        this.statusPanel.setBorder(BorderFactory.createTitledBorder("플레이어 상태"));
        
        //메인 패널 생성
        this.mainPanel = new JPanel(new BorderLayout());
        
        //상태패널 빌드
        buildStatusPanel();
        setupMainPanel();
    }

    private void setupMainPanel() {
        // 보드뷰를 중앙에 배치
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
        //상태패널 자식 컴포넌트 제거
        statusPanel.removeAll();

        //플레이어 수만큼 반복
        for (int i = 0; i < players.size(); i++) {
            //플레이어 객체 가져오기
            Player player = players.get(i);

            //플레이어 이름 레이블 생성
            JLabel playerLabel = new JLabel("<< " + player.getName() + " >>");
            playerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusPanel.add(playerLabel);
            
            //플레이어 토큰 수만큼 반복
            for (Token token : player.getTokens()) {
                //토큰 상태가 READY인 경우
                if (token.getState() == TokenState.READY) {
                    JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    
                    // 색상 표시용 작은 패널
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
            
            // 플레이어 간 구분선
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