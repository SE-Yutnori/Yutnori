[설계 및 구현 리포트]
설계
•	MVC 설계 개요
•	Use Case별 Sequence Diagram
•	Class Diagram
•	Model, View, Controller 내 중요 요소
•	모든 클래스 설계 과정
•	OOAD
•	SOLID 원칙
•	GRASP

구현
•	View 계층 인터페이스 일관성 유지
•	필수 변경점: UI 프레임워크 API 대응
•	변경 불필요: Model / Controller

 
설계
MVC 설계 개요
흐름: View → Controller → Model → Controller → View
해당 프로젝트를 설계함에 있어 중점을 둔 부분은 다음과 같다. Controller에게 데이터 전달자를 담당시키고 Model은 순수 게임 로직, View는 입출력 처리만을 담당하도록 해서 각각의 역할이 명확히 분리되도록 하였다. 이를 기반으로 View 계층을 Swing에서 JavaFX로 전환하더라도 수정 범위를 View에 국한시켜서 다른 계층에서의 변경을 최소화하였다.
Model: 게임의 상태와 로직을 관리하는 부분이다. 예를 들어 GameState 클래스는 말들의 위치, 턴 정보 등 게임 상태를 보관하고 윷 던지기나 말 이동 등의 게임 로직을 처리한다. 또한 YutGameRules와 같은 클래스는 말 이동 시 적용되는 게임 규칙(윷놀이 규칙: 이동 경로, 잡기 등)을 캡슐화한다. Model은 UI에 직접 접근하지 않고, 대신 게임 이벤트 객체(GameEvent)를 생성하여 변경 사항이나 요청 사항을 알리는 방식으로 동작한다.
View: JavaFX 및 Swing으로 구현된 사용자 인터페이스 부분이다. 말판, 말(token) 등의 그래픽 요소를 표시하고 사용자로부터 입력을 받아낸다. View는 Cotroller의 중개를 통해 UI를 업데이트하거나 사용자에게 추가 입력을 요청한다. View 자체는 게임 로직을 직접 수행하지 않고, 표시와 입력 역할에 집중한다.
Controller: 사용자 입력을 model에 전달하고 model의 결과를 view에 반영하도록 중개하는 역할이다. 주된 역할은 뷰로부터 전달된 사용자 액션을 받아 모델의 메서드를 호출하는 것이다. 또한 필요하다면 모델로부터의 이벤트를 받아 추가 로직을 수행하거나 (예: 턴 관리 등), 뷰를 업데이트하는 것을 도울 수 있다. 이를 통해 View와 Model이 서로 직접 통신하지 않으며, Controller가 양측을 연결하면서도 각각의 분리된 책임을 유지한다.
이러한 구조 덕분에 모델-뷰 간 결합도를 낮출 수 있다. 뷰는 모델로부터 받은 이벤트만으로 화면을 갱신하거나 사용자 입력 흐름을 제어한다. 한편 컨트롤러는 사용자 입력이 있을 때 모델의 메서드를 호출하여 모델을 변경하며, 이러한 변경으로 발생한 이벤트는 다시 뷰에 전달된다. 
Use Case별 Sequence Diagram
1. 윷놀이 판 디자인 및 게임 설정
개요: 게임을 시작하여 윷놀이 판을 커스타마이징하고 초기 설정을 수행하는 시나리오이다. 사용자가 게임 시작 옵션을 설정하면, 시스템은 윷놀이 판을 디자인하고 말들을 시작 위치에 배치하는 등 초기화 과정을 거친다. 이 과정에서 게임에 필요한 모든 Model 객체 생성 및 초기 상태 설정이 이루어지며, 최종적으로 초기 화면(UI)에 게임 판과 말들이 준비되었음을 보여준다.

1.1. 보드 빌드 및 GameState/Board 객체 생성
 
1) Main → GameLauncher.start() 호출
•	사용자가 시작하면, GameLauncher.start() 메서드가 JavaFX 애플리케이션 스레드에서 실행된다(GameLauncher).
2) GameLauncher → GameState 생성자 호출
•	GameLauncher 내부에서 new GameState를 실행하여 GameState객체를 만든다.
•	GameState 생성자는 곧바로 new Board를 호출한다.
3) Board → BoardNode 객체 생성 반복
•	Board의 initializeBoard() 메서드에서 createEdgeNodes(...) 및 createCenterPathNodes(...) 을 통해 다각형 보드의 외곽 노드(Edge0-0 ~ EdgeN-5)와 중앙 경로 노드(ToCenter..., Center)를 생성한다.
•	각 노드는 new BoardNode(name, x, y, sides) 로 생성되며, 내부 nodes 리스트에 추가된다.
4) 완성된 Board → GameState 반환
•	모든 BoardNode 생성을 마치면 startNode(예: Edge0-0)를 결정하고, Board 인스턴스가 GameState 생성자에 반환되어 저장된다.
•	이로써 보드 구조가 전부 준비된 상태로 GameState객체가 생성 완료된다.

1.2. 플레이어 및 토큰 생성
 

1) GameState → Player 생성자 호출
•	GameState 생성자에서, playerNames 리스트와 tokenCounts 리스트를 순회하며 각 플레이어를 new Player(name, tokenCount) 로 만든다.
•	Player 생성자는 토큰 개수를 검증(2~5 범위)한 뒤 내부 tokens 리스트를 초기화한다.
2) Player → Token 생성자 호출 반복
•	Player 생성자 안에서 for (int i = 1; i <= tokenCount; i++) { new Token(tokenName, this);}을 통해 각 토큰을 만든다(Player).
•	Token(String name, Player owner) 생성자 실행 시 Token 객체가 초기 상태(READY)로 만들어진다.
3) 완성된 Player 인스턴스 → GameState에 저장
•	각각의 Player 생성이 끝나면, GameState.players.add(player) 로 모든 플레이어가 GameState 내부 리스트에 담긴다.
•	이 시점까지 플레이어와 토큰 데이터가 모델 계층에 준비된 상태다.

1.3. 플레이어 및 토큰 생성
 
1) GameLauncher → InGameView 인스턴스 생성
•	GameLauncher에서 new InGameView(gameState.getBoard().getNodes(), gameState.getPlayers())를 호출하여 게임 화면(UI) 뷰를 초기화한다.
2) GameLauncher → GameController 생성자 호출
•	이어서 new GameController(gameState, inGameView) 를 통해 컨트롤러를 생성한다.
•	GameController 내부에서는 단순히 레퍼런스로 받은 gameState와 InGameView를 필드에 저장한다.
3) InGameView → 이벤트 리스너(setOnRollYut) 연결
•	InGameView.setOnRollYut(() → controller.rollingYut()) 로 “윷 던지기” 버튼 클릭 시 GameController.rollingYut() 이 호출되도록 연결한다.
4) GameLauncher 화면 종료
•	마지막으로 GameLauncher.this.dispose()를 호출하여 런처 윈도우를 닫고, 플레이어는 곧바로 InGameView에서 게임을 진행한다(GameLauncher).

1.4. BoardView 생성 및 refresh() 호출로 초기 보드 그리기
 
1) InGameView → BoardView 인스턴스 생성
•	InGameView 생성자에서 new BoardView(boardNodes, players) 를 호출하여, 화면 중앙에 보드를 그릴 BoardView (JavaFX Pane 상속) 객체를 만든다.
2) BoardView.refresh() 호출 → paintComponent() 내부 로직 수행
•	BoardView 생성자 내부에는 곧바로 refresh() 가 호출되어, 캔버스를 모두 지운 뒤(흰 배경)
o	for (BoardNode node : nodes) { ... } 루프를 통해 모든 노드의 좌표(node.getX(), node.getY())와 연결선(node.getNextNodes())을 그린다.
o	이때 분기점이나 중요한 노드(“Center”, “Edge0-0” 등)는 굵은 원으로 표시한다.
3) 토큰(Token) 정보 조회 후 그리기
•	BoardView에서 players 리스트를 순회하며 각 Player.getTokens() 를 조회하고, TokenPositionManager에서 관리 중인 위치(token.getCurrentNode()) 정보를 가져와서
•	해당 좌표에 토큰 이미지를 그린다.
4) 초기 게임판과 말 상태가 화면에 렌더링 완료
•	refresh() 메서드가 끝나면 BoardView 는 초기 보드와 모든 토큰을 화면 위에 그려주며, 사용자는 곧바로 “윷 던지기” 버튼을 눌러 다음 단계로 진행할 준비가 된 상태가 된다.

2. 말 이동 / 말 업기 / 말 잡기
개요: 플레이어의 말(token)을 이동시키는 시나리오로, 이동 과정에서 말 업기나 말 잡기가 발생하는 상황을 포함한다. 이 시나리오는 사용자가 윷 던지기 결과로 얻은 이동 거리만큼 말을 선택하여 옮기는 과정을 다루며, 게임 규칙에 따른 말의 상태 변화(업기 또는 잡기)까지 처리한다. 크게 UI에서 말을 선택하는 입력, Model의 게임 규칙 적용, View의 보드 갱신으로 구분하였다.

2.1 뷰에서 말 선택 → GameController.rollingYut() → 토큰 선택
 
1) InGameView.onRollYut() (뷰) 호출
•	사용자가 “윷 던지기” 버튼을 누르면, InGameView에서 등록해 둔 리스너(onRollYut)가 실행되어 GameController.rollingYut() 이 호출된다.
2) GameController에서 현재 플레이어 조회
•	GameController.rollingYut() 내부에서 GameState.getCurrentPlayer()를 통해 현재 순서인 Player 객체를 가져온다.
3) 모든 토큰 리스트 요청
•	이동 가능한 토큰 목록을 뷰에 전달하기 위해, GameController는 TokenPositionManager.getAllTokensOnBoard() 를 호출하여 현재 보드 위에 있는 모든 Token 객체를 얻는다.
4) 뷰에 토큰 선택 다이얼로그 표시
•	GameController는 InGameView.selectToken(tokens, steps)를 호출하여, 유효한(TokenState.READY/ACTIVE 상태) 토큰만 골라서 ‘말 선택’ 다이얼로그를 띄운다.
•	이 단계에서 GameController 역할은 종료(Deactivate)되고, 사용자는 이동할 토큰을 선택한다.

2.2. GameController → YutGameRules.applyMoves() → Token.move()
 
