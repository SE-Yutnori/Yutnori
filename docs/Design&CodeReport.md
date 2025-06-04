설계 및 구현 리포트

설계

MVC 설계 개요
	•	흐름: View → Controller → Model → Controller → View
	•	중점 사항:
	•	Controller는 데이터 전달자 역할
	•	Model은 순수한 게임 로직 담당
	•	View는 입출력 담당
	•	Swing → JavaFX 전환 시 View만 수정되도록 구조화

각 계층 역할

Model
	•	GameState: 게임 상태(말의 위치, 턴 등) 저장 및 처리
	•	YutGameRules: 말 이동, 잡기, 업기 등 규칙 처리
	•	Token, Board, Player 등 핵심 게임 요소 포함
	•	이벤트 객체를 통해 변경 사항 전달

View
	•	Swing 또는 JavaFX 기반 UI
	•	사용자 입력 수집 및 시각화
	•	InGameView, BoardView 등 구성

Controller
	•	View로부터 사용자 입력 수신
	•	Model 메서드 호출 및 처리 결과 View에 전달
	•	Model과 View 간 완충 역할

⸻

구현

View 계층 인터페이스 일관성 유지

1. 공통 메서드
	•	refresh(), showMessage(), selectToken(), selectPath() 등
	•	Controller는 View의 구체 구현(Swing/JavaFX)과 무관

2. 이벤트 처리 흐름

// Swing
rollButton.addActionListener(e -> controller.rollingYut());

// JavaFX
rollButton.setOnAction(e -> controller.rollingYut());


⸻

필수 변경점: UI 프레임워크 API 대응

항목	Swing	JavaFX
UI 컴포넌트	JFrame, JPanel, JButton 등	Stage, Pane, Button, Alert 등
그리기	paintComponent(Graphics)	Canvas + GraphicsContext
다이얼로그	JOptionPane	Alert, TextInputDialog 등
이벤트	addActionListener	setOnAction
UI 스레드	SwingUtilities.invokeLater()	Platform.runLater()


⸻

변경 불필요: Model / Controller
	•	Model은 게임 규칙 처리만 담당 (View와 무관)
	•	Controller는 View 인터페이스만 사용
	•	결과: Swing → JavaFX 전환 시 Controller 및 Model은 수정 불필요

⸻

Use Case별 Sequence Diagram 요약
	1.	게임 설정 및 초기화
	•	보드 및 말 생성
	•	GameState와 Board 구성
	•	InGameView와 GameController 연결
	2.	말 이동 / 잡기 / 업기
	•	Token.move(), YutGameRules.applyMoves()
	•	TokenPositionManager 위치 갱신
	•	BoardView.refresh()로 화면 갱신
	3.	분기점 방향 선택
	•	사용자가 방향 선택
	•	GameController.chooseBranch()로 처리
	•	이후 이동 및 UI 갱신
	4.	윷 던지기
	•	반복 던지기 및 결과 재정렬
	•	사용자 입력으로 순서 재정렬
	•	remainingMoves에 결과 저장
	5.	게임 종료
	•	모든 말을 완주한 팀이 승리
	•	GameState.checkVictory()
	•	InGameView.getGameEndChoice()로 재시작/종료 선택

⸻

Class Diagram 및 구성 요소

(이 항목은 다이어그램 이미지 첨부 시 시각적으로 보완 가능)

핵심 클래스별 책임

Model
	•	Player: 토큰 보유 및 상태 관리
	•	Token: 이동, 스택, 상태 관리
	•	Board, BoardNode: 보드 구조
	•	GameState: 전체 상태 및 흐름 관리
	•	YutGameRules: 규칙 적용 및 이동 처리
	•	TokenPositionManager: 위치 저장 및 조회

View
	•	GameLauncher: 시작 화면
	•	InGameView: UI 구성, 입력 수집
	•	BoardView: 보드 시각화

Controller
	•	GameController: 흐름 제어, View-Model 연결

⸻

OOAD 적용

캡슐화
	•	모든 필드 private, 메서드로만 접근
	•	이벤트/결과 객체는 getter만 허용

⸻

SOLID 원칙 적용
	1.	SRP (단일 책임 원칙)
	•	클래스마다 책임 분리 (Token, InGameView 등)
	2.	OCP (개방 폐쇄 원칙)
	•	View만 교체하여 확장 가능
	3.	DIP (의존 역전 원칙)
	•	InGameView는 Runnable로 추상화된 콜백 사용

⸻

GRASP 원칙 적용
	•	Information Expert: Token, GameState, BoardNode
	•	Creator: GameController, GameState 등
	•	Controller: GameController
	•	Low Coupling / High Cohesion: 모든 클래스 대상 명확 적용

⸻
