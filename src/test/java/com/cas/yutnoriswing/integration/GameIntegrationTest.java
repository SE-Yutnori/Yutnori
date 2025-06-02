package com.cas.yutnoriswing.integration;

import com.cas.yutnoriswing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("게임 통합 테스트")
class GameIntegrationTest {

    private GameState gameState;
    private Player player1, player2;
    private TokenPositionManager tokenManager;

    @BeforeEach
    void setUp() {
        // 테스트 환경 설정
        System.setProperty("java.awt.headless", "true");
        YutGameRules.setTestMode(true);
        
        // 2명 플레이어, 각자 2개 토큰으로 간단한 게임 설정
        List<String> playerNames = Arrays.asList("플레이어1", "플레이어2");
        List<Integer> tokenCounts = Arrays.asList(2, 2);
        
        gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        List<Player> players = gameState.getPlayers();
        player1 = players.get(0);
        player2 = players.get(1);
        tokenManager = gameState.getTokenPositionManager();
    }

    @Test
    @DisplayName("완전한 게임 시나리오 - 토큰 시작 위치 배치")
    void testCompleteGameScenario_TokenPlacement() {
        // Given: 초기 상태 확인
        assertFalse(gameState.isGameEnded());
        assertEquals(player1, gameState.getCurrentPlayer());
        
        // When: 플레이어1의 첫 번째 토큰을 시작 위치에 배치
        Token token1 = player1.getTokens().get(0);
        
        // 토큰을 시작 위치에 배치 (내부적으로 setState 호출됨)
        tokenManager.placeTokenAtStart(token1);
        
        // Then: 토큰이 올바르게 배치되었는지 확인
        assertEquals(TokenState.ACTIVE, token1.getState());
        BoardNode tokenPosition = tokenManager.getTokenPosition(token1);
        assertNotNull(tokenPosition, "토큰이 보드에 배치되어야 함");
        
        // 게임 상태 확인
        assertFalse(gameState.isGameEnded());
        assertEquals(1, player1.getActiveTokens().size());
        assertEquals(1, player1.getReadyTokens().size());
    }

    @Test
    @DisplayName("토큰 위치 관리 시나리오")
    void testTokenPositionManagement() {
        // Given: 토큰을 시작 위치에 배치
        Token token1 = player1.getTokens().get(0);
        Token token2 = player2.getTokens().get(0);
        
        tokenManager.placeTokenAtStart(token1);
        tokenManager.placeTokenAtStart(token2);
        
        // When: 토큰 위치 확인
        BoardNode position1 = tokenManager.getTokenPosition(token1);
        BoardNode position2 = tokenManager.getTokenPosition(token2);
        
        // Then: 두 토큰이 시작 위치에 배치되어야 함
        assertNotNull(position1);
        assertNotNull(position2);
        assertEquals(TokenState.ACTIVE, token1.getState());
        assertEquals(TokenState.ACTIVE, token2.getState());
        
        // 보드 위의 모든 토큰 확인
        List<Token> allTokensOnBoard = tokenManager.getAllTokensOnBoard();
        assertTrue(allTokensOnBoard.contains(token1));
        assertTrue(allTokensOnBoard.contains(token2));
    }

    @Test
    @DisplayName("게임 종료 조건 테스트")
    void testGameEndCondition() {
        // Given: 플레이어1의 토큰들을 시작 위치에 배치
        for (Token token : player1.getTokens()) {
            tokenManager.placeTokenAtStart(token);
        }
        
        // When: 플레이어1의 승리 조건 확인 (모든 토큰이 FINISHED 상태여야 함)
        // 실제로는 게임 로직을 통해 토큰들이 FINISHED 상태가 되어야 하지만
        // 여기서는 단순히 현재 상태 확인
        boolean allFinished = player1.getTokens().stream()
                .allMatch(token -> token.getState() == TokenState.FINISHED);
        
        // Then: 아직 게임이 끝나지 않았어야 함 (토큰들이 ACTIVE 상태)
        assertFalse(allFinished, "토큰들이 아직 완주하지 않았어야 함");
        assertFalse(player1.hasFinished(), "플레이어1이 아직 완주하지 않았어야 함");
    }

    @Test
    @DisplayName("턴 전환 시스템 테스트")
    void testTurnSystem() {
        // Given: 초기 상태
        assertEquals(player1, gameState.getCurrentPlayer());
        
        // When: 다음 플레이어로 턴 전환
        gameState.nextPlayer();
        
        // Then
        assertEquals(player2, gameState.getCurrentPlayer());
        
        // When: 다시 턴 전환
        gameState.nextPlayer();
        
        // Then: 다시 플레이어1 차례
        assertEquals(player1, gameState.getCurrentPlayer());
    }

