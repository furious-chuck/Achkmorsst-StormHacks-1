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

                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }

                // --- Execute the command ---
                executeCommand(line);
            }
        } catch (IOException e) {
            System.err.println("Terminal I/O error: " + e.getMessage());
        }
    }

    private static void executeCommand(String commandLine) {
    try {
        String[] command = commandLine.split("\\s+");
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder capturedOutput = new StringBuilder();

        try (BufferedReader processReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String outputLine;
            while ((outputLine = processReader.readLine()) != null) {
                System.out.println(outputLine); // print #1: live, as each line is read
                capturedOutput.append(outputLine).append(System.lineSeparator());
            }
        }

        // print #2: replay of what was actually captured into memory
        System.out.println("----- captured output -----");
        System.out.print(capturedOutput); // print, not println — readLine already stripped the newlines, we re-added them

        int exitCode = process.waitFor();
        System.out.println("[Process completed with exit code: " + exitCode + "]");

    } catch (IOException e) {
        System.err.println("Failed to execute command: " + e.getMessage());
    } catch (InterruptedException e) {
        System.err.println("Command execution was interrupted.");
        Thread.currentThread().interrupt();
    }
}
}