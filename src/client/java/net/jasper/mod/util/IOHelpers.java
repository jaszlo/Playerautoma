package net.jasper.mod.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jasper.mod.PlayerautomaClient;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.jasper.mod.util.data.Recording;

import java.io.*;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOHelpers {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RecordingFileTypes {
        public static final String REC = "rec";
        public static final String JSON = "json";

        public static String[] types() {
            return new String[] { REC, JSON };
        }
    }

    @SuppressWarnings("java:S3776")
    public static Recording loadRecordingFile(File directory, File name) {
        File toLoad = new File(directory, name.getName());
        if (!toLoad.exists()) return new Recording(null);

        Recording result = new Recording(null);
        for (String option : RecordingFileTypes.types()) {
            if (option.equals(RecordingFileTypes.JSON)) {
                try (BufferedReader reader = new BufferedReader(new FileReader(toLoad))) {
                    StringBuilder readFile = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        readFile.append(line);
                    }
                    result = JsonHelpers.deserialize(readFile.toString());
                } catch (Exception e) {
                    // Try rec file then
                    continue;
                }
                return result;
            } else if (option.equals(RecordingFileTypes.REC)) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(toLoad))) {
                    // This can happen when a file is selected and then deleted via the file explorer
                    if (objectInputStream == null) throw new IOException("objectInputStream is null");

                    result = (Recording) objectInputStream.readObject();
                } catch (Exception e) {
                    PlayerautomaClient.LOGGER.warn(e.getMessage());
                }
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
     * @param recording to store to file
     * @param directory where to locate file
     * @param name of the file created
     * @param storeAs file type
     * @param overwrite if exists
     * @return true on success
     */
    public static boolean storeRecordingFile(Recording recording, File directory, String name, String storeAs, boolean overwrite) {
        // If file already exists create new file with "_new" before file type.
        File selected = createNewFileName(directory, name, storeAs, overwrite);
        // Store as .json/.rec according to option
        if (storeAs.equals(RecordingFileTypes.JSON)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selected))) {
                String json = JsonHelpers.serialize(recording);
                bufferedWriter.write(json);
                return true;
            } catch (IOException e) {
                PlayerautomaClient.LOGGER.info("Failed to create BufferedWriter stream for selected file: {}", e.getMessage());
            }
        } else {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(selected))) {
                objectOutputStream.writeObject(recording);
                return true;
            } catch (IOException e) {
                PlayerautomaClient.LOGGER.info("Failed to create ObjectOutputStream for selected file: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Store recording to file in playerautoma recordings folder.
     * The filetype is determined by the current Option set in RecordingStorerScreen
     * If a file with that name exist it will not be replaced instead to the given filename "_new" will be appended
     * @param recording record to store
     * @param name name of the file to store record in
     * @return true if file was successfully created
     */
    public static boolean storeRecordingFile(Recording recording, File directory, String name) {
        String storeAs = Boolean.TRUE.equals(RecordingStorerScreen.useJSON.getValue()) ? RecordingFileTypes.JSON : RecordingFileTypes.REC;
        return storeRecordingFile(recording, directory, name, storeAs, false);
    }

}