1) GameController → YutGameRules.applyMoves(...) 호출
•	GameController에서 토큰이 선택되면 applyMoves(Token token, int steps, TokenPositionManager manager)를 호출하여 이동 로직을 수행한다.
2) Token.getTopMostToken() (업힌 토큰 처리)
•	applyMoves 내부에서 token.getTopMostToken()을 호출하여, 업힌 토큰이 있다면 최상위(대표) 토큰을 가져온다.
3) Token.move(int distance) (실제 노드 이동 계산)
•	Token 클래스의 move(int distance) 메서드를 실행하며, 이 안에서
o	Token.getPreviousNode(), Token.getNextBranchChoice() 등을 참고해 분기점 처리
o	BoardNode next = computeNextNode(...)
o	TokenPositionManager.updateTokenPosition(token, next) (이전 노드 leave(), 새 노드 enter())
•	모든 이동이 끝나면 true 반환. YutGameRules.
4) YutGameRules → TokenPositionManager.updateTokenPosition(...)
•	토큰이 이동한 이후, 모델은 tokenManager.updateTokenPosition(token, newNode) 를 호출하여 내부 Map<Token, BoardNode> 을 갱신하고, BoardNode.leave(oldNode) & BoardNode.enter(newNode)를 수행한다.
5) YutGameRules → 토큰 상태 갱신
•	필요 시 token.setState(TokenState.ACTIVE) 혹은 FINISHED/READY 로 변경한다.
6) YutGameRules → GameController로 MoveResult 반환
•	이동 성패, 잡기(caught), 골인(finished), 메시지를 담은 MoveResult 객체를 리턴하며, GameController 쪽으로 흐름이 돌아간다.

2.3. YutGameRules 골인/업기/잡기 처리
 
1) Token이 위치한 BoardNode 확인
•	YutGameRules에서는 tokenManager.getTokenPosition(token)을 통해 현재 토큰이 위치한 노드를 얻는다.
2) 골인 지점인지 검사
•	만약 targetnode == null이라면, finishToken을 통해 완주 처리한다.
o	token.setState(TokenState.FINISHED)로 상태 변경
o	소유자 Player.incrementFinishedCount() 호출하여 완주 토큰 수 +1.
3) 같은 팀 토큰과 만난 경우 (업기)
•	현재 노드에 이미 같은 팀(Player)의 다른 Token 이 있다면,
o	otherToken을 노드에서 제거(node.leave(otherToken)) 후 token.addStackedToken(otherToken)
o	스택된 토큰은 실제 TokenPositionManager.updateTokenPosition(otherToken, null) 처리되어, 이동 불가 상태로 표시한다.
4) 다른 팀 토큰과 만난 경우 (잡기)
•	적 토큰(enemyToken) 이 노드에 있으면,
o	YutGameRules.resetToken(enemyToken, tokenManager) 호출
o	enemyToken.setState(TokenState.READY) → TokenPositionManager.updateTokenPosition(enemyToken, null) → enemyToken.clearStackedTokens()
o	업힌 토큰도 모두 초기화한다. 

2.4. GameController ← YutGameRules 반환 → 보드 갱신 → BoardView.refresh()

 

1) YutGameRules → GameController로 이동 결과 전달
•	YutGameRules.applyMoves(...) 처리 후, GameController 쪽으로 new MoveResult(success, caught, finished, message) 객체를 반환한다.
•	이때 반환된 MoveResult에는 이동 성공 여부, 상대 말을 잡았는지(혹은 잡혔는지), 골인 여부, 그리고 사용자에게 보여줄 메시지(message)가 담겨 있다.
2) 턴 변경 로직GameController.updateTurnOrder()
•	GameController.receiveMoveResult(MoveResult result) 내부에서, 먼저 gameState.updateTurnOrder() 또는 gameState.nextTurn()을 호출한다.
o	예를 들어 “윷/모”가 나와 추가 턴이 주어진 경우에는 같은 플레이어가 다시 이동하도록 하고, 그렇지 않으면 다음 플레이어로 순서를 넘긴다.
•	이 과정이 끝나면 GameController는 곧바로 화면을 갱신하라는 명령을 뷰에 전달한다.
3) 뷰 갱신 호출InGameView.refresh()
•	GameController는 inGameView.refresh()를 호출한다.
•	InGameView.refresh() 메서드 안에서는 내부적으로BoardView.refresh()를 호출하여 실제 보드판과 토큰을 다시 그리도록 한다.
4) BoardView.refresh() 단계별 세부 동작
•	캔버스 초기화
-	BoardView 클래스가 보유한 Canvas canvas 객체를 가져와, clearRect(0, 0, width, height)로 이전에 그려진 화면을 전부 지운다.
•	배경 그리기
-	흰색 배경으로 캔버스를 채워, 초기화된 화면 위에 보드와 토큰을 다시 그릴 준비를 한다.
•	모든 BoardNode를 순회하며 노드와 연결선 그리기
-	각 BoardNode에서 getNextNodes()를 통해 이어진 노드를 얻고, strokeLine(x1, y1, x2, y2)로 선을 그린다.
-	그 후 strokeOval(...)과 fillOval(...)으로 노드 원형(외곽선과 내부 채우기)을 그려서 보드판을 시각화한다.
•	모든 플레이어 토큰 순회하여 토큰 원형 및 스택 수 표시
-	TokenPositionManager.getTokenPosition(token)을 통해 토큰의 현재 위치인 BoardNode를 얻는다.
-	해당 좌표에 fillOval(...)으로 토큰 원형을 그리며, 토큰이 다른 토큰 위에 올라가 있으면(hasStackedTokens()) strokeText(...)로 스택 수를 표시한다.
5) 이동 결과 메시지 출력
	BoardView.refresh()가 완료되어 보드와 토큰이 다시 렌더링되면, GameController는 inGameView.showMessage(result.getMessage())를 호출한다.

1.	분기점 방향 선택
개요: 윷놀이 말이 보드의 분기점에 도달했을 때 플레이어가 진행 방향을 선택하는 상황을 다룬다. 갈래가 나뉘어지는 지점에서 플레이어는 말이 앞으로 어느 방향으로 진행할 지 결정해야 한다. 해당 시나리오에서는 사용자와 시스템 간의 상호작용이 한 번 더 필요하므로 추가적인 사용자 입력이 필요하다.

3.1. 뷰에서 분기점 선택 → GameController.chooseBranch()
 
1) InGameView.chooseBranch(Token token, String direction) (뷰) 호출
•	분기점에 도달한 Token을 화면에 표시한 뒤, “직진”과 “지름길” 버튼에 각각 액션을 연결해 두고,
•	사용자가 원하는 방향을 클릭하면 GameController.chooseBranch(token, direction)가 호출된다.
•	(실제 프로젝트 코드는 InGameView 클래스 내부에서 btnStraight.setOnAction(e -> controller.chooseBranch(token, "STRAIGHT")); 등으로 등록되어 있음) InGameView
2) GameController에서 버튼 비활성화
•	GameController.chooseBranch(...)가 호출되면, 우선 InGameView.disableBranchButtons()를 호출하여 “직진/지름길” 버튼을 비활성화하고,
•	동일한 토큰에 대한 중복 선택을 방지한다.
•	이후 실제 분기 로직 (YutGameRules) 호출 준비를 한다.

3.2. GameController → YutGameRules 분기 로직 → Token.move()
 
1) GameController.moveToken(...) 호출
•	사용자가 분기점에서 방향을 선택하면, GameController는 실제 이동을 수행하기 위해 다음과 같이 호출한다.
 
•	여기서 세 번째 인자인 options -> view.selectPath(options) 람다는, 분기점에 도달했을 때 options(List<BoardNode>)를 InGameView.selectPath(...)에 넘겨 사용자는 선택한 분기 노드를 반환받는다.
2) GameState.moveToken(...) → YutGameRules.moveToken(...) 전달
•	GameState.moveToken(Token token, int steps, Function<List<BoardNode>, BoardNode> branchSelector) 내부에서는 대표 토큰을 구해 YutGameRules.moveToken(...)으로 모든 이동 로직을 위임한다.
3) YutGameRules.moveToken(...) 내부의 분기점 처리
•	YutGameRules.moveToken(Token token, int steps, TokenPositionManager manager, Function<List<BoardNode>, BoardNode> branchSelector) 는
•	현재 위치(currentNode)에서 currentNode.getNextNodes()를 구한다.
•	이동 중 분기점( currentNode.getNextNodes().size() > 1 )에 도달하면 branchSelector.apply(nextNodes) 를 호출해 사용자가 선택한 BoardNode를 얻는다.
•	그 BoardNode 로 토큰을 보내기 위해 TokenPositionManager.updateTokenPosition(token, chosenNode) 를 호출한다.
•	이어서 잡기/업기/골인 처리를 모두 수행한 뒤, new MoveResult(...) 객체를 만들어 반환한다.
4) MoveResult 반환 후 GameController로 복귀
•	YutGameRules.moveToken(...)이 끝나면, GameState.moveToken(...)이 최종 MoveResult result를 GameController로 반환한다.
•	이 결과에는 성공 여부, 잡힘 여부, 골인 여부, 메시지 등이 담겨 있으며, 컨트롤러에서는 이를 바탕으로 UI 메시지를 띄우거나 턴을 변경한다.

3.3. GameController ← YutGameRules 반환 → 보드 갱신 → BoardView.refresh()
 
1) YutGameRules → GameController 반환
•	지름길 혹은 직진으로 토큰 이동이 모두 끝나면, continueMoveStraight(...)/continueMoveShortcut(...) 메서드는 아무 값도 반환하지 않고(void), GameController 쪽으로 흐름이 돌아온다.
2) 턴 변경 로직 (GameController.updateTurnOrder())
•	GameController.chooseBranch(...) 내부에서, 분기 이동이 끝나면 gameState.updateTurnOrder()를 호출하여 다음 플레이어를 설정한다.
•	“윷/모”로 인해 추가 이동 기회가 주어졌다면 같은 플레이어가 다시 턴을 가져간다. 그렇지 않으면 다음 플레이어로 순환한다.
3) 뷰 갱신 호출 (InGameView.refresh())
•	턴 변경이 끝나면 GameController는 inGameView.refresh()를 호출한다.
•	내부에서 BoardView.refresh()를 실행하여, 캔버스를 지우고 보드와 토큰을 다시 그린다.
4) BoardView.refresh() 단계별 세부 동작
•	이전과 동일하게 Canvas.clearRect(...)로 캔버스를 초기화하고, 흰색 배경을 채운 뒤 모든 BoardNode를 순회하며 연결선, 원 등을 그린다.
•	그다음 각 Player의 Token을 TokenPositionManager.getTokenPosition(token)으로 위치를 가져와 drawToken(...)으로 그린다.
•	만약 토큰이 스택된 상태라면, drawStackCount(...)로 스택 수를 표시한다.


