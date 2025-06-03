package com.cas.yutnoriswing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token 클래스 테스트")
class TokenTest {

    private Player owner;
    private Token token;
    private BoardNode node1, node2;

    @BeforeEach
    void setUp() {
        owner = new Player("테스트플레이어", 3);
        token = new Token("테스트말", owner);
        
        // 테스트용 BoardNode 생성
        node1 = new BoardNode("TestNode1", 100.0f, 100.0f, 4);
        node2 = new BoardNode("TestNode2", 200.0f, 200.0f, 4);
    }

    @Test
    @DisplayName("Token 생성자 테스트")
    void testTokenConstructor() {
        // Given & When
        Token newToken = new Token("새말", owner);
        
        // Then
        assertEquals("새말", newToken.getName());
        assertEquals(owner, newToken.getOwner());
        assertEquals(TokenState.READY, newToken.getState());
        assertTrue(newToken.getStackedTokens().isEmpty());
        assertNull(newToken.getNextBranchChoice());
        assertNull(newToken.getPreviousNode());
    }

    @Test
    @DisplayName("Token setter 테스트")
    void testSetState() {
        // Given & When
        token.setState(TokenState.ACTIVE);
        
        // Then
        assertEquals(TokenState.ACTIVE, token.getState());
        
        // When
        token.setState(TokenState.FINISHED);
        
        // Then
        assertEquals(TokenState.FINISHED, token.getState());
    }

    @Test
    @DisplayName("업힌 말 추가/제거 테스트")
    void testStackedTokenManagement() {
        // Given
        Token stackedToken1 = new Token("업힌말1", owner);
        Token stackedToken2 = new Token("업힌말2", owner);
        
        // When: 스택 토큰 추가
        token.addStackedToken(stackedToken1);
        token.addStackedToken(stackedToken2);
        
        // Then
        List<Token> stackedTokens = token.getStackedTokens();
        assertEquals(2, stackedTokens.size());
        assertTrue(stackedTokens.contains(stackedToken1));
        assertTrue(stackedTokens.contains(stackedToken2));
        
        // When: 중복 추가 시도
        token.addStackedToken(stackedToken1);
        
        // Then: 중복 추가되지 않음
        assertEquals(2, token.getStackedTokens().size());
        
        // When: 토큰 제거
        token.removeStackedToken(stackedToken1);
        
        // Then
        List<Token> remainingTokens = token.getStackedTokens();
        assertEquals(1, remainingTokens.size());
        assertFalse(remainingTokens.contains(stackedToken1));
        assertTrue(remainingTokens.contains(stackedToken2));
    }

    @Test
    @DisplayName("업힌 말 전체 제거 테스트")
    void testClearStackedTokens() {
        // Given
        Token stackedToken1 = new Token("업힌말1", owner);
        Token stackedToken2 = new Token("업힌말2", owner);
        token.addStackedToken(stackedToken1);
        token.addStackedToken(stackedToken2);
        
        // When
        token.clearStackedTokens();
        
        // Then
        assertTrue(token.getStackedTokens().isEmpty());
    }


    // 모든 분기를 지나칠때의 로직이 nextNode를 logic에 맞게 설정하여 가도록 한다
    // 따라서 nextNode를 설정하는 test는 중요하다.
    @Test
    @DisplayName("다음 분기 선택 설정/해제 테스트")
    void testNextBranchChoice() {
        // When: 분기 선택 설정
        token.setNextBranchChoice(node1);
        
        // Then
        assertEquals(node1, token.getNextBranchChoice());
        
        // When: 분기 선택 변경
        token.setNextBranchChoice(node2);
        
        // Then
        assertEquals(node2, token.getNextBranchChoice());
        
        // When: 분기 선택 해제
        token.clearNextBranchChoice();
        
        // Then
        assertNull(token.getNextBranchChoice());
    }


