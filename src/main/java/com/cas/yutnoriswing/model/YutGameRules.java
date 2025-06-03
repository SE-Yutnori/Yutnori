package com.cas.yutnoriswing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 윷 던지기 로직을 처리하는 클래스
 */
public class YutGameRules {
    private static boolean testMode = false;
    private static final Random random = new Random();

    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

    /**
     * 윷 던지기 결과를 담는 클래스
     */
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

    /**
     * 이동 결과를 담는 클래스
     */
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

    /**
     * 순서 재배열 요청을 담는 클래스
     */
    public static class ReorderRequest {
        private final List<Integer> originalResults;
        private final String playerName;
        
        public ReorderRequest(List<Integer> originalResults, String playerName) {
            this.originalResults = new ArrayList<>(originalResults);
            this.playerName = playerName;
        }
        
        public List<Integer> getOriginalResults() { return new ArrayList<>(originalResults); }
        public String getPlayerName() { return playerName; }
        public String getPromptMessage() {
            String originalStr = originalResults.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return playerName + "님의 윷 결과: [" + originalStr + "]\n" +
                   "원하는 말 이동 순서 (예: 5,4,5,3)";
        }
    }

    /**
     * 순서 재배열 결과를 담는 클래스
     */
    public static class ReorderResult {
        private final boolean success;
        private final List<Integer> reorderedResults;
        private final String errorMessage;
        
        public static ReorderResult success(List<Integer> results) {
            return new ReorderResult(true, results, null);
        }
        
        public static ReorderResult error(String message) {
            return new ReorderResult(false, null, message);
        }
        
