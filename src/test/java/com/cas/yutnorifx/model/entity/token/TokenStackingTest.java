package com.cas.yutnorifx.model.entity.token;

import com.cas.yutnorifx.model.entity.Player;
import com.cas.yutnorifx.model.entity.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Token 업기(스택) 기능 테스트 (TDD)
 */
class TokenStackingTest {
    
    private Player testPlayer1;
    private Player testPlayer2;
    
    private Token baseToken;
    private Token stackedToken1;
    private Token stackedToken2;
    
    @BeforeEach
    void setUp() {
        testPlayer1 = new Player("테스트플레이어1", 3);
        testPlayer2 = new Player("테스트플레이어2", 3);
        
        baseToken = new Token("기본토큰", testPlayer1);
        stackedToken1 = new Token("업힌토큰1", testPlayer1);
        stackedToken2 = new Token("업힌토큰2", testPlayer2);
    }
    
    @Test
    void 새로_생성된_토큰은_빈_스택을_가져야_한다() {
        // Then: 새 토큰의 스택은 비어있어야 한다
        assertTrue(baseToken.getStackedTokens().isEmpty());
        assertEquals(0, baseToken.getStackedTokens().size());
    }
    
    @Test
    void 토큰에_다른_토큰을_업힐_수_있어야_한다() {
        // When: 토큰에 다른 토큰을 업히면
        baseToken.addStackedToken(stackedToken1);
        
        // Then: 스택에 해당 토큰이 포함되어야 한다
        assertEquals(1, baseToken.getStackedTokens().size());
        assertTrue(baseToken.getStackedTokens().contains(stackedToken1));
    }
    
    @Test
    void 토큰에_여러_토큰을_업힐_수_있어야_한다() {
        // When: 토큰에 여러 토큰을 업히면
        baseToken.addStackedToken(stackedToken1);
        baseToken.addStackedToken(stackedToken2);
        
        // Then: 스택에 모든 토큰이 포함되어야 한다
        assertEquals(2, baseToken.getStackedTokens().size());
        assertTrue(baseToken.getStackedTokens().contains(stackedToken1));
        assertTrue(baseToken.getStackedTokens().contains(stackedToken2));
    }
    
    @Test
    void 중복된_토큰은_업힐_수_없어야_한다() {
        // When: 같은 토큰을 두 번 업히면
        baseToken.addStackedToken(stackedToken1);
        baseToken.addStackedToken(stackedToken1); // 중복 추가
        
        // Then: 스택에는 하나만 있어야 한다
        assertEquals(1, baseToken.getStackedTokens().size());
        assertTrue(baseToken.getStackedTokens().contains(stackedToken1));
    }
    
    @Test
    void 모든_업힌_토큰을_제거할_수_있어야_한다() {
        // Given: 여러 토큰이 업혀있을 때
        baseToken.addStackedToken(stackedToken1);
        baseToken.addStackedToken(stackedToken2);
        
        // When: 모든 업힌 토큰을 제거하면
        baseToken.clearStackedTokens();
        
        // Then: 스택이 비어있어야 한다
        assertTrue(baseToken.getStackedTokens().isEmpty());
        assertEquals(0, baseToken.getStackedTokens().size());
    }
} 