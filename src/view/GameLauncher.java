package view;

import controller.GameController;
import model.BoardBuilder;
import model.BoardNode;
import model.Token;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//초기 설정 창 역할의 클래스
public class GameLauncher {
    public void start() {
        SwingUtilities.invokeLater(() -> {
            // 보드 만들기 - 밑에 보드 커스터마이징 메소드 호출 (요구사항...)
            int sides = boardCustom();
            //커스텀한 값으로 보드 생성 (model의 BoardBuilder가 생성)
            List<BoardNode> board = BoardBuilder.buildCustomizingBoard(sides, 2f);

            //시작노드는 노드들을 다 돌아서 Edge 0-0(start 노드임) 으로 설정. 모든 노드가 순서가 딱히 없어어 다 찾아야 함.. [0-0, 0-1, 1-2....]
            BoardNode startNode = null;
            for (int i = 0; i < board.size(); i++) {
                BoardNode node = board.get(i);
                if (node.getName().equals("Edge0-0")) {
                    startNode = node;
                    break;
                }
            }

            //예외 처리 : 시작 노드를 못 찾으면 종료
            if (startNode == null) {
                System.out.println("시작 노드를 찾을 수 없습니다.");
                System.exit(1); // 또는 return, throw 등
            }

            //테스트용 코드이기 때문에 현재는 Player1 하나와 말도 하나만 생성
            Token token = new Token("Player1", startNode);

            // 보드와 토큰(model)을 보드패널(view)에 전달
            BoardPanel boardPanel = new BoardPanel(board, token);
            InGameView inGameView = new InGameView(boardPanel);

            // Controller 생성 및 token, inGameView 연결
            GameController controller = new GameController(token, inGameView);

            // Frame 구성 - 기본틀... 나중에 이쁘게 보이려면 여기 수정
            JFrame frame = new JFrame("윷놀이");
            frame.setLayout(new BorderLayout());
            frame.add(boardPanel, BorderLayout.CENTER);

            JButton rollButton = new JButton("윷 던지기");
            rollButton.addActionListener(e -> controller.rollingYut());
            frame.add(rollButton, BorderLayout.SOUTH);

            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    //보드 커스터마이징
    private int boardCustom() {
        //sides 는 n각형의 n값
        int sides = 0;
        //3각형 이하는 게임 진행 불가로 기본 4에서 시작
        while (sides < 3) {
            String input = JOptionPane.showInputDialog(null, "몇 각형 보드로 커스텀하실 건가요? (4각형부터 생성 가능)");
            //아무것도 입력 안하면 종료
            if (input == null) System.exit(0);

            //sides 입력받기
            sides = Integer.parseInt(input);
        }
        return sides;
    }
}
