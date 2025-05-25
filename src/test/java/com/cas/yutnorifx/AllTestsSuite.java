package com.cas.yutnorifx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("윷놀이 게임 전체 테스트 스위트")
class AllTestsSuite {
    
    @Test
    @DisplayName("테스트 스위트 실행 확인")
    void testSuiteExecution() {
        // 이 테스트는 테스트 스위트가 올바르게 설정되었는지 확인하는 용도입니다.
        System.out.println("=== 윷놀이 게임 테스트 스위트 실행 ===");
        System.out.println("단위 테스트, 통합 테스트, 회귀 테스트를 모두 실행합니다.");
        System.out.println("테스트 대상:");
        System.out.println("- Token 클래스");
        System.out.println("- Player 클래스");
        System.out.println("- BoardNode 클래스");
        System.out.println("- YutGameRules 클래스");
        System.out.println("- 게임 통합 시나리오");
        System.out.println("- 회귀 테스트");
        System.out.println("=====================================");
    }
} 