package com.cas.yutnorifx;

import com.cas.yutnorifx.view.fx.FXGameLauncher;
import com.cas.yutnorifx.view.swing.SwingGameLauncher;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;

/**
 * 윷놀이 게임 메인 런처 클래스
 * JavaFX와 Swing 중 선택해서 실행 가능
 */
public class YutnoriGameMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        new FXGameLauncher().start();
    }

    public static void main(String[] args) {
        String uiType = System.getProperty("ui");
        
        if ("swing".equals(uiType)) {
            // Swing 직접 실행
            System.out.println("Swing mode...");
            SwingUtilities.invokeLater(() -> new SwingGameLauncher().start());
        } else {
            // 기본값: JavaFX 실행 (fx 또는 아무것도 지정 안함)
            System.out.println("JavaFX mode...");
            System.out.println("if you want other UI then -> ./gradlew run -Dui=swing");
            launch(args);
        }
    }
}