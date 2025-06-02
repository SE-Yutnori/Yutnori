package com.cas.yutnoriswing;

import com.cas.yutnoriswing.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 윷놀이 게임의 회귀 테스트 모음
 * 
 * 주요 기능들이 기존 요구사항대로 작동하는지 확인하는 테스트들
 */
@DisplayName("윷놀이 게임 회귀 테스트 스위트")
public class TestSuite {

    @BeforeEach
    void setUp() {
        // 각 테스트 전 환경 초기화
        YutGameRules.setTestMode(false);
    }

    @Test
    @DisplayName("회귀테스트 - 기본 게임 설정")
    void testBasicGameSetup() {
        // Given: 표준 게임 설정
        GameState gameState = new GameState(5, 2.0f, 
            java.util.Arrays.asList("플레이어1", "플레이어2"), 
            java.util.Arrays.asList(4, 4));
        
        // Then: 기본 요구사항 충족 확인
        assertNotNull(gameState.getBoard(), "보드가 생성되어야 함");
        assertEquals(2, gameState.getPlayers().size(), "2명의 플레이어");
        
        for (Player player : gameState.getPlayers()) {
            assertEquals(4, player.getTokens().size(), "각 플레이어당 4개의 토큰");
            for (Token token : player.getTokens()) {
                assertEquals(TokenState.READY, token.getState(), "초기 토큰 상태는 READY");
            }
        }
        
        assertFalse(gameState.isGameEnded(), "게임 시작 시 종료되지 않은 상태");
        assertEquals(gameState.getPlayers().get(0), gameState.getCurrentPlayer(), "첫 번째 플레이어부터 시작");
    }

    @Test
    @DisplayName("회귀테스트 - 다양한 보드 크기 지원 (4-6각형)")
    void testVariousBoardSizes() {
        // Given & When & Then: 4, 5, 6각형 모두 지원해야 함
        for (int sides = 4; sides <= 6; sides++) {
            final int boardSides = sides; // final 변수로 선언
            assertDoesNotThrow(() -> {
                GameState game = new GameState(boardSides, 2.0f, 
                    java.util.Arrays.asList("테스트"), 
                    java.util.Arrays.asList(2));
                
                assertTrue(game.getBoard().getNodes().size() > 0, 
                    boardSides + "각형 보드에 노드가 존재해야 함");
            }, boardSides + "각형 보드 생성 실패");
        }
    }

    @Test
    @DisplayName("회귀테스트 - 플레이어 수 제한 (2-4명)")
    void testPlayerCountLimits() {
        // Given: 유효한 플레이어 수
        for (int playerCount = 2; playerCount <= 4; playerCount++) {
            final int count = playerCount; // final 변수로 선언
            // When & Then: 정상 생성되어야 함
            assertDoesNotThrow(() -> {
                java.util.List<String> names = new java.util.ArrayList<>();
                java.util.List<Integer> tokens = new java.util.ArrayList<>();
                
                for (int i = 1; i <= count; i++) {
                    names.add("플레이어" + i);
                    tokens.add(3);
                }
                
                GameState game = new GameState(4, 2.0f, names, tokens);
                assertEquals(count, game.getPlayers().size());
            }, count + "명 플레이어 게임 생성 실패");
        }
    }

    @Test
    @DisplayName("회귀테스트 - 토큰 수 제한 (2-5개)")
    void testTokenCountLimits() {
        // Given: 유효한 토큰 수
        for (int tokenCount = 2; tokenCount <= 5; tokenCount++) {
            // When
            Player player = new Player("테스트플레이어", tokenCount);
            
            // Then
            assertEquals(tokenCount, player.getTokens().size(), 
                tokenCount + "개 토큰 생성 실패");
        }
    }

    @Test
    @DisplayName("회귀테스트 - 테스트 모드 기능")
    void testTestModeFeature() {
        // Given: 테스트 모드 비활성화 상태
        YutGameRules.setTestMode(false);
        assertFalse(YutGameRules.isTestMode());
        
        // When: 테스트 모드 활성화
        YutGameRules.setTestMode(true);
        
        // Then: 예측 가능한 결과
        assertTrue(YutGameRules.isTestMode());
        assertEquals(1, YutGameRules.throwOneYut(), "테스트 모드에서는 항상 1");
        
        // When: 다시 일반 모드로
        YutGameRules.setTestMode(false);
        
        // Then: 랜덤 결과
        assertFalse(YutGameRules.isTestMode());
        int result = YutGameRules.throwOneYut();
        assertTrue((result == -1) || (result >= 1 && result <= 5), 
            "일반 모드에서는 -1 또는 1-5 범위");
    }

    @Test
    @DisplayName("회귀테스트 - 토큰 이름 규칙")
    void testTokenNamingConvention() {
        // Given
        Player player = new Player("김철수", 3);
        
        // When & Then: "플레이어이름-번호" 형식 (1부터 시작)
        assertEquals("김철수-1", player.getTokens().get(0).getName());
        assertEquals("김철수-2", player.getTokens().get(1).getName());
        assertEquals("김철수-3", player.getTokens().get(2).getName());
    }

