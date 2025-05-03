package view;

import model.BoardBuilder;
import model.BoardNode;
import model.Player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            // 내부에서 getPlayerCount() 메서드를 내부에서 불러와 플레이어 정보(플레이어 수, 플레이어 이름, 말 수)를 입력받음
            List<Player> players = getPlayers();
        });
    }

    /**
     * 사용자에게 n각형 커스터마이징을 입력받는 메서드
     * @return : sides(입력받은 n값 반환)
     */
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

    /**
     * 플레이어 정보를 입력 받는 메서드 (플레이어 수 , 플레이어 이름, 사용할 말의 갯수)
     * @return : List[Player]
     */
    private List<Player> getPlayers() {
        int numPlayers = getPlayerCount(); //사용자 수를 입력받는 메서드
        int numTokens = getTokenCount(); //사용할 말의 갯수를 입력받는 메서드

        List<Player> players = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        for (int i = 1; i <= numPlayers; i++) {
            String name = null;
            while (true) {
                name = JOptionPane.showInputDialog(null, "플레이어 " + i + "의 이름을 입력하세요:");
                if (name == null) System.exit(0); // 예외처리 : 취소 혹은 창 닫기를 누를 시 종료.
                name = name.trim(); //공백 이름 허용 안함
                if (name.isEmpty()) { //예외처리 : 공백, 중복 -> 잘못된 입력은 무시하고 재입력하게끔
                    JOptionPane.showMessageDialog(null, "이름은 필수입니다.");
                } else if (usedNames.contains(name)) {
                    JOptionPane.showMessageDialog(null, "중복 이름입니다.");
                } else {
                    usedNames.add(name);
                    break;
                }
            }
            // 플레이어 생성
            players.add(new Player(name, numTokens));
        }
        return players;
    }

    /**
     * 플레이어 수를 입력받는 메서드 (2-4명)
     * @return : numPlayers
     */
    private int getPlayerCount() {
        int numPlayers = 0;
        while (numPlayers < 2 || numPlayers > 4) { //2-4 사이의 플레이어 수를 입력할 때까지 반복
            String input = JOptionPane.showInputDialog(null, "플레이어 수를 입력하세요 (2 - 4명)");
            if (input == null) System.exit(0); // 예외처리 : 취소 혹은 창 닫기를 누를 시 종료.
            try {
                numPlayers = Integer.parseInt(input);
            } catch (NumberFormatException e) {//예외처리 : 잘못된 입력은 무시하고 재입력하게끔
                numPlayers = 0;
            }
        }
        return numPlayers;
    }

    /**
     * 사용할 말의 갯수를 입력 받는 메서드 (2-5명)
     * @return : tokenCount
     */
    private int getTokenCount(){
        int tokenCount = 0;
        while (tokenCount < 2 || tokenCount > 5) { //2-5 사이의 말의 갯수를 입력할 때까지 반복
            String input = JOptionPane.showInputDialog(null, "플레이어가 사용할 말의 갯수를 입력하세요. (2 - 5개):", "말 갯수 설정", JOptionPane.QUESTION_MESSAGE);
            if (input == null) System.exit(0); // 예외처리 : 취소 혹은 창 닫기를 누를 시 종료.
            try {
                tokenCount = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {//예외처리 : 잘못된 입력은 무시하고 재입력하게끔
                tokenCount = 0;
            }
        }
        return tokenCount;
    }
}
