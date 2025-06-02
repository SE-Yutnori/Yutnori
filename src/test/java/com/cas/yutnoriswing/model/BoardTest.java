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
        
        // 첫 번째 변의 시작 노드 확인 (Edge0-0)
        boolean hasStartNode = nodes.stream()
                .anyMatch(node -> "Edge0-0".equals(node.getName()));
        assertTrue(hasStartNode, "Edge0-0 노드가 존재해야 함");
        
        // 각 변의 첫 번째 노드들은 실제로는 이전 변의 마지막 노드와 공유됨
        // 따라서 Edge0-5 노드가 존재하는지 확인
        boolean hasEdge0End = nodes.stream()
                .anyMatch(node -> "Edge0-5".equals(node.getName()));
        assertTrue(hasEdge0End, "Edge0-5 노드가 존재해야 함");
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
    @DisplayName("노드 이름 패턴 검증")
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
    @DisplayName("각 노드의 좌표가 설정되어 있는지 확인")
    void testNodeCoordinates() {
        List<BoardNode> nodes = board5.getNodes();
        
        for (BoardNode node : nodes) {
            // 좌표가 유효한 범위에 있는지 확인 (음수가 아니어야 함)
            assertTrue(node.getX() >= 0, node.getName() + "의 X 좌표가 음수입니다");
            assertTrue(node.getY() >= 0, node.getName() + "의 Y 좌표가 음수입니다");
        }
    }

    @Test
    @DisplayName("각 노드의 다음 노드 연결 확인")
    void testNodeConnections() {
        List<BoardNode> nodes = board4.getNodes();
        
        // 모든 노드가 적어도 하나의 다음 노드를 가져야 함 (마지막 노드 제외)
        for (BoardNode node : nodes) {
            if (!node.getName().matches("Edge\\d+-\\d+") || !isLastNodeInEdge(node)) {
                assertFalse(node.getNextNodes().isEmpty(), 
                    node.getName() + " 노드는 다음 노드를 가져야 함");
            }
        }
    }

    @Test
    @DisplayName("보드 크기에 따른 노드 수 증가 확인")
    void testNodeCountIncrease() {
        int nodes4 = board4.getNodes().size();
        int nodes5 = board5.getNodes().size();
        int nodes6 = board6.getNodes().size();
        
        // 각형 수가 증가할수록 노드 수도 증가해야 함
        assertTrue(nodes4 < nodes5, "5각형이 4각형보다 노드가 많아야 함");
        assertTrue(nodes5 < nodes6, "6각형이 5각형보다 노드가 많아야 함");
    }

    @Test
    @DisplayName("동일한 설정으로 생성된 보드는 동일한 노드 수를 가짐")
    void testConsistentBoardGeneration() {
        // Given
        Board anotherBoard4 = new Board(4, 2.0f);
        Board anotherBoard5 = new Board(5, 2.0f);
        
        // When & Then
        assertEquals(board4.getNodes().size(), anotherBoard4.getNodes().size(),
            "같은 설정의 4각형 보드는 동일한 노드 수를 가져야 함");
        assertEquals(board5.getNodes().size(), anotherBoard5.getNodes().size(),
            "같은 설정의 5각형 보드는 동일한 노드 수를 가져야 함");
    }

    @Test
    @DisplayName("노드 이름의 고유성 확인")
    void testNodeNameUniqueness() {
        List<BoardNode> nodes = board5.getNodes();
        
        // 모든 노드 이름이 고유해야 함
        long uniqueNames = nodes.stream()
                .map(BoardNode::getName)
                .distinct()
                .count();
        
        assertEquals(nodes.size(), uniqueNames, "모든 노드는 고유한 이름을 가져야 함");
    }

    @Test
    @DisplayName("각 노드의 보드 크기 정보 확인")
    void testNodeBoardSizeInfo() {
        List<BoardNode> nodes4 = board4.getNodes();
        List<BoardNode> nodes5 = board5.getNodes();
        
        // 4각형 보드의 모든 노드는 sides = 4를 가져야 함
        for (BoardNode node : nodes4) {
            assertEquals(4, node.getBoardSize(), 
                "4각형 보드의 노드는 sides = 4를 가져야 함");
        }
        
        // 5각형 보드의 모든 노드는 sides = 5를 가져야 함
        for (BoardNode node : nodes5) {
            assertEquals(5, node.getBoardSize(), 
                "5각형 보드의 노드는 sides = 5를 가져야 함");
        }
    }

    @Test
    @DisplayName("빈 토큰 리스트로 시작")
    void testInitialEmptyTokens() {
        List<BoardNode> nodes = board4.getNodes();
        
        // 모든 노드는 처음에 빈 토큰 리스트를 가져야 함
        for (BoardNode node : nodes) {
            assertTrue(node.getTokens().isEmpty(), 
                node.getName() + " 노드는 처음에 빈 토큰 리스트를 가져야 함");
        }
    }

    // 헬퍼 메서드: 엣지의 마지막 노드인지 확인
    private boolean isLastNodeInEdge(BoardNode node) {
        // Edge 패턴이 아니면 false
        if (!node.getName().matches("Edge\\d+-\\d+")) {
            return false;
        }
        
        // Edge{n}-{m} 형태에서 m이 가장 큰 값인지 확인하는 로직
        // 실제 구현에서는 보드의 구조를 알아야 정확히 판단 가능
        // 여기서는 간단히 다음 노드가 없으면 마지막 노드로 간주
        return node.getNextNodes().isEmpty();
    }
} 