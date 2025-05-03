package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어의 정보가 담긴 클래스
 */
public class Player {
    //player 이름 attribute
    private String name;
    //player 보유 토큰 리스트
    private List<Token> tokens;

    /**
     * Player 생성자
     * 토큰 개수 validation 및 tokenCount만큼 token 생성하여 list에 추가
     * @param name : GameLauncher에서 값 입력받아 설정
     * @param tokenCount : GameLauncher에서 값 입력받아 설정
     */
    public Player(String name, int tokenCount) {
        this.name = name;
        this.tokens = new ArrayList<>();

        // 토큰 갯수가 2~5 범위인지 확인 (그 외의 범위 들어올 시 4로 설정인데 아마 경고창 때문에 그런 값 안 들어올 듯..)
        if (tokenCount < 2 || tokenCount > 5) {
            tokenCount = 4;
        }

        // tokenCount 만큼 말 생성
        for (int i = 1; i <= tokenCount; i++) {
            String tokenName = name + "-" + i;
            Token token = new Token(tokenName,  this);
            tokens.add(token);
        }
    }

    public String getName() {
        return name;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * 모든 토큰이 FINISHED 상태인 지를 통해 승리 플레이어 결정
     * @return : 해당 플레이어 승리 여부 반환 (boolean)
     */
    public boolean hasFinished() {
        for (Token token : tokens) {
            if (token.getState() != TokenState.FINISHED) {
                return false;
            }
        }
        return true;
    }
}

