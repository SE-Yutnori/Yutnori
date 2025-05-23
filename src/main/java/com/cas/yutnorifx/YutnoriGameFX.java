package com.cas.yutnorifx;

import com.cas.yutnorifx.view.GameLauncher;
import javafx.application.Application;
import javafx.stage.Stage;

public class YutnoriGameFX extends Application {
    @Override
    public void start(Stage stage) {
        // GameLauncher를 통해 게임 시작
        GameLauncher launcher = new GameLauncher();
        launcher.start();
    }

    public static void main(String[] args) {
        launch();
    }
}