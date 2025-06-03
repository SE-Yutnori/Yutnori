package com.cas.yutnoriswing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

@DisplayName("YutGameRules 클래스 테스트")
class YutGameRulesTest {

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 테스트 모드를 기본값(false)로 리셋
        YutGameRules.setTestMode(false);
    }

    @Test
    @DisplayName("테스트 모드 설정 가능 테스트")
    void testTestMode() {
        // 초기 상태: 테스트 모드 비활성화
        //Given
        assertFalse(YutGameRules.isTestMode());
        
        // 테스트 모드 활성화
        //When
        YutGameRules.setTestMode(true);

        //Then
        assertTrue(YutGameRules.isTestMode());
    }

    @Test
    @DisplayName("일반 모드 윷 던지기 - 여러 번 실행하여 랜덤성 확인 및 유효한 결과인지 확인 테스트")
    void testThrowOneYut_NormalMode() {
        // Given: 일반 모드
        YutGameRules.setTestMode(false);
        
        // When: 윷을 여러 번 던져서 다양한 결과가 나오는지 확인
        boolean hasVariety = false;
        int firstResult = YutGameRules.throwOneYut();
        
        for (int i = 0; i < 50; i++) {
            int result = YutGameRules.throwOneYut();
            if (result != firstResult) {
                hasVariety = true;
                break;
            }
        }
        
        // Then: 랜덤하게 다양한 값이 나와야 함
        assertTrue(hasVariety, "윷 던지기가 랜덤하게 다양한 값을 생성해야 함");
        
        // 모든 결과가 유효한 범위 내에 있어야 함 (-1, 1-5)
        for (int i = 0; i < 20; i++) {
            int result = YutGameRules.throwOneYut();
            assertTrue((result == -1) || (result >= 1 && result <= 5), 
                "윷 던지기 결과는 -1 또는 1-5 범위여야 함, 실제: " + result);
        }
    }

    @Test
    @DisplayName("연속 윷 던지기 - 윷이나 모가 나올 때까지 계속 던지기")
    void testThrowYut_ContinuousThrow() {
        // Given: 일반 모드
        YutGameRules.setTestMode(false);
        
        // When: 연속 던지기 실행
        YutGameRules.YutThrowResult result = YutGameRules.throwYut();
        
        // Then: 결과 검증
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertNotNull(result.getResultMessages());
        assertFalse(result.getResults().isEmpty());
        assertFalse(result.getResultMessages().isEmpty());
        
        // 마지막 결과는 4 미만이어야 함 (윷이나 모가 아닌 경우에만 종료)
        int lastResult = result.getResults().get(result.getResults().size() - 1);
        assertTrue(lastResult < 4, "마지막 결과는 도/개/걸이어야 함");
    }

    @Test
    @DisplayName("순서 재배열 검증 - 유효한 입력")
    void testValidateReorderInput_ValidInput() {
        // Given: 원본 결과와 유효한 재배열 입력
        List<Integer> originalResults = Arrays.asList(1, 3, 4, 5);
        String validInput = "4,1,5,3";
        
        // When: 검증 실행
        YutGameRules.ReorderResult result = YutGameRules.validateReorderInput(validInput, originalResults);
        
        // Then: 성공해야 함
        assertTrue(result.isSuccess());
        assertEquals(Arrays.asList(4, 1, 5, 3), result.getReorderedResults());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("순서 재배열 검증 - 잘못된 입력들")
    void testValidateReorderInput_InvalidInputs() {
        List<Integer> originalResults = Arrays.asList(1, 3, 4);
        
        // 1. 빈 입력
        YutGameRules.ReorderResult result1 = YutGameRules.validateReorderInput("", originalResults);
        assertFalse(result1.isSuccess());
        
        // 2. 개수 불일치
        YutGameRules.ReorderResult result2 = YutGameRules.validateReorderInput("1,3", originalResults);
        assertFalse(result2.isSuccess());
        
        // 3. 범위 벗어난 값
        YutGameRules.ReorderResult result3 = YutGameRules.validateReorderInput("1,3,6", originalResults);
        assertFalse(result3.isSuccess());
        
        // 4. 숫자가 아닌 값
        YutGameRules.ReorderResult result4 = YutGameRules.validateReorderInput("1,a,3", originalResults);
        assertFalse(result4.isSuccess());
        
        // 5. 다른 값들
        YutGameRules.ReorderResult result5 = YutGameRules.validateReorderInput("1,2,3", originalResults);
        assertFalse(result5.isSuccess());
    }

    @Test
    @DisplayName("토큰 이동 - 정상 이동")
    void testMoveToken_NormalMove() {
        // Given: 4각형 게임 설정
        List<String> playerNames = Arrays.asList("플레이어1");
        List<Integer> tokenCounts = Arrays.asList(1);
        GameState gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        
        Player player = gameState.getPlayers().get(0);
        Token token = player.getTokens().get(0);
        TokenPositionManager tokenManager = gameState.getTokenPositionManager();
        
        // 토큰을 시작 위치에 배치
        tokenManager.placeTokenAtStart(token);
        
        // When: 3칸 이동
        YutGameRules.MoveResult result = YutGameRules.moveToken(token, 3, tokenManager, null);
        
        // Then: 성공적으로 이동해야 함
        assertTrue(result.isSuccess());
        assertFalse(result.isCatched());
        assertFalse(result.isFinished());
        
        // 위치 확인
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge0-3", finalPos.getName());
    }

    @Test
    @DisplayName("토큰 이동 - 완주 처리")
    void testMoveToken_Finish() {
        // Given: 게임 설정
        List<String> playerNames = Arrays.asList("플레이어1");
        List<Integer> tokenCounts = Arrays.asList(1);
        GameState gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        
        Player player = gameState.getPlayers().get(0);
        Token token = player.getTokens().get(0);
        TokenPositionManager tokenManager = gameState.getTokenPositionManager();
        Board board = gameState.getBoard();
        
        // 토큰을 완주 직전 위치에 배치
        tokenManager.placeTokenAtStart(token);
        BoardNode nearFinish = board.findNodeByName("Edge3-4"); // 완주 직전
        if (nearFinish != null) {
            BoardNode currentPos = tokenManager.getTokenPosition(token);
            if (currentPos != null) currentPos.leave(token);
            nearFinish.enter(token);
            tokenManager.updateTokenPosition(token, nearFinish);
        }
        
        // When: 충분한 칸수로 이동 (완주되도록)
        YutGameRules.MoveResult result = YutGameRules.moveToken(token, 5, tokenManager, null);
        
        // Then: 완주 처리되어야 함
        assertTrue(result.isSuccess());
        assertTrue(result.isFinished());
        assertEquals("말이 완주했습니다!", result.getMessage());
        
        // 토큰이 보드에서 제거되어야 함
        assertNull(tokenManager.getTokenPosition(token));
        assertEquals(TokenState.FINISHED, token.getState());
    }

    @Test
    @DisplayName("토큰 뒤로 이동 - 빽도 처리")
    void testMoveTokenBackward() {
        // Given: 게임 설정
        List<String> playerNames = Arrays.asList("플레이어1");
        List<Integer> tokenCounts = Arrays.asList(1);
        GameState gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        
        Player player = gameState.getPlayers().get(0);
        Token token = player.getTokens().get(0);
        TokenPositionManager tokenManager = gameState.getTokenPositionManager();
        Board board = gameState.getBoard();
        
        // 토큰을 Edge0-4 위치에 배치
        tokenManager.placeTokenAtStart(token);
        BoardNode targetPos = board.findNodeByName("Edge0-4");
        if (targetPos != null) {
            BoardNode currentPos = tokenManager.getTokenPosition(token);
            if (currentPos != null) currentPos.leave(token);
            targetPos.enter(token);
            tokenManager.updateTokenPosition(token, targetPos);
        }
        
        // When: 2칸 뒤로 이동
        YutGameRules.MoveResult result = YutGameRules.moveTokenBackward(token, 2, tokenManager);
        
        // Then: 성공적으로 뒤로 이동해야 함
        assertTrue(result.isSuccess());
        
        // 위치 확인 (Edge0-4 → Edge0-2)
        BoardNode finalPos = tokenManager.getTokenPosition(token);
        assertEquals("Edge0-2", finalPos.getName());
    }

    @Test
    @DisplayName("토큰 잡기 처리")
    void testMoveToken_Capture() {
        // Given: 게임 설정 (2명의 플레이어)
        List<String> playerNames = Arrays.asList("플레이어1", "플레이어2");
        List<Integer> tokenCounts = Arrays.asList(1, 1);
        GameState gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        
        Player player1 = gameState.getPlayers().get(0);
        Player player2 = gameState.getPlayers().get(1);
        Token token1 = player1.getTokens().get(0);
        Token token2 = player2.getTokens().get(0);
        TokenPositionManager tokenManager = gameState.getTokenPositionManager();
        Board board = gameState.getBoard();
        
        // 두 토큰 모두 활성화
        tokenManager.placeTokenAtStart(token1);
        tokenManager.placeTokenAtStart(token2);
        
        // player2의 토큰을 Edge0-3에 배치
        BoardNode targetPos = board.findNodeByName("Edge0-3");
        if (targetPos != null) {
            BoardNode currentPos = tokenManager.getTokenPosition(token2);
            if (currentPos != null) currentPos.leave(token2);
            targetPos.enter(token2);
            tokenManager.updateTokenPosition(token2, targetPos);
        }
        
        // When: player1의 토큰이 같은 위치로 이동 (잡기)
        YutGameRules.MoveResult result = YutGameRules.moveToken(token1, 3, tokenManager, null);
        
        // Then: 잡기가 성공해야 함
        assertTrue(result.isSuccess());
        assertTrue(result.isCatched());
        assertEquals("상대방 말을 잡았습니다!", result.getMessage());
        
        // 잡힌 토큰은 초기화되어야 함
        assertNull(tokenManager.getTokenPosition(token2));
        assertEquals(TokenState.READY, token2.getState());
    }

    @Test
    @DisplayName("토큰 업기 처리")
    void testMoveToken_Stacking() {
        // Given: 게임 설정 (같은 팀 토큰 2개)
        List<String> playerNames = Arrays.asList("플레이어1");
        List<Integer> tokenCounts = Arrays.asList(2);
        GameState gameState = new GameState(4, 2.0f, playerNames, tokenCounts);
        
        Player player = gameState.getPlayers().get(0);
        Token token1 = player.getTokens().get(0);
        Token token2 = player.getTokens().get(1);
        TokenPositionManager tokenManager = gameState.getTokenPositionManager();
        Board board = gameState.getBoard();
        
        // 두 토큰 모두 활성화
        tokenManager.placeTokenAtStart(token1);
        tokenManager.placeTokenAtStart(token2);
        
        // token2를 Edge0-3에 배치
        BoardNode targetPos = board.findNodeByName("Edge0-3");
        if (targetPos != null) {
            BoardNode currentPos = tokenManager.getTokenPosition(token2);
            if (currentPos != null) currentPos.leave(token2);
            targetPos.enter(token2);
            tokenManager.updateTokenPosition(token2, targetPos);
        }
        
        // When: token1이 같은 위치로 이동 (업기)
        YutGameRules.MoveResult result = YutGameRules.moveToken(token1, 3, tokenManager, null);
        
        // Then: 업기가 성공해야 함
        assertTrue(result.isSuccess());
        assertFalse(result.isCatched());
        
        // token1이 대표 토큰이 되고, token2는 업힌 상태
        assertEquals(1, token1.getStackedTokens().size());
        assertTrue(token1.getStackedTokens().contains(token2));
        assertNull(tokenManager.getTokenPosition(token2)); // 업힌 토큰은 위치가 null
    }
} 