package com.cas.yutnorifx.model.event;

import java.util.ArrayList;
import java.util.List;

public class GameEventNotifier {
    private final List<GameEventObserver> observers = new ArrayList<>();
    
    public void addObserver(GameEventObserver observer) {
        observers.add(observer);
    }

    public void notifyEvent(GameEvent event) {
        for (GameEventObserver observer : observers) {
            observer.onGameEvent(event);
        }
    }
    
    public void notifyEvent(GameEvent.Type type, String message) {
        notifyEvent(new GameEvent(type, message));
    }
    
    public void notifyEvent(GameEvent.Type type, Object data, String message) {
        notifyEvent(new GameEvent(type, data, message));
    }
} 