    @Test
    @DisplayName("회귀테스트 - 턴 시스템 순환")
    void testTurnRotationSystem() {
        // Given: 3명 플레이어 게임
        GameState game = new GameState(4, 2.0f, 
            java.util.Arrays.asList("A", "B", "C"), 
            java.util.Arrays.asList(2, 2, 2));
        
        // When & Then: 순환하는 턴 시스템
        assertEquals("A", game.getCurrentPlayer().getName());
        
        game.nextPlayer();
        assertEquals("B", game.getCurrentPlayer().getName());
        
        game.nextPlayer();
        assertEquals("C", game.getCurrentPlayer().getName());
        
        game.nextPlayer(); // 다시 처음으로
        assertEquals("A", game.getCurrentPlayer().getName());
    }

    @Test
    @DisplayName("회귀테스트 - 승리 조건 초기값")
    void testInitialWinCondition() {
        // Given: 새로 생성된 플레이어
        Player player = new Player("테스트", 2);
        
        // Then: 초기에는 승리하지 않음
        assertFalse(player.hasFinished(), "초기에는 승리하지 않음");
        
        // 토큰별 초기 상태 확인
        assertEquals(2, player.getReadyTokens().size(), "초기에는 모든 토큰이 READY");
        assertEquals(0, player.getActiveTokens().size(), "초기에는 ACTIVE 토큰 없음");
        assertEquals(0, player.getFinishedTokens().size(), "초기에는 FINISHED 토큰 없음");
    }

    @Test
    @DisplayName("회귀테스트 - 윷 결과 유효성")
    void testYutResultValidity() {
        // Given: 일반 모드
        YutGameRules.setTestMode(false);
        
        // When & Then: 100번 던져서 모든 결과가 유효 범위 내인지 확인
        for (int i = 0; i < 100; i++) {
            int result = YutGameRules.throwOneYut();
            assertTrue((result == -1) || (result >= 1 && result <= 5), 
                "윷 결과는 -1(빽도) 또는 1-5(도,개,걸,윷,모) 범위여야 함, 실제값: " + result);
        }
    }

    @Test
    @DisplayName("회귀테스트 - 토큰 위치 관리자 기본 동작")
    void testTokenPositionManagerBasics() {
        // Given: 게임 설정
        GameState game = new GameState(4, 2.0f, 
            java.util.Arrays.asList("테스트플레이어"), 
            java.util.Arrays.asList(2));
        
        TokenPositionManager manager = game.getTokenPositionManager();
        Player player = game.getPlayers().get(0);
        Token token = player.getTokens().get(0);
        
        // When: 토큰을 시작 위치에 배치
        manager.placeTokenAtStart(token);
        
        // Then: 토큰 상태와 위치 확인
        assertEquals(TokenState.ACTIVE, token.getState());
        assertNotNull(manager.getTokenPosition(token), "토큰 위치가 기록되어야 함");
        
        // 보드 위의 토큰 확인
        assertTrue(manager.getAllTokensOnBoard().contains(token), 
            "보드 위의 토큰 목록에 포함되어야 함");
    }

    @Test
    @DisplayName("회귀테스트 - 게임 상태 일관성")
    void testGameStateConsistency() {
        // Given: 다양한 설정으로 게임 생성
        GameState game = new GameState(5, 2.0f, 
            java.util.Arrays.asList("P1", "P2", "P3"), 
            java.util.Arrays.asList(3, 4, 2));
        
        // Then: 일관성 검증
        assertEquals(3, game.getPlayers().size(), "플레이어 수 일치");
        assertEquals(3, game.getPlayers().get(0).getTokens().size(), "P1 토큰 수");
        assertEquals(4, game.getPlayers().get(1).getTokens().size(), "P2 토큰 수");
        assertEquals(2, game.getPlayers().get(2).getTokens().size(), "P3 토큰 수");
        
        assertFalse(game.isGameEnded(), "초기 게임 종료 상태");
        assertEquals("P1", game.getCurrentPlayer().getName(), "첫 번째 플레이어");
        
        assertNotNull(game.getBoard(), "보드 존재");
        assertNotNull(game.getTokenPositionManager(), "토큰 위치 관리자 존재");
    }

    @Test
    @DisplayName("회귀테스트 - 메모리 안정성")
    void testMemoryStability() {
        // Given & When: 여러 게임 인스턴스 생성 및 기본 검증
        for (int i = 0; i < 20; i++) {
            GameState game = new GameState(4, 2.0f, 
                java.util.Arrays.asList("임시플레이어" + i), 
                java.util.Arrays.asList(2));
            
            // 기본 검증
            assertNotNull(game.getBoard(), "보드 생성 " + i);
            assertNotNull(game.getPlayers(), "플레이어 생성 " + i);
            assertNotNull(game.getTokenPositionManager(), "위치관리자 생성 " + i);
            assertEquals(1, game.getPlayers().size(), "플레이어 수 " + i);
        }
        
        // Then: 예외 없이 정상 생성되어야 함
        assertTrue(true, "20개 게임 인스턴스 안정적 생성 완료");
    }
} 