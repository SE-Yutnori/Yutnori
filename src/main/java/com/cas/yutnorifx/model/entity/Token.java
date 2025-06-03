package com.cas.yutnorifx.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Token {
    private final String name;
    private final Player owner;
    private TokenState state;
    private List<Token> stackedTokens;
    private BoardNode nextBranchChoice;
    private BoardNode previousNode;

    public Token(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        this.state = TokenState.READY;
        this.stackedTokens = new ArrayList<>();
        this.nextBranchChoice = null;
        this.previousNode = null;
    }

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

    public void setState(TokenState state) {
        this.state = state;
    }

    public void addStackedToken(Token token) {
        if (!stackedTokens.contains(token)) {
            stackedTokens.add(token);
        }
    }

    public void clearStackedTokens() {
        stackedTokens.clear();
    }

    public void clearNextBranchChoice() {
        this.nextBranchChoice = null;
    }

    public void setPreviousNode(BoardNode previousNode) {
        this.previousNode = previousNode;
    }

    public Token getTopMostToken() {
        for (Token tok : owner.getTokens()) {
            if (tok.getStackedTokens().contains(this)) {
                return tok.getTopMostToken();
            }
        }
        return this;
    }
}

