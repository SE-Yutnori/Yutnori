package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//플레이어의 정보가 담긴 클래스
public class Player {
    private String name;            //플레이어 이름
    private List<Token> tokens;     //보유 중인 말들

    public Player(String name, int tokenCount) {
        this.name = name;
        this.tokens = new ArrayList<>();

        // 토큰 갯수가 2~5 범위인지 확인 (그 외의 범위 들어올 시 4로 설정인데 아마 경고창 때문에 그런 값 안 들어올 듯..)
        if (tokenCount < 2 || tokenCount > 5) {
            tokenCount = 4;
        }

        // tokenCount 만큼 말 생성
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

    //모든 토큰이 FINISHED 상태인 지를 통해 승리 플레이어 결정
    public boolean hasFinished() {
        for (Token token : tokens) {
            if (token.getState() != TokenState.FINISHED) {
                return false;
            }
        }
        return true;
    }
    
    //이동 가능한 토큰들을 반환 (FINISHED가 아닌 토큰들)
    public List<Token> getMovableTokens() {
        return tokens.stream()
                .filter(token -> token.getState() != TokenState.FINISHED)
                .collect(Collectors.toList());
    }
    
    //빽도로 이동 가능한 토큰들을 반환 (ACTIVE 상태인 토큰들)
    public List<Token> getBackwardMovableTokens() {
        return tokens.stream()
                .filter(token -> token.getState() == TokenState.ACTIVE)
                .collect(Collectors.toList());
    }
    
    //대기 상태인 토큰들을 반환 (READY 상태인 토큰들)
    public List<Token> getReadyTokens() {
        return tokens.stream()
                .filter(token -> token.getState() == TokenState.READY)
                .collect(Collectors.toList());
    }
    
    //게임 중인 토큰들을 반환 (ACTIVE 상태인 토큰들)
    public List<Token> getActiveTokens() {
        return tokens.stream()
                .filter(token -> token.getState() == TokenState.ACTIVE)
                .collect(Collectors.toList());
    }
    
    //완주한 토큰들을 반환 (FINISHED 상태인 토큰들)
    public List<Token> getFinishedTokens() {
        return tokens.stream()
                .filter(token -> token.getState() == TokenState.FINISHED)
                .collect(Collectors.toList());
    }
    
    //특정 이름의 토큰을 찾아 반환
    public Token getTokenByName(String tokenName) {
        return tokens.stream()
                .filter(token -> tokenName.equals(token.getName()))
                .findFirst()
                .orElse(null);
    }
    
    //모든 토큰을 초기 상태로 리셋
    public void resetAllTokens() {
        for (Token token : tokens) {
            if (token.getState() != TokenState.READY) {
                token.setState(TokenState.READY);
                token.clearStackedTokens();
            }
        }
    }
}