2.	윷 던지기
개요: 플레이어가 윷을 던지고 결과를 처리하는 과정을 다룬다. 윷 던지기 결과로 이동할 수 있는 칸 수가 결정되며 (예: 도, 개, 걸, 윷, 모, 빽도 등..), 경우에 따라 추가 턴이 주어지기도 한다. 이 시나리오에서는 사용자가 윷 던지기 UI를 조작하는 입력부터 Model의 랜덤 로직을 통한 결과 산출, View에 결과 표시까지의 흐름을 포함한다.

4.1. 뷰 → 컨트롤러: rollingYut() 호출 및 누적 던지기
 
1) InGameView.rollingYut() 호출
•	뷰의 “윷 던지기” 버튼을 누르면 GameController.rollingYut()가 실행된다.
2) GameController.getCurrentPlayer() 호출
•	GameController.rollingYut() 내부에서, 로 현재 턴인 Player 객체를 가져온다.
3) 반환된 YutThrowResult
•	YutThrowResult 객체에는 List<Integer> results (예: [5, 1]), List<String>resultMessages (예: ["모", "도"]) 가 담겨 있고, 컨트롤러로 돌아온다.
4) GameController.showYutThrowMessages(...) 호출
•	컨트롤러는 view.showYutThrowMessages(messageList)를 호출하여,
•	뷰는 messageList의 항목을 순서대로 화면에 “모”, “도” 등의 텍스트 또는 아이콘으로 표시한다.

4.2. GameController: 누적 던지기 결과를 받고 reorderResults 호출 → GameState에 저장 → 뷰에 표시
 
1) 현재 플레이어 가져오기
•	rollingYut()에서 gameState.getCurrentPlayer() 를 호출하여 Player currentPlayer를 얻는다.
2) handleNormalModeThrows(currentPlayer, allResults)
•	내부 while(continueThrow) 루프를 돌며,
•	int result = YutGameRules.throwSingleYut().getResults().get(0) 로 단일 윷 던지기 결과를 받고,
•	view.showMessage(...) 로 즉시 결과 혹은 추가 턴에 대한 메시지를 뷰에 보여준다.
•	“윷(4)” 또는 “모(5)”면 continueThrow = true로 다시 반복, 그렇지 않으면 반복 종료.
•	allResults에 던진 결과를 순서대로 저장한다.
3) 누적된 allResults 판단
•	만약 allResults.size() == 1(한 번만 던진 경우: 도(1), 개(2), 걸(3), 빽도(−1))라면, 곧바로 orderedResults = allResults 로 설정하고 건너뛴다.
•	allResults.size() > 1(두 번 이상 던진 경우: “모+도” 또는 “윷+…” 같은 조합)라면, handleReorderResults(allResults, playerName)를 호출하여 사용자 입력으로 순서를 재정렬 받는다.
 
4) handleReorderResults(...) 내부 흐름
•	ReorderRequest request = new ReorderRequest(results, playerName); 를 만들고,
•	while(true) 루프:
o	input = view.requestInput(request.getPromptMessage(), "윷 순서 재배열"); 로 뷰에 팝업 입력창을 띄워 사용자에게 “[도(1), 모(5)] 중 이동 순서를 입력해주세요. (예: 1,5)”를 요청한다.
o	InGameView.validateReorderInput(input, results) 로 사용자가 입력한 문자열("1,5", "5,1")을 검증하여 성공 시 정렬된 List<Integer>를 반환 ([1,5] 또는 [5,1]).
o	실패(문자열 형식 오류 등)하면 view.showError(...)로 에러 메시지를 뷰에 띄우고 다시 입력 대기한다.
o	취소(input == null)하면 handleReorderResults가 null을 반환하여 최종 rollingYut() 자체를 종료한다.
5) gameState.setRemainingMoves(orderedResults)
•	orderedResults를 게임 상태에 저장하여, 이후 “말 이동” 단계에서 이 순서대로 step 값을 꺼내 쓸 수 있게 한다.

4.3. GameState.getRemainingMoves()로 순서를 가져와서 말 이동 반복 → endTurn() 
1) gameState.getRemainingMoves()
rollingYut() → handleNormalModeThrows() → handleReorderResults()를 거쳐
최종 orderedResults가 gameState.remainingMoves에 저장되어 있다.
•	여기서 remainingMoves 리스트를 가져온다. 예: [1,5], [4,2,3] 등.
2) step = remainingMoves.remove(0)
•	리스트의 첫 번째 요소(step)를 꺼낸다. 예: step = 1.
•	remainingMoves는 그 뒤에 남은 칸수만을 포함하게 업데이트된다 (예: [5]).
3) gameState.getMovableTokens(step)
 
•	step ≥ 0 → FINISHED 상태가 아닌 모든 토큰(READY, ACTIVE) 반환
•	step < 0→ ACTIVE 상태 토큰만 반환
4) 뷰에 “이동할 말 선택” 요청 (promptSelectToken)
•	view.promptSelectToken(availableTokens)를 호출하여,
o	화면의 토큰을 클릭 가능한 상태로 하이라이트하거나
o	다이얼로그로 “이동할 말 선택” 리스트를 보여준다.
•	사용자가 토큰을 선택하면 view는 onTokenSelected(selectedToken) 콜백으로 GameController에 반환한다.
selectedToken == null이면 “취소”로 간주하고 해당 step 이동을 건너뛴다.
5) gameState.moveToken(...) 호출
•	실제 토큰 이동 로직 수행.
 
6) view.refresh()
•	현재 보드의 상태(BoardNode 연결 구조, 모든 토큰들의 새 위치)를 기반으로 캔버스를 다시 렌더링한다.
7) 남은 이동(remainingMoves) 검사

5. 한 팀이 모든 말을 내보냈을 때 게임 종료
개요: 게임 종료 조건을 만족했을 때 승자를 결정하고 게임을 종료하는 시나리오이다. 윷놀이 게임에서는 한 팀의 모든 말이 결승 지점(골인 지점)을 통과하여 판을 떠나면 그 팀이 승리하며 게임이 끝난다. 본 시나리오는 특정 팀의 말 완주 확인부터 게임 종료 처리 및 결과 표시까지의 흐름을 포함한다. 일반적으로 말 이동 후에 종료 조건을 체크하며, 조건 만족 시 더 이상의 턴을 중지하고 최종 결과를 보여준다.

5.1. GameController → GameState.checkVictory
 

1) GameController → GameState.checkVictory(currentPlayer) 호출
•	GameController는 말 이동(gameState.moveToken(...))과 view.refresh()가 끝난 직후, 다음과 같이 현재 플레이어가 모든 말을 FINISHED 상태로 만들었는지 확인하기 위해 checkVictory(...)를 호출한다.
 
2) GameState.checkVictory(...) 내부
•	Player.hasFinished()가 true면,
o	내부 필드 phase를 GamePhase.FINISHED로 변경
o	winner 필드에 해당 Player 객체를 저장
o	true를 반환
•	그렇지 않으면 false를 반환한다.
3) 돌려받은 결과가 true인 경우
•	GameController는 즉시 handleGameEnd(currentPlayer)를 호출하여 “게임 종료 → 승리자 화면”으로 분기한다.
•	이때 return하여 현재 루프(턴 진행 로직)를 중지한다.
4) 결과가 false인 경우
•	승리자가 없으므로 아무런 추가 동작 없이 다음 턴으로 이어진다.

5.2. GameController.handleGameEnd → InGameView.getGameEndChoice 
1) GameController.handleGameEnd(...) 진입
•	checkVictory(currentPlayer)가 true일 때, handleGameEnd(currentPlayer)를 호출한다.
2) GameState.getWinner() 호출
•	화면에 보여줄 승자 이름을 얻기 위해 gameState.getWinner()를 호출하여,
Player winner 객체를 가져온다.
3) InGameView.getGameEndChoice(...) 호출
•	GameController는 view.getGameEndChoice(winner.getName() + " 승리!")를 호출한다.
•	이 메서드는 JavaFX 를 띄워,
o	"플레이어A 승리!" 메시지를 띄우고
o	“다시 시작”과 “종료” 버튼 중 하나를 보여준다.
•	사용자가 클릭한 버튼에 따라, GameEndChoice.RESTART 혹은 GameEndChoice.EXIT 값이 choice 변수로 반환된다.
4) 사용자 입력(choice)을 GameController로 반환
•	이때 GameController는 반환 받은 choice 값을 바탕으로 다음 단계(재시작 또는 종료) 로직을 결정한다.

5.3. GameEndChoice에 따라 재시작 or 애플리케이션 종료
 
1) “다시 시작(RESTART)” 선택 시
•	handleGameEnd(...) 내부에서 반환된 choice가 GameEndChoice.RESTART라면, 초기 설정 화면으로 돌아가면서 새 게임을 시작한다.
2) “종료(EXIT)” 선택 시
•	choice가 GameEndChoice.EXIT라면, 애플리케이션 전체 프로세스가 종료된다.


Class Diagram
 

 
Model, View, Controller 내 중요 요소
MVC 아키텍처에 따라 View, Model, Controller 각 계층별로 핵심 클래스를 정리하고, 주요 메서드와 해당 계층의 역할 및 다른 계층과의 상호작용 방식을 설명한다. 이 구조는 앞서 언급했듯이 각 계층이 자신의 책임만을 수행하도록 설계하였다. 이는 분리를 바탕으로 UI를 Swing에서 JavaFX로 교체할 시에 View계층에서의 코드 수정을 최소화하기 위함이다.

1. View 계층
1.1. InGameView
 
