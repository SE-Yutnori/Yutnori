package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.function.Function;

//윷 던지기 로직(옻놀이 규칙)을 처리하는 클래스
public class YutGameRules {
    private static boolean testMode = false;
    private static final Random random = new Random();

    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

    //윷 던지기 결과를 담는 클래스
    public static class YutThrowResult {
        private final List<Integer> results;
        private final List<String> resultMessages;
        
        public YutThrowResult(List<Integer> results, List<String> resultMessages) {
            this.results = new ArrayList<>(results);
            this.resultMessages = new ArrayList<>(resultMessages);
        }
        
        public List<Integer> getResults() { return new ArrayList<>(results); }
        public List<String> getResultMessages() { return new ArrayList<>(resultMessages); }
    }

    //이동 결과를 담는 클래스
    public static class MoveResult {
        private final boolean success;
        private final boolean catched;
        private final boolean finished;
        private final String message;
        
        public MoveResult(boolean success, boolean catched, boolean finished, String message) {
            this.success = success;
            this.catched = catched;
            this.finished = finished;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public boolean isCatched() { return catched; }
        public boolean isFinished() { return finished; }
        public String getMessage() { return message; }
    }

    //순서 재배열 요청(컨트롤러에 요청)을 담는 클래스
    public static class ReorderRequest {
        private final List<Integer> originalResults;
        private final String playerName;

        public ReorderRequest(List<Integer> originalResults, String playerName) {
            this.originalResults = new ArrayList<>(originalResults);
            this.playerName = playerName;
        }

        public String getPromptMessage() {
            String originalStr = originalResults.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return playerName + "님의 윷 결과: [" + originalStr + "]\n" +
                   "원하는 말 이동 순서 (예: 5,4,5,3)";
        }
    }

    //기본 윷 던지기 (테스트 모드면 테스트 모드로 실행)
    public static int throwOneYut() {
        if (testMode) {
            return 1;
        }

        //각각 윷이 나올 확률을 계산
        Random rand = new Random();
        int backCount = 0;
        for (int i = 0; i < 4; i++) {
            if (rand.nextBoolean()) {
                backCount++;
            }
        }

        if (backCount == 1) {
            if (rand.nextInt(4) == 0) {
                return -1;  // 빽도 (25% 확률)
            } else {
                return 1;   // 도 (75% 확률)
            }
        }

        switch (backCount) {
            case 0: return 5;   // 모
            case 2: return 2;   // 개
            case 3: return 3;   // 걸
            case 4: return 4;   // 윷
            default: return 0;  // 에러값
        }
    }

    //테스트 모드
    public static boolean isTestMode() {
        return testMode;
    }

    // 단일 윷 던지기 (연속 던지기 없음)
    public static YutThrowResult throwSingleYut() {
        List<Integer> results = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        
        int result = getYutResult();
        results.add(result);
        
        String yutName = getYutName(result);
        messages.add(yutName + " (" + result + "칸)");
        
        return new YutThrowResult(results, messages);
    }

    // 윷 던지기 결과 생성 (윷, 모면 계속 생성)
    public static YutThrowResult throwYut() {
        List<Integer> results = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        
        int throwCount = 0;
        boolean continueThrow = true;
        
        while (continueThrow) {
            int result = getYutResult();
            results.add(result);
            
            String yutName = getYutName(result);
            String message;
            if (throwCount == 0) {
                message = yutName + " (" + result + "칸)";
            } else {
                message = "추가 윷 던지기 결과 : " + yutName + " (" + result + "칸)";
            }
            messages.add(message);
            
            throwCount++;
            
            // 윷이나 모가 아니면 종료
            if (result < 4) {
                continueThrow = false;
            }
        }
        
        return new YutThrowResult(results, messages);
    }

    // 윷 던지기 결과 검증
    private static int getYutResult() {
        if (testMode) {
            return 1;
        }
        
        // 실제 윷 던지기
        int backCount = 0;
        for (int i = 0; i < 4; i++) {
            if (random.nextBoolean()) {
                backCount++;
            }
        }
        
        switch (backCount) {
            case 0: return 5; // 모
            case 1: return 4; // 윷
            case 2: return 3; // 걸
            case 3: return 2; // 개
            case 4: return 1; // 도
            default: return 1;
        }
    }

