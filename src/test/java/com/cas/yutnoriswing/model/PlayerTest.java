package com.cas.yutnoriswing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player 클래스 테스트")
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("테스트플레이어", 3);
    }

    @Test
    @DisplayName("Player 생성자 - 정상 케이스")
    void testPlayerConstructor_Valid() {
        // Given & When
        Player newPlayer = new Player("김철수", 4);
        
        // Then
        assertEquals("김철수", newPlayer.getName());
        assertEquals(4, newPlayer.getTokens().size());
        
        // 토큰 이름 확인
        assertEquals("김철수-1", newPlayer.getTokens().get(0).getName());
        assertEquals("김철수-4", newPlayer.getTokens().get(3).getName());
        
        // 토큰 소유권 확인
        for (Token token : newPlayer.getTokens()) {
            assertEquals(newPlayer, token.getOwner());
            assertEquals(TokenState.READY, token.getState());
        }
    }

    @Test
    @DisplayName("Player 생성자 - 토큰 개수 생성 로직 테스트")
    void testPlayerConstructor_TokenCount() {
        // 최소값 (2개)
        Player player2 = new Player("최소", 2);
        assertEquals(2, player2.getTokens().size());
        
        // 최대값 (5개)
        Player player5 = new Player("최대", 5);
        assertEquals(5, player5.getTokens().size());
        
        // 범위 밖 값 (1개) - 4개로 조정되어야 함
        Player player1 = new Player("범위밖1", 1);
        assertEquals(4, player1.getTokens().size());
        
        // 범위 밖 값 (6개) - 4개로 조정되어야 함
        Player player6 = new Player("범위밖6", 6);
        assertEquals(4, player6.getTokens().size());
    }

    @Test
    @DisplayName("player finished 상태 검증 테스트 - 모든 토큰이 완주하지 않은 경우")
    void testHasFinished_NotAllFinished() {
        assertFalse(player.hasFinished());
    }

    @Test
    @DisplayName("player finished 상태 검증 테스트 - 모든 토큰이 완주한 경우")
    void testHasFinished_AllFinished() {
        // Given: 모든 토큰을 FINISHED 상태로 변경
        for (Token token : player.getTokens()) {
            token.setState(TokenState.FINISHED);
        }
        
        // When & Then
        assertTrue(player.hasFinished());
    }

    @Test
    @DisplayName("player 이동 가능 토큰 검증 테스트 - FINISHED가 아닌 토큰 반환")
    void testGetMovableTokens() {
        // Given: 일부 토큰을 FINISHED 상태로 변경
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.FINISHED);
        tokens.get(1).setState(TokenState.ACTIVE);
        // tokens.get(2)는 READY 상태 유지
        
        // When
        List<Token> movableTokens = player.getMovableTokens();
        
        // Then
        assertEquals(2, movableTokens.size());
        assertFalse(movableTokens.contains(tokens.get(0))); // FINISHED는 제외
        assertTrue(movableTokens.contains(tokens.get(1)));   // ACTIVE 포함
        assertTrue(movableTokens.contains(tokens.get(2)));   // READY 포함
    }

    @Test
    @DisplayName("player 뒤로 이동 가능 토큰 검증 테스트 - ACTIVE 상태 토큰만 반환")
    void testGetBackwardMovableTokens() {
        // Given
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.READY);
        tokens.get(1).setState(TokenState.ACTIVE);
        tokens.get(2).setState(TokenState.FINISHED);
        
        // When
        List<Token> backwardMovableTokens = player.getBackwardMovableTokens();
        
        // Then
        assertEquals(1, backwardMovableTokens.size());
        assertEquals(tokens.get(1), backwardMovableTokens.get(0));
    }

    @Test
    @DisplayName("player 준비 토큰 검증 테스트 - READY 상태 토큰만 반환")
    void testGetReadyTokens() {
        // Given
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.READY);
        tokens.get(1).setState(TokenState.ACTIVE);
        tokens.get(2).setState(TokenState.READY);
        
        // When
        List<Token> readyTokens = player.getReadyTokens();
        
        // Then
        assertEquals(2, readyTokens.size());
        assertTrue(readyTokens.contains(tokens.get(0)));
        assertTrue(readyTokens.contains(tokens.get(2)));
    }

    @Test
    @DisplayName("player 보드판 위 말 검증 테스트 - ACTIVE 상태 토큰만 반환")
    void testGetActiveTokens() {
        // Given
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.ACTIVE);
        tokens.get(1).setState(TokenState.READY);
        tokens.get(2).setState(TokenState.ACTIVE);
        
        // When
        List<Token> activeTokens = player.getActiveTokens();
        
        // Then
        assertEquals(2, activeTokens.size());
        assertTrue(activeTokens.contains(tokens.get(0)));
        assertTrue(activeTokens.contains(tokens.get(2)));
    }

    @Test
    @DisplayName("player 완주 토큰 검증 테스트 - FINISHED 상태 토큰만 반환")
    void testGetFinishedTokens() {
        // Given
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.FINISHED);
        tokens.get(1).setState(TokenState.ACTIVE);
        tokens.get(2).setState(TokenState.FINISHED);
        
        // When
        List<Token> finishedTokens = player.getFinishedTokens();
        
        // Then
        assertEquals(2, finishedTokens.size());
        assertTrue(finishedTokens.contains(tokens.get(0)));
        assertTrue(finishedTokens.contains(tokens.get(2)));
    }

    @Test
    @DisplayName("player 토큰 이름으로 검색 테스트 - 존재하는 토큰 이름으로 검색")
    void testGetTokenByName_ExistingToken() {
        // When
        Token foundToken = player.getTokenByName("테스트플레이어-2");
        
        // Then
        assertNotNull(foundToken);
        assertEquals("테스트플레이어-2", foundToken.getName());
        assertEquals(player, foundToken.getOwner());
    }

    @Test
    @DisplayName("getTokenByName - 존재하지 않는 토큰 이름으로 검색")
    void testGetTokenByName_NonExistingToken() {
        // When
        Token foundToken = player.getTokenByName("존재하지않는토큰");
        
        // Then
        assertNull(foundToken);
    }

    @Test
    @DisplayName("resetAllTokens - 모든 토큰을 READY 상태로 리셋")
    void testResetAllTokens() {
        // Given: 토큰들을 다양한 상태로 변경
        List<Token> tokens = player.getTokens();
        tokens.get(0).setState(TokenState.ACTIVE);
        tokens.get(1).setState(TokenState.FINISHED);
        tokens.get(2).setState(TokenState.READY); // 이미 READY
        
        // 스택 토큰 추가 (리셋 시 제거되어야 함)
        tokens.get(0).addStackedToken(tokens.get(1));
        
        // When
        player.resetAllTokens();
        
        // Then
        for (Token token : tokens) {
            assertEquals(TokenState.READY, token.getState());
            assertTrue(token.getStackedTokens().isEmpty());
        }
    }

    @Test
    @DisplayName("토큰 이름 생성 로직 검증")
    void testTokenNamingPattern() {
        // Given
        Player player = new Player("홍길동", 5);
        
        // When & Then
        List<Token> tokens = player.getTokens();
        for (int i = 0; i < 5; i++) {
            assertEquals("홍길동-" + (i + 1), tokens.get(i).getName());
        }
    }
} 