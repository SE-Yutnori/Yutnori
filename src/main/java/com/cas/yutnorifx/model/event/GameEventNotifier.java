package com.cas.yutnorifx.model.event;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 이벤트를 발생시키고 Observer들에게 통지하는 클래스
 */
public class GameEventNotifier {
    private final List<GameEventObserver> observers = new ArrayList<>();
    
    /**
     * Observer 추가
     */
    public void addObserver(GameEventObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Observer 제거
     */
    public void removeObserver(GameEventObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * 모든 Observer들에게 이벤트 통지
     */
    public void notifyEvent(GameEvent event) {
        for (GameEventObserver observer : observers) {
            observer.onGameEvent(event);
        }
    }
    
    /**
     * 편의 메서드: 이벤트 생성과 동시에 통지
     */
    public void notifyEvent(GameEvent.Type type, String message) {
        notifyEvent(new GameEvent(type, message));
    }
    
    /**
     * 편의 메서드: 데이터와 함께 이벤트 통지
     */
    public void notifyEvent(GameEvent.Type type, Object data, String message) {
        notifyEvent(new GameEvent(type, data, message));
    }
} 