InGameView 클래스는 게임 화면을 구성하고, 사용자 행동(윈도우 버튼 클릭, 토큰 선택, 분기 선택 등)에 대한 입력을 받아 컨트롤러에게 전달하며, 토큰 위치·상태 변경 결과를 시각적으로 보여 주는 역할을 담당한다.
1)	보드(Board) 및 토큰(Token) 렌더링
o	내부에 BoardView나 캔버스(canvas) 컴포넌트를 포함하여, 현재 GameState가 알려주는 토큰 위치 정보를 그린다.
o	토큰 이미지나 말판 이미지 위에 토큰의 좌표를 매핑하여 표시하고, 업혀 있는 토큰들은 계층 구조에 맞게 겹쳐서 렌더링한다.
2)	사용자 입력 처리 (버튼, 분기 선택 등)
o	윷 던지기 버튼 클릭: 사용자가 “윷 던지기” 버튼을 누르면 onRollYut 콜백을 트리거하여 실제 던지기 로직을 실행하도록 한다.
o	토큰 클릭/선택: 사용자가 본인의 말들 중 선택한다면 GameController에게 해당 토큰을 이동할 예정이라고 알린다.
o	분기점 선택 다이얼로그: 만약 현재 이동해야 할 토큰이 분기점(갈림길)에 위치해 있다면, 미리 정의된 콜백(onBranchSelected)을 통해 선택 가능한 경로 목록을 보여주고, 사용자가 하나를 선택하면 그 결과를 컨트롤러에 전달한다.
o	플레이어가 직접 던진 윷 결과들에 대한 원하는 배열을 제출하면 해당 목록을 실제 던진 결과와 비교해 동일한 멀티셋인지, 범위가 올바른지 확인하고, 이상이 없으면 재배열된 리스트를, 문제 있으면 에러 메시지를 담아 반환한다.
o	
3)	상태 패널
o	화면 우측에 플레이어 및 말 등의 게임 정보를 보여준다.
4)	게임 초기화 및 종료 뷰 관리
o	initialize() 메서드를 통해, 새 게임이 시작될 때 보드판과 토큰 초기 위치를 설정한다.
o	게임이 끝나면 clearBoard() 등의 메서드를 호출하여, 화면을 초기화하고 “재시작/종료” 선택 창을 띄워준다.

2. Model 계층
2.1. YutGameRules
 
YutGameRules 클래스는 윷놀이에서 윷 던지기 결과 산출부터 토큰 이동 및 그에 따른 잡기·업기·완주 처리까지, 실제 게임 규칙 전반을 구현하는 모듈이다.
1)	윷 던지기 결과 계산
o	실제 난수를 기반으로 “도·개·걸·윷·모·빽도” 결과를 산출하고,
o	“윷이나 모가 나오면 한 번 더 던진다”는 룰에 따라 반복 던지기를 처리하여 최종 결과 목록과 메시지를 반환한다.
2)	토큰(Token) 이동 로직 (moveToken, moveTokenBackward)
o	moveToken은 파라미터로 받은 토큰과 이동할 칸 수(–1~5), 분기 경로 결정자(branchSelector)를 이용해, 현재 위치에서부터 한 칸씩 전진하거나 후진하며 최종 도착 지점을 계산한다. 먼저 token.getTopMostToken()으로 실제 움직일 대표 토큰을 정하고, 상태가 ACTIVE인지 확인한다. 그 뒤 calculateTargetNode를 호출해, 각 칸마다 current.getNextNodes()를 조회하고 분기점에서는 branchSelector로 경로를 선택하여 목표 노드를 결정한다. 만약 분기점이 아닌 단일 경로라면 자동으로 한 칸씩 다음 노드를 따라 이동하고, 보드 끝을 만나면 targetNode는 null이 되어 곧바로 완주 처리(finishToken)로 넘어간다.
o	목표 노드(targetNode)가 null이 아닐 때는, 현재 위치(currentNode)에서 leave(actualToken)로 토큰을 제거한 뒤, enter(actualToken)로 목표 노드에 올리고 tokenManager.updateTokenPosition으로 위치 정보를 갱신한다. 이때 업힌 토큰(stackedTokens)이 있다면, 대표 토큰과 같은 노드로만 위치 정보만 변경한다. 이후 handleCaptureAndStacking을 호출해 해당 노드 위에 존재하는 다른 플레이어 소유 토큰은 resetToken으로 잡아 출발점으로 돌려보내고, 같은 소유 토큰은 addStackedToken으로 대표 토큰 아래로 올려 업음으로써 잡기·업기 기능을 모두 수행한다. 마지막으로 MoveResult 객체를 반환해 “이동 성공 여부(success), 잡기 여부(catched), 완주 여부(finished), 메시지”를 컨트롤러에 전달한다.
o	moveTokenBackward는 빽도(–1칸) 결과가 나왔을 때 호출하는 메서드로, 동작 원리는 moveToken과 유사하나 calculateTargetNode 대신 current.getPreviousNodes()를 따라 한 칸씩 뒤로 이동한다. 이후에도 목표 노드가 없다면(출발점보다 뒤로 더 갈 수 없으면 이동 실패), 아니면 앞서 설명한 대표 토큰 제거→목표 노드 입장→위치 갱신→업힌 토큰 동기화→잡기·업기 처리 순으로 진행하여 MoveResult를 리턴한다.
o	이렇게 moveToken과 moveTokenBackward 두 메서드를 통해 “한 칸씩 이동하면서 분기점에서는 branchSelector를 호출해 다음 노드를 선택하고, 보드 끝이나 출발점까지 가면 완주·실패 처리를 한다”는 핵심 로직이 완결된다.
3)	잡기 및 업기 처리 (handleCaptureAndStacking)
o	이동을 완료한 토큰이 도착한 노드에 올라와 있는 다른 토큰들을 검사하여
	다른 플레이어 소유면 resetToken을 호출해 해당 토큰을 출발점으로 되돌리고(잡기),
	같은 플레이어 소유면 해당 토큰을 현재 대표 토큰 아래로 쌓아 업는 처리를 수행한다.
o	잡기 여부를 리턴해 상위 로직에서 한 번 더 던지기 등 추가 행동을 결정할 수 있도록 돕는다.
4)	결과 객체 반환
o	YutThrowResult: 던진 윷 결과 목록과 메시지 리스트를 담아 반환한다.
o	MoveResult: 이동 성공 여부(success), 잡기 발생 여부(catched), 완주 여부(finished), 그리고 출력할 메시지를 담아 반환한다.
o	이를 통해 다른 모듈은 복잡한 내부 로직 없이도 무슨 일이 일어났는지만 확인해서 다음 흐름을 이어갈 수 있다.

2.2. GameState
 	
GameState 클래스는 게임 전체의 진행 흐름과 상태를 한 번에 관리하는 핵심 책임을 가진다. 즉, 개별 말 이동이나 룰 적용은 YutGameRules에 위임하되, 누구의 턴인지, 남은 이동 횟수가 얼마인지, 현재 어떤 단계에 있는지, 그리고 최종 승리자가 누구인지 등을 추적·판단하는 기능을 담당한다.
1)	플레이어 목록과 순서 관리
o	게임에 참여한 Player 객체들을 List<Player> players로 보관하고, 매 턴마다 currentPlayer를 갱신하여 누구 차례인지를 관리한다.
2)	보드(Board) 및 말 위치(TokenPositionManager) 관리
o	실제 윷놀이판을 표현하는 Board 객체와, 각 토큰들이 보드 위 어느 칸에 있는지를 기록·조회하는 TokenPositionManager를 한 곳에서 참조하여, 말이 이동할 때마다 위치를 업데이트하거나 잡기/업기 상황을 판단할 수 있도록 돕는다.
3)	게임 단계 관리
o	GamePhase enum을 통해 아직 시작 전인지, 진행 중인지, 혹은 이미 종료되었는지를 상태 변수 GamePhase로 표시한다. 예를 들어 FINISHED 단계일 때는 더 이상 윷 던지기나 이동 명령을 받지 않도록 막아준다.
4)	남은 이동 횟수(Remaining Moves) 추적
o	한 턴 동안 윷을 던져서 나온 여러 회차(윷·모가 나와서 한 번 더 던짐)를 List<Integer> remainingMoves에 보관해 두고, 플레이어가 움직여야 할 연속된 칸 수를 차례대로 꺼내 사용하도록 관리한다. 이 데이터가 바닥나면 “턴 종료 → 다음 플레이어로 넘어감”을 자동으로 판단하게 된다.
5)	승리자 판단(Win Condition)
o	이동 도중 혹은 턴 종료 시점마다 어떤 플레이어가 모든 말을 완주했는지를 감시하여, 최종적으로 Player winner 필드에 이긴 플레이어 객체를 저장한다. 이후 게임 단계(phase)를 FINISHED로 바꾸어, 더 이상 턴을 진행할 수 없음을 Controller에 알려준다.

2.3. Token
 
Token 클래스는 각 플레이어가 사용하는 말 객체 하나를 나타내며, 게임 진행 중 말이 가질 수 있는 모든 상태(활성/비활성/완주), 업기 여부, 분기 선택 정보 등을 전부 관리한다. 이 클래스를 통해 컨트롤러는 어떤 토큰이 어디에 있고, 누구 소유이며, 업혀 있는 말은 어떤 것인지를 쉽게 파악하고 조작할 수 있다.
1)	말 식별 정보 관리
o	Name 필드를 통해 말 하나를 고유하게 구분한다.
o	owner (Player 객체)를 통해 이 말이 어느 플레이어에게 속해 있는지 저장하여, 잡기·업기 처리 시 소유자를 구별할 수 있다.
2)	업기 정보 관리
o	stackedTokens: List<Token> 리스트에, 이 토큰 위에 업혀 있는 모든 하위 토큰을 저장한다.
o	addStackedToken(Token t) 메서드를 통해 다른 토큰을 현재 대표 토큰(“top-most”) 아래로 쌓을 수 있으며, getStackedTokens()로 업힌 말 목록을 조회한다.
o	업힌 말이 대표 토큰과 함께 이동하거나 완주 처리될 때, 재귀적으로 함께 움직이도록 설계되어 있다.
3)	대표 토큰 결정
o	getTopMostToken() 메서드는 현재 이 토큰이 만약 다른 토큰 밑에 업혀 있다면, 최종적으로 보드 위에서 실제로 움직일 대표 토큰이 무엇인지를 찾아 반환한다. 소유자(owner)가 가진 모든 토큰을 순회하며, 자신이 stackedTokens 목록에 포함된 토큰이라면 그 위의 대표 토큰을 재귀적으로 찾아 올라간다.
o	이를 통해 토큰 이동 명령이 들어왔을 때 실제 보드 위에서 움직여야 할 토큰을 손쉽게 식별할 수 있다.
4)	분기 선택 및 이전 노드 정보 저장
o	nextBranchChoice: BoardNode 필드는, 분기점에 진입했을 때 다음 이동 구간으로 어느 노드를 선택했는지를 기억한다.
o	previousNode: BoardNode 필드는, 직진할 때 이전에 머물렀던 노드를 저장한다.
Getter/Setter 및 편의 메서드 제공
o	getName(), getOwner(), getState(), getStackedTokens(), getNextBranchChoice(), getPreviousNode() 등 다양한 접근자(Accessor)로 내부 데이터를 안전하게 읽는다.
o	setState(TokenState newState), setNextBranchChoice(BoardNode node), setPreviousNode(BoardNode node) 등의 설정자(Mutator)로 상태나 분기 정보를 업데이트한다.
o	clearStackedTokens() 메서드는 완주 시점이나 리셋이 필요할 때 업힌 토큰 목록을 비워준다.

