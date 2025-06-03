package com.cas.yutnorifx.model.core;

import com.cas.yutnorifx.model.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenPositionManager {
    private final Map<Token, BoardNode> tokenPositions;
    private final Board board;

    public TokenPositionManager(Board board) {
        this.board = board;
        this.tokenPositions = new HashMap<>();
    }

    public void placeTokenAtStart(Token token) {
        BoardNode startNode = board.getStartNode();
        if (startNode != null && token.getState() == TokenState.READY) {
            token.setState(TokenState.ACTIVE);
            startNode.enter(token);
            tokenPositions.put(token, startNode);
        }
    }

    public BoardNode getTokenPosition(Token token) {
        return tokenPositions.get(token);
    }

    public void updateTokenPosition(Token token, BoardNode newPosition) {
        if (newPosition == null) {
            tokenPositions.remove(token);
        } else {
            tokenPositions.put(token, newPosition);
        }
    }

    public Board getBoard() {
        return board;
    }

    public boolean moveTokenToNode(Token token, BoardNode targetNode) {
        Token actualToken = token.getTopMostToken();
        
        if (actualToken.getState() != TokenState.ACTIVE) {
            return false;
        }
        
        List<Token> stackedTokens = new ArrayList<>(actualToken.getStackedTokens());
        
        BoardNode currentNode = getTokenPosition(actualToken);
        if (currentNode != null) {
            currentNode.leave(actualToken);
        }
        
        if (targetNode != null) {
            targetNode.enter(actualToken);
            updateTokenPosition(actualToken, targetNode);
            
            for (Token stackedToken : stackedTokens) {
                updateTokenPosition(stackedToken, targetNode);
            }
        } else {
            updateTokenPosition(actualToken, null);
            actualToken.setState(TokenState.FINISHED);
            
            for (Token stackedToken : stackedTokens) {
                updateTokenPosition(stackedToken, null);
                stackedToken.setState(TokenState.FINISHED);
            }
            actualToken.clearStackedTokens();
        }
        
        return true;
    }
    

    public void resetTokenToStart(Token token) {
        for (Token stacked : token.getStackedTokens()) {
            updateTokenPosition(stacked, null);
            stacked.setState(TokenState.READY);
            stacked.clearStackedTokens();
        }
        
        BoardNode currentNode = getTokenPosition(token);
        if (currentNode != null) {
            currentNode.leave(token);
        }
        updateTokenPosition(token, null);
        token.setState(TokenState.READY);
        token.clearStackedTokens();
    }
    
    public boolean handleTokenInteractions(Token movedToken, BoardNode targetNode) {
        if (targetNode == null) return false;
        
        boolean caught = false;
        List<Token> tokensOnNode = new ArrayList<>(targetNode.getTokens());
        
        for (Token t : tokensOnNode) {
            if (t != movedToken && t.getOwner() != movedToken.getOwner()) {
                resetTokenToStart(t);
                caught = true;
            }
        }
        
        for (Token t : tokensOnNode) {
            if (t != movedToken && t.getOwner() == movedToken.getOwner()) {
                List<Token> existingStackedTokens = new ArrayList<>(t.getStackedTokens());
                
                targetNode.leave(t);
                updateTokenPosition(t, null);
                
                movedToken.addStackedToken(t);
                
                for (Token stackedToken : existingStackedTokens) {
                    movedToken.addStackedToken(stackedToken);
                }
                
                t.clearStackedTokens();
            }
        }
        
        return caught;
    }
} 