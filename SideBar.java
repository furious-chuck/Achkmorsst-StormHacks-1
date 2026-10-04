import java.awt.*;

public class SideBar implements Paintable {

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(0x7F7F7F7F, true));
        g.drawRect(0, 0, 100, Consts.WINDOW_HEIGHT);
    }
}
