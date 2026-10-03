public class Main {

    public static void main(String[] args) {

        BlockExpression be = new AtomicExpression.StringExpression("Hello, World!");
        Clock c = new Clock(10);

        for (int i = 0; i < 1080; i++) {
            Global.display.paintQueue = new Paintable[] { be };
            Global.display.display();
            c.tick();
        }

    }

}
