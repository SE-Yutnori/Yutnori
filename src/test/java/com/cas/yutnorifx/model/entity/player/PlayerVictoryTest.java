package com.cas.yutnorifx.model.entity.player;

import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import com.cas.yutnorifx.model.entity.TokenState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player 승리 조건 테스트 (TDD)
 */
class PlayerVictoryTest {
    
    private Player player;
    
    @BeforeEach
    void setUp() {
        player = new Player("테스트플레이어", 3);
    }
    
    @Test
    void 모든_토큰이_READY_상태면_승리하지_않았어야_한다() {
        // Given: 모든 토큰이 READY 상태일 때
        // (기본 상태이므로 별도 설정 불필요)
        
        // Then: 승리하지 않았어야 한다
        assertFalse(player.hasFinished());
    }
    
    @Test
    void 일부_토큰만_FINISHED_상태면_승리하지_않았어야_한다() {
        // Given: 일부 토큰만 FINISHED 상태일 때
        player.getTokens().get(0).setState(TokenState.FINISHED);
        player.getTokens().get(1).setState(TokenState.ACTIVE);
        // 세 번째 토큰은 READY 상태 유지
        
        // Then: 승리하지 않았어야 한다
        assertFalse(player.hasFinished());
    }
    
    @Test
    void 모든_토큰이_FINISHED_상태면_승리해야_한다() {
        // Given: 모든 토큰이 FINISHED 상태일 때
        for (Token token : player.getTokens()) {
            token.setState(TokenState.FINISHED);
        }
        
        // Then: 승리해야 한다
        assertTrue(player.hasFinished());
    }
    
    @Test
    void 일부_토큰이_ACTIVE_상태면_승리하지_않았어야_한다() {
        // Given: 대부분 토큰이 FINISHED이고 하나가 ACTIVE일 때
        player.getTokens().get(0).setState(TokenState.FINISHED);
        player.getTokens().get(1).setState(TokenState.FINISHED);
        player.getTokens().get(2).setState(TokenState.ACTIVE);
        
        // Then: 승리하지 않았어야 한다
        assertFalse(player.hasFinished());
    }
} 