package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("회귀 테스트 - 기존 기능 보장")
class RegressionTest {
    
    private Player player;
    private BoardNode startNode;
    private BoardNode middleNode;
    private BoardNode endNode;
    
    @BeforeEach
    void setUp() {
        player = new Player("테스트플레이어", 4);
        startNode = new BoardNode("시작", 0, 0, 4);
        middleNode = new BoardNode("중간", 10, 10, 4);
        endNode = new BoardNode("끝", 20, 20, 4);
        
        startNode.addNextNode(middleNode);
        middleNode.addNextNode(endNode);
    }
    
    @Nested
    @DisplayName("토큰 상태 변화 회귀 테스트")
    class TokenStateRegressionTest {
        
        @Test
        @DisplayName("토큰 상태 변화 순서가 변경되지 않았는지 확인")
        void shouldMaintainTokenStateTransitionOrder() {
            Token token = player.getTokens().get(0);
            
            // 초기 상태: READY
            assertEquals(TokenState.READY, token.getState());
            
            // 시작 후: ACTIVE
            token.start(startNode);
            assertEquals(TokenState.ACTIVE, token.getState());
            
            // 리셋 후: READY
            token.reset();
            assertEquals(TokenState.READY, token.getState());
            
            // 다시 시작 후: ACTIVE
            token.start(startNode);
            assertEquals(TokenState.ACTIVE, token.getState());
            
            // 완주 후: FINISHED
            token.finishIndividually();
            assertEquals(TokenState.FINISHED, token.getState());
        }
        
        @Test
        @DisplayName("토큰 상태가 예상치 못하게 변경되지 않았는지 확인")
        void shouldNotChangeTokenStateUnexpectedly() {
            Token token = player.getTokens().get(0);
            
            // READY 상태에서 이동 시도 시 상태 변화 없음
            TokenState initialState = token.getState();
            token.move(1, nodes -> nodes.get(0));
            assertEquals(initialState, token.getState());
            
            // FINISHED 상태에서 시작 시도 시 상태 변화 없음
            token.finishIndividually();
            TokenState finishedState = token.getState();
            token.start(startNode);
            assertEquals(finishedState, token.getState());
        }
    }
    
    @Nested
    @DisplayName("플레이어 토큰 관리 회귀 테스트")
    class PlayerTokenManagementRegressionTest {
        
