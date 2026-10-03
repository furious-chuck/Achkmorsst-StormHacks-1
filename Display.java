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

    // Color bgColor = new Color(0, 0, 0);

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

        addKeyListener(new KeyHandler());
        addMouseListener(new MouseHandler());

        createBufferStrategy(2);
        bs = getBufferStrategy();

    }

    private class KeyHandler implements KeyListener {
        // This is a surprise tool that will help us later
        @Override
        public void keyPressed(KeyEvent e) {
            /*
            String symbolTyped = KeyEvent.getKeyText(e.getKeyCode());
            if (null != symbolTyped) switch (symbolTyped) {
                case "W":
                    Inputs.wHeld = true;
                    break;
                case "A":
                    Inputs.aHeld = true;
                    break;
                case "S":
                    Inputs.sHeld = true;
                    break;
                case "D":
                    Inputs.dHeld = true;
                    break;
                case "E":
                    Inputs.eHeld = true;
                    break;
                case "I":
                    Inputs.iHeld = true;
                    break;
                case "Shift":
                    Inputs.shiftHeld = true;
                    break;
                case "Escape":
                    Inputs.escHeld = true;
                    break;
                default:
                    break;
            }
            */
        }

        @Override
        public void keyReleased(KeyEvent e) {
            /*
            String symbolTyped = KeyEvent.getKeyText(e.getKeyCode());
            if (null != symbolTyped) switch (symbolTyped) {
                case "W":
                    Inputs.wHeld = false;
                    break;
                case "A":
                    Inputs.aHeld = false;
                    break;
                case "S":
                    Inputs.sHeld = false;
                    break;
                case "D":
                    Inputs.dHeld = false;
                    break;
                case "E":
                    Inputs.eHeld = false;
                    break;
                case "I":
                    Inputs.iHeld = false;
                    break;
                case "Shift":
                    Inputs.shiftHeld = false;
                    break;
                case "Escape":
                    Inputs.escHeld = false;
                    break;
                default:
                    break;
            }
            */
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // intentionally blank
        }
    }

    private class MouseHandler implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // intentionally empty, for now.
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int button = e.getButton();
            switch (button) {
                case MouseEvent.BUTTON1:
                    // Inputs.lmbHeld = true;
                    // todo: add logic for mouse held.
                    break;
                case MouseEvent.BUTTON2:
                    // Inputs.rmbHeld = true;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int button = e.getButton();
            switch (button) {
                case MouseEvent.BUTTON1:
                    // Inputs.lmbHeld = false;
                    break;
                case MouseEvent.BUTTON2:
                    // Inputs.rmbHeld = false;
                    break;
                default:
                    break;
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
        // g.setColor(bgColor);
        // g.fillRect(0, 0, MagicNumbers.SCREEN_WIDTH, MagicNumbers.SCREEN_HEIGHT);
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
        // Inputs.mousePos = RectVector.castFromPoint(mouseCoor);
        // todo: store mouse pos.
        // System.out.println("Cursor in window: " + mousePos.getX() + ", " + mousePos.getY());
        display();
    }

}


// Toolkit.getDefaultToolkit().sync(); ??
