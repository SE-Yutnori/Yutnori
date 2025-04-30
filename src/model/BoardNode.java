package model;

import java.util.ArrayList;
import java.util.List;

//각 노드들의 정보(이름, 좌표, 분기 등..)가 담긴 코드
public class BoardNode {
    private String name;
    private float x, y;
    private int boardSize;
    private List<BoardNode> nextNodes;
    private List<Token> tokens;

    public BoardNode(String name, float x, float y, int boardSize) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.boardSize = boardSize;
        this.nextNodes = new ArrayList<>();
        this.tokens = new ArrayList<>();
    }
    //nextNodes를 추가하는 역할 - BoardBuilder에서 노드의 연결을 생성할 때 사용
    public void addNext(BoardNode node) {
        nextNodes.add(node);
    }

    //말의 시작 위치 관련
    public void enter(Token token) {
        tokens.add(token);
    }

    //노드의 이름 반환
    public String getName() {
        return name;
    }

    //노드의 x좌표 반환
    public float getX() {
        return x;
    }

    //노드의 y좌표 반환
    public float getY() {
        return y;
    }

    //현재 윷놀이 판의 사이즈 반환
    public int getBoardSize() {
        return boardSize;
    }

    //다음 노드들을 리스트 형식으로 반환
    public List<BoardNode> getNextNodes() {
        return nextNodes;
    }
}
