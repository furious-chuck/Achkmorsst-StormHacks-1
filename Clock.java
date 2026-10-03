/**
 * A tool entirely for ensuring a limited, stable framerate.
 * Warns via console if a frame is taking longer than provided.
 * Notice that this always seem to happen on the first frame, presumably
 * due to loading images.
 */
public class Clock {

    private long lastTickEnd;
    /**
     * Vulnerable to overflowing if the game runs for over 2 years at 30 fps!
     */
    int framesPassed;
    long timeTaken = 0;
    boolean trackFramesWhilePausedOrInventoryOpen;

    int FPS;

    public Clock(int FPS) {
        lastTickEnd = System.currentTimeMillis();
        framesPassed = 0;
        this.FPS = FPS;
    }

    public void tick(long milliseconds) {
        long timeToSleep = milliseconds - System.currentTimeMillis() + lastTickEnd;
        timeTaken = milliseconds - timeToSleep;
        if (timeToSleep < 0) {
            System.out.println(" !! Long Frame (" + (milliseconds - timeToSleep) + " > " + milliseconds + "ms)");
        } else {
            try {
                Thread.sleep(timeToSleep);
                // System.out.println("    Frame length: " + ((int) (((double) (delay - timeToSleep) / delay) * 1000)) / 10.0 + " %");
            } catch (InterruptedException e) {
                // I have no idea if this can ever happen but apparently Thread.sleep throws InterruptedException.
                System.out.println("!!! InterruptedException at Clock.tick");
            }
        }
        lastTickEnd = System.currentTimeMillis();
    }

    public void tick() {
        tick((long) (1000 / FPS));
    }
}
