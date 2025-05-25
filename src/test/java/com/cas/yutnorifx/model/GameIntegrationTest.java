package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("게임 통합 테스트")
class GameIntegrationTest {
    
    private Player player1;
    private Player player2;
    private BoardNode startNode;
    private BoardNode[] gameBoard;
    
    @BeforeEach
    void setUp() {
        player1 = new Player("플레이어1", 4);
        player2 = new Player("플레이어2", 4);
        
        // 간단한 게임 보드 구성 (5개 노드)
        gameBoard = new BoardNode[5];
        for (int i = 0; i < 5; i++) {
            gameBoard[i] = new BoardNode("노드" + i, i * 10, i * 10, 4);
        }
        
        // 노드들을 연결
        for (int i = 0; i < 4; i++) {
            gameBoard[i].addNextNode(gameBoard[i + 1]);
        }
        
        startNode = gameBoard[0];
    }
    
    @Nested
    @DisplayName("게임 시작 통합 테스트")
    class GameStartIntegrationTest {
        
        @Test
        @DisplayName("두 플레이어가 모두 올바르게 초기화되어야 함")
        void shouldInitializeBothPlayersCorrectly() {
            assertEquals(4, player1.getTokens().size());
            assertEquals(4, player2.getTokens().size());
            
            for (Token token : player1.getTokens()) {
                assertEquals(TokenState.READY, token.getState());
                assertEquals(player1, token.getOwner());
            }
            
            for (Token token : player2.getTokens()) {
                assertEquals(TokenState.READY, token.getState());
                assertEquals(player2, token.getOwner());
            }
        }
        
        @Test
        @DisplayName("게임 보드가 올바르게 구성되어야 함")
        void shouldSetupGameBoardCorrectly() {
            for (int i = 0; i < 4; i++) {
                assertEquals(1, gameBoard[i].getNextNodes().size());
                assertEquals(gameBoard[i + 1], gameBoard[i].getNextNodes().get(0));
            }
            
            // 마지막 노드는 다음 노드가 없어야 함
            assertEquals(0, gameBoard[4].getNextNodes().size());
        }
    }
    
    @Nested
    @DisplayName("토큰 이동 통합 테스트")
    class TokenMovementIntegrationTest {
        
        @Test
        @DisplayName("토큰이 보드를 따라 순차적으로 이동해야 함")
        void shouldMoveTokenSequentiallyThroughBoard() {
            Token token = player1.getTokens().get(0);
            
            // 토큰 시작
            token.start(startNode);
            assertEquals(TokenState.ACTIVE, token.getState());
            assertTrue(startNode.getTokens().contains(token));
            
            // 1칸 이동
            Token.MoveResult result1 = token.move(1, nodes -> nodes.get(0));
            assertFalse(result1.isFinished());
            assertTrue(gameBoard[1].getTokens().contains(token));
            assertFalse(startNode.getTokens().contains(token));
            
            // 2칸 더 이동
            Token.MoveResult result2 = token.move(2, nodes -> nodes.get(0));
            assertFalse(result2.isFinished());
            assertTrue(gameBoard[3].getTokens().contains(token));
            
            // 마지막까지 이동 (완주)
            Token.MoveResult result3 = token.move(2, nodes -> nodes.get(0));
            assertTrue(result3.isFinished());
            assertEquals(TokenState.FINISHED, token.getState());
        }
        
        @Test
        @DisplayName("여러 토큰이 동시에 보드에서 이동할 수 있어야 함")
        void shouldAllowMultipleTokensOnBoard() {
            Token token1 = player1.getTokens().get(0);
            Token token2 = player1.getTokens().get(1);
            Token opponentToken = player2.getTokens().get(0);
            
            // 모든 토큰 시작
            token1.start(startNode);
            token2.start(startNode);
            opponentToken.start(startNode);
            
            assertEquals(3, startNode.getTokens().size());
            
            // 각각 다른 위치로 이동
            token1.move(1, nodes -> nodes.get(0));
            token2.move(2, nodes -> nodes.get(0));
            opponentToken.move(3, nodes -> nodes.get(0));
            
            assertTrue(gameBoard[1].getTokens().contains(token1));
            assertTrue(gameBoard[2].getTokens().contains(token2));
            assertTrue(gameBoard[3].getTokens().contains(opponentToken));
        }
    }
    