    //5,6각형에서의 기본 분기 설정과 다르게 4각형 Center 기본 분기 설정에서는 어느 방향에서 왔는지 알아야 하기 때문에
    //previousNode를 설정해야 한다.. 해당 previousNode를 통해 어느 방향에서 왔는지 알 수 있다.
    // 그렇기 때문에 previousNode를 설정하는 test를 생성했다.
    @Test
    @DisplayName("이전 노드 설정/해제 테스트 (4각형 보드 Center 기본 분기 설정 용)")
    void testPreviousNode() {
        // When: 이전 노드 설정
        token.setPreviousNode(node1);
        
        // Then
        assertEquals(node1, token.getPreviousNode());
        
        // When: 이전 노드 변경
        token.setPreviousNode(node2);
        
        // Then
        assertEquals(node2, token.getPreviousNode());
        
        // When: 이전 노드 해제
        token.clearPreviousNode();
        
        // Then
        assertNull(token.getPreviousNode());
    }

    @Test
    @DisplayName("업힌 말 없을 때 대표 말 검증 테스트")
    void testGetTopMostToken_NotStacked() {
        // When & Then
        assertEquals(token, token.getTopMostToken());
    }

    @Test
    @DisplayName("업힌 말 1개 일 때 대표 말 검증 테스트")
    void testGetTopMostToken_SingleStack() {
        // Given
        Token bottomToken = owner.getTokens().get(0);
        Token topToken = owner.getTokens().get(1);
        
        // topToken이 bottomToken 위에 스택됨
        bottomToken.addStackedToken(topToken);
        
        // When & Then
        assertEquals(bottomToken, topToken.getTopMostToken());
        assertEquals(bottomToken, bottomToken.getTopMostToken());
    }

    @Test
    @DisplayName("3개 이상 업힌 말 중 대표 말 검증 테스트")
    void testGetTopMostToken_MultipleStack() {
        // Given
        Token bottomToken = owner.getTokens().get(0);  // 테스트플레이어-1
        Token middleToken = owner.getTokens().get(1);  // 테스트플레이어-2
        Token topToken = owner.getTokens().get(2);     // 테스트플레이어-3
        
        // 스택 구조: bottomToken <- middleToken <- topToken
        bottomToken.addStackedToken(middleToken);
        middleToken.addStackedToken(topToken);
        
        // When & Then: 모든 토큰이 bottomToken을 최상위로 인식해야 함
        assertEquals(bottomToken, bottomToken.getTopMostToken());
        assertEquals(bottomToken, middleToken.getTopMostToken());
        assertEquals(bottomToken, topToken.getTopMostToken());
    }

    @Test
    @DisplayName("Token 불변 속성 테스트")
    void testTokenImmutableProperties() {
        // Given & When & Then
        assertEquals("테스트말", token.getName());
        assertEquals(owner, token.getOwner());
        
        // 이름과 소유자는 생성 후 변경할 수 없어야 함 (final 필드)
    }

    @Test
    @DisplayName("존재하지 않는 토큰 제거 시도")
    void testRemoveNonExistentStackedToken() {
        // Given
        Token nonExistentToken = new Token("존재하지않는토큰", owner);
        Token existingToken = new Token("존재하는토큰", owner);
        token.addStackedToken(existingToken);
        
        // When
        token.removeStackedToken(nonExistentToken);
        
        // Then: 기존 토큰은 그대로 유지
        assertEquals(1, token.getStackedTokens().size());
        assertTrue(token.getStackedTokens().contains(existingToken));
    }

    @Test
    @DisplayName("TokenState 열거형 테스트")
    void testTokenStateEnum() {
        // Given & When & Then: 모든 TokenState 값이 설정 가능해야 함
        token.setState(TokenState.READY);
        assertEquals(TokenState.READY, token.getState());
        
        token.setState(TokenState.ACTIVE);
        assertEquals(TokenState.ACTIVE, token.getState());
        
        token.setState(TokenState.FINISHED);
        assertEquals(TokenState.FINISHED, token.getState());
    }
} 