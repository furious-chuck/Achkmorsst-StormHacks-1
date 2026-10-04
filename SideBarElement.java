import java.awt.*;

public class SideBarElement implements Paintable {

    private final BlockExpression template;
    private final RectVector position;
    private final SideBar parent;

    SideBarElement(BlockExpression block, RectVector pos, SideBar parentBar) {
        template = block;
        template.moveSelfAndAllChildrenTo(pos);
        position = pos;
        parent = parentBar;
    }

    @Override
    public void paint(Graphics g) {
        template.moveSelfAndAllChildrenTo(translatePosition());
        template.paint(g);
    }

    public BlockExpression getTemplate() {
        return template;
    }

    public BlockExpression makeBlock() {
        return template;//make this clone it and stuff
    }

    public RectVector translatePosition() {
        return (RectVector)position.add(new RectVector(0,parent.getScroll()));
    }
}
