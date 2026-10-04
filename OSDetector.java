import java.awt.*;
import java.util.Objects;


public class OSDetector {
    static String OSName = "";
    static String OSNameFinal = "";
    public static String getOS() {
        if (!Objects.equals(OSNameFinal, "")) return OSNameFinal;
        if (Objects.equals(OSName, "")) OSName = System.getProperty("os.name").toLowerCase();
        
        if (OSName.contains("win")) {
            OSNameFinal = "windows";
        } else if (OSName.contains("mac")) {
            OSNameFinal = "mac";
        } else if (OSName.contains("nix") || OSName.contains("nux") || OSName.contains("aix")) {
            OSNameFinal = "linux";
        } else {
            OSNameFinal = "unknown";
        }
        return OSNameFinal;
    }
    public static Font CreateFont() {
        String fontName = switch (getOS()) {
            case "windows" -> "Consolas";
            case "linux" -> "Inconsolata";
            case "mac" -> "Menlo"; // Apple's standard crisp terminal/code font
            default -> Font.MONOSPACED; // Safe fallback (usually Courier)
        };
        return new Font(fontName, Font.PLAIN, 14);
    }
}