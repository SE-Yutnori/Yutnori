package controller;

import model.BoardNode;
import model.Token;
import view.InGameView;

import java.util.List;
import java.util.function.Function;

//사용자 입력받고 token에게 이동 지시 (UI -> in game)
//이동 후 UI에 다시 표시 (in game -> UI)

public class GameController {
    private final Token token;
    private final InGameView view;

    public GameController(Token token, InGameView view) {
        this.token = token;
        this.view = view;
    }

    //윷을 던지고 이동까지
    public void rollingYut() {
        int steps = view.doGaeGirl();
        //현재는 음수는 입력 못하게 되어 있는데 결국은 빽도가 추가될 예정이라 테스트 코드 이후 완성 코드에서는 바뀔 예정
        if (steps <= 0) {
            view.showError("숫자를 정확히 입력해주세요!");
            return;
        }
        // Token의 move 메소드에서 이동하는 것을 가져와
        boolean ended = token.move(steps, callSelectPath());
        //말을 옮겨준다.
        view.refresh();
        //move는 더 이상 움직일 곳이 없으면 true 반환
        if (ended) {
            view.showGameOver("게임이 종료되었습니다!");
        }
    }

    //분기 선택 창을 띄워주는 selectPath를 호출...
    private Function<List<BoardNode>, BoardNode> callSelectPath() {
        return (options) -> view.selectPath(options);
    }
}
