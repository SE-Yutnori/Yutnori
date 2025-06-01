package com.cas.yutnorifx.model.event;

//게임 이벤트를 관찰하는 Observer 인터페이스
public interface GameEventObserver {
    /**
     * 게임 이벤트가 발생했을 때 호출되는 메서드
     * @param event 발생한 게임 이벤트
     */
    void onGameEvent(GameEvent event);
} 