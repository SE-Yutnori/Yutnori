package com.cas.yutnorifx.model.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 토큰의 위치를 관리하는 클래스
 */
public class TokenPositionManager {
    private final Map<Token, BoardNode> tokenPositions;
    private final Board board;

    public TokenPositionManager(Board board) {
        this.board = board;
        this.tokenPositions = new HashMap<>();
    }

    /**
     * 토큰을 시작 위치에 배치
     */
    public void placeTokenAtStart(Token token) {
        BoardNode startNode = board.getStartNode();
        if (startNode != null && token.getState() == TokenState.READY) {
            token.setState(TokenState.ACTIVE);
            startNode.enter(token);
            tokenPositions.put(token, startNode);
        }
    }

    /**
     * 토큰의 현재 위치 반환
     */
    public BoardNode getTokenPosition(Token token) {
        return tokenPositions.get(token);
    }

    /**
     * 토큰 위치 업데이트
     */
    public void updateTokenPosition(Token token, BoardNode newPosition) {
        if (newPosition == null) {
            tokenPositions.remove(token);
        } else {
            tokenPositions.put(token, newPosition);
        }
    }

    /**
     * 보드 객체 반환
     */
    public Board getBoard() {
        return board;
    }

    /**
     * 토큰을 특정 노드로 이동 (TokenPositionManager의 직권)
     * @param token 이동할 토큰
     * @param targetNode 목표 노드
     * @return 이동 성공 여부
     */
    public boolean moveTokenToNode(Token token, BoardNode targetNode) {
        // 실제 이동할 대표 토큰 찾기 (업힌 토큰이라면 그를 업고 있는 대표 토큰)
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return false;
        }
        
        // 업힌 토큰들 가져오기 (미리 복사)
        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());
        
        // 현재 노드에서 제거
        BoardNode currentNode = getTokenPosition(actualToken);
        if (currentNode != null) {
            currentNode.leave(actualToken);
        }
        
        // 목표 노드에 진입
        if (targetNode != null) {
            targetNode.enter(actualToken);
            updateTokenPosition(actualToken, targetNode);
            
            // 업힌 토큰들 위치 업데이트 (노드에는 진입시키지 않음)
            for (Token stackedToken : stackedTokens) {
                updateTokenPosition(stackedToken, targetNode);
            }
        } else {
            // targetNode가 null이면 완주 처리
            updateTokenPosition(actualToken, null);
            actualToken.setState(TokenState.FINISHED);
            
            // 업힌 토큰들도 완주 처리
            for (Token stackedToken : stackedTokens) {
                updateTokenPosition(stackedToken, null);
                stackedToken.setState(TokenState.FINISHED);
            }
            actualToken.clearStackedTokens();
        }
        
        return true;
    }
    
    /**
     * 토큰을 시작 위치로 되돌리기 (잡혔을 때)
     * @param token 되돌릴 토큰
     */
    public void resetTokenToStart(Token token) {
        // 업힌 토큰들도 함께 초기화
        for (Token stacked : token.getStackedTokens()) {
            updateTokenPosition(stacked, null);
            stacked.setState(TokenState.READY);
            stacked.clearStackedTokens();
        }
        
        // 대표 토큰 초기화
        BoardNode currentNode = getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }
        updateTokenPosition(token, null);
        token.setState(TokenState.READY);
        token.clearStackedTokens();
    }
    
    /**
     * 같은 노드의 토큰들간 상호작용 처리 (잡기/업기)
     * @param movedToken 방금 이동한 토큰
     * @param targetNode 도착한 노드
     * @return 상대방 토큰을 잡았는지 여부
     */
    public boolean handleTokenInteractions(Token movedToken, BoardNode targetNode) {
        if (targetNode == null) return false;
        
        boolean caught = false;
        List<Token> tokensOnNode = new ArrayList<>(targetNode.getTokens());
        
        // 잡기 처리 - 상대방 토큰들 제거
        for (Token t : tokensOnNode) {
            if (t != movedToken && t.getOwner() != movedToken.getOwner()) {
                resetTokenToStart(t);
                caught = true;
            }
        }
        
        // 업기 처리 - 같은 팀 토큰들을 업기
        for (Token t : tokensOnNode) {
            if (t != movedToken && t.getOwner() == movedToken.getOwner()) {
                // 기존 대표 토큰이 가지고 있던 업힌 토큰들을 먼저 수집
                List<Token> existingStackedTokens = new ArrayList<>(t.getStackedTokens());
                
                // 기존 대표 토큰을 노드에서 제거
                targetNode.leave(t);
                // 기존 대표 토큰의 위치를 null로 설정 (업힌 상태임을 표시)
                updateTokenPosition(t, null);
                
                // 기존 대표 토큰을 새로운 대표 토큰에 업기
                movedToken.addStackedToken(t);
                
                // 기존 대표 토큰이 가지고 있던 업힌 토큰들도 모두 새로운 대표 토큰으로 이전
                for (Token stackedToken : existingStackedTokens) {
                    movedToken.addStackedToken(stackedToken);
                }
                
                // 기존 대표 토큰의 업힌 토큰 리스트 정리
                t.clearStackedTokens();
            }
        }
        
        return caught;
    }
} 