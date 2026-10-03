import java.awt.*;

public abstract class AtomicExpression extends BlockExpression {
    @Override 
    public Expression[] getChildExpressions() {
        return new Expression[] {};
    }

    public static class Numbers {
        private Numbers() {}


        static class IntNumber extends AtomicExpression {

            int value;
            public IntNumber(int value) {
                this.value = value;
                mainColor = Color.BLUE;
                outlineColor = Color.BLACK;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }

            @Override
            public void paint(Graphics g) {

            }
        }

        static class DoubleNumber extends AtomicExpression {

            double value;
            public DoubleNumber(int value) {
                this.value = value;
                mainColor = Color.CYAN;
                outlineColor = Color.BLACK;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }

            @Override
            public void paint(Graphics g) {

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
            mainColor = Color.GREEN;
            outlineColor = Color.BLACK;
            textColor = Color.BLACK;
        }
        @Override
        public String compile() {
            return "\"" + stringContents + "\"";
        }

        @Override
        public void paint(Graphics g) {

        }
    }

}
