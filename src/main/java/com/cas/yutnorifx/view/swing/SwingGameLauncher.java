package com.cas.yutnorifx.view.swing;

import com.cas.yutnorifx.model.core.*;
import com.cas.yutnorifx.model.entity.*;
import com.cas.yutnorifx.controller.GameController;
import com.cas.yutnorifx.view.GameEndChoice;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SwingGameLauncher {

    public void start() {
        SwingUtilities.invokeLater(() -> {
            int sides = boardCustom();

            boolean testMode = getTestMode();
            YutGameRules.setTestMode(testMode);

            List<String> playerNames = new ArrayList<>();
            List<Integer> tokenCounts = new ArrayList<>();
            
            int numPlayers = getPlayerCount();
            int numTokens = getTokenCount();
            
            Set<String> usedNames = new HashSet<>();
            for (int i = 1; i <= numPlayers; i++) {
                while (true) {
                    String name = JOptionPane.showInputDialog(
                        null, 
                        "플레이어 " + i + "의 이름을 입력하세요:", 
                        "플레이어 이름 입력", 
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (name == null) System.exit(0);
                    name = name.trim();
                    
                    if (name.isEmpty()) {
                        showError("이름은 필수입니다.");
                    } else if (usedNames.contains(name)) {
                        showError("중복 이름입니다.");
                    } else {
                        usedNames.add(name);
                        playerNames.add(name);
                        tokenCounts.add(numTokens);
                        break;
                    }
                }
            }

            GameState gameState = new GameState(sides, 2.0f, playerNames, tokenCounts);

            SwingInGameView inGameView = new SwingInGameView(gameState.getBoard().getNodes(), gameState.getPlayers());

            GameController controller = new GameController(gameState);
            
            gameState.addObserver(inGameView);
            
            inGameView.setOnRollYut(() -> controller.rollingYut());
            
            inGameView.setOnGameEnd(choice -> controller.handleGameEndChoice(choice));
            
            inGameView.setOnBranchSelection(response -> controller.handleBranchSelection(response));
            
            inGameView.setOnTokenSelection(response -> controller.handleTokenSelection(response));
            
            inGameView.setOnYutTestSelection(response -> controller.handleYutTestSelection(response));
            
            inGameView.setOnMessageConfirmed(() -> controller.handleMessageConfirmed());
            
            inGameView.setOnReorderSelection(response -> controller.handleReorderSelection(response));
            
            controller.setOnGameRestart(() -> restartApplication());
            controller.setOnGameExit(() -> exitApplication());

            JFrame frame = new JFrame("윷놀이");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(inGameView.getRoot());
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            this.currentFrame = frame;
        });
    }

    private JFrame currentFrame;

    private void restartApplication() {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        start();
    }

    private void exitApplication() {
        System.exit(0);
    }

    private int boardCustom() {
        while (true) {
            String result = JOptionPane.showInputDialog(
                null, 
                "몇 각형 보드로 커스텀할까요? (권장 4-6)", 
                "보드 커스터마이징", 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == null) System.exit(0);

            try {
                int sides = Integer.parseInt(result);
                if (sides >= 4 && sides <= 6) return sides;
            } catch (NumberFormatException e) {
            }
        }
    }

    private boolean getTestMode() {
        int result = JOptionPane.showConfirmDialog(
            null,
            "테스트 모드로 진행하시겠습니까?",
            "게임 모드 선택",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }

    private int getPlayerCount() {
        while (true) {
            String result = JOptionPane.showInputDialog(
                null, 
                "플레이어 수를 입력하세요 (2 - 4명)", 
                "플레이어 수 입력", 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == null) System.exit(0);
            
            try {
                int count = Integer.parseInt(result);
                if (count >= 2 && count <= 4) return count;
            } catch (NumberFormatException e) {
            }
        }
    }

    private int getTokenCount() {
        while (true) {
            String result = JOptionPane.showInputDialog(
                null, 
                "플레이어가 사용할 말의 갯수를 입력하세요. (2 - 5개)", 
                "말 갯수 설정", 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == null) System.exit(0);
            
            try {
                int count = Integer.parseInt(result.trim());
                if (count >= 2 && count <= 5) return count;
            } catch (NumberFormatException e) {
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            null, 
            message, 
            "입력 오류", 
            JOptionPane.ERROR_MESSAGE
        );
    }
} 