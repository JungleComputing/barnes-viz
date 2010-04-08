/*
 * Created on Mar 29, 2006 by rob
 */
package viz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JComponent;

class SpeedInfoElement {
    int iteration;

    long stamp;

    double speed;

    public SpeedInfoElement(int iteration, double speed, long stamp) {
        this.iteration = iteration;
        this.speed = speed;
        this.stamp = stamp;
    }
}

class SpeedInfo {
    ArrayList speedData = new ArrayList();

    public synchronized void add(SpeedInfoElement e) {
        speedData.add(e);
    }

    public synchronized double getSpeedAt(long time) {
        double prevSpeed = 0;
        for (int i = 0; i < speedData.size(); i++) {
            SpeedInfoElement elt = (SpeedInfoElement) speedData.get(i);

            if (elt.stamp > time) {
                return prevSpeed;
            }
            prevSpeed = elt.speed;
        }

        return prevSpeed;
    }

    public synchronized double maxSpeedInSeries(long beginStamp) {
        double maxSpeed = -1;

        for (int i = 0; i < speedData.size(); i++) {
            SpeedInfoElement elt = (SpeedInfoElement) speedData.get(i);

            if (elt.stamp < beginStamp) continue;

            if (elt.speed > maxSpeed) {
                maxSpeed = elt.speed;
            }
        }

        return maxSpeed;
    }
}

public class InfoComponent extends JComponent {

    static final int LOAD_GRAPH_SIZE = 200;

    static final int LOAD_GRAPH_X_OFFSET = 10;

    static final int LOAD_GRAPH_Y_OFFSET = 50;

    static final boolean LOAD_GRAPH_TRANSPARENT = true;

    public static final int LOAD_MEMORY = 1000 * 60; // in millis

    Visualization v;

    private int lastIteration;

    private long lastStamp;

    private double lastSpeed;

    java.text.NumberFormat nf;

    private SpeedInfo speedInfo = new SpeedInfo();

    InfoComponent(Visualization v) {
        this.v = v;

        nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);

        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        setPreferredSize(new Dimension(240, 500));
        setSize(new Dimension(240, 500));
        setVisible(true);
    }

    public void update(Graphics g) {
        System.err.println("update");
        paint(g);
    }

    private void drawLoadGraph(Graphics g) {
        long time = System.currentTimeMillis();
        long begin = time - LOAD_MEMORY;
        double maxYScale;
        double maxSpeed = speedInfo.maxSpeedInSeries(begin);
        int scaleIncrement;
        
        if (maxSpeed < 5) {
            maxYScale = maxSpeed + (maxSpeed % 1 == 0 ? 0 : (1 - maxSpeed % 1));
            if (maxYScale == 0) maxYScale = 1;
            scaleIncrement = 1;
        } else if (maxSpeed < 100) {
            maxYScale = maxSpeed + (maxSpeed % 5 == 0 ? 0 : (5 - maxSpeed % 5));
            if (maxYScale == 0) maxYScale = 5;
            scaleIncrement = 5;
        } else {
            maxYScale = maxSpeed
                + (maxSpeed % 100 == 0 ? 0 : (100 - maxSpeed % 100));
            scaleIncrement = 50;
        }

        if (!LOAD_GRAPH_TRANSPARENT) {
            g.setColor(Color.BLACK);
            g.fillRect(LOAD_GRAPH_X_OFFSET - 10, LOAD_GRAPH_Y_OFFSET - 10,
                LOAD_GRAPH_SIZE + 40, LOAD_GRAPH_SIZE + 20);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(LOAD_GRAPH_X_OFFSET, LOAD_GRAPH_Y_OFFSET, LOAD_GRAPH_SIZE,
            LOAD_GRAPH_SIZE);

        double Y_SCALE_FACTOR = (double) LOAD_GRAPH_SIZE / maxYScale;
        double X_SCALE_FACTOR = (double) LOAD_GRAPH_SIZE / LOAD_MEMORY;

        // draw some grid lines in the graph
        for (int i = 0; i <= maxYScale; i += scaleIncrement) {
            int y = LOAD_GRAPH_Y_OFFSET + LOAD_GRAPH_SIZE
                - (int) (i * Y_SCALE_FACTOR);
            int x = LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE + 10;
            g.setColor(Color.WHITE);
            g.drawLine(LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE, y,
                LOAD_GRAPH_X_OFFSET + LOAD_GRAPH_SIZE + 4, y);
            g.setColor(Color.DARK_GRAY);
            g.drawLine(LOAD_GRAPH_X_OFFSET + 1, y, LOAD_GRAPH_X_OFFSET
                + LOAD_GRAPH_SIZE - 1, y);

            g.setColor(Color.WHITE);
            g.drawString("" + i, x, y + 5);
        }

        for (int x = 1; x < LOAD_GRAPH_SIZE; x++) {
            long xTime = begin + (long) (x / X_SCALE_FACTOR);

            double mySpeed = speedInfo.getSpeedAt(xTime);

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(LOAD_GRAPH_X_OFFSET + x, LOAD_GRAPH_Y_OFFSET
                + LOAD_GRAPH_SIZE, LOAD_GRAPH_X_OFFSET + x, LOAD_GRAPH_Y_OFFSET
                + LOAD_GRAPH_SIZE - (int) (mySpeed * Y_SCALE_FACTOR));
        }

        // draw title
        g.setColor(Color.WHITE);
        Font old = g.getFont();
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Iterations per second", LOAD_GRAPH_X_OFFSET,
            LOAD_GRAPH_Y_OFFSET - 30);
        g.setFont(old);
    }

    public void paint(Graphics g) {
        Rectangle r = g.getClipBounds();
        g.setColor(Color.BLACK);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);

        Snapshot snapshot = v.getSnapshot();

        double speed = lastSpeed;
        if (snapshot != null && snapshot.getIteration() != lastIteration ) {
            long stamp = System.currentTimeMillis();
            speed = 1.0 / (snapshot.getRuntime() / 1000.0);
            lastSpeed = speed;
            lastStamp = stamp;
            lastIteration = snapshot.getIteration();
            SpeedInfoElement e = new SpeedInfoElement(lastIteration, lastSpeed,
                lastStamp);
            speedInfo.add(e);
        }

        String speedString = nf.format(speed);

        if (snapshot != null) {
            Font old = g.getFont();
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g
                .drawString("Simulating " + snapshot.getBodyCount() + " stars",
                    LOAD_GRAPH_X_OFFSET, 50 + LOAD_GRAPH_Y_OFFSET
                        + LOAD_GRAPH_SIZE);

            g
                .drawString("iteration: " + snapshot.getIteration(),
                    LOAD_GRAPH_X_OFFSET, 90 + LOAD_GRAPH_Y_OFFSET
                        + LOAD_GRAPH_SIZE);

            g
                .drawString(speedString + " iterations / s",
                    LOAD_GRAPH_X_OFFSET, 130 + LOAD_GRAPH_Y_OFFSET
                        + LOAD_GRAPH_SIZE);
            g.setFont(old);
        }

        drawLoadGraph(g);
    }
    
    void reset() {
        speedInfo = new SpeedInfo();
        lastIteration = 0;
        lastStamp = 0;
        lastSpeed = 0.0;
    }
}