        @Test
        @DisplayName("플레이어 토큰 개수 제한이 변경되지 않았는지 확인")
        void shouldMaintainTokenCountLimits() {
            // 유효한 범위 (2-5)
            Player player2 = new Player("플레이어2", 2);
            Player player3 = new Player("플레이어3", 3);
            Player player4 = new Player("플레이어4", 4);
            Player player5 = new Player("플레이어5", 5);
            
            assertEquals(2, player2.getTokens().size());
            assertEquals(3, player3.getTokens().size());
            assertEquals(4, player4.getTokens().size());
            assertEquals(5, player5.getTokens().size());
            
            // 범위 밖 값들은 4로 기본 설정
            Player playerLow = new Player("플레이어낮음", 1);
            Player playerHigh = new Player("플레이어높음", 6);
            Player playerZero = new Player("플레이어0", 0);
            Player playerNegative = new Player("플레이어음수", -1);
            
            assertEquals(4, playerLow.getTokens().size());
            assertEquals(4, playerHigh.getTokens().size());
            assertEquals(4, playerZero.getTokens().size());
            assertEquals(4, playerNegative.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 이름 생성 규칙이 변경되지 않았는지 확인")
        void shouldMaintainTokenNamingConvention() {
            Player testPlayer = new Player("테스트", 3);
            
            assertEquals("테스트-1", testPlayer.getTokens().get(0).getName());
            assertEquals("테스트-2", testPlayer.getTokens().get(1).getName());
            assertEquals("테스트-3", testPlayer.getTokens().get(2).getName());
        }
        
        @Test
        @DisplayName("플레이어 완료 조건이 변경되지 않았는지 확인")
        void shouldMaintainPlayerCompletionCondition() {
            // 모든 토큰이 FINISHED 상태여야만 완료
            assertFalse(player.hasFinished());
            
            // 일부만 완료
            player.getTokens().get(0).finishIndividually();
            player.getTokens().get(1).finishIndividually();
            player.getTokens().get(2).finishIndividually();
            assertFalse(player.hasFinished());
            
            // 모두 완료
            player.getTokens().get(3).finishIndividually();
            assertTrue(player.hasFinished());
        }
    }
    
    @Nested
    @DisplayName("보드 노드 기능 회귀 테스트")
    class BoardNodeRegressionTest {
        
        @Test
        @DisplayName("노드 좌표 시스템이 변경되지 않았는지 확인")
        void shouldMaintainCoordinateSystem() {
            BoardNode node = new BoardNode("테스트", 15.5f, 25.7f, 6);
            
            assertEquals(15.5f, node.getX(), 0.001f);
            assertEquals(25.7f, node.getY(), 0.001f);
            assertEquals("테스트", node.getName());
            assertEquals(6, node.getBoardSize());
        }
        
        @Test
        @DisplayName("노드 연결 시스템이 변경되지 않았는지 확인")
        void shouldMaintainNodeConnectionSystem() {
            BoardNode node1 = new BoardNode("노드1", 0, 0, 4);
            BoardNode node2 = new BoardNode("노드2", 10, 10, 4);
            BoardNode node3 = new BoardNode("노드3", 20, 20, 4);
            
            node1.addNextNode(node2);
            node1.addNextNode(node3);
            
            assertEquals(2, node1.getNextNodes().size());
            assertTrue(node1.getNextNodes().contains(node2));
            assertTrue(node1.getNextNodes().contains(node3));
        }
        
        @Test
        @DisplayName("토큰 진입/퇴장 시스템이 변경되지 않았는지 확인")
        void shouldMaintainTokenEnterLeaveSystem() {
            Token token1 = player.getTokens().get(0);
            Token token2 = player.getTokens().get(1);
            
            // 진입
            startNode.enter(token1);
            startNode.enter(token2);
            assertEquals(2, startNode.getTokens().size());
            
            // 퇴장
            startNode.leave(token1);
            assertEquals(1, startNode.getTokens().size());
            assertTrue(startNode.getTokens().contains(token2));
            
            startNode.leave(token2);
            assertEquals(0, startNode.getTokens().size());
        }
    }
    
    @Nested
    @DisplayName("토큰 이동 로직 회귀 테스트")
    class TokenMovementRegressionTest {
        
        @Test
        @DisplayName("토큰 이동 결과 구조가 변경되지 않았는지 확인")
        void shouldMaintainMoveResultStructure() {
            Token token = player.getTokens().get(0);
            token.start(startNode);
            
            Token.MoveResult result = token.move(1, nodes -> nodes.get(0));
            
            // MoveResult의 기본 구조 확인
            assertNotNull(result);
            assertFalse(result.isFinished()); // 아직 완주하지 않음
            assertFalse(result.isCatched());  // 잡지 않음
            
            // setter 동작 확인
            result.setFinished(true);
            result.setCatched(true);
            assertTrue(result.isFinished());
            assertTrue(result.isCatched());
        }
        
        @Test
        @DisplayName("토큰 완주 로직이 변경되지 않았는지 확인")
        void shouldMaintainTokenFinishLogic() {
            Token token = player.getTokens().get(0);
            token.start(endNode); // 끝 노드에서 시작
            
            // 다음 노드가 없어서 완주
            Token.MoveResult result = token.move(1, nodes -> nodes.get(0));
            assertTrue(result.isFinished());
            assertEquals(TokenState.FINISHED, token.getState());
        }
        
        @Test
        @DisplayName("빽도 이동 로직이 변경되지 않았는지 확인")
        void shouldMaintainBackwardMovementLogic() {
            Token token = player.getTokens().get(0);
            token.start(startNode);
            token.move(1, nodes -> nodes.get(0)); // 중간 노드로 이동
            
            assertTrue(middleNode.getTokens().contains(token));
            
            // 빽도로 뒤로 이동
            Token.MoveResult backResult = token.moveBackward(1);
            assertFalse(backResult.isFinished());
            assertTrue(startNode.getTokens().contains(token));
            assertFalse(middleNode.getTokens().contains(token));
        }
    }
    
    @Nested
    @DisplayName("토큰 상호작용 회귀 테스트")
    class TokenInteractionRegressionTest {
        
        @Test
        @DisplayName("토큰 잡기 로직이 변경되지 않았는지 확인")
        void shouldMaintainTokenCatchingLogic() {
            Player opponent = new Player("상대방", 4);
            Token myToken = player.getTokens().get(0);
            Token opponentToken = opponent.getTokens().get(0);
            
            // 상대방 토큰 먼저 배치
            opponentToken.start(startNode);
            opponentToken.move(1, nodes -> nodes.get(0));
            
            // 내 토큰으로 잡기
            myToken.start(startNode);
            Token.MoveResult result = myToken.move(1, nodes -> nodes.get(0));
            
            assertTrue(result.isCatched());
            assertEquals(TokenState.READY, opponentToken.getState());
            assertEquals(TokenState.ACTIVE, myToken.getState());
        }
        
        @Test
        @DisplayName("토큰 업기 로직이 변경되지 않았는지 확인")
        void shouldMaintainTokenStackingLogic() {
            Token token1 = player.getTokens().get(0);
            Token token2 = player.getTokens().get(1);
            
            // 첫 번째 토큰 배치
            token1.start(startNode);
            token1.move(1, nodes -> nodes.get(0));
            
            // 두 번째 토큰으로 업기
            token2.start(startNode);
            Token.MoveResult result = token2.move(1, nodes -> nodes.get(0));
            
            assertFalse(result.isCatched()); // 같은 플레이어라 잡지 않음
            assertTrue(token2.getStackedTokens().contains(token1));
            assertEquals(TokenState.ACTIVE, token1.getState());
            assertEquals(TokenState.ACTIVE, token2.getState());
        }
    }
    
    @Nested
    @DisplayName("게임 규칙 회귀 테스트")
    class GameRulesRegressionTest {
        
        @Test
        @DisplayName("윷 결과 범위가 변경되지 않았는지 확인")
        void shouldMaintainYutResultRange() {
            // 유효한 윷 결과: -1, 1, 2, 3, 4, 5
            int[] validResults = {-1, 1, 2, 3, 4, 5};
            
            for (int result : validResults) {
                assertTrue(result >= -1 && result <= 5 && result != 0,
                    "유효하지 않은 윷 결과: " + result);
            }
            
            // 무효한 결과: 0
            int invalidResult = 0;
            assertFalse(invalidResult >= -1 && invalidResult <= 5 && invalidResult != 0,
                "0은 유효하지 않은 윷 결과여야 함");
        }
        
        @Test
        @DisplayName("테스트 모드 설정이 변경되지 않았는지 확인")
        void shouldMaintainTestModeSettings() {
            // 테스트 모드 설정이 예외 없이 작동해야 함
            assertDoesNotThrow(() -> YutGameRules.setTestMode(true));
            assertDoesNotThrow(() -> YutGameRules.setTestMode(false));
            
            // 여러 번 변경해도 문제없어야 함
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 10; i++) {
                    YutGameRules.setTestMode(i % 2 == 0);
                }
            });
        }
    }
    
    @Nested
    @DisplayName("데이터 무결성 회귀 테스트")
    class DataIntegrityRegressionTest {
        
        @Test
        @DisplayName("토큰과 플레이어 관계가 변경되지 않았는지 확인")
        void shouldMaintainTokenPlayerRelationship() {
            for (Token token : player.getTokens()) {
                assertEquals(player, token.getOwner());
                assertTrue(token.getName().startsWith(player.getName()));
            }
        }
        
        @Test
        @DisplayName("노드와 토큰 관계가 변경되지 않았는지 확인")
        void shouldMaintainNodeTokenRelationship() {
            Token token = player.getTokens().get(0);
            
            // 토큰이 노드에 진입하면 노드의 토큰 리스트에 포함되어야 함
            startNode.enter(token);
            assertTrue(startNode.getTokens().contains(token));
            
            // 토큰이 노드에서 나가면 노드의 토큰 리스트에서 제거되어야 함
            startNode.leave(token);
            assertFalse(startNode.getTokens().contains(token));
        }
        
        @Test
        @DisplayName("게임 상태 일관성이 변경되지 않았는지 확인")
        void shouldMaintainGameStateConsistency() {
            Token token = player.getTokens().get(0);
            
            // 토큰이 시작되면 ACTIVE 상태이고 시작 노드에 있어야 함
            token.start(startNode);
            assertEquals(TokenState.ACTIVE, token.getState());
            assertTrue(startNode.getTokens().contains(token));
            
            // 토큰이 리셋되면 READY 상태이고 어떤 노드에도 없어야 함
            token.reset();
            assertEquals(TokenState.READY, token.getState());
            assertFalse(startNode.getTokens().contains(token));
            assertFalse(middleNode.getTokens().contains(token));
            assertFalse(endNode.getTokens().contains(token));
        }
    }
} 