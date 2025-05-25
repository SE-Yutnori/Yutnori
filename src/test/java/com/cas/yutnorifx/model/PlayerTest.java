package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player 클래스 테스트")
class PlayerTest {
    
    private Player player;
    
    @BeforeEach
    void setUp() {
        player = new Player("테스트플레이어", 4);
    }
    
    @Nested
    @DisplayName("플레이어 생성 테스트")
    class PlayerCreationTest {
        
        @Test
        @DisplayName("플레이어가 올바르게 생성되어야 함")
        void shouldCreatePlayerCorrectly() {
            assertEquals("테스트플레이어", player.getName());
            assertEquals(4, player.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 개수가 2개일 때 올바르게 생성되어야 함")
        void shouldCreatePlayerWith2Tokens() {
            Player player2 = new Player("플레이어2", 2);
            assertEquals(2, player2.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 개수가 5개일 때 올바르게 생성되어야 함")
        void shouldCreatePlayerWith5Tokens() {
            Player player5 = new Player("플레이어5", 5);
            assertEquals(5, player5.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 개수가 범위를 벗어나면 4개로 설정되어야 함")
        void shouldDefaultTo4TokensWhenOutOfRange() {
            Player playerLow = new Player("플레이어낮음", 1);
            Player playerHigh = new Player("플레이어높음", 6);
            
            assertEquals(4, playerLow.getTokens().size());
            assertEquals(4, playerHigh.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰들이 올바른 이름으로 생성되어야 함")
        void shouldCreateTokensWithCorrectNames() {
            assertEquals("테스트플레이어-1", player.getTokens().get(0).getName());
            assertEquals("테스트플레이어-2", player.getTokens().get(1).getName());
            assertEquals("테스트플레이어-3", player.getTokens().get(2).getName());
            assertEquals("테스트플레이어-4", player.getTokens().get(3).getName());
        }
        
        @Test
        @DisplayName("모든 토큰의 소유자가 플레이어여야 함")
        void shouldOwnAllTokens() {
            for (Token token : player.getTokens()) {
                assertEquals(player, token.getOwner());
            }
        }
    }
    
    @Nested
    @DisplayName("게임 완료 테스트")
    class GameFinishTest {
        
        @Test
        @DisplayName("모든 토큰이 READY 상태일 때 완료되지 않아야 함")
        void shouldNotFinishWhenAllTokensReady() {
            assertFalse(player.hasFinished());
        }
        
        @Test
        @DisplayName("일부 토큰만 FINISHED 상태일 때 완료되지 않아야 함")
        void shouldNotFinishWhenSomeTokensFinished() {
            // 첫 번째 토큰만 완주 처리
            player.getTokens().get(0).finishIndividually();
            
            assertFalse(player.hasFinished());
        }
        
        @Test
        @DisplayName("모든 토큰이 FINISHED 상태일 때 완료되어야 함")
        void shouldFinishWhenAllTokensFinished() {
            // 모든 토큰을 완주 처리
            for (Token token : player.getTokens()) {
                token.finishIndividually();
            }
            
            assertTrue(player.hasFinished());
        }
        
        @Test
        @DisplayName("일부 토큰이 ACTIVE 상태일 때 완료되지 않아야 함")
        void shouldNotFinishWhenSomeTokensActive() {
            BoardNode testNode = new BoardNode("테스트", 0, 0, 4);
            
            // 첫 번째 토큰을 ACTIVE 상태로 만듦
            player.getTokens().get(0).start(testNode);
            
            // 나머지 토큰들을 완주 처리
            for (int i = 1; i < player.getTokens().size(); i++) {
                player.getTokens().get(i).finishIndividually();
            }
            
            assertFalse(player.hasFinished());
        }
    }
    
    @Nested
    @DisplayName("토큰 상태 변화 테스트")
    class TokenStateChangeTest {
        
        @Test
        @DisplayName("토큰 상태 변화가 완료 상태에 반영되어야 함")
        void shouldReflectTokenStateChanges() {
            BoardNode testNode = new BoardNode("테스트", 0, 0, 4);
            
            // 초기에는 완료되지 않음
            assertFalse(player.hasFinished());
            
            // 모든 토큰을 시작시킴
            for (Token token : player.getTokens()) {
                token.start(testNode);
            }
            assertFalse(player.hasFinished());
            
            // 하나씩 완주 처리
            for (int i = 0; i < player.getTokens().size() - 1; i++) {
                player.getTokens().get(i).finishIndividually();
                assertFalse(player.hasFinished());
            }
            
            // 마지막 토큰 완주 처리
            player.getTokens().get(player.getTokens().size() - 1).finishIndividually();
            assertTrue(player.hasFinished());
        }
    }
    
    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {
        
        @Test
        @DisplayName("토큰 개수 0일 때 4개로 설정되어야 함")
        void shouldDefaultTo4TokensWhenZero() {
            Player playerZero = new Player("플레이어0", 0);
            assertEquals(4, playerZero.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 개수 음수일 때 4개로 설정되어야 함")
        void shouldDefaultTo4TokensWhenNegative() {
            Player playerNegative = new Player("플레이어음수", -1);
            assertEquals(4, playerNegative.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 개수 매우 큰 수일 때 4개로 설정되어야 함")
        void shouldDefaultTo4TokensWhenVeryLarge() {
            Player playerLarge = new Player("플레이어큰수", 100);
            assertEquals(4, playerLarge.getTokens().size());
        }
    }
} 