3. Controller 계층
3.1. GameController
 
게임 진행 제어의 중심 클래스로, 한 판의 윷놀이 게임에 필요한 상태 관리와 흐름 제어를 담당한다. 그러나 게임 규칙의 실제 판단이나 데이터 변경 로직은 모두 Model 클래스들에 위임하며, GameController 자체는 그 호출 순서와 흐름을 관리한다.
1)	윷 던지기 제어 및 결과 수집
o	rollingYut() 메서드를 통해 사용자가 “윷 던지기” 버튼을 눌렀을 때 호출된다.
o	테스트 모드 혹은 일반 모드 여부에 따라 YutGameRules.throwSingleYut()을 반복 호출하여 “도·개·걸·윷·모·빽도” 결과를 수집하고, 결과마다 뷰에 메시지를 띄워준다.
2)	획득한 이동 결과 목록(steps 리스트) 전개 및 토큰 이동 루프
o	던진 윷의 결과가 리스트 형태로 누적되면, 그 순서대로 하나씩 꺼내어 토큰을 움직일 차례를 실행한다.
o	각 결과마다 handleMoveExecution() 메서드를 호출하여 “현재 플레이어가 이동 가능한 토큰을 선택 → 분기점이면 경로 선택 → 실제 이동(모델 호출) → 뷰 업데이트”를 반복한다.
o	토큰 이동 시 발생하는 잡기(isCatched) 여부나 완주(isFinished) 여부를 확인한다. 잡기가 발생하면 추가 턴에 관한 메시지를, 완주가 발생하면 곧바로 handleGameEnd()를 호출하여 승리자 판별과 재시작/종료 선택 창을 뜨게 한다.
3)	토큰 선택 및 분기 선택 중재
o	이동 차례가 돌아온 플레이어는 view.selectToken(...)을 통해 자신이 움직일 토큰을 직접 골라야 한다.
o	만약 현재 토큰의 위치가 분기점(BoardNode.getNextNodes().size() > 1)이라면, 컨트롤러는 view.selectPath(options)를 호출해 사용자가 어느 방향으로 갈지 선택하도록 한 뒤,
o	선택된 경로를 토큰의 setNextBranchChoice(...)에 저장하여, 모델이 해당 분기 경로를 따르도록 한다.
4)	모델(GameState/YutGameRules) 호출 및 상태 동기화
o	실제 “토큰을 한 칸씩 움직여야 할지, 잡기·업기·완주 여부는 무엇인지” 같은 구체적인 룰 처리는 모두 YutGameRules.moveToken(...)에 위임이다.
o	컨트롤러는 GameState를 통해 현재 각 토큰의 위치(TokenPositionManager)를 조회하거나 업데이트하고,
o	YutGameRules로부터 반환된 MoveResult(성공 여부, 잡기 여부, 완주 여부, 메시지 등)를 받아서 다음 화면 갱신 또는 턴 전환, 게임 종료 로직을 결정한다.
5)	뷰 업데이트 및 사용자 피드백
o	토큰 이동이 정상적으로 처리되면(moveResult.isSuccess()), 컨트롤러는 view.updateBoard(...)와 view.updateTokens()를 호출하여 실제 보드 UI와 토큰 위치를 화면에 다시 그린다.
o	또한 view.showMessage(...)와 view.showError(...)를 통해 “잡혔습니다!”, “완주했습니다!” 등 각 단계별 메시지를 사용자에게 보여준다.
6)	턴 교체 및 승리 판단
o	이동 결과 리스트가 모두 소진되면(remainingMoves가 빈 목록이 되면) 컨트롤러는 내부적으로 gameState.nextPlayer()를 호출하여 “다음 플레이어로 턴을 넘기거나”,
o	MoveResult.finished == true인 경우에는 handleGameEnd()를 통해 해당 플레이어를 승리자로 확정하고, 재시작 혹은 종료 여부를 묻는 창(GameEndChoice)을 띄워준다.
o	재시작을 선택하면 onGameRestart.run()을, 종료를 선택하면 onGameExit.run()을 실행해 애플리케이션 차원의 후속 처리(화면 전환, 리소스 정리 등)를 트리거한다.

모든 클래스 설계 과정
1. Model 계층
1.1.	Player - 플레이어 정보 관리
1.2.	Token - 말(토큰) 상태 관리
1.3.	Board - 게임 보드 구조
1.4.	BoardNode - 보드의 각 노드
1.5.	GameState - 전체 게임 상태 관리
1.6.	YutGameRules - 윷 던지기 및 게임 로직
1.7.	TokenPositionManager - 토큰 위치 관리
2.	View 계층
2.1.	GameLauncher - 게임 시작 및 초기화
2.2.	InGameView - 게임 화면 뷰
2.3.	BoardView - 보드 그래픽 뷰
3.	Controller 계층
3.1.	GameController - 게임 제어 로직

1. Model
1.1. Player
1) Information Expert (정보 전문가)
적용: 플레이어가 소유한 모든 토큰의 상태를 초기화하는 책임을 Player 내부에서 수행
이유: Player 객체가 자신의 토큰 리스트를 제어할 수 있는 전문가이므로, 토큰 상태를 보여주는 로직을 Player 클래스 안에 둠

2) Low Coupling (낮은 결합도)
적용: Player는 오직 Token 객체 목록만 알고 있고, 게임 흐름이나 UI 등 다른 모듈과의 직접적인 결합이 없음
이유: 필요한 정보(Token 리스트)만 참조하여 설계함으로써, 다른 클래스 변경 시 영향도를 최소화

3) High Cohesion (높은 응집도)
 
적용: Player 클래스 안에는 ‘플레이어 이름(name)’ 저장, ‘토큰 목록(tokens)’ 관리, ‘토큰 상태 초기화(resetAllTokens)’ 등 오직 플레이어 정보와 토큰 관리 로직만 모여 있음
이유: 한 클래스가 하나의 책임(플레이어 정보 유지 및 토큰 관리)에만 집중하도록 설계되어 응집도가 높음

4) SOLID 원칙
Single Responsibility Principle (단일 책임 원칙, SRP)
적용: Player 클래스는 오직 ‘플레이어 이름 및 토큰 관리’라는 하나의 책임만 수행
이유: 클래스가 여러 책임(게임 진행 처리, UI 처리 등)을 갖지 않고, 플레이어 본연의 데이터 처리만 담당

1.2. Token
1) Information Expert (정보 전문가)
 
적용: 토큰이 자신을 대표할 최상위 토큰을 찾는 책임을 가짐
이유: 토큰 객체가 자신의 상태·스택 구조에 대한 전문가이므로, 조회 로직을 Token 클래스 내부에서 처리함

2) Low Coupling (낮은 결합도)
private final Player owner; // Player 정보만 참조

적용: Token은 오직 owner 정보(참조)만 알고, 게임 흐름·UI 등 다른 책임에는 관여하지 않음
이유: 토큰이 자신과 직접 관련된 데이터만 보유하여, 다른 클래스와의 의존성을 최소화함

3) High Cohesion (높은 응집도)
 
적용: Token 클래스 내부에 ‘토큰의 상태(state) 관리’, ‘스택 구조(stackedTokens)’, ‘분기 선택(nextBranchChoice)’, ‘이전 노드(previousNode)’ 등 토큰 관련 로직만 모여 있음
이유: 단일 개념(Token)과 직접 관련된 필드·메서드만 포함시켜 클래스를 한 가지 책임에 집중시킴

4) Single Responsibility Principle (단일 책임 원칙, SRP)
적용: Token 클래스는 오직 말(토큰)의 상태·소유자·이동 정보만 관리하며, 게임 흐름 제어나 UI 처리 코드는 전혀 포함하지 않음
이유: 클래스가 하나의 책임(Token 데이터·행동 관리)만 가지므로, 변경 시 영향 범위를 최소화함

1.3. Board
1) Information Expert (정보 전문가)
 
적용: 보드의 노드 구조(nodes 리스트, startNode)를 생성·관리하는 책임을 Board 클래스 내부에서 처리
이유: 보드 전체 구조에 대한 정보를 가장 잘 아는 전문가가 스스로 노드를 생성·연결해야 응집도를 높일 수 있음

2) Creator (창조자 원칙)
 
 
적용: initializeBoard()와 connectNodes() 안에서 new BoardNode() 호출을 통해 BoardNode 객체를 직접 생성
이유: Board가 보드의 노드 생성에 필요한 정보를 알고 있으므로, BoardNode를 생성·초기화할 책임을 갖게 함

3) Low Coupling (낮은 결합도)
 
적용: Board는 오직 BoardNode 객체(구조)만 알고 있고, 게임 진행 로직이나 UI와는 전혀 결합되지 않음
이유: 보드 구조 관리만 담당하여, 다른 모듈이 바뀌어도 Board는 독립적으로 유지될 수 있도록 함

 
 
 

