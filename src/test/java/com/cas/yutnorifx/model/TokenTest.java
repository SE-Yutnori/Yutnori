package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token 클래스 테스트")
class TokenTest {
    
    private Player player1;
    private Player player2;
    private Token token1;
    private Token token2;
    private BoardNode startNode;
    private BoardNode nextNode;
    private BoardNode endNode;
    
    @BeforeEach
    void setUp() {
        player1 = new Player("플레이어1", 4);
        player2 = new Player("플레이어2", 4);
        token1 = new Token("토큰1", player1);
        token2 = new Token("토큰2", player2);
        
        // 테스트용 보드 노드 생성
        startNode = new BoardNode("시작", 0, 0, 4);
        nextNode = new BoardNode("다음", 1, 1, 4);
        endNode = new BoardNode("끝", 2, 2, 4);
        
        // 노드 연결
        startNode.addNextNode(nextNode);
        nextNode.addNextNode(endNode);
    }
    
    @Nested
    @DisplayName("토큰 생성 및 기본 상태 테스트")
    class TokenCreationTest {
        
        @Test
        @DisplayName("토큰이 올바르게 생성되어야 함")
        void shouldCreateTokenCorrectly() {
            assertEquals("토큰1", token1.getName());
            assertEquals(player1, token1.getOwner());
            assertEquals(TokenState.READY, token1.getState());
            assertTrue(token1.getStackedTokens().isEmpty());
        }
        
        @Test
        @DisplayName("초기 상태에서 토큰은 READY 상태여야 함")
        void shouldBeReadyInitially() {
            assertEquals(TokenState.READY, token1.getState());
        }
    }
    
    @Nested
    @DisplayName("토큰 시작 테스트")
    class TokenStartTest {
        
        @Test
        @DisplayName("READY 상태의 토큰이 시작 노드에서 출발해야 함")
        void shouldStartFromStartNode() {
            token1.start(startNode);
            
            assertEquals(TokenState.ACTIVE, token1.getState());
            assertTrue(startNode.getTokens().contains(token1));
        }
        
        @Test
        @DisplayName("ACTIVE 상태의 토큰은 다시 시작할 수 없어야 함")
        void shouldNotStartWhenAlreadyActive() {
            token1.start(startNode);
            int initialTokenCount = startNode.getTokens().size();
            
            token1.start(startNode); // 다시 시작 시도
            
            assertEquals(initialTokenCount, startNode.getTokens().size());
        }
    }
    
    @Nested
    @DisplayName("토큰 리셋 테스트")
    class TokenResetTest {
        
