package model;

import java.util.List;
import java.util.function.Function;

public class Token {
    private String owner;
    private BoardNode currentNode;
    private int index;//전 위치를 알아야 중앙에서 기본경로 설정이 가능하기 때문에 정의..

    //토큰(말) 생성
    public Token(String owner, BoardNode start) {
        this.owner = owner;
        this.currentNode = start;
        start.enter(this);
    }

    //현재 위치
    public BoardNode getCurrentNode() {
        return currentNode;
    }

    //게임 종료를 전달해야해서 boolean 타입으로 반환
    public boolean move(int steps, Function<List<BoardNode>, BoardNode> branchSelect) {
        int start_step = steps;
        //한 칸씩 이동 - 인터페이스에는 결과만 보여줌
        while (steps > 0) {
            List<BoardNode> nextNodes = currentNode.getNextNodes();
            if (nextNodes.isEmpty()) {
                return true;
            }

            //이동 로직 - 분기가 한개의 경우 바로 이동, 분기가 두개인데 이미 이동 중이면 기본경로(지름길이 아닌 길)로 이동, 분기 도착하면 어디로 갈 지 선택할 수 있게
            if (currentNode.getName().equals("Center") && nextNodes.size() >= 2 && start_step == steps) {// 중앙 분기 - 기본경로(오던 길의 반대길) 또는 도착지로 바로 연결되는 길
                currentNode = branchSelect.apply(nextNodes);

            } else if (nextNodes.size() >= 2 && start_step == steps) {// 꼭짓점 분기 - 기본경로 또는 골목길
                currentNode = branchSelect.apply(nextNodes);

            } else if (currentNode.getName().equals("Center") && index < currentNode.getBoardSize() / 2) {// 골목길에 있지만 아직 index < boardSize/2이면 기본 경로 - 정반대편 길로 이동
                currentNode = nextNodes.get(index);

            } else {// 기본 경로
                currentNode = nextNodes.get(0);
            }

            // 중앙분기를 넘어가는 용도의 index 계산 - 전에 있던 말의 위치에 따라 기본 경로가 바뀌기 때문에
            if (currentNode.getName().startsWith("ToCenter") && currentNode.getName().contains("-")) {
                String[] parts = currentNode.getName().replace("ToCenter", "").split("-");
                index = Integer.parseInt(parts[0]);
            }
            //한 칸 이동한거임
            steps--;
        }
        return false;
    }
}
