import java.awt.*;

public class SideBarExpression extends SideBarElement {

    final private BlockExpression template;

    SideBarExpression(BlockExpression tem,  RectVector pos, SideBar parentBar) {
        super(tem, pos, parentBar);
        template = tem;
        template.moveSelfAndAllChildrenTo(pos);
    }

    @Override
    public void paint(Graphics g) {
        template.moveSelfAndAllChildrenTo(translatePosition());
        template.paint(g);
    }

    public void makeTemplate() {

    }


}
