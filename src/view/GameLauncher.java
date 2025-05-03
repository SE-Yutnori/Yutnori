package view;

import model.BoardBuilder;
import model.BoardNode;

import javax.swing.*;
import java.util.List;

//게임을 시작하는 class
public class GameLauncher {
    /**
     * 게임에 필요한 정보 수집 이후 플레이
     * 1. 보드 각형
     * 2. 테스트 모드 여부
     * 3. 플레이어 수
     * 4. 플레이어가 사용할 말 개수
     */
    public void start() {
        // JFrame 생성, Panel 추가 등 모든 UI 설정 코드
        SwingUtilities.invokeLater(() -> {

            // boardCustom() model
            int sides = boardCustom();

            // sides 이용하여 BoardBuilder의 buildCustomizingBoard 메서드를 불러와 BoardNode 리스트 생성
            List<BoardNode> board = BoardBuilder.buildCustomizingBoard(sides, 2f);


        });
    }
    // 사용자에게 n각형 커스터마이징을 입력받는 메서드
    private int boardCustom() {
        int sides = 0;
        while (sides < 3) { //입력이 올바르게 될 때까지 반복
            String input = JOptionPane.showInputDialog(null, "몇 각형 보드로 커스텀할까요? (권장 4-6)");
            if (input == null) System.exit(0); //예외처리 : 취소 혹은 창 닫기를 누를 시 종료.
            try {
                sides = Integer.parseInt(input);
            } catch (NumberFormatException e) {//예외처리 : 잘못된 입력은 무시하고 재입력하게끔
                sides = 0;
            }
        }
        return sides; //입력받은 n값을 반환.
    }
}
