package com.cas.yutnorifx.model.request;

/**
 * 테스트 모드 윷 선택 응답 정보를 담는 클래스
 */
public class YutTestResponse {
    private final String requestId;
    private final int selectedYutResult;
    private final boolean cancelled;
    
    public YutTestResponse(String requestId, int selectedYutResult) {
        this.requestId = requestId;
        this.selectedYutResult = selectedYutResult;
        this.cancelled = false;
    }
    
    public YutTestResponse(String requestId, boolean cancelled) {
        this.requestId = requestId;
        this.selectedYutResult = -999;
        this.cancelled = cancelled;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public int getSelectedYutResult() {
        return selectedYutResult;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
} 