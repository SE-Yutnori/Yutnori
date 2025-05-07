package view;

import model.BoardNode;
import model.Player;
import model.Token;
import model.TokenState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//윷놀이 판을 그리는 클래스
public class BoardPanel extends JPanel {
    private List<BoardNode> nodes;
    private List<Player> players;

    private int spacing = 180;
    private int offset = 10;

    //각 플레이어 말 색상은 기본으로 지정
    private final Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA};

    public BoardPanel(List<BoardNode> nodes, List<Player> players) {
        this.nodes = nodes;
        this.players = players;

        setPreferredSize(new Dimension(spacing * 8, spacing * 5));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D arrow = (Graphics2D) g;

        // 노드 간 연결선 및 분기점 연결선은 화살표로 그리기 - 이게 기존에 제안한 방식....
//        for (BoardNode node : nodes) {
//            List<BoardNode> nextNodes = node.getNextNodes();
//            int x1 = (int) (node.getX() * spacing + offset);
//            int y1 = (int) (node.getY() * spacing + offset);
//
//            for (BoardNode next : nextNodes) {
//                int x2 = (int) (next.getX() * spacing + offset);
//                int y2 = (int) (next.getY() * spacing + offset);
//
//                if (nextNodes.size() >= 2 || node.getName().equals("Edge0-0")) {
//                    double ratio = (Math.hypot(x2 - x1, y2 - y1) - 22) / Math.hypot(x2 - x1, y2 - y1);
//                    int tx = (int) (x1 + (x2 - x1) * ratio);
//                    int ty = (int) (y1 + (y2 - y1) * ratio);
//
//                    arrow.setStroke(new BasicStroke(3));
//                    arrow.setColor(Color.darkGray);
//                    arrow.drawLine(x1, y1, tx, ty);
//                    drawArrowHead(arrow, x1, y1, tx, ty);
//
//                    int midX = (2 * x1 + 3 * x2) / 5;
//                    int midY = (2 * y1 + 3 * y2) / 5;
//                    String label = next.getName().contains("Edge") ? "1" : next.getName().replace("ToCenter", "");
//
//                    arrow.setFont(new Font("Arial", Font.BOLD, 18));
//                    arrow.setColor(Color.red);
//                    arrow.drawString(label, midX, midY);
//                } else {
//                    arrow.setStroke(new BasicStroke(1));
//                    arrow.setColor(Color.darkGray);
//                    arrow.drawLine(x1, y1, x2, y2);
//                }
//            }
//        }

        // 노드 간 연결선 및 분기점 연결선은 화살표로 그리기 - 이게 교수님께서 제안한 방식....
        for (BoardNode node : nodes) {
            List<BoardNode> nextNodes = node.getNextNodes();
            int size = nextNodes.size();
            int x1 = (int) (node.getX() * spacing + offset);
            int y1 = (int) (node.getY() * spacing + offset);

            if (size >= 2 || node.getName().equals("Edge0-0")) {
                for (int k = 1; k < size - 1; k++) {
                    BoardNode mid = nextNodes.get(k);
                    int mx = (int) (mid.getX() * spacing + offset);
                    int my = (int) (mid.getY() * spacing + offset);
                    arrow.setStroke(new BasicStroke(1));
                    arrow.setColor(Color.darkGray);
                    arrow.drawLine(x1, y1, mx, my);
                }

                int[] idx = new int[]{ 0, size - 1 };
                for (int j : idx) {
                    if (j < 0 || j >= size) continue;
                    BoardNode next = nextNodes.get(j);
                    int x2 = (int) (next.getX() * spacing + offset);
                    int y2 = (int) (next.getY() * spacing + offset);

                    double dist = Math.hypot(x2 - x1, y2 - y1);
                    double ratio = (dist - 22) / dist;
                    int tx = (int) (x1 + (x2 - x1) * ratio);
                    int ty = (int) (y1 + (y2 - y1) * ratio);

                    arrow.setStroke(new BasicStroke(3));
                    arrow.setColor(Color.darkGray);
                    arrow.drawLine(x1, y1, tx, ty);
                    drawArrowHead(arrow, x1, y1, tx, ty);

                    int midX = (2 * x1 + 3 * x2) / 5;
                    int midY = (2 * y1 + 3 * y2) / 5;
                    String label = next.getName().contains("Edge")
                            ? "1"
                            : next.getName().replace("ToCenter", "");
                    arrow.setFont(new Font("Arial", Font.BOLD, 18));
                    arrow.setColor(Color.red);
                    arrow.drawString(label, midX, midY);
                }
            } else if (size == 1) {
                BoardNode only = nextNodes.get(0);
                int x2 = (int) (only.getX() * spacing + offset);
                int y2 = (int) (only.getY() * spacing + offset);

                arrow.setStroke(new BasicStroke(1));
                arrow.setColor(Color.darkGray);
                arrow.drawLine(x1, y1, x2, y2);
            }
        }

        // 노드 (원) 그리기
        for (BoardNode node : nodes) {
            int x = (int) (node.getX() * spacing + offset);
            int y = (int) (node.getY() * spacing + offset);
            String name = node.getName();

            int out = 56;
            int radius = 36;
            if (name.equals("Center") || name.matches("Edge\\d+-5") || name.matches("Edge\\d+-0")) {
                int outX = x - out / 2;
                int outY = y - out / 2;
                g.setColor(Color.white);
                g.fillOval(outX, outY, out, out);
                g.setColor(Color.BLACK);
                g.drawOval(outX, outY, out, out);

                int in = 36;
                int inX = x - in / 2;
                int inY = y - in / 2;
                g.setColor(Color.white);
                g.fillOval(inX, inY, in, in);
                g.setColor(Color.black);
                g.drawOval(inX, inY, in, in);
            } else {
                int drawX = x - radius / 2;
                int drawY = y - radius / 2;
                g.setColor(Color.white);
                g.fillOval(drawX, drawY, radius, radius);
                g.setColor(Color.black);
                g.drawOval(drawX, drawY, radius, radius);
            }

            // 시작 노드에 "start" 글자 추가 (여기서는 이름이 "Edge0-0" 인 노드로 가정)
            if (name.equals("Edge0-0")) {
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(Color.RED);
                int textX = x - 15;
                int textY = y - out/2;
                g.drawString("start", textX, textY);
            }
        }


        // 말 표시 (위에는 이름 - 업힌 말들 표시)
        for (BoardNode node : nodes) {
            List<Token> tokens = node.getTokens();

            List<Token> activeTokens = tokens.stream()
                    .filter(t -> t.getState() == TokenState.ACTIVE)
                    .toList();

            if (activeTokens.isEmpty()) continue;

            Token top = activeTokens.get(0);
            Player owner = top.getOwner();
            int playerIndex = players.indexOf(owner);
            Color color = playerColors[playerIndex % playerColors.length];

            int cx = (int) (node.getX() * spacing + offset - 10);
            int cy = (int) (node.getY() * spacing + offset - 10);

            // 말 원 그리기
            g.setColor(color);
            g.fillOval(cx, cy, 20, 20);
            g.setColor(Color.BLACK);
            g.drawOval(cx, cy, 20, 20);

            // 이름 표시: Player1-1,2,3 식
            String baseName = top.getName().split("-")[0];
            List<String> indices = activeTokens.stream()
                    .map(t -> t.getName().split("-")[1])
                    .toList();

            String label = baseName + " - " + String.join(",", indices);

            // 이름 표시
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString(label, cx - 5, cy - 5);
        }
    }

    // 화살표 머리 그리기 수작업...
    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double rho = theta + Math.toRadians(30);
        int x = (int) (x2 - 8 * Math.cos(rho));
        int y = (int) (y2 - 8 * Math.sin(rho));
        g2.drawLine(x2, y2, x, y);

        rho = theta - Math.toRadians(30);
        x = (int) (x2 - 8 * Math.cos(rho));
        y = (int) (y2 - 8 * Math.sin(rho));
        g2.drawLine(x2, y2, x, y);
    }
}
