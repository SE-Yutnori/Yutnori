package com.cas.yutnorifx.model.request;

import com.cas.yutnorifx.model.entity.*;
import java.util.List;

/**
 * 토큰 선택 요청 정보를 담는 클래스
 */
public class TokenSelectionRequest {
    private final List<Token> availableTokens;
    private final int steps;
    private final String requestId;
    
    public TokenSelectionRequest(List<Token> availableTokens, int steps, String requestId) {
        this.availableTokens = availableTokens;
        this.steps = steps;
        this.requestId = requestId;
    }
    
    public List<Token> getAvailableTokens() {
        return availableTokens;
    }
    
    public int getSteps() {
        return steps;
    }
    
    public String getRequestId() {
        return requestId;
    }
} 