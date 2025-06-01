package com.cas.yutnorifx.model.request;

//테스트 모드 윷 선택 요청 정보를 담는 클래스
public class YutTestRequest {
    private final String playerName;
    private final String requestId;
    
    public YutTestRequest(String playerName, String requestId) {
        this.playerName = playerName;
        this.requestId = requestId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getRequestId() {
        return requestId;
    }
} 