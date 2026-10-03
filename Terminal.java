import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Terminal {
    private static final BufferedReader terminalReader = new BufferedReader(new InputStreamReader(System.in));
    public static void runTerminalLoop() {
        String line;
            try {
                while (true) {
                    // WRITE a prompt to the terminal
                    System.out.print("> ");
                    System.out.flush(); // make sure the prompt shows before reading

                    // READ a line from the terminal (blocks until Enter is pressed)
                    line = terminalReader.readLine();

                    // readLine() returns null when the stream ends (Ctrl-D / pipe closed)
                    if (line == null) {
                        System.out.println("(input stream closed)");
                        break;
                    }

                    line = line.trim();
                    if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                        System.out.println("Goodbye!");
                        break;
                    }

                    // --- Put your command handling here ---
                    System.out.println("You typed: " + line);
                }
            } catch (IOException e) {
                System.err.println("Terminal I/O error: " + e.getMessage());
            }
}
}