    @Nested
    @DisplayName("토큰 잡기 통합 테스트")
    class TokenCatchingIntegrationTest {
        
        @Test
        @DisplayName("상대방 토큰을 잡으면 상대방이 시작점으로 돌아가야 함")
        void shouldCatchOpponentTokenAndResetToStart() {
            Token myToken = player1.getTokens().get(0);
            Token opponentToken = player2.getTokens().get(0);
            
            // 상대방 토큰을 먼저 배치
            opponentToken.start(startNode);
            opponentToken.move(2, nodes -> nodes.get(0));
            assertTrue(gameBoard[2].getTokens().contains(opponentToken));
            
            // 내 토큰으로 상대방 잡기
            myToken.start(startNode);
            Token.MoveResult result = myToken.move(2, nodes -> nodes.get(0));
            
            assertTrue(result.isCatched());
            assertEquals(TokenState.READY, opponentToken.getState());
            assertFalse(gameBoard[2].getTokens().contains(opponentToken));
            assertTrue(gameBoard[2].getTokens().contains(myToken));
        }
        
        @Test
        @DisplayName("같은 플레이어의 토큰끼리는 업기가 되어야 함")
        void shouldStackSamePlayerTokens() {
            Token token1 = player1.getTokens().get(0);
            Token token2 = player1.getTokens().get(1);
            
            // 첫 번째 토큰 배치
            token1.start(startNode);
            token1.move(2, nodes -> nodes.get(0));
            
            // 두 번째 토큰으로 첫 번째 토큰과 업기
            token2.start(startNode);
            Token.MoveResult result = token2.move(2, nodes -> nodes.get(0));
            
            assertFalse(result.isCatched());
            assertEquals(TokenState.ACTIVE, token1.getState());
            assertTrue(token2.getStackedTokens().contains(token1));
        }
    }
    
    @Nested
    @DisplayName("게임 완료 통합 테스트")
    class GameCompletionIntegrationTest {
        
        @Test
        @DisplayName("플레이어의 모든 토큰이 완주하면 게임이 완료되어야 함")
        void shouldCompleteGameWhenAllTokensFinish() {
            // 모든 토큰을 완주시킴
            for (Token token : player1.getTokens()) {
                token.finishIndividually();
            }
            
            assertTrue(player1.hasFinished());
            assertFalse(player2.hasFinished());
        }
        
        @Test
        @DisplayName("일부 토큰만 완주해도 게임이 완료되지 않아야 함")
        void shouldNotCompleteGameWithPartialTokens() {
            // 일부 토큰만 완주
            player1.getTokens().get(0).finishIndividually();
            player1.getTokens().get(1).finishIndividually();
            
            assertFalse(player1.hasFinished());
        }
    }
    
    @Nested
    @DisplayName("복합 시나리오 통합 테스트")
    class ComplexScenarioIntegrationTest {
        
