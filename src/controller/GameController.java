package controller;

import model.BoardNode;
import model.Player;
import model.YutGameRules;
import view.GameEndChoice;
import view.InGameView;

import java.util.List;

public class GameController {
    private final List<Player> players;
    private final InGameView view;
    public static int currentPlayerIndex = 0;
    private final BoardNode startNode;

    public GameController(List<Player> players, InGameView view, BoardNode startNode) {
        this.players = players;
        this.view = view;
        this.startNode = startNode;
    }

    // "윷 던지기"  버튼 클릭 시 호출되는 메서드
    public void rollingYut() {
        Player currentPlayer = players.get(currentPlayerIndex);

        // 윷 결과 누적 (윷 or 모가 나올 때마다 누적... 이후 순서 선택 가능하게)
        List<Integer> throwResults = YutGameRules.accumulateYut(currentPlayer, view);
        if (throwResults == null || throwResults.isEmpty()) {
            return;
        }

        // 던진 윷들 순서 선택 가능하게 (모, 모, 개 -> 2칸, 5칸, 5칸 으로 재배열)
        List<Integer> orderedResults;
        if (throwResults.size() == 1) {
            orderedResults = throwResults;
        } else {
            orderedResults = YutGameRules.reorderResults(throwResults, currentPlayer.getName(), view);
            if (orderedResults == null || orderedResults.isEmpty()) {
                return;
            }
        }

        // 이동 후 잡았는 지 확인
        boolean catched = YutGameRules.applyMoves(currentPlayer, orderedResults, startNode, view);

        // 승리 조건 확인
        if (currentPlayer.hasFinished()) {
            // 승리 시, InGameView를 통해 게임 종료 화면을 표시
            GameEndChoice choice = view.getGameEndChoice(currentPlayer.getName() + " 승리!");
            if (choice == GameEndChoice.RESTART) {
                view.restartGame();  // 재시작
            } else {
                view.exitGame();  // 종료
            }
            return;
        }

        // 잡았으면 추가 턴 부여
        if (catched) {
            view.showMessage(currentPlayer.getName() + "님이 말을 잡아 추가 턴을 얻었습니다!", "추가 턴");
        } else {
            nextPlayer();
        }
    }

    // 다음 플레이어에게 턴 넘기기
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
