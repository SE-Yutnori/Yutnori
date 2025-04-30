package view;

import model.BoardNode;
import model.Token;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//보드의 모습을 인터페이스에 보여주는 클래스
public class BoardPanel extends JPanel {
    private List<BoardNode> nodes;
    private Token token; // 일단은 테스트라 토큰 한개만... 나중에 게임 만들 때는 List<Token>으로 바꾸기
    //보드 크기 얼마나 키울 건지
    private int spacing = 180;
    //창에서의 위치 어디쯤 할건지
    private int offset = 10;


    //node랑 token 보여주기
    public BoardPanel(List<BoardNode> nodes, Token token) {
        this.nodes = nodes;
        this.token = token;

        // 패널 크기 및 배경 설정 위에 spacing, offset, setPreferredSize는 적당한 크기로 알아서...
        setPreferredSize(new Dimension(spacing * 8, spacing * 5));
        //색도 흰색으로 배경설정
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 노드 간 연결선 (분기랑 분기가 아닌 연결선 나누기)
        Graphics2D arrow = (Graphics2D) g; //분기를 화살표로 보여주기 위함
        for (int i = 0; i < nodes.size(); i++) {
            BoardNode node = nodes.get(i);
            List<BoardNode> nextNodes = node.getNextNodes();
            //기준 노드 좌표
            int x1 = (int) (node.getX() * spacing + offset);
            int y1 = (int) (node.getY() * spacing + offset);

            for (int j = 0; j < nextNodes.size(); j++) {
                BoardNode next = nextNodes.get(j);
                //기준 노드 다음 노드 좌표
                int x2 = (int) (next.getX() * spacing + offset);
                int y2 = (int) (next.getY() * spacing + offset);

                // 분기 점 있는 애들이랑 시작점은 방향을 굵은 선 + 화살표
                if (nextNodes.size() >= 2 || node.getName().equals("Edge0-0")) {
                    //화살표가 원에 가려지기 때문에 길이를 일부 줄여야 함..
                    double ratio = (Math.hypot(x2 - x1, y2 - y1) - 22) / Math.hypot(x2 - x1, y2 - y1);
                    //화살표 끝좌표
                    int tx = (int) (x1 + (x2 - x1) * ratio);
                    int ty = (int) (y1 + (y2 - y1) * ratio);

                    // 굵은 선 + 화살표 UI
                    arrow.setStroke(new BasicStroke(3));
                    arrow.setColor(Color.darkGray);
                    arrow.drawLine(x1, y1, tx, ty);
                    drawArrowHead(arrow, x1, y1, tx, ty); // 이 지점에 화살표 끝부분

                    // 분기 방향을 표시해 줄 좌표 (3/5 지점이 적당해 보여서 설정)
                    int midX = (2*x1 + 3*x2) / 5;
                    int midY = (2*y1 + 3*y2) / 5;

                    //외곽 분기는 1로 표시하고 내부 골목길에서의 방향은 임의의 숫자(node를 생성할 때 쓴..)로 표시
                    String label;
                    if (next.getName().contains("Edge")) {
                        label = "1";
                    } else {
                        label = next.getName().replace("ToCenter", "");
                    }

                    //분기 방향 UI
                    arrow.setFont(new Font("Arial", Font.BOLD, 18));
                    arrow.setColor(Color.red);
                    arrow.drawString(label, midX, midY);
                } else {
                    // 노드 간 연결선 UI
                    arrow.setStroke(new BasicStroke(1)); // 얇게
                    arrow.setColor(Color.darkGray);
                    arrow.drawLine(x1, y1, x2, y2);
                }
            }
        }

        // 노드 그리기 - 예시와 같은 디자인으로..
        for (int i = 0; i < nodes.size(); i++) {
            BoardNode node = nodes.get(i);
            // 노드 찍기
            int x = (int) (node.getX() * spacing + offset);
            int y = (int) (node.getY() * spacing + offset);

            // Center 노드 or 꼭짓점 노드라면 더 크게
            // 이름으로 특이점(분기점)인 지 확인 - 특이점이면 이중 원
            String name = node.getName();
            if (name.equals("Center") || name.matches("Edge\\d+-5")|| name.matches("Edge\\d+-0")){
                // 큰 원
                int out = 56;
                int outX = (int) (node.getX() * spacing + offset - out / 2);
                int outY = (int) (node.getY() * spacing + offset - out / 2);

                //바깥 원 UI
                g.setColor(Color.white);
                g.fillOval(outX, outY, out, out);
                g.setColor(Color.BLACK);
                g.drawOval(outX, outY, out, out);

                // 작은 원
                int in = 36;
                int inX = (int) (node.getX() * spacing + offset - in / 2);
                int inY = (int) (node.getY() * spacing + offset - in / 2);

                // 안쪽 원 UI
                g.setColor(Color.white);
                g.fillOval(inX, inY, in, in);
                g.setColor(Color.black);
                g.drawOval(inX, inY, in, in);
            } else {
                // 나머지 기본 노드
                int radius = 36;
                int drawX = (int) (node.getX() * spacing + offset - radius / 2);
                int drawY = (int) (node.getY() * spacing + offset - radius / 2);

                //기본 노드 UI
                g.setColor(Color.white);
                g.fillOval(drawX, drawY, radius, radius);
                g.setColor(Color.black);
                g.drawOval(drawX, drawY, radius, radius);
            }
        }

        // 테스트 말
        if (token != null) {
            BoardNode current = token.getCurrentNode();
            int cx = (int) (current.getX() * spacing + offset - 10);
            int cy = (int) (current.getY() * spacing + offset - 10);
            g.setColor(Color.RED);
            g.fillOval(cx, cy, 20, 20);
            g.setColor(Color.BLACK);
            g.drawOval(cx, cy, 20, 20);
        }
    }
    //화살표 끝부분을 수동으로 만드는 코드
    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
        //연결 노드의 기울기
        double theta = Math.atan2(y2 - y1, x2 - x1);

        //한쪽 날개 생성
        double rho = theta + Math.toRadians(30);
        int x = (int) (x2 - 8 * Math.cos(rho));
        int y = (int) (y2 - 8 * Math.sin(rho));
        g2.drawLine(x2, y2, x, y);

        //반대쪽 날개 생성
        rho = theta - Math.toRadians(30);
        x = (int) (x2 - 8 * Math.cos(rho));
        y = (int) (y2 - 8 * Math.sin(rho));
        g2.drawLine(x2, y2, x, y);
    }
}