        @Test
        @DisplayName("토큰이 리셋되면 READY 상태로 돌아가야 함")
        void shouldResetToReadyState() {
            token1.start(startNode);
            token1.reset();
            
            assertEquals(TokenState.READY, token1.getState());
            assertFalse(startNode.getTokens().contains(token1));
            assertTrue(token1.getStackedTokens().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("토큰 이동 테스트")
    class TokenMoveTest {
        
        @Test
        @DisplayName("토큰이 정상적으로 이동해야 함")
        void shouldMoveCorrectly() {
            token1.start(startNode);
            
            Token.MoveResult result = token1.move(1, nodes -> nodes.get(0));
            
            assertFalse(result.isFinished());
            assertFalse(result.isCatched());
            assertTrue(nextNode.getTokens().contains(token1));
            assertFalse(startNode.getTokens().contains(token1));
        }
        
        @Test
        @DisplayName("READY 상태의 토큰은 이동할 수 없어야 함")
        void shouldNotMoveWhenReady() {
            Token.MoveResult result = token1.move(1, nodes -> nodes.get(0));
            
            assertFalse(result.isFinished());
            assertFalse(result.isCatched());
        }
        
        @Test
        @DisplayName("다음 노드가 없으면 완주 처리되어야 함")
        void shouldFinishWhenNoNextNodes() {
            // 끝 노드에는 다음 노드가 없음
            token1.start(endNode);
            
            Token.MoveResult result = token1.move(1, nodes -> nodes.get(0));
            
            assertTrue(result.isFinished());
            assertEquals(TokenState.FINISHED, token1.getState());
        }
    }
    
    @Nested
    @DisplayName("토큰 잡기 테스트")
    class TokenCatchTest {
        
        @Test
        @DisplayName("상대방 토큰을 잡으면 상대방이 리셋되어야 함")
        void shouldCatchOpponentToken() {
            // token2를 먼저 다음 노드에 배치
            token2.start(startNode);
            token2.move(1, nodes -> nodes.get(0));
            
            // token1이 이동하여 token2를 잡음
            token1.start(startNode);
            Token.MoveResult result = token1.move(1, nodes -> nodes.get(0));
            
            assertTrue(result.isCatched());
            assertEquals(TokenState.READY, token2.getState());
        }
        
        @Test
        @DisplayName("같은 플레이어의 토큰은 잡지 않아야 함")
        void shouldNotCatchSamePlayerToken() {
            Token samePlayerToken = new Token("같은플레이어토큰", player1);
            
            token1.start(startNode);
            samePlayerToken.start(startNode);
            
            Token.MoveResult result = token1.move(1, nodes -> nodes.get(0));
            
            assertFalse(result.isCatched());
            assertEquals(TokenState.ACTIVE, samePlayerToken.getState());
        }
    }
    
    @Nested
    @DisplayName("토큰 업기 테스트")
    class TokenStackTest {
        
        @Test
        @DisplayName("같은 플레이어의 토큰과 업기가 되어야 함")
        void shouldStackWithSamePlayerToken() {
            Token samePlayerToken = new Token("같은플레이어토큰", player1);
            
            // 첫 번째 토큰을 먼저 다음 노드에 배치
            samePlayerToken.start(startNode);
            samePlayerToken.move(1, nodes -> nodes.get(0));
            
            // 두 번째 토큰이 같은 위치로 이동하여 업기
            token1.start(startNode);
            token1.move(1, nodes -> nodes.get(0));
            
            assertTrue(token1.getStackedTokens().contains(samePlayerToken));
        }
    }
    
    @Nested
    @DisplayName("토큰 뒤로 이동 테스트")
    class TokenBackwardMoveTest {
        
        @Test
        @DisplayName("토큰이 뒤로 이동해야 함")
        void shouldMoveBackward() {
            token1.start(startNode);
            token1.move(1, nodes -> nodes.get(0)); // 다음 노드로 이동
            
            Token.MoveResult result = token1.moveBackward(1);
            
            assertFalse(result.isFinished());
            assertTrue(startNode.getTokens().contains(token1));
            assertFalse(nextNode.getTokens().contains(token1));
        }
        
        @Test
        @DisplayName("READY 상태의 토큰은 뒤로 이동할 수 없어야 함")
        void shouldNotMoveBackwardWhenReady() {
            Token.MoveResult result = token1.moveBackward(1);
            
            assertFalse(result.isFinished());
            assertFalse(result.isCatched());
        }
    }
    
    @Nested
    @DisplayName("MoveResult 테스트")
    class MoveResultTest {
        
        @Test
        @DisplayName("MoveResult가 올바르게 생성되어야 함")
        void shouldCreateMoveResultCorrectly() {
            Token.MoveResult result = new Token.MoveResult();
            
            assertFalse(result.isFinished());
            assertFalse(result.isCatched());
        }
        
        @Test
        @DisplayName("MoveResult 상태를 올바르게 설정할 수 있어야 함")
        void shouldSetMoveResultStatesCorrectly() {
            Token.MoveResult result = new Token.MoveResult();
            
            result.setFinished(true);
            result.setCatched(true);
            
            assertTrue(result.isFinished());
            assertTrue(result.isCatched());
        }
    }
    
    @Nested
    @DisplayName("토큰 완주 테스트")
    class TokenFinishTest {
        
        @Test
        @DisplayName("개별 완주가 올바르게 처리되어야 함")
        void shouldFinishIndividually() {
            token1.start(startNode);
            token1.finishIndividually();
            
            assertEquals(TokenState.FINISHED, token1.getState());
            assertFalse(startNode.getTokens().contains(token1));
        }
    }
    
    @Nested
    @DisplayName("최상위 토큰 테스트")
    class TopMostTokenTest {
        
        @Test
        @DisplayName("업히지 않은 토큰은 자기 자신이 최상위 토큰이어야 함")
        void shouldReturnSelfAsTopMostWhenNotStacked() {
            assertEquals(token1, token1.getTopMostToken());
        }
    }
} 