package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

//말에 대한 정보가 담긴 클래스
public class Token {
    private String name;
    private Player owner;
    private BoardNode currentNode;
    private boolean finished = false;
    private int index;
    private TokenState state = TokenState.READY;
    private List<Token> stackedTokens = new ArrayList<>();
    private Deque<BoardNode> history = new ArrayDeque<>();

    //static nested class 관련도 높아서 여기에...
    // 이동 결과 : 잡기 or 끝
    public static class MoveResult {
        private boolean finished;
        private boolean catched;

        public boolean isFinished() { return finished; }
        public void setFinished(boolean finished) {
            this.finished = finished;
        }
        public boolean isCatched() {
            return catched;
        }
        public void setCatched(boolean catched) {
            this.catched = catched;
        }
    }

    public Token(String name, Player owner) {
        this.name = name;
        this.owner = owner;
        this.currentNode = null;
    }

    //getter들
    public String getName() {
        return name;
    }
    public Player getOwner() {
        return owner;
    }
    public TokenState getState() {
        return state;
    }
    public List<Token> getStackedTokens() {
        return stackedTokens;
    }

    // READY 상태의 말은 startNode에서 출발
    public void start(BoardNode startNode) {
        if (state == TokenState.READY) {
            history.clear();
            this.currentNode = startNode;
            this.state = TokenState.ACTIVE;
            startNode.enter(this);
        }
    }

    /**
     * 말이 잡혔을 때는 READY 상태로 state 초기화 및
     * 부가적인 값들 clear method로 초기화
     */
    public void reset() {
        if (currentNode != null) {
            currentNode.leave(this);
        }
        currentNode = null;
        state = TokenState.READY;
        stackedTokens.clear();
        history.clear();
    }


    // steps 만큼 이동하고, 잡기 및 업기 처리 결과를 MoveResult로 반환
    /**
     * steps 만큼 이동하고, 잡기 및 업기 처리 결과를 MoveResult로 반환
     * @param steps : 이동할 step수
     * @param branchSelect : 다음 갈 수 있는 노드 리스트(List<BoardNode>)를 받아 → 그 중 하나를 선택해서 반환하는 함수
     * @return : MoveResult
     */
    public MoveResult move(int steps, Function<List<BoardNode>, BoardNode> branchSelect) {
        MoveResult result = new MoveResult();
        if (state != TokenState.ACTIVE) {
            return result;
        }

        // 현재 노드에서 빠짐
        if (currentNode != null) {
            currentNode.leave(this);
        }

        int initSteps = steps;
        while (steps > 0) {
            history.push(currentNode);
            List<BoardNode> nextNodes = currentNode.getNextNodes();
            if (nextNodes.isEmpty()) {
                // 더 이동할 곳이 없으면 완주 처리 (업에 쌓인 토큰들도 함께 처리)
                finishWithStack();
                result.setFinished(true);
                return result;
            }

            // 분기 처리 - Center 노드와 분기 조건에 따른 branchSelect() 호출
            if (currentNode.getName().equals("Center") && nextNodes.size() >= 2 && steps == initSteps) {
                currentNode = branchSelect.apply(nextNodes);
            } else if (nextNodes.size() >= 2 && steps == initSteps) {
                currentNode = branchSelect.apply(nextNodes);
            } else if (currentNode.getName().equals("Center") && index < currentNode.getBoardSize() / 2) {
                //기존에 제안한 방식..
//                currentNode = nextNodes.get(index);
                //교수님께서 제안한 방식..
                currentNode = nextNodes.get(nextNodes.size()-1);
            } else {
                currentNode = nextNodes.get(0);
            }

            // ToCenterX-Y 노드일 경우 X를 index로 (참고로 index는 모서리 번호임 시작 노드가 포함된 모서리는 0 그다음은 1.. 이런 식)
            if (currentNode.getName().startsWith("ToCenter") && currentNode.getName().contains("-")) {
                String[] parts = currentNode.getName().replace("ToCenter", "").split("-");
                index = Integer.parseInt(parts[0]);
            }

            steps--;
        }

        // 최종 노드에 진입
        currentNode.enter(this);

        // 도착 후 잡기 및 업기
        processArrival(result);

        return result;
    }

    // 빽도 이동 : 주어진 steps(빽도는 무조건 1이긴 함) 만큼 이전 노드를 따라 이동
    // 로직 자체는 steps 만큼 정방향으로 이동하는 move 로직과 동일(대신 분기가 없이 단순히 직전 노드로 이동)
    public MoveResult moveBackward(int steps) {
        MoveResult result = new MoveResult();
        if (state != TokenState.ACTIVE) {
            return result;
        }

        // 현재 노드에서 나와서
        if (currentNode != null) {
            currentNode.leave(this);
        }

        // 뒤로 한 칸 이동
        while (steps > 0) {
            if(history.size() <= 0  && Objects.equals(currentNode.getName(), "Edge0-0")) {
                state = TokenState.FINISHED;
                break;
            }
            currentNode = history.pop();
            steps--;
        }

        // 최종 노드에 진입
        currentNode.enter(this);

        // 도착 후 잡기 및 업기
        processArrival(result);

        return result;
    }

    // 잡기 및 업기 처리 메서드
    private void processArrival(MoveResult result) {
        // 잡기 처리 : 상대를 잡으면 reset() 호출
        List<Token> tokensOnNode = new ArrayList<>(currentNode.getTokens());
        for (Token t : tokensOnNode) {
            if (t != this && t.getOwner() != this.owner) {
                t.reset();
                result.setCatched(true);
            }
        }

        // 업기 처리 : 본인 토큰의 칸에 가면 리스트에 추가
        for (Token t : tokensOnNode) {
            if (t != this && t.getOwner() == this.owner && !stackedTokens.contains(t)) {
                stackedTokens.add(t);
            }
        }

        // 업힌 토큰들도 같이 이동하게끔
        for (Token stacked : stackedTokens) {
            if (stacked.getState() == TokenState.ACTIVE) {
                stacked.moveWith(this.currentNode);
            }
        }
    }

    // 도착할 때, 대표 토큰과 함께 업힌 토큰들도 FINISHED 상태로 해주는 메서드
    private void finishWithStack() {
        if (currentNode != null) {
            currentNode.leave(this);
        }
        currentNode = null;
        finished = true;
        state = TokenState.FINISHED;

        // 업힌 토큰들을 각각 finish로
        for (Token stacked : stackedTokens) {
            stacked.finishIndividually();
        }
        stackedTokens.clear();
        history.clear();
    }

    // 개별 finish 해주는 메서드  - 계속 동시에 골인하지 못해서 하나하나 수작업으로 도착시키기
    public void finishIndividually() {
        if (currentNode != null) {
            currentNode.leave(this);
        }
        currentNode = null;
        finished = true;
        state = TokenState.FINISHED;
    }

    // 업힌 토큰이 같이 이동하는 메서드
    public void moveWith(BoardNode node) {
        if (currentNode != null) {
            currentNode.leave(this);
        }
        this.currentNode = node;
        node.enter(this);
    }

    // 업힌 토큰들이 다 대표 토큰이 될 수 있게..
    public Token getTopMostToken() {
        for (Token tok : owner.getTokens()) {
            if (tok.getStackedTokens().contains(this)) {
                return tok.getTopMostToken();
            }
        }
        return this;
    }
}

