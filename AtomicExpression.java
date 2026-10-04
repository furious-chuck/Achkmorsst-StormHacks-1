import java.awt.*;

public abstract class AtomicExpression extends BlockExpression {
    @Override 
    public Expression[] getChildExpressions() {
        return new Expression[] {};
    }
    public abstract double getValueWidth();

    public abstract String getValueString();

    @Override
    public double getCascadingHeight() {
        return Consts.LETTER_HEIGHT + 10;
    }

    @Override
    public double getCascadingWidth() {
        return getValueWidth() + 10;
    }

    //@Override
    public void setChildExpression(int childID, Expression newExpression) {
        Util.unableToCan();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(mainColor);
        Polygon textEntryField = Util.createPolygon(
                new Vector[] {
                        (new RectVector(5, 5)).add(position).subtract(Global.cameraPos),
                        (new RectVector(getValueWidth() + 15, 5)).add(position).subtract(Global.cameraPos),
                        (new RectVector(getValueWidth() + 15, Consts.LETTER_HEIGHT + 15)).add(position).subtract(Global.cameraPos),
                        (new RectVector(5, Consts.LETTER_HEIGHT + 15)).add(position).subtract(Global.cameraPos)
                }
        );
        GraphicsUtils.drawThatGoofyExpressionShape(
            g,
            mainColor,
            outlineColor,
            position.subtract(Global.cameraPos),
            new RectVector(getValueWidth() + 20,Consts.LETTER_HEIGHT + 20)
        );
        g.setColor(secondaryColor);
        g.fillPolygon(textEntryField);
        g.setColor(outlineColor);
        g.drawPolygon(textEntryField);

        g.setColor(textColor);
        g.drawString(getValueString(), 10 - (int) (Global.cameraPos.getX() - position.getX()), Consts.LETTER_HEIGHT + 10 - (int) (Global.cameraPos.getY() - position.getY()));
    }

    public static class Numbers {
        private Numbers() {}


        static class IntNumber extends AtomicExpression {

            int value;
            public IntNumber(int value) {
                this.value = value;
                mainColor = new Color(0, 0, 255);
                secondaryColor = new Color(127, 127, 255);
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

            @Override
            public String getValueString() {
                return String.valueOf(value);
            }

            @Override
            public void setChildElement(int childID, Expression newExpression) {
                Util.unableToCan();
            }
        }

        static class DoubleNumber extends AtomicExpression {

            double value;
            public DoubleNumber(int value) {
                this.value = value;
                mainColor = new Color(0, 255, 255);
                secondaryColor = new Color(127, 255, 255);
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

            @Override
            public String getValueString() {
                return String.valueOf(value);
            }

            @Override
            public void setChildElement(int childID, Expression newExpression) {
                Util.unableToCan();
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
            mainColor = new Color(0, 255, 0);
            secondaryColor = new Color(127, 255, 127);
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

        @Override
        public String getValueString() {
            return stringContents;
        }

        @Override
        public void setChildElement(int childID, Expression newExpression) {
            Util.unableToCan();
        }
    }

}
