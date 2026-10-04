import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

/**
 * Handles the inputs and outputs on the highest level I guess.
 */
class Display extends JFrame {

    BufferStrategy bs;  // This variable's name describes this entire class very accurately.
    Graphics g;

    Color bgColor = Color.WHITE;

    Paintable[] paintQueue = {};


    public Display(String name) {

        super(GraphicsEnvironment
              .getLocalGraphicsEnvironment()
              .getDefaultScreenDevice()
              .getDefaultConfiguration());

        System.out.println("display created!");

        setTitle(name);
        setSize(Consts.WINDOW_WIDTH,
                Consts.WINDOW_HEIGHT);
        // setIconImage(Global.textureManager.UITextures.get(0));

        setLocationRelativeTo(null); // Centers the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // todo: change to DO_NOTHING_ON_CLOSE and actually handle the closing.
                                                        // There is a 99% chance that I will forget anyways so if anyone is reading this, please remind me!
                                                        // Its been a while. I forgor what I was doing here! No one reminded me!!!

        setVisible(true);

        addMouseListener(new MouseHandler());

        createBufferStrategy(2);
        bs = getBufferStrategy();

    }

    private static class MouseHandler implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // intentionally empty, for now.
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int button = e.getButton();
            if (button == MouseEvent.BUTTON1) {
                Inputs.mouseHeld = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int button = e.getButton();
            if (button == MouseEvent.BUTTON1) {
                Inputs.mouseHeld = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            //
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            //
        }
        
    }


    public void display() {
        g = bs.getDrawGraphics();
        g.setFont(Font.getFont(Font.MONOSPACED));
        g.setColor(bgColor);
        g.fillRect(0, 0, Consts.WINDOW_WIDTH, Consts.WINDOW_HEIGHT);
        for (Paintable paintable : paintQueue) {
            paintable.paint(g);
        }
        g.dispose();
        bs.show();
        paintQueue = new Paintable[0];
    }

    public void update() {
        Point mouseCoor = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseCoor, this);
        Inputs.mousePos = RectVector.castFromPoint(mouseCoor);
        display();

        Inputs.mousePressed = Inputs.mouseHeld && ! Inputs.mousePressed;
        Inputs.mouseHeldLastFrame = Inputs.mouseHeld;
    }

}


// Toolkit.getDefaultToolkit().sync(); ??
