import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.ArrayList;

public class SideBar implements Paintable {

    private int width = 200;
    private int scroll = 0;
    private int maxScroll = 10;
//    public ArrayList<SideBarElement> statements = new ArrayList<SideBarStatement>();
//    public ArrayList<SideBarElement> expressions = new ArrayList<SideBarExpression>();

    SideBar() {
        //addTemplate((BlockExpression) (ExpressionPackingStatement)new VarDeclaration(new AtomicExpression.ClassBlock("int"), new AtomicExpression.VarBlock("x"), new AtomicExpression.Numbers.IntNumber(10)));
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(0));
        g.drawRect(0, 0, width, Consts.WINDOW_HEIGHT);
        g.setColor(new Color(0x7FBFBFBF, true));
        g.fillRect(0, 0, width, Consts.WINDOW_HEIGHT);
        g.setColor(new Color(0x000000));
        g.drawString("ACHKMORSSCRIPT", 30, 30 + JFrame.getFrames()[0].getInsets().top);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int newWidth) {
        width = Math.clamp(newWidth, 50, Consts.WINDOW_WIDTH-50);
    }

    public void scrollVert(int amount) {
        scroll += Math.clamp(scroll+amount, 0, maxScroll);
    }

    public int getScroll() {
        return scroll;
    }

    public void setMaxScroll(int amount) {
        maxScroll = amount;
        if (scroll>maxScroll) scroll = maxScroll;
    }

//    public void addTemplate(BlockExpression template) {
//        int newY = Consts.SIDEBAR_BUTTON_GAP;
//        for (SideBarExpression tem:templates) {
//            newY += (int)Math.ceil(tem.getTemplate().getCascadingHeight()) + Consts.SIDEBAR_BUTTON_GAP;
//        }
//        templates.add(new SideBarElement(template, new RectVector(Consts.SIDEBAR_LEFT_OFFSET, newY), this));
//    }
}
