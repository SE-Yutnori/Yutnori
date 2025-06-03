package com.cas.yutnorifx.model.entity;

import java.util.ArrayList;
import java.util.List;

public class BoardNode {
    private String name;                  // 노드 이름 (예: Edge0-1, Center 등)
    private float x, y;                   // 좌표
    private List<BoardNode> nextNodes;    // 다음 노드들
    private List<Token> tokens;           // 현재 올라와 있는 토큰들
    private int sides;                    // 보드의 각 수 (중앙 경로 분기 판단용)

    public BoardNode(String name, float x, float y, int sides) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.sides = sides;
        this.nextNodes = new ArrayList<>();
        this.tokens = new ArrayList<>();
    }

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


    public void enter(Token token) {
        tokens.add(token);
    }

    public void leave(Token token) {
        tokens.remove(token);
    }

    public List<Token> getTokens() {
        return tokens;
    }

}
