package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoardNode 클래스 테스트")
class BoardNodeTest {
    
    private BoardNode node;
    private Player player;
    private Token token;
    
    @BeforeEach
    void setUp() {
        node = new BoardNode("테스트노드", 10.5f, 20.5f, 4);
        player = new Player("테스트플레이어", 4);
        token = new Token("테스트토큰", player);
    }
    
    @Nested
    @DisplayName("노드 생성 테스트")
    class NodeCreationTest {
        
        @Test
        @DisplayName("노드가 올바르게 생성되어야 함")
        void shouldCreateNodeCorrectly() {
            assertEquals("테스트노드", node.getName());
            assertEquals(10.5f, node.getX());
            assertEquals(20.5f, node.getY());
            assertEquals(4, node.getBoardSize());
            assertTrue(node.getNextNodes().isEmpty());
            assertTrue(node.getTokens().isEmpty());
        }
        
        @Test
        @DisplayName("정수 좌표로도 노드가 생성되어야 함")
        void shouldCreateNodeWithIntegerCoordinates() {
            BoardNode intNode = new BoardNode("정수노드", 5, 10, 6);
            assertEquals(5.0f, intNode.getX());
            assertEquals(10.0f, intNode.getY());
        }
        
        @Test
        @DisplayName("음수 좌표로도 노드가 생성되어야 함")
        void shouldCreateNodeWithNegativeCoordinates() {
            BoardNode negativeNode = new BoardNode("음수노드", -5.5f, -10.5f, 8);
            assertEquals(-5.5f, negativeNode.getX());
            assertEquals(-10.5f, negativeNode.getY());
        }
    }
    
    @Nested
    @DisplayName("다음 노드 관리 테스트")
    class NextNodeManagementTest {
        
        @Test
        @DisplayName("다음 노드를 추가할 수 있어야 함")
        void shouldAddNextNode() {
            BoardNode nextNode = new BoardNode("다음노드", 15, 25, 4);
            node.addNextNode(nextNode);
            
            assertEquals(1, node.getNextNodes().size());
            assertTrue(node.getNextNodes().contains(nextNode));
        }
        
        @Test
        @DisplayName("여러 다음 노드를 추가할 수 있어야 함")
        void shouldAddMultipleNextNodes() {
            BoardNode nextNode1 = new BoardNode("다음노드1", 15, 25, 4);
            BoardNode nextNode2 = new BoardNode("다음노드2", 20, 30, 4);
            BoardNode nextNode3 = new BoardNode("다음노드3", 25, 35, 4);
            
            node.addNextNode(nextNode1);
            node.addNextNode(nextNode2);
            node.addNextNode(nextNode3);
            
            assertEquals(3, node.getNextNodes().size());
            assertTrue(node.getNextNodes().contains(nextNode1));
            assertTrue(node.getNextNodes().contains(nextNode2));
            assertTrue(node.getNextNodes().contains(nextNode3));
        }
        
        @Test
        @DisplayName("같은 노드를 여러 번 추가할 수 있어야 함")
        void shouldAllowDuplicateNextNodes() {
            BoardNode nextNode = new BoardNode("다음노드", 15, 25, 4);
            node.addNextNode(nextNode);
            node.addNextNode(nextNode);
            
            assertEquals(2, node.getNextNodes().size());
        }
    }
    
    @Nested
    @DisplayName("토큰 관리 테스트")
    class TokenManagementTest {
        
        @Test
        @DisplayName("토큰이 노드에 진입할 수 있어야 함")
        void shouldAllowTokenToEnter() {
            node.enter(token);
            
            assertEquals(1, node.getTokens().size());
            assertTrue(node.getTokens().contains(token));
        }
        
        @Test
        @DisplayName("여러 토큰이 노드에 진입할 수 있어야 함")
        void shouldAllowMultipleTokensToEnter() {
            Token token2 = new Token("토큰2", player);
            Token token3 = new Token("토큰3", player);
            
            node.enter(token);
            node.enter(token2);
            node.enter(token3);
            
            assertEquals(3, node.getTokens().size());
            assertTrue(node.getTokens().contains(token));
            assertTrue(node.getTokens().contains(token2));
            assertTrue(node.getTokens().contains(token3));
        }
        
        @Test
        @DisplayName("토큰이 노드에서 떠날 수 있어야 함")
        void shouldAllowTokenToLeave() {
            node.enter(token);
            node.leave(token);
            
            assertEquals(0, node.getTokens().size());
            assertFalse(node.getTokens().contains(token));
        }
        