4) High Cohesion (높은 응집도)
적용: Board 클래스 내부에는 “보드 노드 생성( initializeBoard )”, “노드 연결( connectNodes )”, “다음/이전 노드 조회( getNextNode, getPreviousNode )”, “보드 속성 조회( getSides, getRadius )” 같은 메서드만 모여 있음
이유: 클래스 하나가 오직 “보드의 노드 구조 관리”라는 단일 개념에 집중하도록 설계되어 응집도가 높음

5) Single Responsibility Principle (단일 책임 원칙, SRP)
적용: Board 클래스는 “게임 보드 노드 구조 생성·연결” 및 “노드 조회”라는 하나의 책임만 수행
이유: 다른 로직(토큰 이동, 점수 계산 등)은 전혀 포함되어 있지 않고, 오직 보드 구조만 관리하므로 SRP를 준수

1.4. BoardNode
1) Information Expert (정보 전문가)
 
적용: BoardNode가 자신에게 올라와 있는 tokens 리스트를 관리
이유: “토큰이 노드 위에 존재한다”라는 정보는 BoardNode가 가장 잘 알고 있으므로, 토큰 추가·제거·조회 책임을 전부 BoardNode 내부에서 처리함

2) Creator (창조자 원칙)
 
적용: 생성자 안에서 new ArrayList<>()를 호출하여 nextNodes와 tokens 리스트를 직접 생성
이유: BoardNode가 “이 노드에 필요”한 컬렉션 객체(nextNodes, tokens)를 스스로 만들어야 책임 분리가 명확해짐

3) Low Coupling (낮은 결합도)
 
적용: BoardNode는 오직 BoardNode 타입 (nextNodes)과 Token 타입 (tokens)만 참조하며, 게임 흐름 제어나 UI와는 전혀 결합되지 않음
이유: 노드-토큰 관계만 알고, 다른 모듈(예: GameController, View 등)에 의존하지 않도록 설계하여 변경 영향을 최소화함

4) High Cohesion (높은 응집도)
 
적용: BoardNode 클래스 자체가 “한 노드의 이름·좌표·연결(nextNodes)·토큰 정보(tokens)”만 관리
이유: 한 클래스가 오로지 노드 단위 책임(“이 노드의 상태와 연결” 관리)만 수행하도록 설계되어 응집도가 높음

5) Single Responsibility Principle (단일 책임 원칙, SRP)
적용: BoardNode 클래스는 오직 “한 노드의 데이터 유지(이름, 좌표, 보드 분기 정보 등)”와 “토큰 입·출입 관리” 및 “다음 노드(nextNodes) 연결”만 담당
이유: 이 외 다른 책임(예: 토큰 이동 로직, 게임 승리 조건 판단, UI 렌더링 등)은 전부 다른 클래스(예: GameController, View 등)가 담당하도록 분리되어 있음

1.5. GameState
1) Information Expert (정보 전문가)
 
 
적용: GameState가 “현재 플레이어 교체(nextPlayer)”와 “게임 종료 판단(isGameEnded → checkVictory 위임)”을 스스로 처리
이유: GameState는 전체 플레이어 목록(players)과 승리 조건을 가장 잘 알고 있는 객체이므로, 턴 전환·종료 판정을 책임짐

2) Creator (창조자 원칙)
 
적용: 이 생성자 안에서 Player, Board, TokenPositionManager 객체를 직접 new로 생성
이유: GameState는 게임을 시작하기 위해 “플레이어 리스트·보드·토큰 위치 관리자”를 모두 알고 있어야 하므로, 이들을 생성할 책임을 가짐

3) Low Coupling (낮은 결합도)
 
적용: 실제 토큰 이동 로직이나 승리 판정 로직은 TokenPositionManager에 위임하고, GameState는 “어떤 토큰을 움직일지”만 결정
이유: 토큰 위치 관리 책임을 TokenPositionManager로 분리하여, GameState와 토큰 처리 로직 간 결합도를 최소화

4) High Cohesion (높은 응집도)
적용: GameState 클래스 내부에는 “게임 시작(startGame) → 윷 던지기(throwYut) → 턴 관리(nextTurn, nextPlayer, addMoves, useNextMove, hasRemainingMoves) → 토큰 이동(getMovableTokens, moveToken) → 승리 체크(checkVictory, isGameEnded) → 상태 조회(getter)”라는 일관된 역할만 포함
이유: 클래스 하나가 오직 “게임 전체 흐름과 상태를 관리”하는 책임에만 집중하도록 설계되어 응집도가 높음
 

5) Single Responsibility Principle (단일 책임 원칙, SRP)
 
적용: GameState는 “플레이어 관리, 보드 참조, 토큰 위치 관리자, 현재 플레이어/상태/남은 이동, 승자 기록” 등 ‘게임 상태를 보유하고 진행하는’ 역할만 수행
이유: UI 렌더링, DB 저장, 네트워크 통신 등 다른 책임은 전혀 포함하지 않고, 오직 게임 로직의 상태 전환과 관리만 수행함

1.6. YutGameRules
1) Information Expert (정보 전문가)
 
적용 위치: moveToken 내부에서 “목표 노드 계산”을 calculateTargetNode(...)에게 위임
이유: “토큰이 어디로 가야 하는지”라는 정보는 규칙 로직을 가장 잘 아는 YutGameRules가 스스로 처리하도록 하여 정보의 전문가(Expert) 역할을 부여함

2) Creator (창조자 원칙)
 
적용 위치: moveToken, finishToken, reorderMoves 등에서 결과 객체(MoveResult, ReorderResult)를 new로 생성
이유: YutGameRules가 “이 메소드가 반환해야 할 결과 타입”을 직접 알고 있으므로, 해당 객체를 생성할 책임을 가짐

3) Low Coupling (낮은 결합도)
 
 
적용 위치: moveToken, calculateTargetNode, finishToken 내부에서 “토큰 위치 조회/갱신”을 모두 TokenPositionManager에 위임
이유: 토큰의 좌표/위치 관리 책임을 TokenPositionManager에 맡김으로써, YutGameRules는 이동 “계산” 로직만 다루고, 실제 위치 저장·조회에 대한 의존을 줄임

4) High Cohesion (높은 응집도)
 
적용 위치: 클래스 전체 구성
이유: YutGameRules는 오직 “윷 던지기 결과 계산, 토큰 이동 규칙, 순서 재배열 검증”이라는 한 가지 큰 책임(“게임 규칙 로직”)만을 수행하도록 메서드들이 모여 있음

1.7. TokenPositionManager
1) Information Expert (정보 전문가)
 
적용: “토큰을 시작 위치(startNode)에 배치”하는 구체적인 책임을 TokenPositionManager 내부에서 처리
이유: 토큰의 위치 정보(tokenPositions)를 가장 잘 알고 있는 객체이므로, 토큰 배치 로직을 스스로 수행하도록 함
 
적용: “토큰의 현재 위치 반환(getTokenPosition)” 및 “토큰 위치 갱신(updateTokenPosition)” 기능을 TokenPositionManager가 전담
이유: 토큰별 현재 위치를 저장하고 관리하는 데이터(tokenPositions)를 가장 잘 아는 전문가이기 때문에, 조회·갱신 책임을 맡김

2) Creator (창조자 원칙)
 
적용: 생성자 내부에서 new HashMap<>()을 호출하여 tokenPositions 맵을 직접 생성
이유: TokenPositionManager가 “토큰 위치 관리용 Map”의 필요성과 사용 방식을 알고 있으므로, 이 객체를 생성·초기화하도록 함

3) Low Coupling (낮은 결합도)
적용: “특정 노드의 토큰들 얻기” 기능에서, 노드 조회( board.findNodeByName(nodeName) )와 실제 토큰 목록(node.getTokens())만 사용, “보드 위의 모든 노드 순회 후 토큰 수집” 기능을 수행할 때에도 board.getNodes()와 node.getTokens()만 호출
이유: TokenPositionManager는 오직 Board와 BoardNode에만 의존해도 충분하므로, 게임 전체 로직이나 UI와는 결합되지 않음 / 토큰 위치 관리 책임을 TokenPositionManager에 집중시키고, 다른 모듈(게임 로직·UI)에는 의존하지 않도록 낮은 결합도를 유지

4) High Cohesion (높은 응집도)
 
적용: TokenPositionManager 클래스 내부에는 “토큰 위치를 저장·조회·갱신(placeTokenAtStart, getTokenPosition, updateTokenPosition)”과 “노드별·전체 토큰 조회(getTokensOnNode, getAllTokensOnBoard)” 그리고 “보드 반환(getBoard)” 기능만 포함
이유: 한 클래스가 오직 “토큰이 보드 위에서 어디에 있는지 관리”하는 역할에만 집중하므로, 응집도가 높음

5) Single Responsibility Principle (단일 책임 원칙, SRP)
 
적용: TokenPositionManager는 오직 “토큰 위치 저장 및 조회”라는 하나의 책임만 수행. 게임 진행 로직, UI 렌더링, 상태 전환 등 다른 책임은 포함하지 않음
이유: 변경 사항이 있어도 오직 “토큰 위치 관련 기능”만 수정하면 되고, 다른 모듈에는 영향을 주지 않도록 단일 책임으로 분리

2. Controller
2.1. GameController
1) Controller (제어자 원칙)
 
 
 

적용: GameController 클래스 자체가 “사용자 입력(버튼 클릭 등)을 받아서 모델(GameState, YutGameRules)과 뷰(InGameView)를 연결(조정)하는 역할”을 수행
이유: View에서 발생한 이벤트(‘윷 던지기’ 클릭 등)를 받아서, GameState를 갱신하고, View를 통해 사용자에게 상태를 보여주기 때문에 Controller 역할을 하고 있음

2) Information Expert (정보 전문가)
 
 
적용: “어떤 토큰이 이동 가능한지(gameState.getMovableTokens), 실제 이동(moveToken) 수행” 로직은 GameState와 YutGameRules에게 위임
이유: 토큰 이동 여부와 이동 계산에 필요한 정보를 가장 잘 아는 객체(GameState·YutGameRules)에 책임을 부여함으로써, GameController는 전체 흐름만 제어함

3) Low Coupling (낮은 결합도)
 
 
적용: UI 관련 동작은 InGameView에, 게임 로직 관련 동작은 GameState 및 YutGameRules에 위임
이유: 컨트롤러는 “흐름 제어”만 담당하고, 구체적인 입출력(UI)·로직 연산은 다른 클래스에 위임해 결합도를 최소화함

