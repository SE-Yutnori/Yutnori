package com.cas.yutnorifx.model;

import java.util.ArrayList;
import java.util.List;

// 말(토큰)의 상태를 나타내는 데이터 객체
public class Token {
    private final String name;
    private final Player owner;
    private TokenState state;
    private List<Token> stackedTokens;
    private BoardNode nextBranchChoice; // 다음 이동 시 선택할 분기 경로
    private BoardNode previousNode; // 이전 노드 (직진 계산용)

    public Token(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        this.state = TokenState.READY;
        this.stackedTokens = new ArrayList<>();
        this.nextBranchChoice = null;
        this.previousNode = null;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public TokenState getState() {
        return state;
    }

    public List<Token> getStackedTokens() {
        return new ArrayList<>(stackedTokens);
    }

    public BoardNode getNextBranchChoice() {
        return nextBranchChoice;
    }

    public BoardNode getPreviousNode() {
        return previousNode;
    }

    // Setters (package-private로 설정하여 같은 패키지 내의 GameRules에서만 접근 가능)
    void setState(TokenState state) {
        this.state = state;
    }

    void addStackedToken(Token token) {
        if (!stackedTokens.contains(token)) {
            stackedTokens.add(token);
        }
    }

    void removeStackedToken(Token token) {
        stackedTokens.remove(token);
    }

    void clearStackedTokens() {
        stackedTokens.clear();
    }

    // 다음 분기 선택 설정/해제
    public void setNextBranchChoice(BoardNode branchChoice) {
        this.nextBranchChoice = branchChoice;
    }

    public void clearNextBranchChoice() {
        this.nextBranchChoice = null;
    }

    // 이전 노드 설정/해제
    public void setPreviousNode(BoardNode previousNode) {
        this.previousNode = previousNode;
    }

    public void clearPreviousNode() {
        this.previousNode = null;
    }

    // 업힌 토큰들이 다 대표 토큰이 될 수 있게..
    public Token getTopMostToken() {
        for (Token tok : owner.getTokens()) {
            if (tok.getStackedTokens().contains(this)) {
                return tok.getTopMostToken();
            }
        }
        return this;
    }
}

