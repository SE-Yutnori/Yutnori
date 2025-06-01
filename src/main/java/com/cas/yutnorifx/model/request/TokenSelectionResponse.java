package com.cas.yutnorifx.model.request;

import com.cas.yutnorifx.model.entity.*;

//토큰 선택 응답 정보를 담는 클래스
public class TokenSelectionResponse {
    private final String requestId;
    private final Token selectedToken;
    private final boolean cancelled;
    
    public TokenSelectionResponse(String requestId, Token selectedToken) {
        this.requestId = requestId;
        this.selectedToken = selectedToken;
        this.cancelled = false;
    }
    
    public TokenSelectionResponse(String requestId, boolean cancelled) {
        this.requestId = requestId;
        this.selectedToken = null;
        this.cancelled = cancelled;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public Token getSelectedToken() {
        return selectedToken;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
} 