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

        //Center노드의 X,Y 좌표 설정
        float centerX = 2.5f;
        float centerY = 2.5f;

        //Center노드 생성자로 생성
        BoardNode center = new BoardNode("Center", centerX, centerY, sides);
        // 노드 리스트에 추가
        nodes.add(center);
        // 리스트 생성
        // 외곽 변 노드 리스트
        BoardNode[][] edgeNodes = new BoardNode[sides][6];
        //Center로 향하는 변에 위치한 노드 리스트 2개 생성
        BoardNode[] toCenterPath1 = new BoardNode[sides];
        BoardNode[] toCenterPath2 = new BoardNode[sides];
        return nodes;
    }
}
