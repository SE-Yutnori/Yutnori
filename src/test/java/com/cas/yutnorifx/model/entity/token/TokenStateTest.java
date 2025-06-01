package com.cas.yutnorifx.model.entity.token;

import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import com.cas.yutnorifx.model.entity.TokenState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Token 상태 관리 테스트 (TDD)
 */
class TokenStateTest {
    
    private Player testPlayer;
    private Token token;
    
    @BeforeEach
    void setUp() {
        testPlayer = new Player("테스트플레이어", 3);
        token = new Token("테스트토큰", testPlayer);
    }
    
    @Test
    void 토큰_상태를_ACTIVE로_변경할_수_있어야_한다() {
        // When: 토큰 상태를 ACTIVE로 변경하면
        token.setState(TokenState.ACTIVE);
        
        // Then: 토큰 상태가 ACTIVE여야 한다
        assertEquals(TokenState.ACTIVE, token.getState());
    }
    
    @Test
    void 토큰_상태를_FINISHED로_변경할_수_있어야_한다() {
        // When: 토큰 상태를 FINISHED로 변경하면
        token.setState(TokenState.FINISHED);
        
        // Then: 토큰 상태가 FINISHED여야 한다
        assertEquals(TokenState.FINISHED, token.getState());
    }
    
    @Test
    void 토큰_상태를_여러번_변경할_수_있어야_한다() {
        // When: 토큰 상태를 여러 번 변경하면
        token.setState(TokenState.ACTIVE);
        token.setState(TokenState.FINISHED);
        token.setState(TokenState.READY);
        
        // Then: 마지막 설정 상태가 반영되어야 한다
        assertEquals(TokenState.READY, token.getState());
    }
} 