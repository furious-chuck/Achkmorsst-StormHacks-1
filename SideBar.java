import java.awt.*;

public class SideBar implements Paintable {

    @Override
    public void paint(Graphics g) {
        // System.out.println("mrow");
        g.setColor(new Color(0));
        g.drawRect(0, 0, 200, Consts.WINDOW_HEIGHT);
        g.setColor(new Color(0xBFBFBF7F, true));
        g.fillRect(0, 0, 200, Consts.WINDOW_HEIGHT);
    }
}
