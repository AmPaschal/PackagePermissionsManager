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
        String outputFile = null;
        String permissionFilePath = null;
        boolean fineGranularity = false;
        boolean enableCache = false;
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
                    } else if ("outputFile".equals(argName)) {
                        outputFile = argValue;
                    } else if ("permFilePath".equals(argName)) {
                        permissionFilePath = argValue;
                    } else if ("granularity".equals(argName)) {
                        fineGranularity = argValue.equals("fine");
                    } else if ("enableCache".equals(argName)) {
                        enableCache = Boolean.parseBoolean(argValue);
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

        if (monitor && outputFile == null) {
            
            outputFile = System.getProperty("user.dir") + "/next-jsm-policy.json";
            
        }

        PermissionArgs agentArgs = new PermissionArgs(permissionFilePath, outputFile, enforce, monitor, fineGranularity, enableCache);

        // TODO: Validate the presence of necessary arguments
        return agentArgs;
    }
}
