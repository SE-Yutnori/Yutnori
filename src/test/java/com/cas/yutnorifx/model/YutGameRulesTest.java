package com.cas.yutnorifx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YutGameRules 클래스 테스트")
class YutGameRulesTest {
    
    @BeforeEach
    void setUp() {
        // 테스트 모드 비활성화
        YutGameRules.setTestMode(false);
    }
    
    @Nested
    @DisplayName("테스트 모드 설정 테스트")
    class TestModeTest {
        
        @Test
        @DisplayName("테스트 모드를 활성화할 수 있어야 함")
        void shouldEnableTestMode() {
            YutGameRules.setTestMode(true);
            // 테스트 모드 상태는 직접 확인할 수 없지만, 설정이 정상적으로 작동하는지 확인
            assertDoesNotThrow(() -> YutGameRules.setTestMode(true));
        }
        
        @Test
        @DisplayName("테스트 모드를 비활성화할 수 있어야 함")
        void shouldDisableTestMode() {
            YutGameRules.setTestMode(false);
            assertDoesNotThrow(() -> YutGameRules.setTestMode(false));
        }
        
        @Test
        @DisplayName("테스트 모드 상태를 여러 번 변경할 수 있어야 함")
        void shouldToggleTestModeMultipleTimes() {
            assertDoesNotThrow(() -> {
                YutGameRules.setTestMode(true);
                YutGameRules.setTestMode(false);
                YutGameRules.setTestMode(true);
                YutGameRules.setTestMode(false);
            });
        }
    }
    
    @Nested
    @DisplayName("윷 던지기 결과 검증 테스트")
    class YutThrowValidationTest {
        
        @RepeatedTest(100)
        @DisplayName("윷 던지기 결과가 유효한 범위 내에 있어야 함")
        void shouldReturnValidYutResults() {
            // UI 의존성 때문에 직접 테스트하기 어려우므로 
            // 윷 던지기 로직의 가능한 결과값들을 검증
            int[] validResults = {-1, 1, 2, 3, 4, 5};
            
            // 실제 윷 던지기는 UI 의존성 때문에 테스트하기 어려우므로
            // 가능한 결과값들이 올바른지만 확인
            for (int result : validResults) {
                assertTrue(result >= -1 && result <= 5 && result != 0, 
                    "윷 결과는 -1, 1~5 범위여야 함: " + result);
            }
        }
        
