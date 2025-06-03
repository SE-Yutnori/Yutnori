package com.cas.yutnorifx.controller;

import com.cas.yutnorifx.model.core.GameState;
import com.cas.yutnorifx.model.request.*;
import com.cas.yutnorifx.view.GameEndChoice;

public class GameController {
    private final GameState gameState;
    
    private Runnable onGameRestart;
    private Runnable onGameExit;

    public GameController(GameState gameState) {
        this.gameState = gameState;
    }
    
    public void setOnGameRestart(Runnable callback) {
        this.onGameRestart = callback;
    }
    
    public void setOnGameExit(Runnable callback) {
        this.onGameExit = callback;
    }

    public void rollingYut() {
        gameState.startYutProcess();
    }

    public void handleGameEndChoice(GameEndChoice choice) {
        if (choice == GameEndChoice.RESTART) {
            if (onGameRestart != null) {
                onGameRestart.run();
            }
        } else {
            if (onGameExit != null) {
                onGameExit.run();
            }
        }
    }
    
    public void handleBranchSelection(BranchSelectionResponse response) {
        gameState.handleBranchSelection(response);
    }
    
    public void handleTokenSelection(TokenSelectionResponse response) {
        gameState.handleTokenSelection(response);
    }

    public void handleYutTestSelection(YutTestResponse response) {
        gameState.handleYutTestSelection(response);
    }

    public void handleMessageConfirmed() {
        gameState.handleMessageConfirmed();
    }

    public void handleReorderSelection(ReorderResponse response) {
        gameState.handleReorderSelection(response);
    }
}
