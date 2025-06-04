package com.cas.yutnoriswing.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InGameView 클래스 테스트")
class InGameViewTest {

    @Test
    @DisplayName("순서 재배열 검증 - 유효한 입력")
    void testValidateReorderInput_ValidInput() {
        // Given: 원본 결과와 유효한 재배열 입력
        List<Integer> originalResults = Arrays.asList(5, 4, 4, 2);
        String validInput = "4,4,5,2";
        
        // When: 검증 실행
        InGameView.ReorderResult result = InGameView.validateReorderInput(validInput, originalResults);
        
        // Then: 성공해야 함
        assertTrue(result.isSuccess());
        assertEquals(Arrays.asList(4, 4, 5, 2), result.getReorderedResults());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("순서 재배열 검증 - 잘못된 입력들")
    void testValidateReorderInput_InvalidInputs() {
        List<Integer> originalResults = Arrays.asList(5, 4, 1);
        
        // 1. 빈 입력
        InGameView.ReorderResult result1 = InGameView.validateReorderInput("", originalResults);
        assertFalse(result1.isSuccess());
        assertNotNull(result1.getErrorMessage());
        
        // 2. 개수 불일치
        InGameView.ReorderResult result2 = InGameView.validateReorderInput("1,5", originalResults);
        assertFalse(result2.isSuccess());
        assertNotNull(result2.getErrorMessage());
        
        // 3. 범위 벗어난 값
        InGameView.ReorderResult result3 = InGameView.validateReorderInput("1,4,6", originalResults);
        assertFalse(result3.isSuccess());
        assertNotNull(result3.getErrorMessage());
        
        // 4. 숫자가 아닌 값
        InGameView.ReorderResult result4 = InGameView.validateReorderInput("1,a,5", originalResults);
        assertFalse(result4.isSuccess());
        assertNotNull(result4.getErrorMessage());
        
        // 5. 다른 값들 (원본과 다른 숫자들)
        InGameView.ReorderResult result5 = InGameView.validateReorderInput("1,4,3", originalResults);
        assertFalse(result5.isSuccess());
        assertNotNull(result5.getErrorMessage());
    }


    @Test
    @DisplayName("순서 재배열 검증 - 공백 및 형식 처리")
    void testValidateReorderInput_WhitespaceHandling() {
        List<Integer> originalResults = Arrays.asList(5, 5, 3);
        
        // 공백이 포함된 입력
        InGameView.ReorderResult result1 = InGameView.validateReorderInput(" 5, 5 , 3 ", originalResults);
        assertTrue(result1.isSuccess());
        assertEquals(Arrays.asList(5, 5, 3), result1.getReorderedResults());
        
        // 앞뒤 공백
        InGameView.ReorderResult result2 = InGameView.validateReorderInput("  5,5,3  ", originalResults);
        assertTrue(result2.isSuccess());
        assertEquals(Arrays.asList(5, 5, 3), result2.getReorderedResults());
    }

    @Test
    @DisplayName("순서 재배열 검증 - 중복값 처리")
    void testValidateReorderInput_DuplicateHandling() {
        List<Integer> originalResults = Arrays.asList(5, 5, 3);
        
        // 올바른 중복값 재배열
        InGameView.ReorderResult result1 = InGameView.validateReorderInput("5,5,5,3", originalResults);
        assertFalse(result1.isSuccess());
    }
} 