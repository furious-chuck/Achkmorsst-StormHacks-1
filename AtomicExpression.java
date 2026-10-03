public abstract class AtomicExpression implements Expression {
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
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }
        }

        static class DoubleNumber extends AtomicExpression {

            double value;
            public DoubleNumber(int value) {
                this.value = value;
            }

            @Override
            public String compile() {
                return String.valueOf(value);
            }
        }

    }

    public static class StringExpression extends AtomicExpression {
        String stringContents;
        public StringExpression(String stringContents) {
            for (int i = 0; i < stringContents.length(); i++) {
                if (stringContents.charAt(i) == '"') this.stringContents += "\\";
                this.stringContents += stringContents.charAt(i);
            }
        }
        @Override
        public String compile() {
            return "\"" + stringContents + "\"";
        }
    }

}
