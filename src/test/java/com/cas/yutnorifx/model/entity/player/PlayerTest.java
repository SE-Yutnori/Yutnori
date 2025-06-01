package com.cas.yutnorifx.model.entity.player;

import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import com.cas.yutnorifx.model.entity.TokenState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player 클래스 기본 기능 테스트 (TDD)
 */
class PlayerTest {
    
    @Test
    void 플레이어_이름_반환_테스트() {
        // Given: 플레이어 이름이 주어졌을 때
        String playerName = "테스트플레이어";
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, 4);
        
        // Then: 플레이어는 해당 이름을 반환해야 한다
        assertEquals(playerName, player.getName());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void 플레이어에게_입력된_수만큼_토큰_생성되는지_테스트(int tokenCount) {
        // Given: 플레이어 이름과 토큰 수가 주어졌을 때
        String playerName = "테스트플레이어";
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, tokenCount);
        
        // Then: 플레이어는 지정된 수만큼 토큰을 가져야 한다
        assertEquals(tokenCount, player.getTokens().size());
    }
    
    @Test
    void 플레이어_토큰_이름_생성_로직_테스트() {
        // Given: 플레이어 이름과 토큰 수가 주어졌을 때
        String playerName = "테스트플레이어";
        int tokenCount = 3;
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, tokenCount);
        
        // Then: 토큰들은 올바른 이름을 가져야 한다
        assertEquals("테스트플레이어-1", player.getTokens().get(0).getName());
        assertEquals("테스트플레이어-2", player.getTokens().get(1).getName());
        assertEquals("테스트플레이어-3", player.getTokens().get(2).getName());
    }
    
    @Test
    void 플레이어_토큰_소유자_테스트() {
        // Given: 플레이어 이름과 토큰 수가 주어졌을 때
        String playerName = "테스트플레이어";
        int tokenCount = 4;
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, tokenCount);
        
        // Then: 모든 토큰의 소유자가 해당 플레이어여야 한다
        for (Token token : player.getTokens()) {
            assertEquals(player, token.getOwner());
        }
    }
    
    @Test
    void 새로_생성된_플레이어의_모든_토큰은_READY_상태인지_확인_테스트() {
        // Given: 플레이어 이름과 토큰 수가 주어졌을 때
        String playerName = "테스트플레이어";
        int tokenCount = 4;
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, tokenCount);
        
        // Then: 모든 토큰이 READY 상태여야 한다
        for (Token token : player.getTokens()) {
            assertEquals(TokenState.READY, token.getState());
        }
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 6, 10, -1, 0})
    void 유효하지_않은_토큰_수_들어오면_기본값_4개_가지는지_테스트(int invalidTokenCount) {
        // Given: 유효하지 않은 토큰 수가 주어졌을 때 (2-5 범위 밖)
        String playerName = "테스트플레이어";
        
        // When: 플레이어를 생성하면
        Player player = new Player(playerName, invalidTokenCount);
        
        // Then: 기본값인 4개의 토큰을 가져야 한다
        assertEquals(4, player.getTokens().size());
    }
} 