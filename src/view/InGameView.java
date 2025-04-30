package view;

import model.BoardNode;

import javax.swing.*;
import java.util.List;

//게임 안에서 벌어지는 팝업, 오류창, 윷(테스트 코드에서는 사용자가 입력) 등을 보여줌
public class InGameView {
    private final BoardPanel boardPanel;

    //보드 보여주기
    public InGameView(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
    }

    //말이 움직이고 난 후 움직임을 적용시킨 것을 보기 위함
    public void refresh() {
        boardPanel.repaint();
    }

    //게임 종료 메세지 팝업으로 표시
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(null, message, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
    }

    //오류 메세지 팝업으로 표시
    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }

    //테스트 용이라 한 번에 이동 많이 할 수 있게 사용자가 입력 받아서 테스트. 추후에는 랜덤 숫자 -1(빽도), 1(도), 2(개), 3(걸), 4(윷), 5(모) 적용 예정
    public int doGaeGirl() {
        String input = JOptionPane.showInputDialog(null, "몇 칸 이동하시겠습니까?");
        if (input == null) return -1; // 취소

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // 잘못된 입력
        }
    }

    //분기점에 도착하고 출발 시 어느 분기로 갈 지 결정
    public BoardNode selectPath(List<BoardNode> options) {
        //외곽 분기는 1로 표시하고 내부 골목길에서의 방향은 임의의 숫자(node를 생성할 때 쓴..)로 표시해서 선택하게끔
        String[] whereToGo = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            BoardNode node = options.get(i);
            if (node.getName().contains("Edge")) {
                whereToGo[i] = "1";
            } else {
                whereToGo[i] = node.getName().replace("ToCenter", "");
            }
        }

        // 분기 선택창 띄우기
        String selectedLabel = (String) JOptionPane.showInputDialog(
                null,
                "어느 방향으로 진행하시겠습니까?",
                "분기 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                whereToGo,
                whereToGo[0]
        );

        // 선택 안 하면 경고창
        if (selectedLabel == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "경로를 선택하셔야 진행할 수 있습니다!",
                    "선택 필요",
                    JOptionPane.WARNING_MESSAGE
            );
            return selectPath(options); // 선택 안하면 계속 선택 요청
        }

        // 선택한 분기로 가게끔
        for (int i = 0; i < whereToGo.length; i++) {
            if (whereToGo[i].equals(selectedLabel)) {
                return options.get(i);
            }
        }
        return null; // 아마 여기까지는 없을 거임
    }
}
