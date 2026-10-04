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

    // set by Main so the update loop can test mouse-hover against all blocks
    static BlockManager activeBlockManager = null;


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
        @Override
        public void keyPressed(KeyEvent e) {
            String symbolTyped = KeyEvent.getKeyText(e.getKeyCode());
            switch (symbolTyped) {
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
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            String symbolTyped = KeyEvent.getKeyText(e.getKeyCode());
            switch (symbolTyped) {
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
                default:
                    break;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // intentionally blank
        }
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
                // grab whatever block is under the cursor (if any) and begin dragging
                Inputs.handleDragStart();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int button = e.getButton();
            if (button == MouseEvent.BUTTON1) {
                Inputs.mouseHeld = false;
                // the next frame's Inputs.handleDrag() will drop the grabbed block
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

        // determine whether the mouse is hovering over a block (and which one);
        // prints hover enter/leave messages to the terminal. While a drag is in
        // progress, the dragged block's own subtree is excluded from the hit-test so
        // the copy glued to the cursor can never shadow the real blocks underneath.
        Inputs.getHoveredBlock(activeBlockManager, Inputs.currentlyDraggingABlock);

        // press -> grab, hold -> move with cursor, release -> drop
        Inputs.handleDrag(activeBlockManager);

        display();

        Inputs.mousePressed = Inputs.mouseHeld && ! Inputs.mousePressed;
        Inputs.mouseHeldLastFrame = Inputs.mouseHeld;
    }

}


// Toolkit.getDefaultToolkit().sync(); ??
