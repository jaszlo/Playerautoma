package net.jasper.mod.util;

import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.jasper.mod.util.data.Recording;

import java.io.*;
import java.nio.file.Path;

public class IOHelpers {

    public static class RecordingFileTypes {
        public static String REC = "rec";
        public static String JSON = "json";

        public static String[] types() {
            return new String[] { REC, JSON };
        }
    }

    public static Recording loadRecordingFile(File directory, File name) {
        File toLoad = new File(directory, name.getName());
        if (!toLoad.exists()) return new Recording(null);

        Recording result = new Recording(null);
        for (String option : RecordingFileTypes.types()) {
            if (option.equals(RecordingFileTypes.JSON)) {
                try {
                    FileReader fileReader = new FileReader(toLoad);
                    BufferedReader reader = new BufferedReader(fileReader);
                    StringBuilder readFile = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        readFile.append(line);
                    }
                    reader.close();
                    fileReader.close();
                    result = JsonHelpers.deserialize(readFile.toString());
                } catch(Exception e) {
                    // Try rec file then
                    continue;
                }
                break;
            } else if (option.equals(RecordingFileTypes.REC)) {
                ObjectInputStream objectInputStream = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(toLoad);
                    objectInputStream = new ObjectInputStream(fis);
                    // This can happen when a file is selected and then deleted via the file explorer
                    if (objectInputStream == null) throw new IOException("objectInputStream is null");

                    result = (Recording) objectInputStream.readObject();
                    objectInputStream.close();
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    PlayerAutomaClient.LOGGER.warn(e.getMessage());
                    try {
                        if (objectInputStream != null) objectInputStream.close();
                        if (fis != null) fis.close();
                    } catch (IOException closeFailed) {
                        PlayerAutomaClient.LOGGER.warn(closeFailed.getMessage());
                        PlayerAutomaClient.LOGGER.warn("Error closing file (loadRecord) in error handling!"); // This should not happen :(
                    }
                    continue;

                }
                break;
            }
        }
        return result;
    }

    private static File createNewFileName(File directory, String name, String storeAs, boolean overwrite) {
        File selected = Path.of(directory.getAbsolutePath(), name).toFile();
        storeAs = "." + storeAs; // "." + "rec"|"json"
        String newName = name;
        if (overwrite) {
            return selected;
        }
        while (selected.exists()) {
            newName = newName.substring(0, newName.length() - storeAs.length()) + "_new" + storeAs;
            selected = Path.of(directory.getAbsolutePath(), newName).toFile();
        }
        return selected;
    }

    /**
     * Stores a recording to a file with a given name to a given directory and as a given type. Can overwrite existing file
     * @param record to store to file
     * @param directory where to locate file
     * @param name of the file created
     * @param storeAs file type
     * @param overwrite if exists
     * @return true on success
     */
    public static boolean storeRecordingFile(Recording record, File directory, String name, String storeAs, boolean overwrite) {
        File selected = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            // If file already exists create new file with "_new" before file type.
            selected = createNewFileName(directory, name, storeAs, overwrite);
            FileOutputStream fos = new FileOutputStream(selected);
            objectOutputStream = new ObjectOutputStream(fos);
            if (objectOutputStream == null) throw new IOException("objectInputStream is null");
            // Store as .json/.rec according to option
            if (storeAs.equals(RecordingFileTypes.JSON)) {
                String json = JsonHelpers.serialize(record);
                FileWriter fileWriter = new FileWriter(selected);
                BufferedWriter writer = new BufferedWriter(fileWriter);
                writer.write(json);
                writer.close();
                fileWriter.close();
            } else {
                objectOutputStream.writeObject(record);
            }
            objectOutputStream.close();
            fos.close();
            return true;
        } catch(IOException e) {
            PlayerAutomaClient.LOGGER.warn(e.getMessage());
            try {
                if (objectOutputStream != null) objectOutputStream.close();
                PlayerAutomaClient.LOGGER.info("Deletion of failed file: {}", selected.delete());
            } catch (IOException closeFailed) {
                PlayerAutomaClient.LOGGER.warn(closeFailed.getMessage());
                PlayerAutomaClient.LOGGER.warn("Error closing file (storeRecord) in error handling!"); // This should not happen :(
            }
            PlayerAutomaClient.LOGGER.info("Failed to create output stream for selected file");
        }
        return false;
    }

    /**
     * Store record to file in playerautoma recordings folder.
     * The filetype is determined by the current Option set in RecordingStorerScreen
     * If a file with that name exist it will not be replaced instead to the given filename "_new" will be appended
     * @param record record to store
     * @param name name of the file to store record in
     * @return true if file was successfully created
     */
    public static boolean storeRecordingFile(Recording record, File directory, String name) {
        String storeAs = RecordingStorerScreen.useJSON.getValue() ? RecordingFileTypes.JSON : RecordingFileTypes.REC;
        return storeRecordingFile(record, directory, name, storeAs, false);
    }

}
