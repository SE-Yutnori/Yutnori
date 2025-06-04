# 윷놀이 게임 (Swing 버전)

Java Swing을 사용하여 구현한 전통 윷놀이 게임입니다.

## 주요 기능

- **커스터마이징 가능한 보드**: 4각형부터 6각형까지 다양한 형태의 보드 지원
- **멀티플레이어**: 2-4명의 플레이어 지원
- **테스트 모드**: 윷 결과를 직접 선택할 수 있는 테스트 모드
- **UI - single view**: Swing 기반의 깔끔하고 사용하기 쉬운 인터페이스
- **실시간 게임**: 플레이어별 말 상태를 실시간으로 확인

## 시스템 요구사항

- Java 17 이상
- Java Desktop (Swing) 지원 환경

## 컴파일 및 실행

### 컴파일
   ```bash
mkdir -p bin
javac -d bin src/main/java/com/cas/yutnoriswing/model/*.java src/main/java/com/cas/yutnoriswing/view/*.java src/main/java/com/cas/yutnoriswing/controller/*.java src/main/java/com/cas/yutnoriswing/*.java src/main/java/module-info.java
```

### 실행
   ```bash
java --module-path bin -m com.cas.yutnoriswing/com.cas.yutnoriswing.YutnoriGameSwing
```

## 게임 규칙

1. **보드 설정**: 게임 시작 시 4-6각형 보드를 선택할 수 있습니다.
2. **플레이어 설정**: 2-4명의 플레이어가 참여할 수 있으며, 각 플레이어는 2-5개의 말을 사용합니다.
3. **윷 던지기**: 
   - 도(1칸), 개(2칸), 걸(3칸), 윷(4칸), 모(5칸), 빽도(-1칸)
   - 윷이나 모가 나오면 추가 기회를 얻습니다.
4. **말 이동**: 윷 결과에 따라 말을 이동시킵니다.
5. **승리 조건**: 모든 말을 먼저 도착시키는 플레이어가 승리합니다.

## 프로젝트 구조

```
src/main/java/com/cas/yutnoriswing/
├── YutnoriGameSwing.java          # 메인 클래스
├── model/                         # 게임 로직 모델
│   ├── Board.java                 # 게임 보드
│   ├── BoardNode.java             # 보드 노드
│   ├── GameState.java             # 게임 상태 관리
│   ├── Player.java                # 플레이어
│   ├── Token.java                 # 게임 말
│   ├── TokenState.java            # 말 상태
│   ├── TokenPositionManager.java  # 말 위치 관리
│   └── YutGameRules.java          # 윷놀이 규칙
├── view/                          # UI 컴포넌트
│   ├── GameLauncher.java          # 게임 시작 화면
│   ├── InGameView.java            # 게임 플레이 화면
│   ├── BoardView.java             # 보드 렌더링
│   └── GameEndChoice.java         # 게임 종료 선택
└── controller/                    # 게임 컨트롤러
    └── GameController.java        # 게임 로직 제어
```
