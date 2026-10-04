public class Main {

    public static void main(String[] args) {

        BlockStatement be = new Assigner(new AtomicExpression.Numbers.IntNumber(10), new AtomicExpression.Numbers.DoubleNumber(10.1));
        BlockStatement ba = new Assigner(new AtomicExpression.StringExpression("hello"), new AtomicExpression.Numbers.DoubleNumber(1021));

        be.connectNextStatement(ba);

        be.position = new RectVector();
        Clock c = new Clock(10);
        

        for (int i = 0; i < 1080; i++) {
            Global.display.paintQueue = new Paintable[] { be };
            Global.display.display();
            c.tick();
        }

    }

}
