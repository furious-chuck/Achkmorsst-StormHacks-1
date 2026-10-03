public class NumberCollection {
    private NumberCollection() {}

    
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

}
