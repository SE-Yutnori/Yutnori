테스트 리포트

테스트 환경
	•	프레임워크: JUnit 5

⸻

테스트 케이스 설명

1. PlayerTest - Player 클래스 단위 테스트

케이스	설명
testPlayerConstructor_Valid	생성자 동작과 속성 값 검증
testPlayerConstructor_TokenCount	유효/비유효 토큰 개수에 따른 생성자 로직 테스트
testHasFinished_NotAllFinished	일부 토큰만 FINISHED일 때 false 반환 확인
testHasFinished_AllFinished	모든 토큰이 FINISHED일 때 true 반환 확인
testGetMovableTokens	READY/ACTIVE 상태 토큰만 반환
testGetBackwardMovableTokens	ACTIVE 상태 토큰만 반환
testGetReadyTokens	READY 상태만 필터링
testGetActiveTokens	ACTIVE 상태만 필터링
testGetFinishedTokens	FINISHED 상태만 필터링
testGetTokenByName_ExistingToken	유효한 이름으로 토큰 검색 성공
testGetTokenByName_NonExistingToken	존재하지 않는 이름 검색 시 null 반환 확인
testResetAllTokens	모든 토큰 상태 리셋 및 스택 제거 확인
testTokenNamingPattern	“플레이어이름-번호” 형식으로 이름 생성 여부 확인


⸻

2. TokenTest - Token 클래스 단위 테스트

케이스	설명
testTokenConstructor	생성자 및 초기 상태 검증
testSetState	READY/ACTIVE/FINISHED 상태 변경 확인
testStackedTokenManagement	스택 추가/제거 및 중복 방지 테스트
testClearStackedTokens	업힌 말 전체 제거 확인
testNextBranchChoice	분기 노드 설정/해제 테스트
testPreviousNode	이전 노드 설정/해제 확인
testGetTopMostToken_*	최상위 토큰 반환 검증 (미업힘/단일스택/다중스택)
testTokenImmutableProperties	이름, 소유자 속성 불변성 확인
testRemoveNonExistentStackedToken	존재하지 않는 토큰 제거 시 영향 없음 확인
testTokenStateEnum	모든 TokenState 값 적용 가능 여부 확인


⸻

3. BoardTest - 다양한 보드 구조 검증

케이스	설명
testBoardConstructor_*	4각형, 5각형, 6각형 보드의 구조 및 명명 검증
testNodeNamingPattern	Edge{숫자}-{숫자} 패턴 확인
testNodeConnections	노드 간 연결 확인
testInitialEmptyTokens	생성 직후 모든 노드에 토큰 없음 확인


⸻

4. YutGameRulesTest - 게임 규칙 및 윷 던지기 검증

케이스	설명
testTestMode	테스트 모드 on/off 확인
testThrowOneYut_NormalMode	일반 모드에서 다양한 결과 발생 확인
testThrowYut_ContinuousThrow	윷/모일 때 연속 던지기 로직 확인
testMoveToken_*	정상 이동, 완주, 빽도, 잡기, 업기 처리 확인


⸻

5. InGameViewTest - 사용자 입력 처리 로직 검증

케이스	설명
testValidateReorderInput_ValidInput	유효한 순서 재배열 입력 통과 확인
testValidateReorderInput_InvalidInputs	잘못된 형식, 범위 초과, 개수 불일치 거부 확인
testValidateReorderInput_WhitespaceHandling	공백 처리 확인
testValidateReorderInput_DuplicateHandling	중복 입력 허용 여부 확인


⸻

6. Branch Selection Regression Test - 회귀 테스트

6.1. 사각형(4각형)
	•	testBranchPassthrough_Edge0~2
	•	testCenterPath_ToCenter1~2

6.2. 오각형(5각형)
	•	testBranchPassthrough_Edge0~3
	•	testCenterPath_ToCenter1~3

6.3. 육각형(6각형)
	•	testBranchPassthrough_Edge0~4
	•	testCenterPath_ToCenter1~3

각 테스트는 특정 위치에서의 이동 시 예상 도착 위치에 정확히 도달하는지 확인함.


⸻

테스트 의의
	•	단순 테스트의 한계
	•	생성자/getter/setter 중심 테스트는 구현 후 맞춰 작성하게 되어, 실질적인 품질 향상에는 기여도가 낮음.
	•	TDD 미적용으로 인해 설계 개선에 있어 테스트의 역할이 제한됨.
	•	회귀 테스트의 강점
	•	4각형 및 6각형 분기 로직의 오류를 자동으로 탐지하며, 직접 실행 검증의 한계를 보완함.
	•	블랙박스 회귀 테스트는 수동 테스트의 반복을 줄이고, 특정 시나리오를 자동화하여 디버깅 및 유지보수 시간을 절약.
	•	TDD의 필요성
	•	분기 선택 로직 개발 당시 TDD가 적용되었다면, Red → Green → Refactor의 자연스러운 흐름 속에서 효율적인 리팩토링과 검증이 가능했을 것으로 평가됨.
	•	한계점 및 개선 가능성
	•	GUI 기반 사용자 상호작용은 아직 테스트 자동화가 어려움.
	•	추후 입력 로직에 대한 low-coupling 구조 개선 및 mocking 적용으로 더 일반화된 회귀 테스트 구축 가능.
