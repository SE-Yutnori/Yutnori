package com.cas.yutnoriswing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YutGameRules 클래스 테스트")
class YutGameRulesTest {

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 테스트 모드를 기본값(false)로 리셋
        YutGameRules.setTestMode(false);
    }

    @Test
    @DisplayName("테스트 모드 설정 및 확인")
    void testTestMode() {
        // 초기 상태: 테스트 모드 비활성화
        assertFalse(YutGameRules.isTestMode());
        
        // 테스트 모드 활성화
        YutGameRules.setTestMode(true);
        assertTrue(YutGameRules.isTestMode());
        
        // 테스트 모드 비활성화
        YutGameRules.setTestMode(false);
        assertFalse(YutGameRules.isTestMode());
    }

    @Test
    @DisplayName("일반 모드 윷 던지기 - 여러 번 실행하여 랜덤성 확인")
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
    @DisplayName("테스트 모드 윷 던지기 - 항상 1 반환")
    void testThrowOneYut_TestMode() {
        // Given: 테스트 모드
        YutGameRules.setTestMode(true);
        
        // When & Then: 테스트 모드에서는 항상 1을 반환해야 함
        for (int i = 0; i < 10; i++) {
            int result = YutGameRules.throwOneYut();
            assertEquals(1, result, "테스트 모드에서는 항상 1을 반환해야 함");
        }
    }

    @Test
    @DisplayName("테스트 모드와 일반 모드 전환")
    void testModeSwitch() {
        // Given: 테스트 모드
        YutGameRules.setTestMode(true);
        assertEquals(1, YutGameRules.throwOneYut());
        assertEquals(1, YutGameRules.throwOneYut());
        
        // When: 일반 모드로 전환
        YutGameRules.setTestMode(false);
        
        // Then: 일반 모드 동작 확인
        assertFalse(YutGameRules.isTestMode());
        
        // 다시 테스트 모드로 전환
        YutGameRules.setTestMode(true);
        assertEquals(1, YutGameRules.throwOneYut()); // 테스트 모드에서는 1
    }

    @Test
    @DisplayName("윷 결과 값 범위 확인")
    void testYutResultRange() {
        YutGameRules.setTestMode(true);
        
        // 테스트 모드에서는 1만 나옴
        int result = YutGameRules.throwOneYut();
        assertEquals(1, result);
        
        // 일반 모드로 전환
        YutGameRules.setTestMode(false);
        
        // 여러 번 던져서 유효한 값들이 나오는지 확인
        for (int i = 0; i < 100; i++) {
            int normalResult = YutGameRules.throwOneYut();
            assertTrue((normalResult == -1) || (normalResult >= 1 && normalResult <= 5),
                "윷 결과는 -1(빽도) 또는 1-5(도,개,걸,윷,모) 범위여야 함, 실제: " + normalResult);
        }
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 스레드에서 윷 던지기")
    void testConcurrentYutThrow() throws InterruptedException {
        // Given: 일반 모드
        YutGameRules.setTestMode(false);
        
        // When: 여러 스레드에서 동시에 윷 던지기
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    int result = YutGameRules.throwOneYut();
                    results[index] = ((result == -1) || (result >= 1 && result <= 5));
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }
        
        // 모든 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then: 모든 스레드에서 유효한 결과가 나와야 함
        for (int i = 0; i < threadCount; i++) {
            assertTrue(results[i], "스레드 " + i + "에서 유효하지 않은 결과 발생");
        }
    }

    @Test
    @DisplayName("테스트 모드 상태 지속성 확인")
    void testTestModePersistence() {
        // Given: 테스트 모드 활성화
        YutGameRules.setTestMode(true);
        assertTrue(YutGameRules.isTestMode());
        
        // When: 여러 번 윷 던지기
        for (int i = 0; i < 10; i++) {
            YutGameRules.throwOneYut();
        }
        
        // Then: 테스트 모드 상태가 유지되어야 함
        assertTrue(YutGameRules.isTestMode());
        
        // 다음 윷 던지기 결과가 예측 가능해야 함
        assertEquals(1, YutGameRules.throwOneYut()); // 테스트 모드에서는 항상 1
    }

    @Test
    @DisplayName("연속된 윷 던지기 테스트")
    void testContinuousYutThrow() {
        YutGameRules.setTestMode(true);
        
        // 테스트 모드에서 100번 연속으로 던져서 모두 1이 나오는지 확인
        for (int i = 0; i < 100; i++) {
            int result = YutGameRules.throwOneYut();
            assertEquals(1, result, i + "번째 던지기에서 예상과 다른 값");
        }
    }

    @Test
    @DisplayName("정적 메서드 동작 확인")
    void testStaticMethodBehavior() {
        // 클래스 인스턴스화 없이 정적 메서드 호출 가능 확인
        assertFalse(YutGameRules.isTestMode());
        
        YutGameRules.setTestMode(true);
        assertTrue(YutGameRules.isTestMode());
        
        int result = YutGameRules.throwOneYut();
        assertEquals(1, result); // 테스트 모드에서는 1
    }
} 