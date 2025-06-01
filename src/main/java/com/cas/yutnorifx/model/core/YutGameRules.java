package com.cas.yutnorifx.model.core;

import com.cas.yutnorifx.model.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

//윷 던지기 로직을 처리하는 클래스
public class YutGameRules {
    private static boolean testMode = false;

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

    //순서 재배열 결과를 담는 클래스
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

    //윷 결과 누적
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

    //기본 윷 던지기
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

    //순서 재배열 검증
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

    //토큰은 총 전진, 후진, 분기 선택 - 세 가지의 움직임이 존재

    // Token 이동 관련 메서드들
    public static MoveResult moveToken(Token token, int steps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        // 윷놀이 로직 검증
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 이동 가능성 확인 및 목표 노드 계산
        BoardNode targetNode = calculateTargetNode(actualToken, steps, steps, tokenManager, branchSelector);
        
        if (targetNode == null) {
            // 완주 처리 - TokenPositionManager에게 위임
            tokenManager.moveTokenToNode(actualToken, null); // null = 완주
            return new MoveResult(true, false, true, "말이 완주했습니다!");
        }

        // 실제 이동 - TokenPositionManager에게 위임
        boolean moveSuccess = tokenManager.moveTokenToNode(actualToken, targetNode);
        if (!moveSuccess) {
            return new MoveResult(false, false, false, "토큰 이동에 실패했습니다.");
        }
        
        // 토큰 상호작용 처리 - TokenPositionManager에게 위임
        boolean caught = tokenManager.handleTokenInteractions(actualToken, targetNode);

        return new MoveResult(true, caught, false, "");
    }

    public static MoveResult moveTokenBackward(Token token, int steps, TokenPositionManager tokenManager) {
        // 윷놀이 로직 검증
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return new MoveResult(false, false, false, "토큰이 활성 상태가 아닙니다.");
        }

        // 뒤로 이동할 목표 노드 계산
        BoardNode currentNode = tokenManager.getTokenPosition(actualToken);
        if (currentNode == null) {
            return new MoveResult(false, false, false, "토큰의 현재 위치를 찾을 수 없습니다.");
        }

        // steps 만큼 뒤로 이동할 노드 계산
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

        // 실제 이동 - TokenPositionManager에게 위임
        boolean moveSuccess = tokenManager.moveTokenToNode(actualToken, targetNode);
        if (!moveSuccess) {
            return new MoveResult(false, false, false, "토큰 이동에 실패했습니다.");
        }

        // 토큰 상호작용 처리 - TokenPositionManager에게 위임
        boolean caught = tokenManager.handleTokenInteractions(actualToken, targetNode);

        return new MoveResult(true, caught, false, "");
    }

    //분기 노드에 대해서
    private static BoardNode calculateTargetNode(Token token, int steps, int originalSteps, TokenPositionManager tokenManager, Function<List<BoardNode>, BoardNode> branchSelector) {
        BoardNode current = tokenManager.getTokenPosition(token);
        BoardNode previous = current; // 이전 노드 추적용
        
        while (steps > 0 && current != null) {
            List<BoardNode> nextNodes = current.getNextNodes();
            if (nextNodes.isEmpty()) {
                return null; // 완주
            }
            
            if (nextNodes.size() > 1) {
                // 토큰의 실제 시작 위치
                BoardNode tokenStartPosition = tokenManager.getTokenPosition(token);
                boolean isStartingFromThisBranch = (tokenStartPosition == current);
                
                // 분기점으로 이동하는 경우
                BoardNode selectedPath = null;
                
                // 핵심 로직: 분기점에서 시작하는 경우에만 분기 선택 허용, 나머지는 모두 지나치기
                if (isStartingFromThisBranch) {
                    // 분기 선택 로직으로 이동 (else 블록으로)
                    selectedPath = null;
                } else {
                    // 외곽 분기점에서는 ToCenter가 아닌 외곽(Edge) 경로를 기본으로 선택
                    if (current.getName().matches("Edge\\d+-0")) {
                        // 모서리 분기점: ToCenter 경로가 아닌 외곽 경로 선택
                        for (BoardNode node : nextNodes) {
                            if (!node.getName().startsWith("ToCenter")) {
                                selectedPath = node;
                                break;
                            }
                        }
                        // ToCenter가 아닌 경로가 없으면 첫 번째 경로
                        if (selectedPath == null) {
                            selectedPath = nextNodes.get(0);
                        }
                    } else if (current.getName().equals("Center")) {
                        // Center에서는 기존 로직 사용
                        selectedPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                        if (selectedPath == null) {
                            selectedPath = nextNodes.get(0);
                        }
                    } else {
                        // 기타 분기점: 첫 번째 경로
                        selectedPath = nextNodes.get(0);
                    }
                }
                
                // 분기점에서 시작하는 경우에만 사용자 선택 처리
                if (selectedPath == null) {
                    // 분기점에서 시작하는 경우: 사용자 선택 허용
                    // 1. 먼저 미리 선택된 경로가 있는지 확인
                    BoardNode preSelectedPath = token.getNextBranchChoice();
                    if (preSelectedPath != null && nextNodes.contains(preSelectedPath)) {
                        selectedPath = preSelectedPath;
                        token.clearNextBranchChoice(); // 사용 후 제거
                    } else {
                        // 2. branchSelector 함수를 호출하여 Observer 패턴으로 분기 선택
                        if (branchSelector != null) {
                            selectedPath = branchSelector.apply(nextNodes);
                        }
                        
                        // 3. branchSelector가 null이거나 선택 결과가 없으면 기본 경로 사용
                        if (selectedPath == null || !nextNodes.contains(selectedPath)) {
                            // 분기점 특성에 따라 기본 경로 결정
                            if (current.getName().equals("Center")) {
                                // Center에서 나가는 분기점: 보드 타입에 따라 기본 경로 결정
                                selectedPath = calculateCenterDefaultPath(token, nextNodes, tokenManager);
                                if (selectedPath == null) {
                                    selectedPath = nextNodes.get(0);
                                }
                            } else {
                                // 모서리 분기점: ToCenter가 아닌 외곽 경로 우선 선택
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
                
                // 선택된 경로로 이동
                if (!current.getName().equals("Center")) {
                    previous = current; // Center가 아닌 일반적인 경우만 이전 노드 업데이트
                }
                current = selectedPath;
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

    // 분기 중 센터에 대해서 Center에서 보드 타입에 따른 기본 경로 계산
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
            // 5각형/6각형: 무조건 goal(ToCenter0-2)이 아닌 경로 선택
            for (BoardNode node : nextNodes) {
                if (!node.getName().equals("ToCenter0-2")) {
                    return node;
                }
            }
        }
        
        // fallback: 첫 번째 옵션
        return nextNodes.get(0);
    }
}
