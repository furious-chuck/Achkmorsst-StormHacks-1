import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Entry point for the application. This class wires the program to the
 * Java terminal (stdin/stdout) so it can both READ from and WRITE to it.
 */
public class Main {

    /** Reads raw input typed into the terminal (System.in = standard input). */
    private static final BufferedReader terminalReader =
            new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        // System.out / System.err = standard output/error: how we WRITE to the terminal.
        System.out.println("potato");
        runTerminalLoop();
    }

    /**
     * A simple REPL (Read-Eval-Print Loop): repeatedly reads a line from the
     * terminal, processes it, and writes the result back out. Type "exit"
     * (or press Ctrl-D) to quit.
     */
    private static void runTerminalLoop() {
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
                    System.out.println("(input stream closed, bye!)");
                    break;
                }

                line = line.trim();
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                // --- Put your command handling here ---
                // Example: echo the line back to prove reading & writing work.
                System.out.println("You typed: " + line);
            }
        } catch (IOException e) {
            System.err.println("Terminal I/O error: " + e.getMessage());
        }
    }
}
