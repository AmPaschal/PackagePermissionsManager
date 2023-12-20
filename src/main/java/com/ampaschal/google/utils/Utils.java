package com.ampaschal.google.utils;

import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.enums.RuntimeMode;

public class Utils {
    
    public static PermissionArgs processAgentArgs(String agentArgString) {
        RuntimeMode runtimeMode = RuntimeMode.ENFORCE;
        String outputPath = "";
        String permissionFilePath = "";
        // Split the agentArgs string into individual arguments
        String[] arguments = agentArgString.split(",");
        
        // Process each argument
        for (String argument : arguments) {
            String[] parts = argument.split("=");
            if (parts.length == 2) {
                String argName = parts[0];
                String argValue = parts[1];
                
                // Process the argument as needed
                if ("mode".equals(argName)) {
                    runtimeMode = RuntimeMode.valueOf(argValue);
                } else if ("outputPath".equals(argName)) {
                    outputPath = argName;
                } else if ("permFilePath".equals(argName)) {
                    permissionFilePath = argValue;
                }
            }
        }

        PermissionArgs agentArgs = new PermissionArgs(runtimeMode, permissionFilePath, outputPath);

        // TODO: Validate the presence of necessary arguments
        return agentArgs;
    }
}
