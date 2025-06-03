package com.cas.yutnorifx.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private String name;
    private List<Token> tokens;

    public Player(String name, int tokenCount) {
        this.name = name;
        this.tokens = new ArrayList<>();

        if (tokenCount < 2 || tokenCount > 5) {
            tokenCount = 4;
        }

        for (int i = 1; i <= tokenCount; i++) {
            String tokenName = name + "-" + i;
            Token token = new Token(tokenName,  this);
            tokens.add(token);
        }
    }

    public String getName() {
        return name;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public boolean hasFinished() {
        for (Token token : tokens) {
            if (token.getState() != TokenState.FINISHED) {
                return false;
            }
        }
        return true;
    }
    
     public List<Token> getMovableTokens() {
        return tokens.stream()
                .filter(token -> token.getState() != TokenState.FINISHED)
                .collect(Collectors.toList());
    }

    public List<Token> getBackwardMovableTokens() {
        return tokens.stream()
                .filter(token -> token.getState() == TokenState.ACTIVE)
                .collect(Collectors.toList());
    }
}