4) High Cohesion (높은 응집도)
적용: GameController 클래스는 “게임 진행 흐름 제어”라는 한 가지 책임만 갖고, 다른 역할(모델 상세 구현, 뷰 그리기 등)은 포함하지 않음
이유: 클래스 내부 메서드들이 모두 “게임의 한 턴 진행 흐름”에 집중되어 있어 응집도가 높음

5) Creator (창조자 원칙)
 
 
적용: 순서 재배열을 위해 ReorderRequest 객체를 직접 생성하고, 게임 종료 후 결과 선택을 위해 GameEndChoice를 받고 사용
이유: 컨트롤러가 “사용자가 재배열하겠다고 선택”했을 때, 그 요청 객체를 생성해서 로직을 수행하므로, 객체 생성 책임을 어느 정도 가짐

6) Single Responsibility Principle (단일 책임 원칙, SRP)
 
적용: GameController는 “버튼 클릭 받아서 한 턴 진행 → 이동 로직 위임 → 결과 출력 → 다음 턴 결정 → 승리 체크 → 종료/재시작 이벤트 호출” 이라는 하나의 책임(‘게임 흐름 중재’)만 수행
이유: 게임 로직 계산(GameState, YutGameRules)과 UI 출력(InGameView)은 전부 다른 클래스에 맡김으로써, 컨트롤러는 ‘흐름만 중재’하도록 책임을 분리

7) Dependency Inversion Principle (의존 역전 원칙, DIP)
 
 
적용: GameController가 구체 뷰 클래스가 아닌 InGameView(가능하면 인터페이스) 타입으로 의존 주입을 받음
이유: 뷰 구현체가 달라져도 InGameView 인터페이스만 구현하면 GameController를 수정하지 않아도 되므로, 의존 역전이 어느 정도 적용되어 있음

3. View
3.1. InGameView
1) Controller (제어자 원칙)
 
적용: 버튼 클릭 시 “onRollYut.run()”만 호출하고 실제 게임 로직은 Controller가 수행하도록 위임
이유: View는 “사용자가 버튼을 눌렀다”는 사실만 알고, 실제 처리(윈유 던지기 → 토큰 이동 등)는 Controller(외부)에게 중재

2) Information Expert (정보 전문가)
적용: View가 “어디에 토큰을 그릴지”라는 정보를 구할 때, Model(BoardNode, Token)에서 필요한 정보(좌표, 색깔 등)를 모두 전달받음
이유: 뷰는 Model에 필요한 정보만 요청해서 화면에 그리므로, 누가 어떤 데이터에 책임이 있는지(누구한테 물어가야 할지) 분명해짐

3) Low Coupling (낮은 결합도)
 
적용: InGameView는 BoardView 내부 로직을 몰라도 boardView.refresh()만 호출하여 화면 갱신
이유: View가 Model의 구체적인 이동 로직이나 데이터 구조를 알지 않고, 오직 BoardView라는 추상화된 화면 업데이트 메서드만 사용하므로 결합도가 낮음

4) High Cohesion (높은 응집도)
 
적용: buildStatusPanel() 메서드 하나가 “오직 statusPanel을 구성하고 갱신하는 일”만 모아서 처리
이유: 상태판 구성과 관련된 모든 UI 작업이 한 메서드 안에서 논리적으로 묶여 있어 응집도가 높음

5) Creator (창조자 원칙)
 
적용: InGameView 생성자에서 new BoardView(board, players)를 호출하여 View가 필요한 BoardView 오브젝트를 직접 생성
이유: InGameView가 BoardView를 표현하는 데 필요한 주요 데이터를 이미 가지고 있으므로, 창조 책임을 View 계층에 둠


6) Single Responsibility Principle (단일 책임 원칙, SRP)
 
적용: InGameView 클래스는 “오직 화면 그리기, 버튼 이벤트 위임, 메시지/입력창 띄우기”만 담당
이유: 게임 로직(토큰 이동 계산, 승패 판단 등)은 전혀 포함하지 않고, View 관련 기능만 한 클래스에 몰아두어 단일 책임을 만족

7) Dependency Inversion Principle (의존 역전 원칙, DIP)
 
 
적용: InGameView는 GameController 같은 구체 타입이 아니라, Runnable(추상화)만 받아 버튼 클릭 시 실행
이유: View가 Controller의 구체 클래스를 직접 참조하지 않고, 추상 Runnable 뒤로 숨겨두어 의존성이 역전됨

3.2. GameLauncher
1) Controller (제어자 원칙)
 
적용: GameLauncher가 “최초 앱 실행 시점”에서 Controller를 생성하여 View와 연결하고, Controller가 다시 GameLauncher의 restartApplication()·exitApplication()을 호출하도록 콜백 연결
이유: GameLauncher는 애플리케이션 전체 흐름(컴포넌트 생성 → 이벤트 위임)을 통제하고 중재자 역할을 수행

2) Information Expert (정보 전문가 원칙)
 
적용: getPlayerCount() 메서드가 “플레이어 수 입력 → 올바른 범위(2~4명)인지 판별 → 그 결과만 반환”
이유: 입력 처리를 담당하는 책임은 해당 메서드가 전문가(Expert)이므로, “숫자 파싱·검증” 논리는 모두 getPlayerCount() 내부에서 처리

3) Low Coupling (낮은 결합도)
 
적용: GameLauncher는 GameState·InGameView·GameController 사이를 “쿨하게” 연결만 하고, 내부 로직(윷 던지기 계산, 토큰 이동, UI 그리기 등)을 직접 참조하지 않음
이유: 각 객체(GameState·InGameView·GameController)는 자체 책임을 가지며, GameLauncher는 이들이 어떻게 동작하는지 자세히 몰라도 연결만 가능 → 결합도가 낮음

4) High Cohesion (높은 응집도)
 
적용: GameLauncher 내부의 각 private 메서드(예: boardCustom(), getTestMode(), getPlayerCount(), getTokenCount() 등)가 “각자 자신의 입력/검증/오류 처리”만 수행
이유: 입력 수집은 getPlayerCount()에서, 토큰 수 수집은 getTokenCount()에서, 오류 메시지는 showError()에서 각각 담당하므로, 각 메서드 역할이 명확히 분리되어 응집도가 높음

5) Single Responsibility Principle (단일 책임 원칙, SRP)
적용: GameLauncher 클래스는 “게임을 띄우기 위해 필요한 사용자 입력(보드 모양, 테스트 모드, 플레이어 수/토큰 수 등)만 수집하고, GameState/InGameView/GameController를 생성해 Stage를 보여주는 전체 흐름”만 담당
이유: 게임 로직(토큰 이동, 승패 판단 등)은 GameController 및 YutGameRules 쪽으로 완전히 분리되어 있어, 런칭/초기화(실제 화면 띄우기) 역할만 한곳에 모아서 단일 책임을 만족

3.3. BoardView
1) Single Responsibility Principle (단일 책임 원칙, SRP)
 
적용: BoardView 클래스는 “Canvas 위에 보드 노드·분기선·토큰을 시각화”하는 책임만 가짐
이유: 게임 로직(예: 토큰 이동 계산, 승패 판단 등)은 전혀 포함하지 않고, 오직 그리기(rendering) 기능만 담당함

2) Creator (창조자 원칙)
 
적용: 생성자 안에서 new Canvas(...), canvas.getGraphicsContext2D(), getChildren().add(canvas)를 호출해 자신이 그릴 Canvas를 직접 생성
이유: BoardView가 “보드 시각화를 위해 반드시 필요한 Canvas와 GraphicsContext 객체”를 스스로 만들 만한 충분한 정보를 가지고 있으므로, 창조 책임을 스스로 수행함

3) Information Expert (정보 전문가 원칙)
 
 
적용: refresh() 메서드는 “BoardNode, Token, Player 등 Model 객체로부터 위치·상태 정보를 받아와서 화면에 그리는 일”만 수행
이유: 화면에 필요한 정보(좌표, 노드 이름, 토큰 상태 등)를 Model로부터 직접 가져와 처리하므로, View가 정보 전문가(Expert) 역할을 수행함

4) Low Coupling (낮은 결합도)
적용: BoardView는 BoardNode, Player, Token 객체의 내부 로직(예: 이동 계산 등)을 몰라도, 오직 해당 객체들이 제공하는 getX(), getY(), getTokens(), getState() 같은 인터페이스만 사용
이유: Model 쪽 변경(예: 노드 좌표 계산 방식 변경)이 있어도, getX()·getY() 의미가 유지되면 BoardView는 그대로 동작하므로 결합도가 낮음

5) High Cohesion (높은 응집도)
적용: refresh() 메서드 하나 안에 “보드 전체를 그리는 로직”이 논리적으로 모여 있어, 서로 관련된 기능이 응집되어 있음
이유: 보드 표시 관련 작업(선 그리기‧원 그리기‧토큰 그리기 등)이 단일 메서드에 집중되어 응집도가 높음

OOAD
캡슐화는 객체 내부의 데이터를 private 등으로 은닉하고, 외부에는 필요한 기능만 public 메서드 형태로 제공하여 상호작용하였다. YutGameRules는 내부 필드를 전부 private으로 숨겨 두었고 외부에서는 오직 public 메서드를 통해서만 동작을 실행하거나 결과를 조회할 수 있게 하였다. 그리고 결과를 담는 객체들 또한 내부 상태를 직접 변경할 수 없도록 private final 필드 + getter만 허용하는 형태로 설계하였다.

 
SOLID 원칙 
1. 단일 책임 원칙 (SRP)
단일 책임 원칙은 한 클래스가 한 가지 책임만을 가져야 하고 변경의 이유도 하나뿐이어야 한다는 원칙이다. 설계 과정에서 SRP를 적용하고자 하였고 잘 따르고 있다. Token 클래스는 게임 말 한 개의 데이터와 관련 동작만 관장하며, InGameView 클래스는 UI 표시 및 입력 처리만 담당하는 등 각 클래스가 명확한 역할을 갖는다. 예를 들어 Token 클래스의 변경은 말의 상태/동작과 관련된 요구사항 변화에 한정되며, UI 변경이나 게임 흐름 제어와는 무관하다. 이러한 SRP 준수로 인해 시스템은 구조가 단순해지고 클래스별로 변경에 강건하며, 개별 구성요소를 이해하거나 수정하기가 수월해졌다.

