/**
 * Moves the camera around the workspace with the arrow keys.
 * Blocks are stored in world coordinates; everything is drawn at
 * (world position - cameraPos), so moving the camera scrolls the whole workspace.
 */
public class Camera {
    private Camera() {}

    /** Call once per frame. */
    static void update() {
        double dx = 0, dy = 0;
        if (Inputs.leftHeld)  dx -= Consts.CAMERA_SPEED;
        if (Inputs.rightHeld) dx += Consts.CAMERA_SPEED;
        if (Inputs.upHeld)    dy -= Consts.CAMERA_SPEED;
        if (Inputs.downHeld)  dy += Consts.CAMERA_SPEED;

        if (dx != 0 || dy != 0) {
            Global.cameraPos.set(Global.cameraPos.getX() + dx,
                                 Global.cameraPos.getY() + dy);
        }
    }
}