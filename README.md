# 윷놀이 (YutnoriSwing)

JavaSwing 사용한 윷놀이 게임입니다.

## 프로젝트 실행

```
javac -d out $(find src -name "*.java")
java -cp out Main
```

## 프로젝트 구조

```
src/
├── Main.java    # JavaSwing 애플리케이션 진입점
├── controller/             # 게임 컨트롤러
├── model/                  # 게임 모델 (보드, 플레이어, 토큰 등)
└── view/                   # JavaSwing 기반 UI 컴포넌트
```

## 게임 실행 방법

1. 게임 시작 시 보드 각형 선택 (권장: 4-6각형)
2. 테스트 모드 여부 선택
3. 플레이어 수 입력 (2-4명)
4. 각 플레이어의 이름 입력
5. 말 개수 선택 (2-5개)
6. 게임 시작!
