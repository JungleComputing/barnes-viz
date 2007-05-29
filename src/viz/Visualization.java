package viz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class Visualization implements GLEventListener, MouseListener,
        MouseMotionListener, MouseWheelListener, KeyListener {

    private static final long serialVersionUID = 1L;

    private static final boolean VERBOSE_MOUSE = false;

    private static final boolean SHOW_TAILS = true;

    private static boolean SHOW_LOAD_GRAPH = true;

    private static BodyHistory history;

    private static BodyProducer producer;

    private Texture texture = null;

    private float particleSizeMax = 16.0f;

    private float particleSizeMin = 8.0f;

    private GLCanvas canvas;

    private boolean haveRotation = false;

    private int showCount = 3;

    private float view_rotx;

    private float view_roty;

    private float view_rotz;

    private float trans_x;

    private float trans_y;

    private float zoom = 0.03f;

    private int prevMouseX, prevMouseY;

    private int width, height;

    private boolean first = true;

    private boolean autoRotate = false;

    private double maxX = 1.5;

    private double maxY = 1.5;

    private double maxZ = 1.5;

    private double minX = -1.5;

    private double minY = -1.5;

    private double minZ = -1.5;

    private int numBodies = 200;

    protected float[] colors;

    private InfoComponent info;

    Snapshot snapshot;

    private Visualization(String model) {
        initScreen();
        boolean fromFile = false;

        if (model.equals("Simple")) {
            initColors(numBodies);
            producer = new SimpleBodyProducer(numBodies);
        } else if (model.equals("Plummer")) {
            initColors(numBodies);
            producer = new PlummerBodyProducer(numBodies);
        } else if (model.equals("Communicating")) {
            producer = new CommunicatingBodyProducer(this);
        } else if (model.equals("Ibis")) {
            producer = new IbisBodyProducer(this);
        } else {
            try {
                producer = new FromFileProducer(model);
                fromFile = true;
            } catch (Exception e) {
                throw new RuntimeException("Unknown model: " + model);
            }
        }
        history = new BodyHistory(producer, fromFile);
    }

    private void initScreen() {
        GLCapabilities glCaps = new GLCapabilities();
        glCaps.setRedBits(8);
        glCaps.setBlueBits(8);
        glCaps.setGreenBits(8);
        glCaps.setAlphaBits(8);

        glCaps.setHardwareAccelerated(true);
        glCaps.setDoubleBuffered(true);

        info = new InfoComponent(this);

        canvas = new GLCanvas(glCaps);

        canvas.addGLEventListener(this);

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseWheelListener(this);

        canvas.setBackground(Color.BLACK);
        
        //        canvas.setSize(256, 256);
        canvas.setVisible(true);
    }

    public void initColors(int numBodies) {

        float[] old = colors;

        // Initialize the colors randomly
        colors = new float[3 * numBodies];

        if (old == null) {
            for (int i = 0; i < 3 * numBodies; i++) {
                colors[i] = (float) (0.4 + (Math.random() / 2.0));
            }
        } else {
            for (int i = 0; i < old.length; i++) {
                colors[i] = old[i];
            }

            for (int i = old.length; i < 3 * numBodies; i++) {
                colors[i] = (float) (0.4 + (Math.random() / 2.0));
            }
        }

        colors[0] = colors[1] = colors[2] = 1.0f;
    }

    public void init(GLAutoDrawable glDrawable) {
        GL gl = glDrawable.getGL();//Get the GL object from glDrawable
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL.GL_FASTEST);

        // Load the texture for the bodies...
        try {
            texture = TextureIO.newTexture(new File("particle.png"), true);
//            System.out.println("Texture loaded!!");
        } catch (Exception e) {
            System.err.println("Failed to load texture " + e);
        }
        if (texture != null) {
            texture.bind();
            if(!SHOW_TAILS) {
                texture.enable();
            }
        }
    }

    private void drawBodies(GL gl) {

        float quadratic[] = { 0.0f, 0.0f, 0.01f };

        gl.glPointParameterfvARB(GL.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
        gl.glPointParameterfARB(GL.GL_POINT_SIZE_MAX_ARB, particleSizeMax);
        gl.glPointParameterfARB(GL.GL_POINT_SIZE_MIN_ARB, particleSizeMin);

        gl.glPointSize(particleSizeMax);

        gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB,
                GL.GL_TRUE);

        gl.glEnable(GL.GL_POINT_SPRITE_ARB);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);

        snapshot = history.getSnapShot();

        if (first) {

//            System.out.println("Getting extremes");

            snapshot.determineExtremes();

            minX = minY = minZ = -snapshot.mostExtreme;
            maxX = maxY = maxZ = snapshot.mostExtreme;

            first = false;
        }

        if (snapshot.history[0] == null) {
            return;
        }

        if (texture != null && SHOW_TAILS) {
            texture.enable();
        }

        int bodyCount = snapshot.history[0].getBodies().length;

        if (colors == null || colors.length < bodyCount) {
            initColors(bodyCount);
        }

        gl.glBegin(GL.GL_POINTS);

        for (int i = 0; i < bodyCount; i += 3) {
            gl.glColor3fv(colors, i);
            gl.glVertex3fv(snapshot.history[0].getBodies(), i);
        }

        gl.glEnd();

        gl.glDisable(GL.GL_POINT_SPRITE_ARB);

        if (texture != null && SHOW_TAILS) {
            texture.disable();
        }

        if (SHOW_TAILS) {

            gl.glLineWidth(1.0f);

            for (int i = 0; i < bodyCount; i += 3) {

                gl.glBegin(GL.GL_LINE_STRIP);

                int index = 0;
                int max = snapshot.history.length;

                while (index < max && snapshot.history[index] != null) {
                    float alpha = (float) (max - index) / (float) max;
                    gl.glColor4f(colors[i], colors[i + 1], colors[i + 2],
                                    alpha);
//                    gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
                    gl.glVertex3fv(snapshot.history[index].getBodies(), i);
                    index++;
                }

                gl.glEnd();
            }
        }

    }

    private void drawLines(GL gl) {

        gl.glLineWidth(2.0f);

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glColor4f(0.2f, 0.2f, 0.8f, 0.8f);

        gl.glBegin(GL.GL_LINES);

        float x = (float) maxX;
        float y = (float) maxY;
        float z = (float) maxZ;

        // FRONT
        gl.glVertex3f(x, y, z);
        gl.glVertex3f(-x, y, z);

        gl.glVertex3f(-x, y, z);
        gl.glVertex3f(-x, -y, z);

        gl.glVertex3f(-x, -y, z);
        gl.glVertex3f(x, -y, z);

        gl.glVertex3f(x, -y, z);
        gl.glVertex3f(x, y, z);

        // FRONT
        gl.glVertex3f(x, y, -z);
        gl.glVertex3f(-x, y, -z);

        gl.glVertex3f(-x, y, -z);
        gl.glVertex3f(-x, -y, -z);

        gl.glVertex3f(-x, -y, -z);
        gl.glVertex3f(x, -y, -z);

        gl.glVertex3f(x, -y, -z);
        gl.glVertex3f(x, y, -z);

        // SIDES
        gl.glVertex3f(x, y, z);
        gl.glVertex3f(x, y, -z);

        gl.glVertex3f(-x, y, z);
        gl.glVertex3f(-x, y, -z);

        gl.glVertex3f(-x, -y, z);
        gl.glVertex3f(-x, -y, -z);

        gl.glVertex3f(x, -y, z);
        gl.glVertex3f(x, -y, -z);

        gl.glEnd();
    }

    private void rotate(GL gl) {
        // This rotates the model
        synchronized (this) {
            haveRotation = false;
        }

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        //gl.glOrtho(-1.5, 1.5, -1.5, 1.5, -1.5, 1.5);
        gl.glOrtho(zoom * 20.0 * minX, zoom * 20.0 * maxX, zoom * 20.0 * minY,
                zoom * 20.0 * maxY, zoom * 20.0 * minZ, zoom * 20.0 * maxZ);

        //GLU glu = new GLU();
        //glu.gluPerspective(5.0*zoom, 1.0, 100, -100);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glRotatef(view_rotz, 0, 0, 1);//We rotate in the eye-to-world part
        gl.glRotatef(view_roty, 0, 1, 0);//We rotate in the eye-to-world part
        gl.glRotatef(view_rotx, 1, 0, 0);//We rotate in the eye-to-world part

        gl.glTranslatef(trans_x, trans_y, 0.0f);
    }

    private synchronized boolean getRotation() {
        return haveRotation;
    }

    public void display(GLAutoDrawable glDrawable) {
        if (autoRotate) {
            view_rotx += 1;
            view_roty += 1;

            synchronized (this) {
                haveRotation = true;
            }
        }

        boolean goodReason = getRotation() || history.haveNewBodies();

        if (goodReason || showCount > 0) {
            GL gl = glDrawable.getGL();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            rotate(gl);
            //            drawLines(gl);            
            drawBodies(gl);

            if (goodReason) {
                showCount = 2;
            } else {
                showCount--;
            }
        } else {
            try {
                Thread.sleep(10);
            } catch (Exception x) {
                // ignore
            }
        }
        info.repaint();
    }

    public void reshape(GLAutoDrawable glDrawable, int i, int i1, int i2, int i3) {
        width = canvas.getWidth();
        height = canvas.getHeight();
    }

    public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
        System.err.println("display changed");
        // TODO Auto-generated method stub
    }

    public void mouseClicked(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseClicked");
        }
    }

    public void mousePressed(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mousePressed");
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseReleased");
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseEntered");
        }
    }

    public void mouseExited(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseExited");
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseDragged");
        }

        int x = e.getX();
        int y = e.getY();
        Dimension size = e.getComponent().getSize();

        float thetaY = 360.0f * ((float) (x - prevMouseX) / (float) size.width);
        float thetaX =
                360.0f * ((float) (prevMouseY - y) / (float) size.height);

        prevMouseX = x;
        prevMouseY = y;

        //        System.err.println("thetax = " + thetaX);

        view_rotx += thetaX;
        view_roty += thetaY;

        synchronized (this) {
            haveRotation = true;
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (VERBOSE_MOUSE) {
            System.out.println("mouseMoved");
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        float newZoom = zoom;

        if (e.getWheelRotation() > 0) { // zoum out
            newZoom = zoom * 1.1f;
        } else { // zoom in
            newZoom = zoom / 1.1f;
        }

        if (newZoom >= 0.0001) {
            zoom = newZoom;
        }

        //        System.err.println("zoom = " + zoom);

        synchronized (this) {
            haveRotation = true;
        }
    }

    public static void main(String[] args) {

        String model = "Simple";

        JFrame frame = new JFrame("Galaxy Simulation");

        int w = -1, h = -1, x = -1, y = -1;
        boolean rotate = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-no-graph")) {
                SHOW_LOAD_GRAPH = false;
            } else if (args[i].equals("-width")) {
                i++;
                w = Integer.parseInt(args[i]);
            } else if (args[i].equals("-height")) {
                i++;
                h = Integer.parseInt(args[i]);
            } else if (args[i].equals("-x")) {
                i++;
                x = Integer.parseInt(args[i]);
            } else if (args[i].equals("-y")) {
                i++;
                y = Integer.parseInt(args[i]);
            } else if (args[i].equals("-rotate")) {
                rotate = true;
            } else {
                model = args[i];
            }
        }

        Visualization v = new Visualization(model);
        if (rotate) {
            v.autoRotate = true;
        }

        frame.addMouseListener(v);
        frame.addMouseMotionListener(v);
        frame.addKeyListener(v);
        frame.addMouseWheelListener(v);

        if (SHOW_LOAD_GRAPH) {
            frame.getContentPane().add("West", v.info);
        }
        frame.getContentPane().add(v.canvas, BorderLayout.CENTER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (w > 0 && h > 0) {
            frame.setSize(w, h);
        } else {
            frame.setSize(screenSize);
        }

        if (x >= 0 && y >= 0) {
            frame.setLocation(x, y);
        }
        frame.show();

        final Animator animator = new Animator(v.canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        animator.start();
    }

    public void keyPressed(KeyEvent arg0) {
    }

    public void keyReleased(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'q') {
            System.exit(0);
        }

        if (e.getKeyChar() == 'w') {
            trans_y += 1.0;
        } else if (e.getKeyChar() == 's') {
            trans_y -= 1.0;
        } else if (e.getKeyChar() == 'a') {
            trans_x -= 1.0;
        } else if (e.getKeyChar() == 'd') {
            trans_x += 1.0;
        } else if (e.getKeyChar() == 'r') {
            autoRotate = !autoRotate;
        } else if (e.getKeyChar() == '+' || e.getKeyChar() == '=') {
            float newZoom = zoom / 1.1f;
            if (newZoom >= 0.0001) {
                zoom = newZoom;
            }
        } else if (e.getKeyChar() == '-') {
            float newZoom = zoom * 1.1f;
            if (newZoom >= 0.0001) {
                zoom = newZoom;
            }
        }

        synchronized (this) {
            haveRotation = true;
        }
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    void resetHistory() {
        history.reset();
        info.reset();
    }
}
