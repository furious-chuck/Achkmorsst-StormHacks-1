import java.awt.*;

public abstract class AtomicExpression extends BlockExpression {
    @Override 
    public Expression[] getChildExpressions() {
        return new Expression[] {};
    }
    public abstract double getValueWidth();

    @Override
    public void paint(Graphics g) {
        g.setColor(mainColor);
        g.fillPolygon(Util.createPolygon(
                new Vector[] {
                        (new RectVector(0, 0)).subtract(Global.cameraPos),
                        (new RectVector(getValueWidth() + 20, 0)).subtract(Global.cameraPos),
                        (new RectVector(getValueWidth() + 20, Consts.LETTER_HEIGHT + 20)).subtract(Global.cameraPos),
                        (new RectVector(0, Consts.LETTER_HEIGHT + 20)).subtract(Global.cameraPos),
                        (new RectVector(0, Consts.LETTER_HEIGHT / 2.0 + 10 + 5)).subtract(Global.cameraPos),
                        (new RectVector(-5, Consts.LETTER_HEIGHT / 2.0 + 10)).subtract(Global.cameraPos),
                        (new RectVector(0, Consts.LETTER_HEIGHT / 2.0 + 10 - 5)).subtract(Global.cameraPos)
                }
        ));
        // g.fillRect(100, 100, 100, 100);

        System.out.println("Painting something!");
    }

    public static class Numbers {
        private Numbers() {}


        static class IntNumber extends AtomicExpression {

            int value;
            public IntNumber(int value) {
                this.value = value;
                mainColor = new Color(127, 127, 255);
                outlineColor = Color.BLACK;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }

            @Override
            public double getValueWidth() {
                return String.valueOf(value).length() * Consts.LETTER_WIDTH;
            }
        }

        static class DoubleNumber extends AtomicExpression {

            double value;
            public DoubleNumber(int value) {
                this.value = value;
                mainColor = new Color(127, 255, 255);
                outlineColor = Color.BLACK;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }

            @Override
            public double getValueWidth() {
                return String.valueOf(value).length() * Consts.LETTER_WIDTH;
            }
        }

    }

    public static class StringExpression extends AtomicExpression {
        String stringContents;
        public StringExpression(String stringContents) {
            for (int i = 0; i < stringContents.length(); i++) {
                // ALL OF THIS MAY BE COMPLETELY BROKEN STRINGS SUCK ANYWAY THO SO ITS OKAY
                if (stringContents.charAt(i) == '"') this.stringContents += "\\";
                this.stringContents += stringContents.charAt(i);
            }
            mainColor = new Color(127, 255, 127);
            outlineColor = Color.BLACK;
            textColor = Color.BLACK;
        }
        @Override
        public String compile() {
            return "\"" + stringContents + "\"";
        }

        @Override
        public double getValueWidth() {
            return String.valueOf(stringContents).length() * Consts.LETTER_WIDTH;
        }
    }

}
