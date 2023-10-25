import java.awt.*;
import java.io.File;

public class FileChooser {

    // TODO: use this in future for multi-platform support
    private static String getDotMinecraftPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String minecraftPath = "";
        if (os.contains("win")) {
            minecraftPath = System.getenv("APPDATA") + "/.minecraft";
        } else if (os.contains("mac")) {
            minecraftPath = System.getProperty("user.home") + "/Library/Application Support/minecraft";
        } else if (os.contains("nix") || os.contains("nux")) {
            minecraftPath = System.getProperty("user.home") + "/.minecraft";
        }
        return minecraftPath;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }
        String operation = args[0];
        // Create fileChooser of either load or store type
        int operationMode = operation.equals("LOAD") ? FileDialog.LOAD : FileDialog.SAVE;

        // Set default folder to .minecraft folder
        String minecraftPath = System.getenv("APPDATA") + File.separator + ".minecraft";

        // Set a file filter to only display record files
        FileDialog fileDialog = new FileDialog(new Frame(), operation + " A Record", operationMode);

        // Apply attributes
        fileDialog.setDirectory(minecraftPath);
        fileDialog.setFile("*.rec");
        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".rec"));
        fileDialog.setVisible(true);

        String selectedFile = fileDialog.getFile();

        if (selectedFile != null) {
            System.out.println(fileDialog.getDirectory() + File.separator + selectedFile);
        }
        System.exit(0);
    }
}