        private ReorderResult(boolean success, List<Integer> reorderedResults, String errorMessage) {
            this.success = success;
            this.reorderedResults = reorderedResults != null ? new ArrayList<>(reorderedResults) : null;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public List<Integer> getReorderedResults() { 
            return reorderedResults != null ? new ArrayList<>(reorderedResults) : null; 
        }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 윷 결과 누적
     */
    public static YutThrowResult accumulateYut(Player currentPlayer) {
        List<Integer> results = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        
        while (true) {
            int steps = throwOneYut();
            String[] yutNames = {"도", "개", "걸", "윷", "모"}; 
            String name;
            if(steps < 0) {
                name = "빽도";
            } else {
                name = yutNames[steps - 1];
            }
            
            messages.add(currentPlayer.getName() + ": " + name + " (" + steps + "칸)");
            results.add(steps);
            
            // 빽도, 도, 개, 걸이면 종료
            if (steps < 4) {
                break;
            }
        }
        
        return new YutThrowResult(results, messages);
    }

    /**
     * 기본 윷 던지기
     */
    public static int throwOneYut() {
        if (testMode) {
            return 1;
        }

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

    /**
     * 순서 재배열 검증
     */
    public static ReorderResult validateReorderInput(String input, List<Integer> originalResults) {
        if (input == null || input.trim().isEmpty()) {
            return ReorderResult.error("입력이 비어있습니다.");
        }

        String[] parts = input.split(",");
        if (parts.length != originalResults.size()) {
            return ReorderResult.error("입력 개수가 실제 개수와 다릅니다!");
        }

        List<Integer> reordered = new ArrayList<>();
        try {
            for (String p : parts) {
                int val = Integer.parseInt(p.trim());
                if ((val < -1 || val > 5) || val == 0) {
                    return ReorderResult.error("윷 값 (-1,1~5)을 벗어났습니다: " + val);
                }
                reordered.add(val);
            }
        } catch (NumberFormatException e) {
            return ReorderResult.error("숫자가 아닌 값이 포함되어 있습니다.");
        }

        List<Integer> sortedOrig = new ArrayList<>(originalResults);
        List<Integer> sortedReorder = new ArrayList<>(reordered);
        Collections.sort(sortedOrig);
        Collections.sort(sortedReorder);
        
        if (!sortedOrig.equals(sortedReorder)) {
            return ReorderResult.error("입력한 값이 실제 윷 결과와 다릅니다.");
        }

        return ReorderResult.success(reordered);
    }

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

    // 윷 던지기 결과 생성
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
                message = "한번 더! " + yutName + " (" + result + "칸)";
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
            return 1; // 테스트 모드에서는 항상 도(1칸)
        }
        
        // 실제 윷 던지기 구현
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

    // 윷 결과 이름 변환
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

    // Token 이동 관련 메서드들
    public static MoveResult moveToken(Token token, int steps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        // 실제 이동할 대표 토큰 찾기 (업힌 토큰이라면 그를 업고 있는 대표 토큰)
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 이동 처리 먼저 확인
        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        BoardNode targetNode = calculateTargetNode(actualToken, steps, steps, tokenManager, branchSelector);
        
        if (targetNode == null) {
            // 완주 처리 - finishToken이 노드에서 제거도 함께 처리
            finishToken(actualToken, tokenManager);
            return new MoveResult(true, false, true, "말이 완주했습니다!");
        }

        // 업힌 토큰들 가져오기 (미리 복사)
        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());

        // 대표 토큰 이동
        if (currentNode != null) {
            currentNode.leave(actualToken);
        }
        targetNode.enter(actualToken);
        tokenManager.updateTokenPosition(actualToken, targetNode);

        // 업힌 토큰들 위치 업데이트 (노드에는 진입시키지 않음)
        for (Token stackedToken : stackedTokens) {
            tokenManager.updateTokenPosition(stackedToken, targetNode);
        }

        // 잡기 및 업기 처리 (대표 토큰으로만 처리)
        boolean caught = handleCaptureAndStacking(actualToken, targetNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

    public static MoveResult moveTokenBackward(Token token, int steps, TokenPositionManager tokenManager) {
        // 실제 이동할 대표 토큰 찾기 (업힌 토큰이라면 그를 업고 있는 대표 토큰)
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 업힌 토큰들 가져오기 (미리 복사)
        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());

        // 현재 위치에서 steps 만큼 뒤로 이동
        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        if (currentNode == null) {
            return new MoveResult(false, false, false, "토큰의 현재 위치를 찾을 수 없습니다.");
        }

        // 대표 토큰을 현재 노드에서 제거
        currentNode.leave(actualToken);

        // steps 만큼 뒤로 이동
        BoardNode targetNode = currentNode;
        for (int i = 0; i < steps && targetNode != null; i++) {
            BoardNode previousNode = tokenManager.getBoard().findPreviousNode(targetNode);
            if (previousNode != null) {
                targetNode = previousNode;
            } else {
                // 더 이상 뒤로 갈 수 없으면 시작 노드로
                targetNode = tokenManager.getBoard().getStartNode();
                break;
            }
        }

        if (targetNode == null) {
            targetNode = tokenManager.getBoard().getStartNode();
        }

        // 대표 토큰을 새 노드에 진입
        targetNode.enter(actualToken);
        tokenManager.updateTokenPosition(actualToken, targetNode);

        // 업힌 토큰들 위치 업데이트 (노드에는 진입시키지 않음)
        for (Token stackedToken : stackedTokens) {
            tokenManager.updateTokenPosition(stackedToken, targetNode);
        }

        // 잡기 및 업기 처리 (대표 토큰으로만 처리)
        boolean caught = handleCaptureAndStacking(actualToken, targetNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

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
                // 모든 분기 선택은 GameController에서 처리하므로 여기서는 저장된 선택이나 기본 경로 사용
                BoardNode preSelectedPath = token.getNextBranchChoice();
                if (preSelectedPath != null && nextNodes.contains(preSelectedPath)) {
                    // 이전에 선택한 경로로 이동
                    previous = current; // 일반적인 경우만 이전 노드 업데이트
                    current = preSelectedPath;
                    token.clearNextBranchChoice(); // 사용 후 제거
                } else {
                    // 분기점 특성에 따라 기본 경로 결정
                    // 현재 노드가 Center인지 확인
                    if (current.getName().equals("Center")) {
                        // Center에서 나가는 분기점: 보드 타입에 따라 기본 경로 결정
                        // Center일 때는 이전 노드를 업데이트하지 않고 기존 previousNode 사용
                        BoardNode defaultPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                        if (defaultPath != null) {
                            current = defaultPath;
                        } else {
                            // 기본 경로를 찾을 수 없으면 첫 번째 옵션
                            current = nextNodes.get(0);
                        }
                    } else {
                        // 모서리 분기점: 항상 첫 번째 옵션(외곽 경로) 선택
                        previous = current; // 일반적인 경우만 이전 노드 업데이트
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
            // 5각형/6각형: 보드 타입별 기본 경로 선택
            if (sides == 6) {
                // 6각형: 항상 ToCenter5-2 방향으로 나감
                for (BoardNode node : nextNodes) {
                    if (node.getName().equals("ToCenter5-2")) {
                        return node;
                    }
                }
            } else {
                // 5각형: goal(ToCenter0-2)이 아닌 경로 선택
                for (BoardNode node : nextNodes) {
                    if (!node.getName().equals("ToCenter0-2")) {
                        return node;
                    }
                }
            }
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
                // 업힌 토큰의 위치를 null로 설정 (업힌 상태임을 표시)
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
