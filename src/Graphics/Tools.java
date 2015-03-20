package Graphics;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;

public class Tools {
    public static GL2 gl;
    public static GLCanvas canvas;
    final public static double maxX = 1600;
    final public static double maxY = 900;
    final public static double centreX = 0;
    final public static double centreY = 0;

    public static void initialiseCanvas(PitchDrawingFrame pdFrame) {
        // generate GLCanvas
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setDoubleBuffered(true);
        final GLCanvas glcanvas = new GLCanvas(capabilities);

        // add PitchDrawingFrame as GLEventLister
        glcanvas.addGLEventListener(pdFrame);
        canvas = glcanvas;

        // set a default size for the canvas
        glcanvas.setSize(1920,1080);

        // generate frame and add canvas to it
        final JFrame frame = new JFrame("Soccer Simulator");
        frame.getContentPane().add(glcanvas);
        frame.setSize(frame.getContentPane().getPreferredSize());

        // make the frame full screen
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        //frame.setUndecorated(true);
        frame.setVisible(true);

        // make program close when window closes
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void displayCanvas() { canvas.display(); }

    // coordinates at the centre of the screen are (0,0) at the centre of the canvas
    // coordinates range from (-maxX/2,-maxY/2) to (maxX/2,maxY/2)
    public static float cordConvertX(double initX) {
        return (float) (initX * 2 / maxX);
    }
    public static float cordConvertY(double initY) {
        return (float) (initY * 2 / maxY);
    }

    public static void clearBuffer() {
        Tools.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Tools.gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    }

    public static void drawLine(double startX, double startY, double endX, double endY, Color c) {
        GL2 gl = canvas.getGL().getGL2();
        setColour(c);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2f(cordConvertX(startX),cordConvertY(startY));
        gl.glVertex2f(cordConvertX(endX),cordConvertY(endY));
        gl.glEnd();
    }

    public static void drawFilledRectangle(double botLeftX, double botLeftY,
                                           double width, double height, Color c) {
        setColour(c);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(cordConvertX(botLeftX), cordConvertY(botLeftY));
        gl.glVertex2f(cordConvertX(botLeftX + width),cordConvertY(botLeftY));
        gl.glVertex2f(cordConvertX(botLeftX + width),cordConvertY(botLeftY + height));
        gl.glVertex2f(cordConvertX(botLeftX),cordConvertY(botLeftY + height));
        gl.glEnd();
    }

    public static void drawFilledCircle(double centreX, double centreY, double radius, Color c) {
        drawCircleHelper(centreX, centreY, radius, c, true);
    }

    public static void drawCircle(double centreX, double centreY, double radius, Color c) {
        drawCircleHelper(centreX,centreY,radius,c,false);
    }

    private static void setColour(Color c) {
        gl.glColor3f((float)c.getRed()/255f,(float)c.getGreen()/255f,(float)c.getBlue()/255f);
    }

    private static void drawCircleHelper(double centreX, double centreY, double radius,
                                         Color c, Boolean filled) {
        setColour(c);
        if (filled) {
            gl.glBegin(GL2.GL_POLYGON);
        } else {
            gl.glBegin(GL2.GL_LINE_LOOP);
        }
        for (int i = 0; i < 360; i++) {
            double iRads = i * Math.PI / 180;
            float xComp = (float)(centreX + radius * Math.cos(iRads));
            float yComp = (float)(centreY + radius * Math.sin(iRads));
            gl.glVertex2f(cordConvertX(xComp),cordConvertY(yComp));
        }
        gl.glEnd();
    }
}
