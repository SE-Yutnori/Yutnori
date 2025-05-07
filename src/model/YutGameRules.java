package model;

import view.InGameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

//윷 던지기 로직을 처리하는 클래스
public class YutGameRules {
    //테스트모드 확인
    private static boolean testMode = false;

    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

    // 윷 던지기 결과가 윷/모이면 한 번 더 도/개/걸이면 종료
    public static List<Integer> accumulateYut(Player currentPlayer, InGameView view) {
        List<Integer> results = new ArrayList<>();
        while (true) {
            int steps = throwOneYut(view);
            // 그냥 취소값을 -999로
            if (steps == -999) {
                return null;
            }
            String[] yutNames = {"도", "개", "걸", "윷", "모"};
            String name;
            if(steps < 0) {
                name = "빽도";  // -1인 경우 빽도 표시
            } else {
                name = yutNames[steps - 1];
            }
            //view에서 처리할 수도 있으나 그냥 윷 던지는 쪽에서 보여주는 게 더 직관적이라고 생각..
            view.showMessage(currentPlayer.getName() + ": " + name + " (" + steps + "칸)", "윷 결과");
            results.add(steps);
            // 빽도, 도, 개, 걸이면 종료
            if (steps < 4) {
                break;
            }
        }
        return results;
    }


    // 기본 윷 던지기
    public static int throwOneYut(InGameView view) {
        if (testMode) {
            return getTestThrow(view);
        }

        //윷놀이의 기본 확률에 대해서 적용해서 윷 결과가 나오게
        Random rand = new Random();
        int backCount = 0;
        for (int i = 0; i < 4; i++) {
            if (rand.nextBoolean()) {
                backCount++;
            }
        }

        if (backCount == 1) {
            if (rand.nextInt(4) == 0) {
                return -1;
            } else {
                return 1;
            }
        }

        switch (backCount) {
            case 0: return 5;   // 모
            case 2: return 2;   // 개
            case 3: return 3;   // 걸
            case 4: return 4;   // 윷
            default: return 0;  // 쓰레기값
        }
    }

    //테스트 윷 던지기면 그 창을 보여줌
    private static int getTestThrow(InGameView view) {
        return view.getTestYutThrow();
    }

    // 모, 모, 개 를 2칸, 5칸, 5칸 씩 이동할 수 있게끔 사용자가 설정할 수 있는 메서드
    public static List<Integer> reorderResults(List<Integer> original, String playerName, InGameView view) {
        if (original == null || original.isEmpty()) {
            return null;
        }
        String originalStr = original.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        while (true) {
            String input = view.requestInput(
                    playerName + "님의 윷 결과: [" + originalStr + "]\n" +
                            "원하는 말 이동 순서 (예: 5,4,5,3)",
                    "윷 순서 재배열"
            );
            if (input == null) {
                return null;
            }

            String[] parts = input.split(",");
            if (parts.length != original.size()) {
                view.showError("입력 개수가 실제 개수와 다릅니다!");
                continue;
            }

            List<Integer> reordered = new ArrayList<>();
            boolean parseError = false;
            try {
                for (String p : parts) {
                    int val = Integer.parseInt(p.trim());
                    if ((val < -1 || val > 5) || val == 0) {
                        view.showError("윷 값 (-1,1~5)을 벗어났습니다: " + val);
                        parseError = true;
                        break;
                    }
                    reordered.add(val);
                }
            } catch (NumberFormatException e) {
                view.showError("숫자가 아닌 값이 포함되어 있습니다.");
                parseError = true;
            }
            if (parseError) {
                continue;
            }

            List<Integer> sortedOrig    = new ArrayList<>(original);
            List<Integer> sortedReorder = new ArrayList<>(reordered);
            Collections.sort(sortedOrig);
            Collections.sort(sortedReorder);
            if (!sortedOrig.equals(sortedReorder)) {
                view.showError("입력한 값이 실제 윷 결과와 다릅니다.");
                continue;
            }

            return reordered;
        }
    }


    // 이동 로직은 단계별로 토큰 이동 및 잡기, 업기 등을 처리
    public static boolean applyMoves(Player currentPlayer, List<Integer> stepsList, BoardNode startNode, InGameView view) {
        boolean catched = false;
        for (int step : stepsList) {
            if (step < 0) {  // 빽도인 경우
                // ACTIVE인 토큰들 중에서만 선택
                List<Token> activeTokens = currentPlayer.getTokens().stream()
                        .filter(t -> t.getState() == TokenState.ACTIVE)
                        .toList();
                if (activeTokens.isEmpty()) {
                    view.showError("모든 말이 대기 중입니다. 빽도는 적용되지 않습니다.");
                    continue;
                }

                Token selectedToken = view.selectToken(activeTokens, step);
                if (selectedToken == null) {
                    view.showError("말을 선택하지 않아 이동을 중단합니다.");
                    return catched;
                }

                Token representative = selectedToken.getTopMostToken();

                Token.MoveResult result = representative.moveBackward(Math.abs(step));
                if (result.isCatched()) {
                    catched = true;
                }
            } else {
                Token selectedToken = view.selectToken(currentPlayer.getTokens(), step);
                if (selectedToken == null) {
                    view.showError("말을 선택하지 않아 이동을 중단합니다.");
                    return catched;
                }

                selectedToken = selectedToken.getTopMostToken();
                if (selectedToken.getState() == TokenState.READY) {
                    selectedToken.start(startNode);
                    view.refresh();
                }
                Token.MoveResult result = selectedToken.move(step, view::selectPath);
                if (result.isCatched()) {
                    catched = true;
                }
            }
            view.refresh();
            if (currentPlayer.hasFinished()) {
                break;
            }
        }
        return catched;
    }
}
