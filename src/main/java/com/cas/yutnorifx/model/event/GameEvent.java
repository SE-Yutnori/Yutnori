package com.cas.yutnorifx.model.event;

//게임에서 발생하는 다양한 이벤트를 정의
public class GameEvent {
    public enum Type {
        YUT_THROW_RESULT,// 윷 던지기 결과
        YUT_TEST_NEEDED,// 테스트 모드 윷 선택 필요
        MOVE_RESULT,// 말 이동 결과
        TOKEN_CAUGHT,// 말 잡기
        GAME_ENDED,// 게임 종료
        TURN_CHANGED,// 턴 변경
        ERROR_OCCURRED,// 오류 발생
        TOKEN_SELECTION_NEEDED,// 토큰 선택 필요
        BRANCH_SELECTION_NEEDED,// 분기 선택 필요
        MESSAGE_CONFIRMED,// 사용자가 메시지 확인 완료
        REORDER_NEEDED// 윷 결과 재배열 필요
    }
    
    private final Type type;
    private final Object data;
    private final String message;
    
    public GameEvent(Type type, Object data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }
    
    public GameEvent(Type type, String message) {
        this(type, null, message);
    }
    
    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
    
    public <T> T getData(Class<T> clazz) {
        return clazz.cast(data);
    }
} 