    // 윷 결과 이름 변환 (숫자->단어)
    private static String getYutName(int steps) {
        return switch (steps) {
            case -1 -> "빽도";
            case 1 -> "도";
            case 2 -> "개";
            case 3 -> "걸";
            case 4 -> "윷";
            case 5 -> "모";
            default -> "알 수 없음";
        };
    }

    // 말 이동 관련 메서드들
    public static MoveResult moveToken(Token token, int steps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        // 실제 이동할 대표 토큰 찾기 (업힌 토큰이라면 그를 업고 있는 대표 토큰) - 실제는 다 같이 이동
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 이동 먼저 확인 후
        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        BoardNode targetNode = calculateTargetNode(actualToken, steps, steps, tokenManager, branchSelector);
        
        if (targetNode == null) {
            // 완주 처리
            finishToken(actualToken, tokenManager);
            return new MoveResult(true, false, true, "말이 완주했습니다!");
        }

        // 업힌 토큰들 가져오기
        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());

        // 대표 토큰 이동
        if (currentNode != null) {
            currentNode.leave(actualToken);
        }
        targetNode.enter(actualToken);
        tokenManager.updateTokenPosition(actualToken, targetNode);

        // 업힌 토큰들 다같이 위치 업데이트
        for (Token stackedToken : stackedTokens) {
            tokenManager.updateTokenPosition(stackedToken, targetNode);
        }

