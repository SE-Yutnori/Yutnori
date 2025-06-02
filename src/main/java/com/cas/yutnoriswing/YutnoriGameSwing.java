package com.cas.yutnoriswing;

import com.cas.yutnoriswing.view.GameLauncher;
import javax.swing.SwingUtilities;

public class YutnoriGameSwing {
    public static void main(String[] args) {
        // Swing은 Event Dispatch Thread에서 실행되어야 함
        SwingUtilities.invokeLater(() -> {
            // GameLauncher를 통해 게임 시작
            GameLauncher launcher = new GameLauncher();
            launcher.start();
        });
    }
} 