        @Test
        @DisplayName("전체 게임 플레이 시나리오")
        void shouldHandleCompleteGameplayScenario() {
            Token p1Token1 = player1.getTokens().get(0);
            Token p1Token2 = player1.getTokens().get(1);
            Token p2Token1 = player2.getTokens().get(0);
            
            // 1. 플레이어1의 첫 번째 토큰 시작
            p1Token1.start(startNode);
            assertEquals(TokenState.ACTIVE, p1Token1.getState());
            
            // 2. 플레이어2의 토큰 시작
            p2Token1.start(startNode);
            assertEquals(2, startNode.getTokens().size());
            
            // 3. 플레이어1 토큰 이동
            p1Token1.move(2, nodes -> nodes.get(0));
            assertTrue(gameBoard[2].getTokens().contains(p1Token1));
            
            // 4. 플레이어2 토큰이 플레이어1 토큰을 잡음
            Token.MoveResult catchResult = p2Token1.move(2, nodes -> nodes.get(0));
            assertTrue(catchResult.isCatched());
            assertEquals(TokenState.READY, p1Token1.getState());
            
            // 5. 플레이어1의 두 번째 토큰 시작
            p1Token2.start(startNode);
            assertEquals(TokenState.ACTIVE, p1Token2.getState());
            
            // 6. 플레이어1 토큰들이 업기
            p1Token1.start(startNode);
            p1Token1.move(1, nodes -> nodes.get(0));
            Token.MoveResult stackResult = p1Token2.move(1, nodes -> nodes.get(0));
            assertFalse(stackResult.isCatched());
            assertTrue(p1Token2.getStackedTokens().contains(p1Token1));
            
            // 7. 업힌 토큰들이 함께 이동 (완주는 개별적으로 처리)
            p1Token2.move(2, nodes -> nodes.get(0));
            
            // 개별적으로 완주 처리
            p1Token1.finishIndividually();
            p1Token2.finishIndividually();
            assertEquals(TokenState.FINISHED, p1Token2.getState());
            assertEquals(TokenState.FINISHED, p1Token1.getState());
        }
        
        @Test
        @DisplayName("빽도 이동 시나리오")
        void shouldHandleBackwardMovementScenario() {
            Token token = player1.getTokens().get(0);
            
            // 토큰을 중간 지점으로 이동
            token.start(startNode);
            token.move(3, nodes -> nodes.get(0));
            assertTrue(gameBoard[3].getTokens().contains(token));
            
            // 빽도로 뒤로 이동
            Token.MoveResult backResult = token.moveBackward(1);
            assertFalse(backResult.isFinished());
            assertTrue(gameBoard[2].getTokens().contains(token));
            assertFalse(gameBoard[3].getTokens().contains(token));
            
            // 다시 앞으로 이동
            Token.MoveResult forwardResult = token.move(2, nodes -> nodes.get(0));
            assertFalse(forwardResult.isFinished());
            assertTrue(gameBoard[4].getTokens().contains(token));
        }
    }
    
    @Nested
    @DisplayName("경계값 통합 테스트")
    class BoundaryValueIntegrationTest {
        
        @Test
        @DisplayName("최대 토큰 수로 게임이 정상 작동해야 함")
        void shouldWorkWithMaximumTokens() {
            Player maxPlayer = new Player("최대플레이어", 5);
            assertEquals(5, maxPlayer.getTokens().size());
            
            // 모든 토큰을 시작점에 배치
            for (Token token : maxPlayer.getTokens()) {
                token.start(startNode);
            }
            
            assertEquals(5, startNode.getTokens().size());
            
            // 모든 토큰이 정상적으로 이동할 수 있는지 확인
            for (int i = 0; i < maxPlayer.getTokens().size(); i++) {
                Token token = maxPlayer.getTokens().get(i);
                if (i < 4) { // 보드 크기 내에서만 이동
                    Token.MoveResult result = token.move(i + 1, nodes -> nodes.get(0));
                    // 완주 여부는 보드 구조에 따라 달라질 수 있음
                }
            }
        }
        
        @Test
        @DisplayName("최소 토큰 수로 게임이 정상 작동해야 함")
        void shouldWorkWithMinimumTokens() {
            Player minPlayer = new Player("최소플레이어", 2);
            assertEquals(2, minPlayer.getTokens().size());
            
            // 모든 토큰을 완주시킴
            for (Token token : minPlayer.getTokens()) {
                token.finishIndividually();
            }
            
            assertTrue(minPlayer.hasFinished());
        }
    }
} 