package com.cas.yutnorifx.model.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//윷 결과 재배열 요청 정보를 담는 클래스
public class ReorderRequest {
    private final List<Integer> originalResults;
    private final String playerName;
    private final String requestId;
    
    public ReorderRequest(List<Integer> originalResults, String playerName, String requestId) {
        this.originalResults = new ArrayList<>(originalResults);
        this.playerName = playerName;
        this.requestId = requestId;
    }
    
    public List<Integer> getOriginalResults() { 
        return new ArrayList<>(originalResults); 
    }
    
    public String getRequestId() { 
        return requestId; 
    }
    
    public String getPromptMessage() {
        String originalStr = originalResults.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return playerName + "님의 윷 결과: [" + originalStr + "]\n" +
               "원하는 말 이동 순서 (예: 4,3)";
    }
} 