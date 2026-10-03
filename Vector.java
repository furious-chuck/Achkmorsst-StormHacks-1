public interface Vector {
    double getX();
    void setX(double a);
    double getY();
    void setY(double a);

    void set(double x, double y);
    void set(Vector other);

    double getAngle();
    double getLength();

    Vector clone();

    Vector add(Vector other);
    Vector subtract(Vector other);
    Vector negate();

    Vector multiply(double other);
    Vector divide(double other);

    boolean softEquals(Vector other, double epsilon);
    boolean strictEquals(Vector other);
    boolean isZero();
}
