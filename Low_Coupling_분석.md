# 윷놀이 게임 Low Coupling 분석

## 🎯 Low Coupling이란?

**Low Coupling(낮은 결합도)**는 한 클래스가 다른 클래스들과의 의존성을 최소화하는 설계 원칙입니다.
- **목표**: 클래스 간 독립성 증대
- **장점**: 유지보수성, 테스트 용이성, 재사용성 향상
- **측정**: 의존하는 클래스의 수와 의존성의 강도

---

## 🏆 Low Coupling이 가장 돋보이는 클래스들

### **1. Token 클래스** ⭐⭐⭐
**"극도로 낮은 결합도의 완벽한 예시"**

```java
public class Token {
    private final Player owner;     // 단 하나의 의존성만 가짐
    private TokenState state;       // 열거형 (의존성 없음)
    private List<Token> stackedTokens;
    private BoardNode nextBranchChoice;
    private BoardNode previousNode;
    
    void setState(TokenState state) {  // package-private
        this.state = state;
    }
}
```

#### ✅ **Low Coupling 특징:**
- **단일 의존성**: `Player` 참조만 가짐
- **데이터 중심**: 게임 로직과 완전 분리
- **수동적 객체**: 상태 변경은 외부에서만 가능 (package-private)
- **최소 지식**: 다른 클래스들에 대해 거의 모름
- **독립적 테스트**: 다른 복잡한 객체 없이도 테스트 가능

#### 📝 **테스트 용이성 예시:**
```java
@Test
void testTokenStateChange() {
    Player owner = new Player("테스트", 2);
    Token token = new Token("토큰", owner);
    
    // 다른 클래스 없이도 독립적으로 테스트 가능
    token.setState(TokenState.ACTIVE);
    assertEquals(TokenState.ACTIVE, token.getState());
}
```

---

### **2. YutGameRules 클래스** ⭐⭐⭐
**"의존성 없는 순수 로직 클래스"**

```java
public class YutGameRules {
    // 정적 메서드로만 구성 - 인스턴스 의존성 제로!
    
    public static int throwOneYut() {
        // 파라미터 없음, 외부 의존성 없음
        if (testMode) return 1;
        // ... 로직
    }
    
    public static MoveResult moveToken(Token token, int steps, 
                                     TokenPositionManager tokenManager, 
                                     Function<List<BoardNode>, BoardNode> branchSelector) {
        // 필요한 모든 것을 파라미터로 받음 - 의존성 주입!
    }
}
```

#### ✅ **Low Coupling 특징:**
- **정적 메서드**: 인스턴스 상태 의존성 제로
- **파라미터 주입**: 필요한 모든 것을 파라미터로 받음
- **순수 함수**: 부작용 최소화
- **독립적 실행**: 다른 클래스 없이도 테스트 가능
- **상태 비의존**: 클래스 내부 상태에 의존하지 않음

#### 📝 **의존성 주입 예시:**
```java
// ✅ 모든 것을 파라미터로 받아서 의존성 제로
YutGameRules.MoveResult result = YutGameRules.moveToken(
    token,              // 필요한 토큰
    steps,              // 이동 칸 수
    tokenManager,       // 위치 관리자
    branchSelector      // 분기 선택 로직
);
```

---

### **3. TokenState 열거형** ⭐⭐
**"완벽한 독립성"**

```java
enum TokenState {
    READY,      // 출발 전
    ACTIVE,     // 이동 중
    FINISHED    // 도착 완료
}
```

#### ✅ **Low Coupling 특징:**
- **제로 의존성**: 어떤 클래스도 참조하지 않음
- **상수 정의**: 단순한 상태 표현
- **전역 사용**: 모든 곳에서 사용 가능
- **불변성**: 열거형 상수는 변경 불가
- **타입 안전**: 컴파일 타임 검증

---

### **4. BoardNode 클래스** ⭐
**"구조적 독립성"**

```java
public class BoardNode {
    private String name;
    private float x, y;
    private List<BoardNode> nextNodes;    // 같은 타입만 참조
    private List<Token> tokens;           // Token만 참조
    
    // 게임 로직과 분리된 순수 데이터 구조
    public void enter(Token token) { tokens.add(token); }
    public void leave(Token token) { tokens.remove(token); }
}
```

#### ✅ **Low Coupling 특징:**
- **구조적 의존성**: 자기 자신과 Token만 알면 됨
- **데이터 중심**: 위치와 연결 정보만 관리
- **게임 로직 분리**: 이동 규칙 등은 모름
- **단순한 책임**: 노드 정보와 토큰 관리만

---

## 🔗 Higher Coupling 클래스들과의 비교

### **GameController (Higher Coupling):**
```java
public class GameController {
    private final GameState gameState;     // GameState 의존
    private final InGameView view;         // View 의존
    // + YutGameRules, Player, Token 등도 간접 의존
    
    public void rollingYut() {
        // 여러 클래스와 상호작용
        Player currentPlayer = gameState.getCurrentPlayer();
        YutGameRules.YutThrowResult result = YutGameRules.throwYut();
        // ... 복잡한 의존성 체인
    }
}
```

