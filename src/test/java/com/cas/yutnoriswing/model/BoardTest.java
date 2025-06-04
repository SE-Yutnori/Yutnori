package com.cas.yutnoriswing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board 클래스 테스트")
class BoardTest {

    private Board board4;
    private Board board5;
    private Board board6;

    @BeforeEach
    void setUp() {
        board4 = new Board(4, 2.0f);
        board5 = new Board(5, 2.0f);
        board6 = new Board(6, 2.0f);
    }

    @Test
    @DisplayName("Board 생성자 - 4각형")
    void testBoardConstructor_Square() {
        // When & Then
        List<BoardNode> nodes = board4.getNodes();
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0, "4각형 보드에 노드가 있어야 함");
        
        // 중앙 노드가 존재하는지 확인
        boolean hasCenterNode = nodes.stream()
                .anyMatch(node -> "Center".equals(node.getName()));
        assertTrue(hasCenterNode, "중앙 노드가 존재해야 함");
        
        // 첫 번째 변의 모든 노드 확인 (Edge0-0 ~ Edge0-5)
        for (int j = 0; j <= 5; j++) {
            String nodeName = "Edge0-" + j;
            boolean hasNode = nodes.stream()
                    .anyMatch(node -> nodeName.equals(node.getName()));
            assertTrue(hasNode, nodeName + " 노드가 존재해야 함");
        }
        
        // 나머지 변들의 노드 확인 (모서리 공유로 인해 j=1부터 시작)
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j <= 5; j++) {
                String nodeName = "Edge" + i + "-" + j;
                boolean hasNode = nodes.stream()
                        .anyMatch(node -> nodeName.equals(node.getName()));
                assertTrue(hasNode, nodeName + " 노드가 존재해야 함");
            }
        }
    }

    @Test
    @DisplayName("Board 생성자 - 5각형")
    void testBoardConstructor_Pentagon() {
        // When & Then
        List<BoardNode> nodes = board5.getNodes();
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0, "5각형 보드에 노드가 있어야 함");
        
        // 5각형이므로 4각형보다 더 많은 노드를 가져야 함
        assertTrue(nodes.size() > board4.getNodes().size(), 
            "5각형 보드가 4각형 보드보다 더 많은 노드를 가져야 함");
        
        // 첫 번째 변의 시작 노드 확인
        boolean hasStartNode = nodes.stream()
                .anyMatch(node -> "Edge0-0".equals(node.getName()));
        assertTrue(hasStartNode, "Edge0-0 노드가 존재해야 함");
        
        // 각 변의 노드들 확인 (0번 변부터 4번 변까지, 각 변의 5번 인덱스까지)
        for (int i = 0; i < 5; i++) {
            for (int j = 1; j <= 5; j++) { // j=0은 이전 변과 공유되므로 j=1부터 시작
                String nodeName = "Edge" + i + "-" + j;
                boolean hasNode = nodes.stream()
                        .anyMatch(node -> nodeName.equals(node.getName()));
                assertTrue(hasNode, nodeName + " 노드가 존재해야 함");
            }
        }
    }

    @Test
    @DisplayName("Board 생성자 - 6각형")
    void testBoardConstructor_Hexagon() {
        // When & Then
        List<BoardNode> nodes = board6.getNodes();
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0, "6각형 보드에 노드가 있어야 함");
        
        // 6각형이므로 5각형보다 더 많은 노드를 가져야 함
        assertTrue(nodes.size() > board5.getNodes().size(), 
            "6각형 보드가 5각형 보드보다 더 많은 노드를 가져야 함");
        
        // 첫 번째 변의 시작 노드 확인
        boolean hasStartNode = nodes.stream()
                .anyMatch(node -> "Edge0-0".equals(node.getName()));
        assertTrue(hasStartNode, "Edge0-0 노드가 존재해야 함");
        
        // 각 변의 노드들 확인 (0번 변부터 5번 변까지, 각 변의 5번 인덱스까지)
        for (int i = 0; i < 6; i++) {
            for (int j = 1; j <= 5; j++) { // j=0은 이전 변과 공유되므로 j=1부터 시작
                String nodeName = "Edge" + i + "-" + j;
                boolean hasNode = nodes.stream()
                        .anyMatch(node -> nodeName.equals(node.getName()));
                assertTrue(hasNode, nodeName + " 노드가 존재해야 함");
            }

        }
    }

    @Test
    @DisplayName("노드 이름 생성 로직 검증")
    void testNodeNamingPattern() {
        List<BoardNode> nodes = board4.getNodes();
        
        // Edge 노드 패턴 확인 (Edge{숫자}-{숫자})
        long edgeNodeCount = nodes.stream()
                .filter(node -> node.getName().matches("Edge\\d+-\\d+"))
                .count();
        assertTrue(edgeNodeCount > 0, "Edge 패턴 노드가 존재해야 함");
        
        // 중앙 노드 확인
        long centerNodeCount = nodes.stream()
                .filter(node -> "Center".equals(node.getName()))
                .count();
        assertEquals(1, centerNodeCount, "Center 노드는 정확히 1개여야 함");
    }

    @Test
    @DisplayName("각 노드의 다음 노드 연결 확인")
    void testNodeConnections() {
        List<BoardNode> nodes = board4.getNodes();
        
        // 모든 노드가 적어도 하나의 다음 노드를 가져야 함 (마지막 노드 제외)
        for (BoardNode node : nodes) {
            if (!node.getName().matches("Edge\\d+-\\d+")) {
                assertFalse(node.getNextNodes().isEmpty(), 
                    node.getName() + " 노드는 다음 노드를 가져야 함");
            }
        }
    }

    @Test
    @DisplayName("빈 토큰 리스트로 시작 테스트")
    void testInitialEmptyTokens() {
        List<BoardNode> nodes = board4.getNodes();
        
        // 모든 노드는 처음에 빈 토큰 리스트를 가져야 함
        for (BoardNode node : nodes) {
            assertTrue(node.getTokens().isEmpty(), 
                node.getName() + " 노드는 처음에 빈 토큰 리스트를 가져야 함");
        }
    }
} 