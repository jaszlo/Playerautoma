package net.jasper.mod.gui;

import net.jasper.mod.PlayerAutoma;
import net.jasper.mod.PlayerAutomaClient;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class FileChooser {
    public enum Operation {
        STORE,
        LOAD
    }

    private static final String COMMAND = "java";
    private static final String ARG = "-classpath";
    private static final String JAR = PlayerAutoma.MOD_ID + PlayerAutoma.MOD_VERSION + ".jar";
    private static final String CLASS = "FileChooser";

    public static String getPath(FileChooser.Operation op) {
        try {
            // Construct the path and File to the .minecraft/mods folder where the Mods Jar ist located
            String modsPath = System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "mods";
            File modsFolder = new File(modsPath);

            // Construct the Command
            String[] command = new String[] { COMMAND, ARG, JAR, CLASS, op.name() };
            Process p = new ProcessBuilder(command).directory(modsFolder).start();

            // Read stdout from process
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Should only be one line and therefore work
            String result = reader.readLine();
            reader.close();

            // Wait for process to terminate
            p.waitFor();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
