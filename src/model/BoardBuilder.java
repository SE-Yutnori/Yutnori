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

        // 외곽 노드 만들기
        for (int i = 0; i < sides; i++) {
            double angle1 = 2 * Math.PI * i / sides - Math.PI / 2;
            double angle2 = 2 * Math.PI * (i + 1) / sides - Math.PI / 2;

            float x1 = (float)(Math.cos(angle1) * radius + centerX);
            float y1 = (float)(Math.sin(angle1) * radius + centerY);
            float x2 = (float)(Math.cos(angle2) * radius + centerX);
            float y2 = (float)(Math.sin(angle2) * radius + centerY);

            for (int j = 0; j < 6; j++) {
                float t = j / 5f;
                float x = x1 + (x2 - x1) * t;
                float y = y1 + (y2 - y1) * t;

                if (j == 0 && i > 0) {
                    // 이전 면의 마지막 노드와 현재 면의 첫 노드가 동일
                    edgeNodes[i][j] = edgeNodes[i - 1][5];
                } else {
                    BoardNode node = new BoardNode("Edge" + i + "-" + j, x, y, sides);
                    edgeNodes[i][j] = node;
                    nodes.add(node);
                }
            }
        }

        // 골목길(센터로 가는) 노드 만들기
        for (int i = 0; i < sides; i++) {
            float cx = edgeNodes[i][0].getX();
            float cy = edgeNodes[i][0].getY();

            float dx = centerX - cx;
            float dy = centerY - cy;

            float midX1 = cx + dx / 3f;
            float midY1 = cy + dy / 3f;
            float midX2 = cx + dx * 2 / 3f;
            float midY2 = cy + dy * 2 / 3f;

            BoardNode path1 = new BoardNode("ToCenter" + i + "-1", midX1, midY1, sides);
            BoardNode path2 = new BoardNode("ToCenter" + i + "-2", midX2, midY2, sides);

            toCenterPath1[i] = path1;
            toCenterPath2[i] = path2;

            nodes.add(path1);
            nodes.add(path2);
        }

        // 각 노드 연결 (분기 및 이동 방향 고려, 사실 빽도를 제외하고는 전부 정방향 이동 - 결국은 도착지로 가게끔)
        for (int i = 0; i < sides; i++) {
            // 외곽 노드 연결
            for (int j = 0; j < 5; j++) {
                edgeNodes[i][j].addNextNode(edgeNodes[i][j + 1]);
            }

            // 꼭짓점과 센터 노드 연결
            if (i != 0 && i <= (sides + 1) / 2) {
                edgeNodes[i][0].addNextNode(toCenterPath1[i]);
                toCenterPath1[i].addNextNode(toCenterPath2[i]);
                toCenterPath2[i].addNextNode(center);
            } else {
                center.addNextNode(toCenterPath2[i]);
                toCenterPath2[i].addNextNode(toCenterPath1[i]);
                if (i == 0) {
                    toCenterPath1[i].addNextNode(edgeNodes[sides - 1][5]);
                } else {
                    toCenterPath1[i].addNextNode(edgeNodes[i - 1][5]);
                }
            }
        }

        return nodes;
    }
}
