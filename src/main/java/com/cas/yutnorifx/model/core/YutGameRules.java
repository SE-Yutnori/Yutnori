package com.cas.yutnorifx.model.core;

import com.cas.yutnorifx.model.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class YutGameRules {
    private static boolean testMode = false;

    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

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
            
            if (steps < 4) {
                break;
            }
        }
        
        return new YutThrowResult(results, messages);
    }

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

    public static MoveResult moveToken(Token token, int steps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        BoardNode targetNode = calculateTargetNode(actualToken, steps, steps, tokenManager, branchSelector);
        
        if (targetNode == null) {
            tokenManager.moveTokenToNode(actualToken, null); // null = 완주
            return new MoveResult(true, false, true, "말이 완주했습니다!");
        }

        boolean moveSuccess = tokenManager.moveTokenToNode(actualToken, targetNode);
        if (!moveSuccess) {
            return new MoveResult(false, false, false, "토큰 이동에 실패했습니다.");
        }
        
        boolean caught = tokenManager.handleTokenInteractions(actualToken, targetNode);

        return new MoveResult(true, caught, false, "");
    }

    public static MoveResult moveTokenBackward(Token token, int steps, TokenPositionManager tokenManager) {
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        if (currentNode == null) {
            return new MoveResult(false, false, false, "토큰의 현재 위치를 찾을 수 없습니다.");
        }

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

        boolean moveSuccess = tokenManager.moveTokenToNode(actualToken, targetNode);
        if (!moveSuccess) {
            return new MoveResult(false, false, false, "토큰 이동에 실패했습니다.");
        }

        boolean caught = tokenManager.handleTokenInteractions(actualToken, targetNode);

        return new MoveResult(true, caught, false, "");
    }

    private static BoardNode calculateTargetNode(Token token, int steps, int originalSteps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        BoardNode current = tokenManager.getTokenPosition(token);
        BoardNode previous = current;
        
        while (steps > 0 && current != null) {
            List<BoardNode> nextNodes = current.getNextNodes();
            if (nextNodes.isEmpty()) {
                return null;
            }
            
            if (nextNodes.size() > 1) {
                BoardNode tokenStartPosition = tokenManager.getTokenPosition(token);
                boolean isStartingFromThisBranch = (tokenStartPosition == current);
                
                BoardNode selectedPath = null;
                
                if (isStartingFromThisBranch) {
                    selectedPath = null;
                } else {
                    if (current.getName().matches("Edge\\d+-0")) {
                        for (BoardNode node : nextNodes) {
                            if (!node.getName().startsWith("ToCenter")) {
                                selectedPath = node;
                                break;
                            }
                        }
                        if (selectedPath == null) {
                            selectedPath = nextNodes.get(0);
                        }
                    } else if (current.getName().equals("Center")) {
                        selectedPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                        if (selectedPath == null) {
                            selectedPath = nextNodes.get(0);
                        }
                    } else {
                        selectedPath = nextNodes.get(0);
                    }
                }
                
                if (selectedPath == null) {
                    BoardNode preSelectedPath = token.getNextBranchChoice();
                    if (preSelectedPath != null && nextNodes.contains(preSelectedPath)) {
                        selectedPath = preSelectedPath;
                        token.clearNextBranchChoice();
                    } else {
                        if (branchSelector != null) {
                            selectedPath = branchSelector.apply(nextNodes);
                        }
                        
                        if (selectedPath == null || !nextNodes.contains(selectedPath)) {
                            if (current.getName().equals("Center")) {
                                selectedPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                                if (selectedPath == null) {
                                    selectedPath = nextNodes.get(0);
                                }
                            } else {
                                for (BoardNode node : nextNodes) {
                                    if (!node.getName().startsWith("ToCenter")) {
                                        selectedPath = node;
                                        break;
                                    }
                                }
                                if (selectedPath == null) {
                                    selectedPath = nextNodes.get(0);
                                }
                            }
                        }
                    }
                }
                
                if (!current.getName().equals("Center")) {
                    previous = current;
                }
                current = selectedPath;
            } else {
                if (nextNodes.get(0).getName().equals("Center")) {
                    token.setPreviousNode(current);
                }
                previous = current;
                current = nextNodes.get(0);
            }
            
            if (!current.getName().equals("Center")) {
                token.setPreviousNode(previous);
            }
            
            steps--;
        }
        
        return current;
    }

    private static BoardNode calculateCenterDefaultPath(Token token, List<BoardNode> nextNodes, TokenPositionManager tokenManager) {
        BoardNode previousNode = token.getPreviousNode();
        if (previousNode == null || nextNodes.isEmpty()) {
            return nextNodes.get(0);
        }
        
        int sides = nextNodes.get(0).getBoardSize();
        
        if (sides == 4) {
            String previousName = previousNode.getName();
            
            if (previousName.startsWith("ToCenter") && previousName.endsWith("-2")) {
                try {
                    String indexStr = previousName.substring(8, previousName.length() - 2);
                    int fromIndex = Integer.parseInt(indexStr);
                    
                    int toIndex = (fromIndex + 2) % 4;
                    String targetName = "ToCenter" + toIndex + "-2";
                    
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
            for (BoardNode node : nextNodes) {
                if (!node.getName().equals("ToCenter0-2")) {
                    return node;
                }
            }
        }
        
        return nextNodes.get(0);
    }
}
