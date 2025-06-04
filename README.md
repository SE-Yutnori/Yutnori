# 윷놀이 (YutnoriFX)

JavaFX를 사용한 윷놀이 게임입니다.

## 필수 요구사항

- Java 23 SDK
- Gradle 8.5 이상
- JavaFX Plugin 0.0.13

## 프로젝트 설정

1. **JavaFX SDK 설치**
   - [JavaFX 23 SDK](https://gluonhq.com/products/javafx/) 다운로드
   - 다운로드한 SDK를 원하는 위치에 압축 해제

2. **환경 변수 설정**
   - `JAVAFX_HOME`: JavaFX SDK가 설치된 경로
   ```bash
   # macOS/Linux
   export JAVAFX_HOME=/path/to/javafx-sdk-23
   
   # Windows
   set JAVAFX_HOME=C:\path\to\javafx-sdk-23
   ```

3. **프로젝트 빌드 및 실행**
   ```bash
   # 프로젝트 빌드
   ./gradlew build
   
   # 프로젝트 실행
   ./gradlew run
   ```

## 프로젝트 구조

```
src/main/java/com/cas/yutnorifx/
├── YutnoriGameFX.java    # JavaFX 애플리케이션 진입점
├── controller/             # 게임 컨트롤러
├── model/                  # 게임 모델 (보드, 플레이어, 토큰 등)
└── view/                   # JavaFX 기반 UI 컴포넌트
```

## 게임 실행 방법

1. 게임 시작 시 보드 각형 선택 (4-6각형)
2. 테스트 모드 여부 선택
3. 플레이어 수 입력 (2-4명)
4. 각 플레이어의 이름 입력 (중복 안됨)
5. 말 개수 선택 (2-5개)
6. 게임 시작

## 문제 해결

만약 JavaFX 관련 오류가 발생한다면:
1. `JAVAFX_HOME` 환경 변수가 올바르게 설정되었는지 확인
2. JavaFX SDK 버전이 Java 23과 호환되는지 확인
3. Gradle 버전이 8.5 이상인지 확인
