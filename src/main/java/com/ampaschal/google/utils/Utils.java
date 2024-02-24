package com.ampaschal.google.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.enums.RuntimeMode;

public class Utils {
    
    public static PermissionArgs processAgentArgs(String agentArgString) {
        boolean enforce = false;
        boolean monitor = false;
        String outputPath = null;
        String permissionFilePath = null;
        // Split the agentArgs string into individual arguments

        if (agentArgString != null) {

            String[] arguments = agentArgString.split(",");
        
            // Process each argument
            for (String argument : arguments) {
                String[] parts = argument.split("=");
                if (parts.length == 2) {
                    String argName = parts[0];
                    String argValue = parts[1];
                    
                    // Process the argument as needed
                    if ("mode".equals(argName)) {
                        RuntimeMode runtimeMode = RuntimeMode.valueOf(argValue);

                        if (RuntimeMode.ENFORCE.equals(runtimeMode) || RuntimeMode.BOTH.equals(runtimeMode)) {
                            enforce = true;
                        } 
                        if (RuntimeMode.MONITOR.equals(runtimeMode) || RuntimeMode.BOTH.equals(runtimeMode)) {
                            monitor = true;
                        }
                    } else if ("outputPath".equals(argName)) {
                        outputPath = argValue;
                    } else if ("permFilePath".equals(argName)) {
                        permissionFilePath = argValue;
                    }
                }
            }
        }
        

        if (!enforce && !monitor) {
            enforce = true;
        }

        if (enforce && permissionFilePath == null) {
            System.out.println("No permission file found. Exiting...");
            System.exit(-1);
        }

        if (monitor) {
            if (outputPath == null) {
                outputPath = System.getProperty("user.dir") + '/';
            } else {
                Path outputDir = Paths.get(outputPath);

                // Check if the outputPath is a valid directory
                if (Files.exists(outputDir) && Files.isDirectory(outputDir)) {
                    if (!outputPath.endsWith("/")) {
                        outputPath += "/";
                    }
                } else {
                    System.out.println("Output path:" + outputDir.toAbsolutePath());
                    // Handle the case when outputPath is not a valid directory
                    throw new IllegalArgumentException("Output path is not a valid directory");
                }
            }

            if (!outputPath.endsWith("/")) {
                outputPath += "/";
            }
        }

        PermissionArgs agentArgs = new PermissionArgs(permissionFilePath, outputPath, enforce, monitor);

        // TODO: Validate the presence of necessary arguments
        return agentArgs;
    }
}
