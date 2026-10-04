import java.awt.*;

public class SideBarElement implements Paintable {

    private final Paintable template;
    private final RectVector position;
    private final SideBar parent;

    SideBarElement(Paintable block, RectVector pos, SideBar parentBar) {
        template = block; //this is unused i think
        position = pos;
        parent = parentBar;
    }

    @Override
    public void paint(Graphics g) {
        template.paint(g);
    }

    public Paintable getBlock() {
        return template;
    }

    public Paintable makeBlock() {
        return template;//make this clone it and stuff
    }

    public RectVector translatePosition() {
        return (RectVector)position.add(new RectVector(0,parent.getScroll()));
    }
}
