public class RectVector implements Vector {

    private double x;
    private double y;

    public RectVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public RectVector() {
        this(0, 0);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double a) {
        this.x = a;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double a) {
        this.y = a;
    }

    @Override
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void set(Vector other) {
        if (other != null) {
            this.x = other.getX();
            this.y = other.getY();
        }
    }

    @Override
    public double getAngle() {
        return Math.atan2(y, x);
    }

    @Override
    public double getLength() {
        System.out.println("h");
        return Math.hypot(x, y);
    }

    @Override
    public Vector clone() {
        return new RectVector(this.x, this.y);
    }

    @Override
    public Vector add(Vector other) {
        return new RectVector(this.x + other.getX(), this.y + other.getY());
    }

    @Override
    public Vector subtract(Vector other) {
        return new RectVector(this.x - other.getX(), this.y - other.getY());
    }

    @Override
    public Vector negate() {
        return new RectVector(-this.x, -this.y);
    }

    @Override
    public Vector multiply(double other) {
        return new RectVector(this.x * other, this.y * other);
    }

    @Override
    public Vector divide(double other) {
        return new RectVector(this.x / other, this.y / other);
    }

    @Override
    public boolean softEquals(Vector other, double epsilon) {
        if (other == null) return false;
        return Math.abs(this.x - other.getX()) <= epsilon && 
               Math.abs(this.y - other.getY()) <= epsilon;
    }

    @Override
    public boolean strictEquals(Vector other) {
        if (other == null) return false;
        return this.x == other.getX() && this.y == other.getY();
    }

    @Override
    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0;
    }
}