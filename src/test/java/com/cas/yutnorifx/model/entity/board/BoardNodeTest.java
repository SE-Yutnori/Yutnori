package com.cas.yutnorifx.model.entity.board;

import com.cas.yutnorifx.model.entity.BoardNode;
import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardNode 클래스 기본 기능 테스트
 */
class BoardNodeTest {
    
    private Player testPlayer1;
    private Player testPlayer2;
    
    private BoardNode node;
    private Token token1;
    private Token token2;
    
    @BeforeEach
    void setUp() {
        testPlayer1 = new Player("테스트플레이어1", 3);
        testPlayer2 = new Player("테스트플레이어2", 3);
        
        node = new BoardNode("테스트노드", 100.0f, 200.0f, 4);
        token1 = new Token("토큰1", testPlayer1);
        token2 = new Token("토큰2", testPlayer2);
    }
    
    @Test
    void 보드노드_이름_반환_테스트() {
        // Given: 노드 이름이 주어졌을 때
        String nodeName = "테스트노드";
        
        // When: 노드를 생성하면
        BoardNode node = new BoardNode(nodeName, 0.0f, 0.0f, 4);
        
        // Then: 노드는 해당 이름을 반환해야 한다
        assertEquals(nodeName, node.getName());
    }
    
    @Test
    void 보드노드_좌표를_반환_테스트() {
        // Given: 노드 좌표가 주어졌을 때
        float x = 100.0f;
        float y = 200.0f;
        
        // When: 노드를 생성하면
        BoardNode node = new BoardNode("테스트노드", x, y, 4);
        
        // Then: 노드는 해당 좌표를 반환해야 한다
        assertEquals(x, node.getX());
        assertEquals(y, node.getY());
    }
    
    @Test
    void 생성된_노드_빈_토큰_리스트_가지는지_여부_테스트() {
        // Then: 새 노드의 토큰 리스트는 비어있어야 한다
        assertTrue(node.getTokens().isEmpty());
        assertEquals(0, node.getTokens().size());
    }
    
    @Test
    void 노드에_토큰_추가_가능_테스트() {
        // When: 노드에 토큰을 추가하면
        node.enter(token1);
        
        // Then: 노드에 해당 토큰이 포함되어야 한다
        assertEquals(1, node.getTokens().size());
        assertTrue(node.getTokens().contains(token1));
    }
    
    @Test
    void 노드_복수_토큰_수용_여부_테스트() {
        // When: 노드에 여러 토큰을 추가하면
        node.enter(token1);
        node.enter(token2);
        
        // Then: 노드에 모든 토큰이 포함되어야 한다
        assertEquals(2, node.getTokens().size());
        assertTrue(node.getTokens().contains(token1));
        assertTrue(node.getTokens().contains(token2));
    }
    
    @Test
    void 노드_토큰_제거_가능_여부_테스트() {
        // Given: 노드에 토큰들이 있을 때
        node.enter(token1);
        node.enter(token2);
        
        // When: 노드에서 토큰을 제거하면
        node.leave(token1);
        
        // Then: 해당 토큰만 제거되어야 한다
        assertEquals(1, node.getTokens().size());
        assertFalse(node.getTokens().contains(token1));
        assertTrue(node.getTokens().contains(token2));
    }
    
    @Test
    void 노드_보드_크기_반환_테스트() {
        // Given: 4각형 보드 노드가 주어졌을 때
        int expectedSides = 4;
        
        // Then: 보드 크기가 4여야 한다
        assertEquals(expectedSides, node.getBoardSize());
    }
}