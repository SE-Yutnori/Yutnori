package model;

import java.util.ArrayList;
import java.util.List;

//노드에 대한 정보(다음 노드, 이전 노드, 좌표, 이름 등..)가 담긴 클래스
public class BoardNode {
    private String name;                  // 노드 이름 (예: Edge0-1, Center 등)
    private float x, y;                   // 좌표
    private List<BoardNode> nextNodes;    // 다음 노드들
    private List<BoardNode> prevNodes;    // <-- 역방향 연결을 위한 리스트
    private List<Token> tokens;           // 현재 올라와 있는 토큰들
    private int sides;                    // ▶️ 보드의 각 수 (중앙 경로 분기 판단용)

    /**
     * BoardNode 생성자
     * @param name : 각 보드 노드당 이름 설정
     * @param sides : 각형 정보
     * @param x : GUI 상 노드의 x 좌표값
     * @param y : GUI 상 노드의 y 좌표값
     */
    public BoardNode(String name, float x, float y, int sides) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.sides = sides;
        this.nextNodes = new ArrayList<>();
        this.prevNodes = new ArrayList<>(); // 초기화
        this.tokens = new ArrayList<>();
    }

    //Getter
    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public List<BoardNode> getNextNodes() {
        return nextNodes;
    }

    public void addNextNode(BoardNode next) {
        nextNodes.add(next);
    }

    public int getBoardSize() {
        return sides;
    }


    /**
     * 해당 노드에 말 위치 시 기록 메서드
     * @param token : 새롭게 위치한 토큰(말)
     */
    public void enter(Token token) {
        tokens.add(token);
    }

    /**
     * 해당 노드에서 말이 떠날 시 해당 토큰 삭제 메서드
     * @param token : 떠난 토큰(말)
     */
    public void leave(Token token) {
        tokens.remove(token);
    }

    /**
     * 해당 노드 위치한 Token(말) 반환 메서드
     * @return : List[Token] 반환
     */
    public List<Token> getTokens() {
        return tokens;
    }

}