2. 개방-폐쇄 원칙 (OCP)
개방-폐쇄 원칙은 소프트웨어 구성 요소가 확장에는 열려 있고 수정에는 닫혀 있어야 한다는 원칙이다. 윷놀이 게임 구현에서 OCP의 적용은 주로 UI 교체 가능 구조와 관련하여 드러난다. 예를 들어, 현재 Swing으로 구현된 InGameView를 JavaFX 기반의 새로운 뷰로 교체하거나 추가하더라도, GameController나 Model 로직을 수정하지 않고도 새로운 View 계층을 추가하여 확장할 수 있다. 이는 GameController가 View의 구체적 클래스에 의존하지 않고 독립적으로 상호작용하도록 설계되었기 때문에 가능하다. 그 결과 시스템은 기존 코드를 변경하지 않고도 새로운 UI 구현을 확장 형태로 받아들일 수 있어 OCP를 만족한다. 또한 게임 규칙의 변경이나 추가 기능 구현 시에도, YutGameRules를 확장하거나 새로운 규칙 클래스(예: 변형된 규칙 집합)를 추가하는 방식으로 구현을 확장할 수 있으며 기존 클래스들의 수정을 최소화한다. 이러한 구조는 변경에 유연하며 요구사항이 추가될 때 안정성을 높인다.

 
GRASP
1. 정보 전문가 (Information Expert)
정보 전문가 원칙은 특정 기능을 수행하는 데 필요한 정보를 가장 많이 알고 있는 클래스에게 그 책임을 할당하는 원칙이다. 본 윷놀이 게임 설계에서는 말(Token)의 위치와 이동 같은 핵심 정보는 Token 클래스가 가장 잘 알고 있으므로, 해당 클래스가 자신의 움직임 처리나 상태 변경 책임을 맡고 있다. 예를 들어, Token 클래스는 자신의 현재 위치, 이동 가능한 경로 및 도착 여부 등을 판단하는 기능의 정보 전문가로서 동작한다. 또한 YutGameRules 클래스는 윷놀이의 규칙(윷 던지기 결과 해석, 이동 거리 결정, 추가 턴 여부 등)에 대한 정보를 집중적으로 보유하고 있어 게임 규칙 적용 책임을 맡는 정보 전문가 역할을 한다. 이처럼 필요한 정보를 많이 가진 클래스에 책임을 할당함으로써 설계가 정보 전문가 원칙을 따르고 있으며, 이는 각 기능이 가장 관련 깊은 클래스에 구현되어 효율적이고 일관된 동작을 가능하게 한다.

2. 창조자 (Creator)
창조자 원칙은 어떤 객체가 다른 객체를 생성할 때, 생성되는 객체에 필요한 데이터를 많이 가지고 있거나 구성 요소로 포함하는 객체가 그 생성 책임을 가져야 한다는 설계 원칙이다. 윷놀이 게임 구현에서 GameController 클래스는 게임 진행에 필요한 여러 객체를 초기화하고 생성하는 책임을 맡는다. 예를 들어, GameController는 게임 시작 시 말(Token) 객체들을 생성하거나, 게임 규칙을 관리하는 YutGameRules 객체를 생성 및 보유한다. GameController가 이러한 객체들을 포함하거나 이들과 밀접하게 연관되어 있으므로, 창조자 원칙에 따라 해당 객체들의 생성을 담당하도록 설계되었다. 이로써 객체 생성 로직이 관련된 한 곳에 모여 유지보수가 쉬워지고, 생성과 사용의 응집도가 높아졌다.

3. 컨트롤러 (Controller)
컨트롤러 원칙은 시스템 외부로부터 들어오는 입력 이벤트를 처리하고 해당 책임을 적절한 객체에 위임하는 조정자 역할의 객체를 두는 설계 개념이다. 본 시스템에서 GameController 클래스가 바로 이러한 컨트롤러 역할을 수행한다. GameController는 사용자 입력(예를 들어, 윷 던지기 버튼 클릭이나 말 이동 선택 등)을 View로부터 받아서, 그에 따른 게임 로직을 호출하고 업데이트를 처리한다. 구체적으로 GameController는 입력을 받으면 Model 영역의 클래스들(예: Token, YutGameRules 등)을 사용하여 게임 상태를 변경하고, 결과를 View에 전달하여 화면을 갱신한다. 이처럼 GameController가 중심이 되어 UI와 게임 로직 사이를 중재함으로써, 시스템 이벤트 처리가 한 곳에서 이루어지는 컨트롤러 원칙이 적용되었다.

구현
Swing에서 JavaFX로 UI 프레임워크가  전환되었지만, View 계층의 클래스 구조와 책임 분담은 거의 그대로 유지되었다. 각 파일의 핵심 로직(보드 그리기, 입력 대화, 상태 표시, 컨트롤러 연계)은 동일한 메서드와 흐름으로 구현되었고, 차이점은 Swing 컴포넌트를 JavaFX 컴포넌트로 대체한 문법적 수정과 약간의 구조 재배치뿐이다. 예를 들어, 메서드 이름과 호출 방식(예: refresh(), showMessage(), selectToken() 등)이 그대로 유지되어 컨트롤러나 모델과의 연계 코드에 변경이 필요 없었고, 내부 구현만 JOptionPane → Alert 같은 대응 API로 교체되었다. BoardPanel/BoardView처럼 역할이 동일한 클래스를 사용하고, GameEndChoice 같은 설계 요소도 재사용함으로써 UI 교체로 인한 영향 범위를 최소화하였다. 결국 MVC 구조의 View 부분이 프레임워크 변경에도 불구하고 동일한 책임을 수행하고 있으며, 코드 변경은 UI 라이브러리 문법상의 차이 외에는 동일한 수준이다.
 
View 계층 인터페이스의 일관성 유지
1. 공통 메서드
-	refresh(), showMessage(String, String), selectToken(), selectPath() 등
-	Controller는 View의 내부 구현(Swing/JFX)과 무관하게 항상 동일한 메서드 호출
-	이처럼 메서드 이름과 파라미터, 반환값을 전혀 변경하지 않고 Swing → JavaFX 간에도 그대로 쓰도록 함으로써, 뷰 교체 후에도 Controller 코드를 한 줄도 수정하지 않도록 할 수 있었다.
2. 이벤트 처리 흐름의 동일성
-	“랜덤 윷 던지기”와 “지정 윷 던지기” 버튼 클릭 시 Controller 호출 로직(rollingYut())
-	Swing: rollButton.addActionListener(e -> controller.rollingYut());
-	JavaFX: rollButton.setOnAction(e -> controller.rollingYut());
-	내부 API(addActionListener vs setOnAction)만 달라졌을 뿐, 버튼 클릭 시 컨트롤러 호출이라는 흐름 자체는 완전히 동일하다.
-	따라서 View 교체 시에도 Controller 쪽 코드는 전혀 손댈 필요 없이 View 레이어의 이벤트 연결부만 Swing→JavaFX 대응 메서드로 바꾸면 된다.

 
필수 변경점: UI 프레임워크 API 대응
Swing과 JavaFX는 컴포넌트, 그래픽 처리, 다이얼로그, 레이아웃 처리 등 모든 면에서 서로 호환되지 않으므로, View 교체 과정에서는 아래 항목들을 반드시 변경해야 했다. 이 변경은 모두 프레임워크가 다르기 때문에 할 수밖에 없는 필수 작업이다.

	Swing	JavaFX
UI 컴포넌트 클래스	javax.swing.JFrame
javax.swing.JPanel
javax.swing.JButton
javax.swing.JOptionPane	javafx.stage.Stage
javafx.scene.layout.Pane/BorderPane
javafx.scene.control.Button
javafx.scene.control.Alert
/javafx.scene.control.TextInputDialog
그리기 방식	protected void paintComponent(Graphics g)
g.drawLine(...), g.fillOval(...), g.drawOval(...)
repaint() 호출로 화면 갱신	Canvas canvas = new Canvas(width, height);
GraphicsContext gc = canvas.getGraphicsContext2D();
gc.strokeLine(...), gc.fillOval(...), gc.strokeOval(...)
canvas 위에 그리는 후, clearRect() + 다시 그리기 방식으로 화면 갱신
다이얼로그 API	JOptionPane.showMessageDialog(...)
JOptionPane.showInputDialog(...)
JOptionPane.showOptionDialog(...)	new Alert(AlertType.INFORMATION) / showAndWait()
new TextInputDialog() / showAndWait()
new ChoiceDialog<>(options) / showAndWait()
이벤트 등록 방식	button.addActionListener(e -> handler());	button.setOnAction(e -> handler());
UI 스레드 
관리 교체	SwingUtilities.invokeLater(() ->	Platform.runLater(() ->
레이아웃
매니저	SwingUtilities.invokeLater(() ->	Platform.runLater(() ->
	위의 모든 변경점은 Swing 코드를 JavaFX로 그대로 옮길 수 없고, 반드시 대응 API를 호출해야만 동작하기 때문에 View 계층에서 필수적으로 수정된 부분이다

 
변경 불필요 영역: Model / Controller
1. Model 클래스
•	게임 규칙과 말 이동·잡기·업기·추가 턴 부여, 커스터마이징 로직을 모두 포함
•	View 교체와 전혀 무관하게 순수한 비즈니스 로직만 수행
•	결과적으로 Swing → JavaFX 전환 시 단 한 줄도 수정할 필요가 없었다.
2. Controller 클래스
•	View에게서 받은 이벤트(rollingYut(), selectToken() 호출 등)에 따라 Model을 호출
•	이동 후 view.refresh(), view.showMessage(), view.selectPath() 등 View 인터페이스 메서드를 호출
•	View가 Swing이든 JavaFX든 상관없이 메서드 시그니처만 동일하면 그대로 동작
•	따라서 Controller 코드 또한 전혀 변경하지 않고 재사용할 수 있었다.
3. 결과: 전체 변경 범위 제한
•	Swing → JavaFX 전환 시, View 계층의 파일만 한정적으로 수정
•	Model과 Controller 계층은 패키지 import를 제외하고는 한 줄도 수정하지 않고 그대로 빌드·실행 가능
•	이로써 UI만 교체해도 동작하는 구조를 달성했다.
 