        @Test
        @DisplayName("존재하지 않는 토큰을 제거해도 오류가 발생하지 않아야 함")
        void shouldHandleRemovingNonExistentToken() {
            assertDoesNotThrow(() -> node.leave(token));
            assertEquals(0, node.getTokens().size());
        }
        
        @Test
        @DisplayName("일부 토큰만 떠날 수 있어야 함")
        void shouldAllowPartialTokenRemoval() {
            Token token2 = new Token("토큰2", player);
            Token token3 = new Token("토큰3", player);
            
            node.enter(token);
            node.enter(token2);
            node.enter(token3);
            
            node.leave(token2);
            
            assertEquals(2, node.getTokens().size());
            assertTrue(node.getTokens().contains(token));
            assertFalse(node.getTokens().contains(token2));
            assertTrue(node.getTokens().contains(token3));
        }
    }
    
    @Nested
    @DisplayName("토큰 진입/퇴장 시나리오 테스트")
    class TokenEnterLeaveScenarioTest {
        
        @Test
        @DisplayName("토큰이 진입 후 다시 진입할 수 있어야 함")
        void shouldAllowTokenToEnterTwice() {
            node.enter(token);
            node.enter(token);
            
            assertEquals(2, node.getTokens().size());
        }
        
        @Test
        @DisplayName("토큰 진입과 퇴장이 순서대로 처리되어야 함")
        void shouldProcessEnterAndLeaveInOrder() {
            Token token2 = new Token("토큰2", player);
            
            node.enter(token);
            node.enter(token2);
            assertEquals(2, node.getTokens().size());
            
            node.leave(token);
            assertEquals(1, node.getTokens().size());
            assertTrue(node.getTokens().contains(token2));
            
            node.leave(token2);
            assertEquals(0, node.getTokens().size());
        }
    }
    
    @Nested
    @DisplayName("보드 크기 테스트")
    class BoardSizeTest {
        
        @Test
        @DisplayName("다양한 보드 크기가 올바르게 설정되어야 함")
        void shouldSetDifferentBoardSizes() {
            BoardNode triangleNode = new BoardNode("삼각형", 0, 0, 3);
            BoardNode squareNode = new BoardNode("사각형", 0, 0, 4);
            BoardNode pentagonNode = new BoardNode("오각형", 0, 0, 5);
            BoardNode hexagonNode = new BoardNode("육각형", 0, 0, 6);
            
            assertEquals(3, triangleNode.getBoardSize());
            assertEquals(4, squareNode.getBoardSize());
            assertEquals(5, pentagonNode.getBoardSize());
            assertEquals(6, hexagonNode.getBoardSize());
        }
        
        @Test
        @DisplayName("보드 크기가 0이어도 설정되어야 함")
        void shouldAllowZeroBoardSize() {
            BoardNode zeroNode = new BoardNode("영크기", 0, 0, 0);
            assertEquals(0, zeroNode.getBoardSize());
        }
        
        @Test
        @DisplayName("보드 크기가 음수여도 설정되어야 함")
        void shouldAllowNegativeBoardSize() {
            BoardNode negativeNode = new BoardNode("음수크기", 0, 0, -1);
            assertEquals(-1, negativeNode.getBoardSize());
        }
    }
    
    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {
        
        @Test
        @DisplayName("노드 체인이 올바르게 구성되어야 함")
        void shouldCreateNodeChainCorrectly() {
            BoardNode node1 = new BoardNode("노드1", 0, 0, 4);
            BoardNode node2 = new BoardNode("노드2", 10, 10, 4);
            BoardNode node3 = new BoardNode("노드3", 20, 20, 4);
            
            node1.addNextNode(node2);
            node2.addNextNode(node3);
            
            assertEquals(1, node1.getNextNodes().size());
            assertEquals(node2, node1.getNextNodes().get(0));
            assertEquals(1, node2.getNextNodes().size());
            assertEquals(node3, node2.getNextNodes().get(0));
            assertEquals(0, node3.getNextNodes().size());
        }
        
        @Test
        @DisplayName("분기 노드가 올바르게 구성되어야 함")
        void shouldCreateBranchNodeCorrectly() {
            BoardNode mainNode = new BoardNode("메인", 0, 0, 4);
            BoardNode branch1 = new BoardNode("분기1", 10, 10, 4);
            BoardNode branch2 = new BoardNode("분기2", 10, -10, 4);
            
            mainNode.addNextNode(branch1);
            mainNode.addNextNode(branch2);
            
            assertEquals(2, mainNode.getNextNodes().size());
            assertTrue(mainNode.getNextNodes().contains(branch1));
            assertTrue(mainNode.getNextNodes().contains(branch2));
        }
    }
} 