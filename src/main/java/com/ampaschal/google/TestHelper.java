package com.ampaschal.google;

import com.ampaschal.google.enums.ProfileKey;

import java.io.FileOutputStream;
import java.io.IOException;

public class TestHelper {

    private static boolean benchmarkMode;
    private static boolean debugBytecodeMode;

    // To use any function in this class, first enable it by calling the setup function in the Agent's premain method
    public static void setup(boolean benchmark, boolean debugBytecode) {
        benchmarkMode = benchmark;
        debugBytecodeMode = debugBytecode;
    }

    public static void writeBytecodeToFile(byte[] byteArray, String className) {

        if (!debugBytecodeMode) {
            return;
        }

        String transformedFile = "/home/pamusuo/research/permissions-manager/PackagePermissionsManager/src/main/java/com/ampaschal/google/transformed/" + className.replaceAll("/", "-") + ".class";

        try (FileOutputStream fos = new FileOutputStream(transformedFile)) {
            fos.write(byteArray);
            System.out.println("Byte array written to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the byte array to file: " + e.getMessage());
        }
    }

    public static void logTime(ProfileKey key) {

        if (!benchmarkMode) {
            return;
        }

        long time = System.currentTimeMillis();

        System.out.println("[PROFILING]" + " " + key + " " + time);
        
    }
}
