package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.List;

// 말의 상태를 나타내는 클래스
public class Token {
    private final String name;
    private final Player owner;
    private TokenState state;
    private List<Token> stackedTokens;
    private BoardNode nextBranchChoice; // 다음 이동 시 선택할 분기 경로
    private BoardNode previousNode;     // 이전 노드 (기본 이동 분기 선택용)

    public Token(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        this.state = TokenState.READY;
        this.stackedTokens = new ArrayList<>();
        this.nextBranchChoice = null;
        this.previousNode = null;
    }

    // Getter 메서드
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

    // 다음 분기 선택할 지 말 지
    public void setNextBranchChoice(BoardNode branchChoice) {
        this.nextBranchChoice = branchChoice;
    }

    public void clearNextBranchChoice() {
        this.nextBranchChoice = null;
    }

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

