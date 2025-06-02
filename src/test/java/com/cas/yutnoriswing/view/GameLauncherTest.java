package com.cas.yutnoriswing.view;

import com.cas.yutnoriswing.model.GameState;
import com.cas.yutnoriswing.model.Player;
import com.cas.yutnoriswing.model.Board;
import com.cas.yutnoriswing.model.YutGameRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameLauncherTest {

    @BeforeEach
    void setUp() {
        // 테스트 환경에서는 실제 GUI가 뜨지 않도록 headless 모드로 설정
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void testGameState_완전한_게임_설정_시나리오() {
        // Given: 5각형, 테스트모드, 3명, 4개토큰
        int boardSides = 5;
        boolean testMode = true;
        List<String> playerNames = Arrays.asList("김철수", "이영희", "박민수");
        List<Integer> tokenCounts = Arrays.asList(4, 4, 4);

        // When: GameState 직접 생성 (GameLauncher 로직과 동일)
        YutGameRules.setTestMode(testMode);
        GameState gameState = new GameState(boardSides, 2.0f, playerNames, tokenCounts);

        // Then: 게임 상태 검증
        assertNotNull(gameState, "GameState가 생성되어야 함");

        // 보드 검증
        Board board = gameState.getBoard();
        assertNotNull(board, "Board가 생성되어야 함");
        assertTrue(board.getNodes().size() > 0, "Board에 노드들이 있어야 함");

        // 플레이어 검증
        List<Player> players = gameState.getPlayers();
        assertEquals(3, players.size(), "플레이어가 3명이어야 함");

        // 각 플레이어 검증
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        Player player3 = players.get(2);

        assertEquals("김철수", player1.getName(), "첫 번째 플레이어 이름 확인");
        assertEquals("이영희", player2.getName(), "두 번째 플레이어 이름 확인");
        assertEquals("박민수", player3.getName(), "세 번째 플레이어 이름 확인");

        // 각 플레이어의 토큰 개수 검증
        assertEquals(4, player1.getTokens().size(), "플레이어1은 4개의 토큰을 가져야 함");
        assertEquals(4, player2.getTokens().size(), "플레이어2는 4개의 토큰을 가져야 함");
        assertEquals(4, player3.getTokens().size(), "플레이어3은 4개의 토큰을 가져야 함");

        // 토큰 이름 검증 (Player-TokenIndex 형식, 1부터 시작)
        assertEquals("김철수-1", player1.getTokens().get(0).getName());
        assertEquals("김철수-2", player1.getTokens().get(1).getName());
        assertEquals("이영희-1", player2.getTokens().get(0).getName());
        assertEquals("박민수-4", player3.getTokens().get(3).getName());

        // 게임 상태 검증
        assertEquals(player1, gameState.getCurrentPlayer(), "첫 번째 플레이어가 현재 플레이어여야 함");
        assertFalse(gameState.isGameEnded(), "게임이 아직 끝나지 않았어야 함");

        // 테스트 모드 설정 검증
        assertTrue(YutGameRules.isTestMode(), "테스트 모드가 활성화되어야 함");
    }

    @Test
    void testGameState_최소_설정_시나리오() {
        // Given: 최소 설정 (4각형, 일반모드, 2명, 2개토큰)
        YutGameRules.setTestMode(false);
        GameState gameState = new GameState(4, 2.0f, 
            Arrays.asList("플레이어1", "플레이어2"),
            Arrays.asList(2, 2));
        
        // Then
        assertNotNull(gameState);
        assertEquals(2, gameState.getPlayers().size());
        assertEquals(2, gameState.getPlayers().get(0).getTokens().size());
        assertEquals(2, gameState.getPlayers().get(1).getTokens().size());
        assertFalse(YutGameRules.isTestMode());
    }

    @Test
    void testGameState_최대_설정_시나리오() {
        // Given: 최대 설정 (6각형, 테스트모드, 4명, 5개토큰)
        YutGameRules.setTestMode(true);
        GameState gameState = new GameState(6, 2.0f, 
            Arrays.asList("A", "B", "C", "D"),
            Arrays.asList(5, 5, 5, 5));
        
        // Then
        assertNotNull(gameState);
        assertEquals(4, gameState.getPlayers().size());
        
        // 모든 플레이어가 5개의 토큰을 가지는지 확인
        for (Player player : gameState.getPlayers()) {
            assertEquals(5, player.getTokens().size());
        }
        
        assertTrue(YutGameRules.isTestMode());
    }

    @Test
    void testPlayerTokenOwnership() {
        // Given
        GameState gameState = new GameState(4, 2.0f, 
            Arrays.asList("주인1", "주인2"),
            Arrays.asList(3, 3));
        
        // Then: 각 토큰이 올바른 플레이어를 주인으로 가지는지 확인
        List<Player> players = gameState.getPlayers();
        
        for (Player player : players) {
            for (int j = 0; j < player.getTokens().size(); j++) {
                assertEquals(player, player.getTokens().get(j).getOwner(), 
                    "토큰의 주인이 올바르게 설정되어야 함");
            }
        }
    }

    @Test
    void testBoardStructure() {
        // Given: 5각형 보드
        GameState gameState = new GameState(5, 2.0f, 
            Arrays.asList("테스터"),
            Arrays.asList(2));
        
        // Then: 보드 구조 검증
        Board board = gameState.getBoard();
        assertNotNull(board.getNodes());
        
        // 5각형이므로 적절한 수의 노드가 있어야 함
        assertTrue(board.getNodes().size() >= 15, "5각형 보드는 최소 15개 이상의 노드를 가져야 함");
        
        // 시작 노드가 존재하는지 확인
        boolean hasStartNode = board.getNodes().stream()
                .anyMatch(node -> node.getName().matches("Edge\\d+-0"));
        assertTrue(hasStartNode, "시작 노드가 존재해야 함");
        
        // 중앙 노드가 존재하는지 확인
        boolean hasCenterNode = board.getNodes().stream()
                .anyMatch(node -> node.getName().equals("Center"));
        assertTrue(hasCenterNode, "중앙 노드가 존재해야 함");
    }

    @Test
    void testDifferentBoardSizes() {
        // Given: 4, 5, 6 각형 보드 모두 테스트
        for (int sides = 4; sides <= 6; sides++) {
            // When
            GameState gameState = new GameState(sides, 2.0f, 
                Arrays.asList("테스터"),
                Arrays.asList(2));
            
            // Then
            assertNotNull(gameState, sides + "각형 보드 생성 실패");
            assertTrue(gameState.getBoard().getNodes().size() > 0, 
                sides + "각형 보드에 노드가 없음");
            
            // 각형 수에 따른 최소 노드 수 확인
            int expectedMinNodes = sides * 3; // 대략적인 최소 노드 수
            assertTrue(gameState.getBoard().getNodes().size() >= expectedMinNodes,
                sides + "각형 보드 노드 수가 예상보다 적음");
        }
    }

    @Test
    void testPlayerTokenNames() {
        // Given: 플레이어 이름과 토큰 개수 설정
        GameState gameState = new GameState(4, 2.0f, 
            Arrays.asList("Alice", "Bob"),
            Arrays.asList(3, 2));
        
        // Then: 토큰 이름이 올바르게 생성되었는지 확인
        List<Player> players = gameState.getPlayers();
        
        // Alice의 토큰들 확인
        Player alice = players.get(0);
        assertEquals("Alice", alice.getName());
        assertEquals(3, alice.getTokens().size());
        assertEquals("Alice-1", alice.getTokens().get(0).getName());
        assertEquals("Alice-2", alice.getTokens().get(1).getName());
        assertEquals("Alice-3", alice.getTokens().get(2).getName());
        
        // Bob의 토큰들 확인
        Player bob = players.get(1);
        assertEquals("Bob", bob.getName());
        assertEquals(2, bob.getTokens().size());
        assertEquals("Bob-1", bob.getTokens().get(0).getName());
        assertEquals("Bob-2", bob.getTokens().get(1).getName());
    }

    @Test
    void testGameInitialState() {
        // Given
        GameState gameState = new GameState(5, 2.0f, 
            Arrays.asList("Player1", "Player2", "Player3"),
            Arrays.asList(4, 4, 4));
        
        // Then: 초기 게임 상태 확인
        assertFalse(gameState.isGameEnded(), "게임이 시작할 때는 끝나지 않은 상태여야 함");
        assertNotNull(gameState.getCurrentPlayer(), "현재 플레이어가 있어야 함");
        assertEquals("Player1", gameState.getCurrentPlayer().getName(), 
            "첫 번째 플레이어가 시작 플레이어여야 함");
        
        // 모든 토큰이 대기 상태인지 확인
        for (Player player : gameState.getPlayers()) {
            for (var token : player.getTokens()) {
                assertEquals(com.cas.yutnoriswing.model.TokenState.READY, 
                    token.getState(), "모든 토큰이 처음에는 READY 상태여야 함");
            }
        }
    }
} 