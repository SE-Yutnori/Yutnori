package model;

import java.util.ArrayList;
import java.util.List;

//노드 생성 및 연결 코드
public class BoardBuilder {
    //보드의 각형 수를 커스텀할 수 있는 메서드
    public static List<BoardNode> buildCustomizingBoard(int sides, float radius) {
        if (sides < 3) {
            throw new IllegalArgumentException("보드는 최소 3각형 이상이어야 합니다.");
        }

        List<BoardNode> nodes = new ArrayList<>();

        return nodes;
    }
}
