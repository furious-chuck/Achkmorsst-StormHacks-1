import java.awt.*;
import javax.swing.*;


public class OSDetector {
    public static String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("mac")) {
            return "mac";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "linux";
        } else {
            return "unknown";
        }
    }
    public static void CreateFont(){
        String OSName=getOS();
        String fontName;
        switch (OSName) {
            case "windows":
                fontName = "Consolas";
                break;
            case "linux":
                fontName = "Inconsolata";
                break;
            case "mac":
                fontName = "Menlo"; // Apple's standard crisp terminal/code font
                break;
            default:
                fontName = Font.MONOSPACED; // Safe fallback (usually Courier)
                break;
        }
        Font codeFont = new Font(fontName, Font.PLAIN, 14);

        // Example: Applying it to a UI Window component
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Monospaced OS Font Example");
            
            // A text area is perfect for code/monospaced fonts
            JTextArea textArea = new JTextArea("public static void main(String[] args) {\n    // This font looks native!\n}");
            textArea.setFont(codeFont);
            textArea.setMargin(new Insets(10, 10, 10, 10));
            
            frame.add(new JScrollPane(textArea));
            frame.setSize(450, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);
    });
}
}