### **GameState (Higher Coupling):**
```java
public class GameState {
    private Board board;                    // Board 의존
    private TokenPositionManager tokenPositionManager;  // Manager 의존
    private List<Player> players;          // Player 의존
    private Player currentPlayer;
    private GamePhase phase;
    
    // 여러 클래스를 조율하는 중앙 집중적 역할
}
```

---

## 📊 Low Coupling 순위표

| 순위 | 클래스 | 의존성 수 | 특징 | 점수 |
|------|--------|-----------|------|------|
| 🥇 | **YutGameRules** | 0개 (정적) | 파라미터 주입, 순수 함수 | 10/10 |
| 🥈 | **Token** | 1개 (Player) | 데이터 중심, 수동적 객체 | 9/10 |
| 🥉 | **TokenState** | 0개 | 완전 독립적 열거형 | 9/10 |
| 4위 | **BoardNode** | 2개 (자기자신, Token) | 구조적 의존성만 | 8/10 |

---

## 💡 Low Coupling의 실제 장점들

### **1. 테스트 용이성**
```java
// Token 클래스 - 간단한 단위 테스트
@Test
void testTokenStackManagement() {
    Player owner = new Player("테스트플레이어", 3);
    Token token = new Token("테스트토큰", owner);
    Token stackedToken = new Token("스택토큰", owner);
    
    // 의존성이 적어서 테스트가 단순함
    token.addStackedToken(stackedToken);
    assertEquals(1, token.getStackedTokens().size());
}

// YutGameRules 클래스 - 독립적 테스트
@Test
void testYutThrow() {
    YutGameRules.setTestMode(true);
    
    // 다른 객체 생성 없이 바로 테스트 가능
    int result = YutGameRules.throwOneYut();
    assertEquals(1, result);
}
```

### **2. 재사용성**
```java
// Token은 다른 게임에서도 재사용 가능
public class ChessGame {
    private Token chessPiece = new Token("폰", player);  // 그대로 사용 가능
}

// YutGameRules도 다른 윷놀이 변형에서 재사용 가능
public class ModernYutGame {
    int result = YutGameRules.throwOneYut();  // 그대로 사용 가능
}
```

### **3. 유지보수 용이성**
```java
// Token 클래스 수정 시 영향 범위가 제한적
public class Token {
    // 새로운 필드 추가해도 다른 클래스에 영향 최소
    private boolean isSpecial;  // 추가
    
    public void setSpecial(boolean special) {  // 추가
        this.isSpecial = special;
    }
}
```

### **4. 병렬 개발 가능**
```java
// 팀 A: Token 클래스 개발 (독립적)
public class Token {
    // Player만 알면 개발 가능
}

// 팀 B: YutGameRules 개발 (독립적)  
public class YutGameRules {
    // 다른 클래스 완성 전에도 개발 가능
    public static int throwOneYut() { ... }
}
```

---

## 🎯 Low Coupling 설계 원칙들

### **1. 의존성 주입 사용**
```java
// ❌ 높은 결합도
public class BadGameController {
    private GameState gameState = new GameState();  // 직접 생성
}

// ✅ 낮은 결합도
public class GoodGameController {
    private final GameState gameState;
    
    public GoodGameController(GameState gameState) {  // 주입받음
        this.gameState = gameState;
    }
}
```

### **2. 인터페이스 활용**
```java
// ✅ 향후 개선 방향
public interface IGameRules {
    int throwYut();
    MoveResult moveToken(Token token, int steps);
}

public class YutGameRules implements IGameRules {
    // 구현체 교체 가능
}
```

### **3. 정적 메서드 활용**
```java
// ✅ YutGameRules처럼 상태 없는 로직은 정적 메서드로
public class MathUtils {
    public static int calculateDistance(int x1, int y1, int x2, int y2) {
        // 인스턴스 의존성 없음
    }
}
```

### **4. 데이터와 로직 분리**
```java
// ✅ Token - 순수 데이터
public class Token {
    private TokenState state;  // 데이터만
}

// ✅ YutGameRules - 순수 로직
public class YutGameRules {
    public static MoveResult moveToken(...) {  // 로직만
    }
}
```

---

## 🚀 결론

우리 윷놀이 게임의 **Token**, **YutGameRules**, **TokenState** 클래스들은 **Low Coupling의 모범 사례**입니다!

### **핵심 성과:**
1. **독립적 테스트** - 각 클래스를 격리된 환경에서 테스트 가능
2. **재사용성** - 다른 프로젝트에서도 활용 가능
3. **유지보수성** - 한 클래스 수정이 다른 클래스에 미치는 영향 최소화
4. **확장성** - 새로운 기능 추가 시 기존 코드 변경 최소화

### **설계 품질 평가: 9/10** 🌟
Low Coupling 관점에서 매우 우수한 설계를 달성했습니다! 