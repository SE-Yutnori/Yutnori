package com.cas.yutnorifx.model.entity.token;

import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import com.cas.yutnorifx.model.entity.TokenState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD 방식으로 개발하는 Token 클래스 기본 기능 테스트
 * Red → Green → Refactor 사이클로 진행
 */
class TokenTest {
    
    private Player testPlayer;
    
    @BeforeEach
    void setUp() {
        testPlayer = new Player("테스트플레이어", 3);
    }
    
    @Test
    void 토큰을_생성하면_이름을_반환해야_한다() {
        // Given: 토큰 이름이 주어졌을 때
        String tokenName = "플레이어1-1";
        
        // When: 토큰을 생성하면
        Token token = new Token(tokenName, testPlayer);
        
        // Then: 토큰은 해당 이름을 반환해야 한다
        assertEquals(tokenName, token.getName());
    }
    
    @Test
    void 토큰을_생성하면_소유자를_반환해야_한다() {
        // Given: 플레이어와 토큰 이름이 주어졌을 때
        String tokenName = "플레이어1-1";
        
        // When: 토큰을 생성하면
        Token token = new Token(tokenName, testPlayer);
        
        // Then: 토큰은 해당 소유자를 반환해야 한다
        assertEquals(testPlayer, token.getOwner());
    }
    
    @Test
    void 새로_생성된_토큰은_READY_상태여야_한다() {
        // Given: 토큰 생성 조건이 주어졌을 때
        String tokenName = "플레이어1-1";
        
        // When: 토큰을 생성하면
        Token token = new Token(tokenName, testPlayer);
        
        // Then: 토큰은 READY 상태여야 한다
        assertEquals(TokenState.READY, token.getState());
    }
} 