    @Test
    @DisplayName("보드와 토큰 위치 관리자 통합 테스트")
    void testBoardTokenManagerIntegration() {
        // Given
        Token token = player1.getTokens().get(0);
        
        // When: 토큰을 시작 위치에 배치
        tokenManager.placeTokenAtStart(token);
        
        // Then: 위치 관리자에서 토큰 위치 확인
        BoardNode tokenPosition = tokenManager.getTokenPosition(token);
        assertNotNull(tokenPosition);
        assertEquals(TokenState.ACTIVE, token.getState());
        
        // 보드의 해당 노드에서도 토큰 확인
        assertTrue(tokenPosition.getTokens().contains(token));
        
        // When: 토큰 위치 업데이트 테스트
        Board board = tokenManager.getBoard();
        List<BoardNode> allNodes = board.getNodes();
        if (allNodes.size() > 1) {
            BoardNode newNode = allNodes.stream()
                    .filter(node -> !node.equals(tokenPosition))
                    .findFirst()
                    .orElse(null);
            
            if (newNode != null) {
                // 수동으로 토큰 이동 시뮬레이션
                tokenPosition.leave(token);
                newNode.enter(token);
                tokenManager.updateTokenPosition(token, newNode);
                
                // Then: 새 위치에서 토큰 확인
                assertEquals(newNode, tokenManager.getTokenPosition(token));
                assertTrue(newNode.getTokens().contains(token));
                assertFalse(tokenPosition.getTokens().contains(token));
            }
        }
    }

    @Test
    @DisplayName("다양한 보드 크기에서의 게임 무결성")
    void testGameIntegrityDifferentBoardSizes() {
        // Given: 다양한 보드 크기로 게임 생성
        for (int sides = 4; sides <= 6; sides++) {
            GameState testGame = new GameState(sides, 2.0f, 
                Arrays.asList("테스터1", "테스터2"), 
                Arrays.asList(2, 2));
            
            // When & Then: 기본 게임 구조 확인
            assertNotNull(testGame.getBoard());
            assertEquals(2, testGame.getPlayers().size());
            assertFalse(testGame.isGameEnded());
            assertNotNull(testGame.getCurrentPlayer());
            assertNotNull(testGame.getTokenPositionManager());
            
            // 보드에 노드가 적절히 생성되었는지 확인
            assertTrue(testGame.getBoard().getNodes().size() > 0);
            
            // 플레이어들이 올바르게 설정되었는지 확인
            for (Player player : testGame.getPlayers()) {
                assertEquals(2, player.getTokens().size());
                for (Token token : player.getTokens()) {
                    assertEquals(TokenState.READY, token.getState());
                    assertEquals(player, token.getOwner());
                }
            }
        }
    }

    @Test
    @DisplayName("토큰 상태 확인 통합 테스트")
    void testTokenStateIntegration() {
        // Given: 플레이어의 토큰들
        List<Token> player1Tokens = player1.getTokens();
        List<Token> player2Tokens = player2.getTokens();
        
        // When: 초기 상태 확인
        List<Token> player1ReadyTokens = player1.getReadyTokens();
        List<Token> player1ActiveTokens = player1.getActiveTokens();
        List<Token> player1FinishedTokens = player1.getFinishedTokens();
        
        // Then: 초기에는 모든 토큰이 READY 상태
        assertEquals(2, player1ReadyTokens.size());
        assertEquals(0, player1ActiveTokens.size());
        assertEquals(0, player1FinishedTokens.size());
        
        // When: 일부 토큰을 시작 위치에 배치 (READY → ACTIVE)
        tokenManager.placeTokenAtStart(player1Tokens.get(0));
        
        // Then: 상태별 토큰 수 변화 확인
        assertEquals(1, player1.getReadyTokens().size());
        assertEquals(1, player1.getActiveTokens().size());
        assertEquals(0, player1.getFinishedTokens().size());
        
        // 전체 토큰 수는 변하지 않음
        assertEquals(2, player1.getTokens().size());
    }

    @Test
    @DisplayName("YutGameRules 통합 테스트")
    void testYutGameRulesIntegration() {
        // Given: 테스트 모드 설정 확인
        assertTrue(YutGameRules.isTestMode());
        
        // When: 윷 던지기 테스트
        int result = YutGameRules.throwOneYut();
        
        // Then: 테스트 모드에서는 항상 1이 나와야 함
        assertEquals(1, result);
        
        // When: 플레이어와 함께 누적 윷 던지기 테스트
        YutGameRules.YutThrowResult throwResult = YutGameRules.accumulateYut(player1);
        
        // Then: 결과가 적절히 생성되어야 함
        assertNotNull(throwResult);
        assertNotNull(throwResult.getResults());
        assertNotNull(throwResult.getResultMessages());
        assertFalse(throwResult.getResults().isEmpty());
        assertFalse(throwResult.getResultMessages().isEmpty());
    }
} 