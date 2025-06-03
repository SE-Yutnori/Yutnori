package com.cas.yutnorifx.model.request;

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