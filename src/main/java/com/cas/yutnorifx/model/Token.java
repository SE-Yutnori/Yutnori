package com.cas.yutnorifx.model;

import java.util.ArrayList;
import java.util.List;

// 말(토큰)의 상태를 나타내는 데이터 객체
public class Token {
    private final String name;
    private final Player owner;
    private TokenState state;
    private List<Token> stackedTokens;

    public Token(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        this.state = TokenState.READY;
        this.stackedTokens = new ArrayList<>();
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

