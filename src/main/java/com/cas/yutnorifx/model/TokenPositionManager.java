package com.cas.yutnorifx.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//토큰의 위치(보드에 두거나, 보드에서 빼거나)를 관리하는 클래스
public class TokenPositionManager {
    private final Map<Token, BoardNode> tokenPositions;
    private final Board board;

    public TokenPositionManager(Board board) {
        this.board = board;
        this.tokenPositions = new HashMap<>();
    }

    //토큰을 시작 위치에 배치
    public void placeTokenAtStart(Token token) {
        BoardNode startNode = board.getStartNode();
        if (startNode != null && token.getState() == TokenState.READY) {
            token.setState(TokenState.ACTIVE);
            startNode.enter(token);
            tokenPositions.put(token, startNode);
        }
    }

    //토큰의 현재 위치 반환
    public BoardNode getTokenPosition(Token token) {
        return tokenPositions.get(token);
    }

    //토큰 위치 업데이트
    public void updateTokenPosition(Token token, BoardNode newPosition) {
        if (newPosition == null) {
            tokenPositions.remove(token);
        } else {
            tokenPositions.put(token, newPosition);
        }
    }

    public Board getBoard() {
        return board;
    }
} 