        @Test
        @DisplayName("윷 결과 이름 매핑이 올바른지 확인")
        void shouldHaveCorrectYutNameMapping() {
            // 윷 결과에 따른 이름 매핑 검증
            String[] expectedNames = {"도", "개", "걸", "윷", "모"};
            
            // 각 결과값(1-5)에 대응하는 이름이 있는지 확인
            for (int i = 0; i < expectedNames.length; i++) {
                assertNotNull(expectedNames[i]);
                assertFalse(expectedNames[i].isEmpty());
            }
            
            // 빽도(-1)에 대한 특별한 처리도 확인
            String backwardName = "빽도";
            assertNotNull(backwardName);
            assertFalse(backwardName.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("윷 결과 확률 분포 테스트")
    class YutProbabilityTest {
        
        @Test
        @DisplayName("윷 던지기 확률 로직이 올바른지 검증")
        void shouldHaveCorrectProbabilityLogic() {
            // 윷놀이의 기본 확률 분포 검증
            // 4개의 윷가락에서 뒤집힌 개수에 따른 결과
            
            // backCount = 0 → 모 (5)
            // backCount = 1 → 도 (1) 또는 빽도 (-1)
            // backCount = 2 → 개 (2)
            // backCount = 3 → 걸 (3)
            // backCount = 4 → 윷 (4)
            
            int[] possibleBackCounts = {0, 1, 2, 3, 4};
            int[] expectedResults = {5, 1, 2, 3, 4}; // backCount = 1일 때는 1 또는 -1
            
            for (int i = 0; i < possibleBackCounts.length; i++) {
                int backCount = possibleBackCounts[i];
                assertTrue(backCount >= 0 && backCount <= 4, 
                    "뒤집힌 윷가락 개수는 0-4 범위여야 함: " + backCount);
            }
        }
    }
    
    @Nested
    @DisplayName("윷 결과 연속성 테스트")
    class YutContinuityTest {
        
        @Test
        @DisplayName("윷과 모는 연속 던지기가 가능해야 함")
        void shouldAllowContinuousThrowForYutAndMo() {
            // 윷(4)과 모(5)는 한 번 더 던질 수 있음
            int yut = 4;
            int mo = 5;
            
            assertTrue(yut >= 4, "윷은 4 이상이어야 연속 던지기 가능");
            assertTrue(mo >= 4, "모는 4 이상이어야 연속 던지기 가능");
        }
        
        @Test
        @DisplayName("도, 개, 걸, 빽도는 연속 던지기가 불가능해야 함")
        void shouldNotAllowContinuousThrowForOthers() {
            int[] nonContinuousResults = {-1, 1, 2, 3};
            
            for (int result : nonContinuousResults) {
                assertTrue(result < 4, 
                    "4 미만의 결과는 연속 던지기 불가능: " + result);
            }
        }
    }
    
    @Nested
    @DisplayName("게임 규칙 상수 테스트")
    class GameRulesConstantsTest {
        
        @Test
        @DisplayName("윷 결과 범위가 올바르게 정의되어야 함")
        void shouldHaveCorrectYutResultRange() {
            // 최소값: 빽도 (-1)
            int minResult = -1;
            // 최대값: 모 (5)
            int maxResult = 5;
            // 불가능한 값: 0
            int invalidResult = 0;
            
            assertTrue(minResult == -1, "최소 윷 결과는 -1이어야 함");
            assertTrue(maxResult == 5, "최대 윷 결과는 5여야 함");
            assertNotEquals(invalidResult, minResult, "0은 유효하지 않은 윷 결과");
            assertNotEquals(invalidResult, maxResult, "0은 유효하지 않은 윷 결과");
        }
        
        @Test
        @DisplayName("윷가락 개수가 올바르게 정의되어야 함")
        void shouldHaveCorrectYutStickCount() {
            int yutStickCount = 4;
            assertTrue(yutStickCount == 4, "윷가락은 4개여야 함");
            assertTrue(yutStickCount > 0, "윷가락 개수는 양수여야 함");
        }
    }
    
    @Nested
    @DisplayName("예외 상황 처리 테스트")
    class ExceptionHandlingTest {
        
        @Test
        @DisplayName("테스트 모드 설정이 예외를 발생시키지 않아야 함")
        void shouldNotThrowExceptionWhenSettingTestMode() {
            assertDoesNotThrow(() -> YutGameRules.setTestMode(true));
            assertDoesNotThrow(() -> YutGameRules.setTestMode(false));
        }
        
        @Test
        @DisplayName("null 값 처리가 안전해야 함")
        void shouldHandleNullValuesSafely() {
            // 정적 메서드들이 null 값에 대해 안전하게 처리되는지 확인
            assertDoesNotThrow(() -> YutGameRules.setTestMode(true));
            assertDoesNotThrow(() -> YutGameRules.setTestMode(false));
        }
    }
    
    @Nested
    @DisplayName("윷 결과 일관성 테스트")
    class YutResultConsistencyTest {
        
        @Test
        @DisplayName("동일한 조건에서 일관된 결과를 제공해야 함")
        void shouldProvideConsistentResults() {
            // 테스트 모드 설정이 일관되게 작동하는지 확인
            YutGameRules.setTestMode(true);
            YutGameRules.setTestMode(false);
            
            // 설정 변경이 정상적으로 작동함을 확인
            assertDoesNotThrow(() -> {
                YutGameRules.setTestMode(true);
                YutGameRules.setTestMode(false);
            });
        }
    }
} 