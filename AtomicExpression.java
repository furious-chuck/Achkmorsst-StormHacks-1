public abstract class AtomicExpression implements Expression {
    @Override 
    public Expression[] getChildExpressions() {
        return new Expression[] {};
    }

    public class Numbers {
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

}
