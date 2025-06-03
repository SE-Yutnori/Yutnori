package com.cas.yutnoriswing.model.regression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.cas.yutnoriswing.model.*;

@DisplayName("윷놀이 분기 로직 Black Box 테스트 - 6각형")
class BranchLogicBlackBoxTest6 {

    private GameState gameState;
    private Player player;
    private TokenPositionManager tokenManager;
    private Board board;
    private Token token;

    //모든 test 전에 공통적으로 실행되는 메서드 모음
    @BeforeEach
    void setUp() {
        //Swing 라이브러리 비활성화
        System.setProperty("java.awt.headless", "true");
        //테스트 모드 설정
        YutGameRules.setTestMode(true);
        
        //하나의 토큰을 가진 플레이어 생성
        List<String> playerNames = Arrays.asList("테스트플레이어");
        List<Integer> tokenCounts = Arrays.asList(1);
        
        //6각형 게임 상태 초기화
        gameState = new GameState(6, 2.0f, playerNames, tokenCounts);
        //플레이어 설정
        player = gameState.getPlayers().get(0);
        //토큰 위치 관리자 설정
        tokenManager = gameState.getTokenPositionManager();
        //보드 설정
        board = gameState.getBoard();
        token = player.getTokens().get(0);
        
        // 토큰을 활성화 (필요한 권한을 위해)
        tokenManager.placeTokenAtStart(token);
    }

    @Test
    @DisplayName("6각형: Edge0-5 분기점을 끼고 3칸 이동")
    void testBranchPassthrough_Edge0() {
        // Given: Edge0-4에서 시작 (Edge0-5 분기점 바로 전)
        placeTokenAt("Edge0-4");
        
        // When: 3칸 이동 (게임의 기본 분기 로직 사용)
        gameState.moveToken(token, 3, null);
        
        // Then: Edge1-2에 도착해야 함 (기본 경로로 통과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge1-2", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: Edge1-5 분기점을 끼고 3칸 이동")
    void testBranchPassthrough_Edge1() {
        // Given: Edge1-4에서 시작 (Edge1-5 분기점 바로 전)
        placeTokenAt("Edge1-4");
        
        // When: 3칸 이동 (게임의 기본 분기 로직 사용)
        gameState.moveToken(token, 3, null);
        
        // Then: Edge2-2에 도착해야 함 (기본 경로로 통과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge2-2", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: Edge2-5 분기점을 끼고 3칸 이동")
    void testBranchPassthrough_Edge2() {
        // Given: Edge2-4에서 시작 (Edge2-5 분기점 바로 전)
        placeTokenAt("Edge2-4");
        
        // When: 3칸 이동 (게임의 기본 분기 로직 사용)
        gameState.moveToken(token, 3, null);
        
        // Then: Edge3-2에 도착해야 함 (기본 경로로 통과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge3-2", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: Edge3-5 분기점을 끼고 3칸 이동")
    void testBranchPassthrough_Edge3() {
        // Given: Edge3-4에서 시작 (Edge3-5 분기점 바로 전)
        placeTokenAt("Edge3-4");
        
        // When: 3칸 이동 (게임의 기본 분기 로직 사용)
        gameState.moveToken(token, 3, null);
        
        // Then: Edge4-2에 도착해야 함 (기본 경로로 통과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge4-2", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: Edge4-5 분기점을 끼고 3칸 이동")
    void testBranchPassthrough_Edge4() {
        // Given: Edge4-4에서 시작 (Edge4-5 분기점 바로 전)
        placeTokenAt("Edge4-4");
        
        // When: 3칸 이동 (게임의 기본 분기 로직 사용)
        gameState.moveToken(token, 3, null);
        
        // Then: Edge5-2에 도착해야 함 (기본 경로로 통과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge5-2", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: 센터에서 나가는 분기 (ToCenter1 → ToCenter5)")
    void testCenterPath_ToCenter1() {
        // Given: ToCenter1-1에서 시작
        placeTokenAt("ToCenter1-1");
        
        // When: 4칸 이동 (게임의 기본 로직 사용)
        gameState.moveToken(token, 4, null);
        
        // Then: ToCenter5-1에 도착해야 함 (실제 결과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("ToCenter5-1", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: 센터에서 나가는 분기 (ToCenter2 → ToCenter5)")
    void testCenterPath_ToCenter2() {
        // Given: ToCenter2-1에서 시작
        placeTokenAt("ToCenter2-1");
        
        // When: 4칸 이동
        gameState.moveToken(token, 4, null);
        
        // Then: ToCenter5-1에 도착해야 함 (실제 결과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("ToCenter5-1", finalPos.getName());
    }

    @Test
    @DisplayName("6각형: 센터에서 나가는 분기 (ToCenter3 → ToCenter5)")
    void testCenterPath_ToCenter3() {
        // Given: ToCenter3-1에서 시작
        placeTokenAt("ToCenter3-1");
        
        // When: 4칸 이동
        gameState.moveToken(token, 4, null);
        
        // Then: ToCenter5-1에 도착해야 함 (실제 결과)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("ToCenter5-1", finalPos.getName());
    }

    // 헬퍼 메서드: 토큰을 지정된 위치에 배치
    private void placeTokenAt(String nodeName) {
        // 기존 위치에서 제거
        BoardNode currentPos = tokenManager.getTokenPosition(token);
        if (currentPos != null) {
            currentPos.leave(token);
        }
        
        // 새 위치에 배치
        BoardNode targetNode = board.findNodeByName(nodeName);
        assertNotNull(targetNode, "노드를 찾을 수 없음: " + nodeName);
        
        targetNode.enter(token);
        tokenManager.updateTokenPosition(token, targetNode);
    }
} 