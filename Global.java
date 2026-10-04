public class Global {
    private Global() {}

    static Display display = createDisplay();

    /**
     * Creates the window, or returns null in headless environments (e.g. automated
     * tests) so logic like hover/drag can still run without an X display.
     */
    static Display createDisplay() {
        if (java.awt.GraphicsEnvironment.isHeadless()) return null;
        return new Display("Main display");
    }

    static Vector cameraPos = new RectVector(-Consts.WINDOW_WIDTH / 2.0, -Consts.WINDOW_HEIGHT / 2.0);

}
