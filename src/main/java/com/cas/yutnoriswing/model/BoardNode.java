package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.List;

//노드에 대한 정보(다음 노드, 이전 노드, 좌표, 이름 등..)가 담긴 클래스
public class BoardNode {
    private String name;                  // 노드 이름 (예: Edge0-1, Center 등)
    private float x, y;                   // 좌표
    private List<BoardNode> nextNodes;    // 다음 노드들
    private List<Token> tokens;           // 현재 올라와 있는 토큰들
    private int sides;                    // 보드의 각 수 (중앙 경로 분기 판단용)

    //BoardNode 생성자
    public BoardNode(String name, float x, float y, int sides) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.sides = sides;
        this.nextNodes = new ArrayList<>();
        this.tokens = new ArrayList<>();
    }

    //Getter 메서드
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


    //해당 노드에 말 위치 시 새로 기록해주는 메서드
    public void enter(Token token) {
        tokens.add(token);
    }

    //해당 노드에서 말이 떠날 시 해당 토큰을 삭제해주는 메서드
    public void leave(Token token) {
        tokens.remove(token);
    }

    //해당 노드 위치한 말을 반환하는 메서드
    public List<Token> getTokens() {
        return tokens;
    }

}
