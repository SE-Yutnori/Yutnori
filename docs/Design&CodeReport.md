# 설계 및 구현 리포트 구성

1. 윷놀이 판 디자인 및 게임 설정
- 보드 쉐이프를 설정
- 플레이어 수와 말 갯수를 설정
- 이때, 플레이어 수가 2명 이상이거나 4명 이하여야 함.
- 또한, 말 갯수가 2명 이상이거나 4명 이하여야 함.
- 이후 보드 생성

[VIEW]
GameLauncher.class
-> boardCustom(): 사용자 인풋 받아서 n각형 커스터마이징 입력 받음 (1)
-> getTestMode(): 사용자 인풋 받아서 테스트 모드 여부 입력 받음 (3)
-> getPlayers(): 사용자 인풋 받아서 플레이어 수 입력 받음 (5)
BoardPanel
-> new BoardPanel(board, players): 보드와 플레이어 수를 통해 보드 UI 생성 (6)
inGameView.class
-> new InGameView(boardPanel, players, statusPanel): 보드 UI, 플레이어 수, JPanel 통해 팝업 창 UI 생성 (7)
[CONTROLLER]
GameContoller.class
-> new GameController(players, inGameView, startNode): 플레이어 수, 인게임뷰, 윷놀이판 시작점을 통해 게임 컨트롤러 생성 (8)
[MODEL]
BoardBuilder.class
-> buildCustomizingBoard(sides, 2f): 사용자 인풋인 sides를 가지고 보드 생성 (2)
YutGameRules
-> setTestMode(testMode): 사용자 인풋인 testMode를 받고 테스트 모드/비테스트 모드 진행 (4)


2. 말 이동/말 업기/ 말 잡기
- 윷이 여러 개일 경우, 각 윷마다 특정 말을 선택하여 움직임
- 말 이동 중인 경우, 말을 업을 수 있을 때 업기
- 말 이동 중인 경우, 말을 잡을 수 있을 때 잡기
- 말을 잡은 경우, 윷을 던지기
[VIEW]
InGameView.class
-> selectToken(activeTokens, step): 나온 윷 숫자에 대해서 움직일 말을 선택하게 하는 메소드 (2)
[CONTROLLER]
GameController.class
-> nextPlayer(): 추가 턴이 없을 경우 다음 사용자에게로 넘김(만약 말을 잡을 경우 다시 한 번 더 진행)
[MODEL]
YutGameRules.class
-> applyMoves(currentPlayer, stepsList, startNode, view): 단계별로 토큰 이동 및 잡기, 업기 처리 (1)

3. 분기점 방향 선택
- 말 이동 중인 경우, 말이 분기점에 있을 때 분기점 선택
[MODEL]
Token.class
-> move(steps, branchSelect): 분기점 이동인지 확인한 후, 조건에 따란 분기점 선택 호출 (1)

4. 윷 던지기
- 도, 개, 걸, 윷, 모 중 윷을 던짐
- 윷, 모인 경우, 윷을 한 번 더 던질 수 있음
- 윷 사이즈가 2개 이상인 경우, 윷 결과를 정렬
[VIEW]
GameLauncher.class
-> JButton("윷 던지기"): 윷 던지기 버튼 표시 (1)
[CONTROLLER]
GameController.class
-> rollingYut(): 윷 던지기 기능 (2)
[MODEL]
YutGameRules.class
-> accumulateYut(currentPlayer, view): 윷 결과가 윷/모인지 도/개/걸인지 확인하고 윷 더 던질지 정해줌 (3)
-> reorderRsults(throwResults, currentPlayer.getName(), view): 윷 수만큼 움직일 말 결정(윷 결과가 2 이상일 경우 정렬 후 결정) (4)

5. 한 팀이 모든 말을 내보냈을 때 게임 종료
- 한 팀의 말들이 모두 도착한 경우 승리 화면을 띄움
- 재시작할 경우, 재시작
- 끝낼 경우 끝냄
[VIEW]
InGameView.class
-> getGameEndChoice(currentPlayer.getName()): 승리 시 게임 종료 화면 표시 및 종료/재시작에 대한 사용자 입력 받음 (2)
-> restartGame(), exitGame(): 재시작 및 종료 기능 (4)
[CONTROLLER]
GameController.class
-> 승리 조건 확인 (1)
[MODEL]
GameEndChoice.class
-> 종료 또는 재시작 속성 (3)