        // 잡기 및 업기 처리 (대표 토큰으로만 처리)
        boolean caught = handleCaptureAndStacking(actualToken, targetNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

    //빽도 메서드 전진과 동일
    public static MoveResult moveTokenBackward(Token token, int steps, TokenPositionManager tokenManager) {
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());

        // 현재 위치에서 steps 만큼 뒤로 이동 (결국은 1로 고정)
        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        if (currentNode == null) {
            return new MoveResult(false, false, false, "토큰의 현재 위치를 찾을 수 없습니다.");
        }

        currentNode.leave(actualToken);

        // steps 만큼 뒤로 이동 (기록된 이전노드로)
        BoardNode targetNode = currentNode;
        for (int i = 0; i < steps && targetNode != null; i++) {
            BoardNode previousNode = tokenManager.getBoard().findPreviousNode(targetNode);
            if (previousNode != null) {
                targetNode = previousNode;
            } else {
                targetNode = tokenManager.getBoard().getStartNode();
                break;
            }
        }

        if (targetNode == null) {
            targetNode = tokenManager.getBoard().getStartNode();
        }

        targetNode.enter(actualToken);
        tokenManager.updateTokenPosition(actualToken, targetNode);

        for (Token stackedToken : stackedTokens) {
            tokenManager.updateTokenPosition(stackedToken, targetNode);
        }

        boolean caught = handleCaptureAndStacking(actualToken, targetNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

    //분기 노드인 지 확인하고 가야 할 길 선택
    private static BoardNode calculateTargetNode(Token token, int steps, int originalSteps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        BoardNode current = tokenManager.getTokenPosition(token);
        BoardNode previous = current; // 이전 노드 추적용
        
        while (steps > 0 && current != null) {
            List<BoardNode> nextNodes = current.getNextNodes();
            if (nextNodes.isEmpty()) {
                return null; // 완주
            }
            
            if (nextNodes.size() > 1) {
                // 분기점으로 이동하는 경우
                // 모든 분기 선택은 Controller에서 처리하므로 여기서는 저장된 선택이나 기본 경로를 사용
                BoardNode preSelectedPath = token.getNextBranchChoice();
                if (preSelectedPath != null && nextNodes.contains(preSelectedPath)) {
                    // 이전에 선택한 경로로 이동
                    previous = current;
                    current = preSelectedPath;
                    token.clearNextBranchChoice(); // 사용 후 제거
                } else {
                    // 현재 노드가 Center인지 확인 (센터와 외곽 노드는 분기 방식이 다름)
                    if (current.getName().equals("Center")) {
                        // Center에서 나가는 분기점 (보드 타입에 따라 유구사항대로 기본 경로 결정)
                        // Center일 때는 이전 노드를 업데이트하지 않고 기존 previousNode 사용
                        BoardNode defaultPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                        if (defaultPath != null) {
                            current = defaultPath;
                        } else {
                            current = nextNodes.get(0);
                        }
                    } else {
                        // 외곽 : 항상 첫 번째 옵션(외곽 경로) 선택
                        previous = current;
                        current = nextNodes.get(0);
                    }
                }
            } else {
                // 일반 노드로 이동
                // Center로 이동하는 경우 미리 previousNode 설정
                if (nextNodes.get(0).getName().equals("Center")) {
                    token.setPreviousNode(current);
                }
                previous = current; // 이전 노드 업데이트
                current = nextNodes.get(0);
            }
            
            // Center가 아닌 경우에만 도착한 노드에 이전 노드 정보 설정
            if (!current.getName().equals("Center")) {
                token.setPreviousNode(previous);
            }
            
            steps--;
        }
        
        return current;
    }

    // Center에서 보드 타입에 따른 기본 경로 계산
    private static BoardNode calculateCenterDefaultPath(Token token, List<BoardNode> nextNodes, TokenPositionManager tokenManager) {
        BoardNode previousNode = token.getPreviousNode();
        if (previousNode == null || nextNodes.isEmpty()) {
            return nextNodes.get(0);
        }
        
        int sides = nextNodes.get(0).getBoardSize();
        
        if (sides == 4) {
            // 4각형: 이전 노드 방향을 고려해서 직진 방향 선택
            // 보드 구조: ToCenter1-2(오른쪽), ToCenter2-2(아래) → Center → ToCenter0-2(위), ToCenter3-2(왼쪽)
            String previousName = previousNode.getName();
            
            if (previousName.startsWith("ToCenter") && previousName.endsWith("-2")) {
                try {
                    // 이전 노드에서 인덱스 추출 (ToCenter{index}-2)
                    String indexStr = previousName.substring(8, previousName.length() - 2);
                    int fromIndex = Integer.parseInt(indexStr);
                    
                    // 직진 매핑:
                    // ToCenter1-2 (오른쪽에서 들어옴) → ToCenter3-2 (왼쪽으로 나감)
                    // ToCenter2-2 (아래에서 들어옴) → ToCenter0-2 (위로 나감)
                    int toIndex = (fromIndex + 2) % 4;
                    String targetName = "ToCenter" + toIndex + "-2";
                    
                    // nextNodes에서 직진 경로 찾기
                    for (BoardNode node : nextNodes) {
                        if (node.getName().equals(targetName)) {
                            return node;
                        }
                    }
                    
                } catch (NumberFormatException e) {
                }
            } else {
            }
        } else {
            //중앙 분기 진입 로직
            return nextNodes.get(nextNodes.size() - 1);
        }
        
        // fallback: 첫 번째 옵션
        return nextNodes.get(0);
    }

    private static boolean handleCaptureAndStacking(Token token, BoardNode node, TokenPositionManager tokenManager) {
        boolean caught = false;
        List<Token> tokensOnNode = new ArrayList<>(node.getTokens());
        
        // 잡기 처리 - 상대방 토큰들 제거
        for (Token t : tokensOnNode) {
            if (t != token && t.getOwner() != token.getOwner()) {
                resetToken(t, tokenManager);
                caught = true;
            }
        }
        
        // 업기 처리 - 같은 팀 토큰들을 업기
        for (Token t : tokensOnNode) {
            if (t != token && t.getOwner() == token.getOwner()) {
                // 업힌 토큰을 노드에서 제거
                node.leave(t);
                tokenManager.updateTokenPosition(t, null);
                // 대표 토큰에 업기
                token.addStackedToken(t);
            }
        }

        return caught;
    }

    private static void resetToken(Token token, TokenPositionManager tokenManager) {
        // 업힌 토큰들도 함께 초기화
        for (Token stacked : token.getStackedTokens()) {
            tokenManager.updateTokenPosition(stacked, null);
            stacked.setState(TokenState.READY);
            stacked.clearStackedTokens();
        }
        
        // 대표 토큰 초기화
        BoardNode currentNode = tokenManager.getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }
        tokenManager.updateTokenPosition(token, null);
        token.setState(TokenState.READY);
        token.clearStackedTokens();
    }

    private static void finishToken(Token token, TokenPositionManager tokenManager) {
        BoardNode currentNode = tokenManager.getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }
        tokenManager.updateTokenPosition(token, null);
        token.setState(TokenState.FINISHED);
        
        // 업힌 토큰들도 완주 처리
        for (Token stacked : token.getStackedTokens()) {
            finishToken(stacked, tokenManager);
        }
        token.clearStackedTokens();
    }
}
