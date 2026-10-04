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
        return Consts.LETTER_HEIGHT + 20; // must match the height paint() actually draws
    }

    @Override
    public double getCascadingWidth() {
        return getValueWidth() + 20; // must match the width paint() actually draws
    }

    /**
     * Atomic expressions are drawn inset by 5px inside their slot bounds (see
     * paint()), so hit-testing must use the same offset rectangle. Without this,
     * grabbing an atomic near its edges misses it and grabs the enclosing statement
     * instead — which drags the whole parent away and leaves the atomic behind,
     * making it look like the block "disappeared" the instant the mouse went down.
     */
    @Override
    public Vector getPaintedPosition() {
        return position.add(new RectVector(5, 5));
    }

    //@Override
    public void setChildExpression(int childID, Expression newExpression) {
        Util.unableToCan();
    }

    @Override
    public void paint(Graphics g) {
        // the block's own bounding box (which is what getCascadingWidth/Height and the
        // parent's slot layout are based on) is centered on `position`, so everything
        // drawn here must be offset by 5 as well. painting at raw screen coordinates
        // used to leave a stale copy of the atomic sitting at the top-left corner of
        // its old slot whenever the block was moved out of (or back into) its parent.
        double drawX = position.getX() - Global.cameraPos.getX();
        double drawY = position.getY() - Global.cameraPos.getY();

        Polygon textEntryField = Util.createPolygon(
                new Vector[] {
                        (new RectVector(0, 0)).add(new RectVector(drawX, drawY)),
                        (new RectVector(getValueWidth() + 10, 0)).add(new RectVector(drawX, drawY)),
                        (new RectVector(getValueWidth() + 10, Consts.LETTER_HEIGHT + 10)).add(new RectVector(drawX, drawY)),
                        (new RectVector(0, Consts.LETTER_HEIGHT + 10)).add(new RectVector(drawX, drawY))
                }
        );
        GraphicsUtils.drawThatGoofyExpressionShape(
            g,
            mainColor,
            outlineColor,
            new RectVector(drawX, drawY),
            new RectVector(getValueWidth() + 20,Consts.LETTER_HEIGHT + 20)
        );
        g.setColor(secondaryColor);
        g.fillPolygon(textEntryField);
        g.setColor(outlineColor);
        g.drawPolygon(textEntryField);

        g.setColor(textColor);
        g.drawString(getValueString(), (int) (drawX + 5), (int) (drawY + Consts.LETTER_HEIGHT + 5));
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
            public DoubleNumber(double value) {
                this.value = value;
                mainColor = new Color(0, 255, 255);
                secondaryColor = new Color(127, 255, 255);
                outlineColor = Color.BLACK;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return "(" + String.valueOf(value) + ")";
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
            this.stringContents = "";
            for (int i = 0; i < stringContents.length(); i++) {
                // ALL OF THIS MAY BE COMPLETELY BROKEN STRINGS SUCK ANYWAY THO SO ITS OKAY
                if (stringContents.charAt(i) == '"') this.stringContents += "\\";
                this.stringContents += stringContents.charAt(i);
            }
            mainColor = new Color(191, 191, 0);
            secondaryColor = new Color(255, 255, 127);
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

    public static class Boolean {
        private Boolean() {}

        public static class True extends AtomicExpression {

            public True() {
                mainColor = Color.GREEN;
                secondaryColor = Color.GREEN;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return "true";
            }

            @Override
            public void setChildElement(int childID, Expression newExpression) {
                Util.unableToCan();
            }

            @Override
            public double getValueWidth() {
                return Consts.LETTER_WIDTH * 5;
            }

            @Override
            public String getValueString() {
                return "true";
            }
            
        }

        public static class False extends AtomicExpression {

            public False() {
                mainColor = Color.RED;
                secondaryColor = Color.RED;
                textColor = Color.BLACK;
            }

            @Override
            public String compile() {
                return "false";
            }

            @Override
            public void setChildElement(int childID, Expression newExpression) {
                Util.unableToCan();
            }

            @Override
            public double getValueWidth() {
                return Consts.LETTER_WIDTH * 5;
            }

            @Override
            public String getValueString() {
                return "false";
            }
            
        }
    }

    public static class ClassBlock extends AtomicExpression {
        String stringContents;
        public ClassBlock(String stringContents) {
            this.stringContents = stringContents;
            mainColor = new Color(127, 127, 127);
            secondaryColor = new Color(191, 191, 191);
            outlineColor = Color.BLACK;
            textColor = Color.BLACK;
        }
        @Override
        public String compile() {
            return stringContents;
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

    public static class VarBlock extends AtomicExpression {
        String stringContents;
        public VarBlock(String stringContents) {
            this.stringContents = stringContents;
            mainColor = new Color(255, 31, 63);
            secondaryColor = new Color(255, 127, 158);
            outlineColor = Color.BLACK;
            textColor = Color.BLACK;
        }
        @Override
        public String compile() {
            return stringContents;
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
