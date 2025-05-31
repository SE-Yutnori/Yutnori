package com.cas.yutnorifx.model;

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

    // 윷 던지기 결과 생성
    public static YutThrowResult throwYut() {
        List<Integer> results = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        
        int result = getYutResult();
        results.add(result);
        
        String yutName = getYutName(result);
        messages.add(yutName + " (" + result + "칸)");
        
        // 윷이나 모가 나오면 한 번 더
        if (result >= 4) {
            int additionalResult = getYutResult();
            results.add(additionalResult);
            
            String additionalYutName = getYutName(additionalResult);
            messages.add("한번 더! " + additionalYutName + " (" + additionalResult + "칸)");
            
            // 또 윷이나 모가 나오면 한 번 더
            if (additionalResult >= 4) {
                int finalResult = getYutResult();
                results.add(finalResult);
                
                String finalYutName = getYutName(finalResult);
                messages.add("또 한번 더! " + finalYutName + " (" + finalResult + "칸)");
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
        if (token.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 현재 노드에서 나오기
        BoardNode currentNode = tokenManager.getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }

        // 이동 처리
        BoardNode targetNode = calculateTargetNode(token, steps, tokenManager, branchSelector);
        if (targetNode == null) {
            finishToken(token, tokenManager);
            return new MoveResult(true, false, true, "말이 완주했습니다!");
        }

        // 새 노드에 진입
        targetNode.enter(token);
        tokenManager.updateTokenPosition(token, targetNode);

        // 잡기 및 업기 처리
        boolean caught = handleCaptureAndStacking(token, targetNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

    public static MoveResult moveTokenBackward(Token token, int steps, TokenPositionManager tokenManager) {
        if (token.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 현재 노드에서 나오기
        BoardNode currentNode = tokenManager.getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }

        // 뒤로 이동 (임시로 첫 노드로 이동)
        BoardNode startNode = tokenManager.getBoard().getStartNode();
        if (startNode == null) {
            return new MoveResult(false, false, false, "이동할 수 없습니다.");
        }

        startNode.enter(token);
        tokenManager.updateTokenPosition(token, startNode);

        // 잡기 및 업기 처리
        boolean caught = handleCaptureAndStacking(token, startNode, tokenManager);

        return new MoveResult(true, caught, false, caught ? "상대방 말을 잡았습니다!" : "");
    }

    private static BoardNode calculateTargetNode(Token token, int steps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        BoardNode current = tokenManager.getTokenPosition(token);
        while (steps > 0 && current != null) {
            List<BoardNode> nextNodes = current.getNextNodes();
            if (nextNodes.isEmpty()) {
                return null; // 완주
            }
            current = nextNodes.size() > 1 ? branchSelector.apply(nextNodes) : nextNodes.get(0);
            steps--;
        }
        return current;
    }

    private static boolean handleCaptureAndStacking(Token token, BoardNode node, TokenPositionManager tokenManager) {
        boolean caught = false;
        List<Token> tokensOnNode = new ArrayList<>(node.getTokens());
        
        // 잡기 처리
        for (Token t : tokensOnNode) {
            if (t != token && t.getOwner() != token.getOwner()) {
                resetToken(t, tokenManager);
                caught = true;
            }
        }
        
        // 업기 처리
        for (Token t : tokensOnNode) {
            if (t != token && t.getOwner() == token.getOwner()) {
                token.addStackedToken(t);
            }
        }

        return caught;
    }

    private static void resetToken(Token token, TokenPositionManager tokenManager) {
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
