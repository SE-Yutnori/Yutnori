package view;

import model.Player;
import model.Token;
import model.TokenState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//게임 내에서 실질적으로 보이는 팝업 창이나 UI를 구현한 클래스
public class InGameView {
    private final BoardPanel boardPanel;
    private final List<Player> players;
    private final JPanel statusPanel;

    //각 플레이어 말 색상은 기본으로 지정
    private final Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA};

    public InGameView(BoardPanel boardPanel, List<Player> players, JPanel statusPanel) {
        this.boardPanel = boardPanel;
        this.players = players;
        this.statusPanel = statusPanel;
    }

    // 화면 전체 다시 그리기
    public void refresh() {
        boardPanel.repaint(); //윷놀이판 다시 그리기
        buildStatusPanel();  //말 상태창 다시 그리기
    }

    // 메시지 출력 - 결과창이나 멘트들을 해당 메서드로 사용자에게 보여줌
    public void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // 메세지 입력창 - 사용자에게 입력받을 것이 있을 때 해당 메서드를 통해 입력 받음
    public String requestInput(String message, String title) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    // 오류창 - 입력 범위 오류, 입력 타입 오류 같은 오류를 해당 메서드를 통해 보여줌
    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }

    // 테스트용 윷 던지기를 진행하게 하는 메서드
    // 기본 윷 던지기는 UI적으로 보여줄 게 없어서 showMessage 메서드를 model 쪽에서 불러와서 실행
    public int getTestYutThrow() {
        String[] options = new String[]{"빽도", "도", "개", "걸", "윷", "모"};
        int selection = JOptionPane.showOptionDialog(
                boardPanel, // 부모 컴포넌트로 지정
                "테스트 모드: 윷 결과를 선택하세요.",
                "테스트 윷 던지기",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]  // 기본 선택을 도
        );
        switch (selection) {
            case 0: return -1;  // 빽도
            case 1: return 1;   // 도
            case 2: return 2;   // 개
            case 3: return 3;   // 걸
            case 4: return 4;   // 윷
            case 5: return 5;   // 모
            default: return 1;  // 기본값은 "도"
        }
    }

    // 나온 윷 숫자에 대해서 움직일 말을 선택하게 하는 메서드
    public Token selectToken(List<Token> tokens, int steps) {
        //names에는 상태가 FINISHED가 아닌 토큰을 담음
        String[] names = tokens.stream()
                .filter(t -> t.getState() != TokenState.FINISHED)
                .map(Token::getName)
                .toArray(String[]::new);

        // names에 아무런 토큰이 없으면 모든 말이 도착했습니다. -> 아마 실질적으로 표시 안될거임!!
        if (names.length == 0) {
            showError("모든 말이 도착했습니다.");
            return null;
        }

        //names 중에서 말을 고를 수 있음
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "어떤 말을 "+ steps +"칸 이동하시겠습니까?",
                "말 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                names,
                names[0]
        );

        //아무런 말을 선택하지 않으면 턴 종료
        if (selected == null) return null;

        //고른 말을 token.class에 반환
        for (Token token : tokens) {
            if (token.getName().equals(selected)) {
                return token;
            }
        }

        return null;
    }

    //분기점에 도착 시 분기를 선택하는 창을 출력하는 메서드 - 이게 기존 제안한 방식....
//    public model.BoardNode selectPath(List<model.BoardNode> options) {
//        String[] whereToGo = new String[options.size()];
//        int size = options.size();
//        //이게 기존에 제안한 방식..
//        for (int i = 0; i < size; i++) {
//            model.BoardNode node = options.get(i);
//            if (node.getName().contains("Edge")) {
//                whereToGo[i] = "1";
//            } else {
//                whereToGo[i] = node.getName().replace("ToCenter", "");
//            }
//        }
//
//
//        String selectedLabel = (String) JOptionPane.showInputDialog(
//                null,
//                "어느 방향으로 진행하시겠습니까?",
//                "분기 선택",
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                whereToGo,
//                whereToGo[0]
//        );
//
//        //이게 기존에 제안한 방식..
//        //선택된 노드를 BoardNode의 nextNode에 반환
//        for (int i = 0; i < options.size(); i++) {
//            if (whereToGo[i].equals(selectedLabel)) {
//                return options.get(i);
//            }
//        }
//
//
//        //선택 안 할 시 기본 분기로 외곽 분기의 경우 외곽길 진행
//        return options.get(0);
//    }

    // 분기점에 도착 시 분기를 선택하는 창을 출력하는 메서드 - 이게 교수님께서 제안한 방식....
    public model.BoardNode selectPath(List<model.BoardNode> options) {
        int size = options.size();
        // 옵션이 하나 이하일 땐 바로 반환
        if (size <= 1) {
            return options.get(0);
        }

        // 첫 번째와 마지막 노드
        model.BoardNode first = options.get(0);
        model.BoardNode last  = options.get(size - 1);

        // 라벨 생성 (원래 로직 그대로)
        String firstLabel = first.getName().contains("Edge")
                ? "1"
                : first.getName().replace("ToCenter", "");
        String lastLabel  = last.getName().contains("Edge")
                ? "1"
                : last.getName().replace("ToCenter", "");

        // 딱 두 개만 담아서 보여주기
        String[] whereToGo = new String[]{ firstLabel, lastLabel };

        String selectedLabel = (String) JOptionPane.showInputDialog(
                null,
                "어느 방향으로 진행하시겠습니까?",
                "분기 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                whereToGo,
                whereToGo[0]
        );

        // 선택한 쪽을 반환 (취소되거나 firstLabel 선택 시 첫 번째)
        if (selectedLabel == null || selectedLabel.equals(firstLabel)) {
            return first;
        } else {
            return last;
        }
    }


    // 현재 각 플레이어의 말 상태를 표시해주는 메서드 (대기 중인 말만 보이게)
    public void buildStatusPanel() {
        statusPanel.removeAll(); // 이전 컴포넌트 모두 제거
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            JLabel playerLabel = new JLabel("<< " + player.getName() + " >>");
            playerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            playerLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            statusPanel.add(playerLabel);

            for (Token token : player.getTokens()) {
                if (token.getState() == TokenState.READY) {
                    JLabel tokenLabel = new JLabel("   ⬤ " + token.getName());
                    tokenLabel.setForeground(playerColors[i % playerColors.length]);
                    statusPanel.add(tokenLabel);
                }
            }
            statusPanel.add(Box.createVerticalStrut(10));
        }
        statusPanel.revalidate();
        statusPanel.repaint();    // 다시 그리기
    }

    // 게임 종료 후 게임을 재시작할 지 종료할 지 묻는 창을 띄우는 메서드
    public GameEndChoice getGameEndChoice(String message) {
        int option = JOptionPane.showOptionDialog(
                boardPanel,  // 부모 컴포넌트 지정
                message + "\n재시작하시겠습니까?",
                "게임 종료",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"재시작", "종료"},
                "재시작"
        );
        return (option == JOptionPane.YES_OPTION) ? GameEndChoice.RESTART : GameEndChoice.EXIT;
    }

    // 게임 종료 시 창을 종료하는 메서드
    public void exitGame() {
        // 부모 컴포넌트(boardPanel)의 창을 찾아 종료
        Window window = SwingUtilities.getWindowAncestor(boardPanel);
        if (window != null) {
            window.dispose();
        }
        System.exit(0); // 또는 shutdown 로직을 호출
    }

    // 게임 재시작을 위한 메서드
    public void restartGame() {
        Window window = SwingUtilities.getWindowAncestor(boardPanel);
        if (window != null) {
            window.dispose();
        }
        new GameLauncher().start();
    }
}
