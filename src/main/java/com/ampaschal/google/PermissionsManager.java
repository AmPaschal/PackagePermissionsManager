package com.ampaschal.google;

import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;
import com.ampaschal.google.utils.PackagePermissionResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.FileWriter;

public class PermissionsManager {

    private static PermissionsCallback callback;
    private static PackagePermissionResolver permissionResolver;

//    private static Map<String, PermissionObject> permissionObjectMap = new HashMap<>();
    private static boolean monitorMode;
    private static boolean enforceMode;
    //private static String permFileOutput;
    private static long timeLastUpdated, duration;

    private static Map<String, PermissionObject> permissionObjectMap = new HashMap<>();
    private static Map<String, PermissionObject> monitorObjectMapDirect = new HashMap<>();
    private static Map<String, PermissionObject> monitorObjectMapTransitive = new HashMap<>();

    public static void log() {
        System.out.println("Permissions check will be done here");

    }

    public static void mockTest(int resourceType, int resourceOp, String resourceItem) throws SecurityException {
        System.out.println("Substituting permission check " + resourceItem + resourceType + resourceOp);

//        throw new SecurityException("Trying things out");
    }

    public static void setup(boolean monitor, boolean enforce, long durationInput, String repoName) {

        //String permissionsFilePath = "src/main/java/com/ampaschal/google/permfiles/sample-permissions.json";
        //permFileOutput = "src/main/java/com/ampaschal/google/permfiles/output.json";
        setMonitorMode(monitor);
        setEnforcementMode(enforce);
        setDuration(durationInput);
        System.out.println("Monitoring Mode: " + monitor);
        System.out.println("Enforcement Mode: " + enforce);

        String permissionsFilePath = "/home/pamusuo/research/permissions-manager/PackagePermissionsManager/src/main/" +
        "java/com/ampaschal/google/permfiles/sample-permissions.json";

        setup(permissionsFilePath, null);
        
        timeLastUpdated = System.currentTimeMillis();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { writeJsonFile(repoName); }
        });

    }

    private static PermissionsCallback getDefaultCallback() {
        return new PermissionsCallback() {
            @Override
            public void onPermissionRequested(LinkedHashSet<String> subjectPaths, int subjectPathSize, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

                Iterator<String> subjectPathsIterator = subjectPaths.iterator();
                

//                 System.out.println("[PERMISSION] " + subject + " " + subjectPathSize + " " + resourceType + " " + resourceOp + " " + resourceItem);
        
                String subject = subjectPathsIterator.next();
                String strippedSubject = stripSubject(subject, 4);
                System.out.println("[PERMISSION] " + strippedSubject + " " + subjectPathSize + " " + resourceType + " " + resourceOp + " " + resourceItem);
                updateMonitorMapDirect(strippedSubject, subjectPathSize, resourceType, resourceOp, resourceItem);
                updateMonitorMapTransitive(strippedSubject, subjectPathSize, resourceType, resourceOp, resourceItem);
                for(int i = 1; i < subjectPathSize; i++)
                {
                    subject = subjectPathsIterator.next();
                    strippedSubject = stripSubject(subject, 4);
                    updateMonitorMapTransitive(strippedSubject, subjectPathSize, resourceType, resourceOp, resourceItem);
                }

                 //long timeNow = System.currentTimeMillis();

                 /*if (timeNow - timeLastUpdated > duration) {

                    writeJsonFile();

                 }*/


            }

            @Override
            public void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

            }
        };
    }

    private static void writeJsonFile(String repoName) {


        System.out.println("FILE WRITE HERE");
        Gson gson = new Gson();
        String jsonOutDirect = gson.toJson(monitorObjectMapDirect);
        String jsonOutTransitive = gson.toJson(monitorObjectMapTransitive);
        String directFileName = "/home/robin489/vulnRecreation/PackagePermissionsManager/applicationDependencies/" + repoName  + "Direct.json";
        String transitiveFileName = "/home/robin489/vulnRecreation/PackagePermissionsManager/applicationDependencies/" + repoName + "Transitive.json";
        try {
            try (FileWriter writer = new FileWriter(directFileName)) {
                writer.write(jsonOutDirect);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            try (FileWriter writer = new FileWriter(transitiveFileName)) {
                writer.write(jsonOutTransitive);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        

    

    private static void updateMonitorMapDirect(String subject, int subjectPathSize, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {
       
            

            if(!monitorObjectMapDirect.containsKey(subject))
            {
                monitorObjectMapDirect.put(subject, new PermissionObject());
            }
            
        
       
           
            
        
        PermissionObject curObjectDirect = monitorObjectMapDirect.get(subject);
        
        
        if(resourceType == ResourceType.FS) {
            if(resourceOp == ResourceOp.READ) {
                curObjectDirect.setFsRead(true);
                
                curObjectDirect.addAllowedPath(resourceItem);
                
                
            }
            else if(resourceOp == ResourceOp.WRITE) {

                curObjectDirect.setFsWrite(true);
                
                curObjectDirect.addAllowedPath(resourceItem); 
                
            }

        }
        else if(resourceType == ResourceType.NET) {
            if(resourceOp == ResourceOp.ACCEPT) {
                curObjectDirect.setNetAccept(true);
                
                curObjectDirect.addAllowedUrl(resourceItem);

            }
            else if(resourceOp == ResourceOp.CONNECT) {
                curObjectDirect.setNetConnect(true);
                
                curObjectDirect.addAllowedUrl(resourceItem);   
                
            }

        }
        else {
            curObjectDirect.setRuntimeExec(true);
            curObjectDirect.addAllowedCommand(resourceItem);    

        }
    }

    private static void updateMonitorMapTransitive(String subject, int subjectPathSize, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {
       
            

            if(!monitorObjectMapTransitive.containsKey(subject))
            {
                monitorObjectMapTransitive.put(subject, new PermissionObject());
            }
            
        
       
           
            
        
        PermissionObject curObjectTransitive = monitorObjectMapTransitive.get(subject);
        
        
        if(resourceType == ResourceType.FS) {
            if(resourceOp == ResourceOp.READ) {
                curObjectTransitive.setFsRead(true);
                
                curObjectTransitive.addAllowedPath(resourceItem);
                
                
            }
            else if(resourceOp == ResourceOp.WRITE) {

                curObjectTransitive.setFsWrite(true);
                
                curObjectTransitive.addAllowedPath(resourceItem); 
                
            }

        }
        else if(resourceType == ResourceType.NET) {
            if(resourceOp == ResourceOp.ACCEPT) {
                curObjectTransitive.setNetAccept(true);
                
                curObjectTransitive.addAllowedUrl(resourceItem);

            }
            else if(resourceOp == ResourceOp.CONNECT) {
                curObjectTransitive.setNetConnect(true);
                
                curObjectTransitive.addAllowedUrl(resourceItem);   
                
            }

        }
        else {
            curObjectTransitive.setRuntimeExec(true);
            curObjectTransitive.addAllowedCommand(resourceItem);    

        }
    }

    public static void setup(String permissionsFile, PermissionsCallback permCallback) {
        
        
        if ((permissionsFile == null || permissionsFile.isEmpty()) && enforceMode) {
            return;
        }
//        Set the permissions object
        
            try {
                if(enforceMode)
                {
                    parseAndSetPermissionsObject(permissionsFile);
                }    
                callback = permCallback != null ? permCallback : getDefaultCallback();
                
            } catch (IOException e) {
                System.out.println("Exception thrown");
                throw new RuntimeException(e);
            }
        
        
    }

    private static void parseAndSetPermissionsObject(String permissionsFilePath) throws IOException {

        File permissionsFile = new File(permissionsFilePath);

        TypeReference<Map<String, PermissionObject>> typeRef = new TypeReference<Map<String, PermissionObject>>() {};

        Map<String, PermissionObject> permMap = new ObjectMapper().readValue(permissionsFile, typeRef);

        if (permMap != null && !permMap.isEmpty()) {
            permissionObjectMap.putAll(permMap);
        }
    }
    private static void setDuration(long durationInput)
    {
        duration = durationInput;
    }
    private static void setMonitorMode(boolean monitor) {
        monitorMode = monitor;

    }
    private static void setEnforcementMode(boolean enforce) {
        enforceMode = enforce;
    }
    private static String getSubjectPath() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String currentClass = stackTrace[1].getClassName();

//        Return the first non-java class in the stackstrace
//        I want to skip the containing class of this method as it clearly can't be the subject
        for (StackTraceElement element: stackTrace) {
            String elementClassName = element.getClassName();
            if (elementClassName.startsWith("java") || elementClassName.startsWith("jdk") || elementClassName.startsWith("sun") || elementClassName.equals(currentClass)) {
                continue;
            }
            return elementClassName;
        }
        return null;
    }

    private static LinkedHashSet<String> getSubjectPaths() {
//        I used a set to avoid repeated entries. This will reduce the overhead when walking the list
        LinkedHashSet<String> subjectPaths = new LinkedHashSet<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String currentClass = stackTrace[1].getClassName();

//        Return the first non-java class in the stackstrace
//        I want to skip the containing class of this method as it clearly can't be the subject
        for (StackTraceElement element: stackTrace) {
            String elementClassName = element.getClassName();
            if (elementClassName.startsWith("java") || elementClassName.startsWith("jdk") || elementClassName.startsWith("sun") || elementClassName.equals(currentClass)) {
                continue;
            }
            subjectPaths.add(elementClassName);
        }
        return subjectPaths;
    }

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, String resourceItem) throws IOException, FileNotFoundException  {

//        System.out.println("Checking permissions: " + ResourceType.getResourceType(resourceTypeInt) + " - " + ResourceOp.getResourceOp(resourceOpInt)  + " - " + resourceItem);

//        I would have first returned true if the permissionsObject is null, but I am assuming instrumentations are done
//        only if the permissions file is present

//        System.out.println("Permissions Object size: " + permissionObjectMap.size());

        ResourceType resourceType = ResourceType.getResourceType(resourceTypeInt);  // Change to string

        ResourceOp resourceOp = ResourceOp.getResourceOp(resourceOpInt);    // Change to string

        if (resourceType == null || resourceOp == null) {
            throw new SecurityException("Invalid Permission Request");
        }

        LinkedHashSet<String> subjectPaths = getSubjectPaths();
        
        
        int subjectPathSize = subjectPaths.size();
        /*while(subjectPathsIterator.hasNext())
        {
            System.out.println(subjectPathsIterator.next());
        }*/
        if(monitorMode) {
        /*System.out.println("Calling callback function");
        System.out.println("Path Size: " + subjectPathSize);
        System.out.println("Resource Type: " + resourceType);
        System.out.println("Resource Op: " + resourceOp);
        System.out.println("Resource Item: " + resourceItem);*/
        callback.onPermissionRequested(subjectPaths, subjectPathSize, resourceType, resourceOp, resourceItem);
        }

//        Get the list of permission objects from the stack trace
        
        Set<PermissionObject> permissionObjects = getPermissions(subjectPaths);
        
        if (permissionObjects.isEmpty()) {
            return;
        }

        // System.out.println("Permission count: " + permissionObjects.size());
    if(enforceMode) {
//        We confirm each package in the stacktrace has the necessary permissions
        for (PermissionObject permissionObject: permissionObjects) {
            boolean permitted = performPermissionCheck(permissionObject, resourceType, resourceOp, resourceItem);

            if (!permitted) {
                callback.onPermissionFailure(subjectPaths, resourceType, resourceOp, resourceItem);
                if (ResourceType.NET.equals(resourceType) || ResourceType.RUNTIME.equals(resourceType)) {
                    throw new IOException("Permission not granted");
                } else if (ResourceType.FS.equals(resourceType)) {
                    throw new FileNotFoundException("Permission not granted");
                }
            }
        }
    }
    

    }

    public static String stripSubject(String subject, int numSegments)
    {
        //System.out.println(subject);
        String[] segments = subject.split("[.]");
        //System.out.println(Arrays.toString(segments));
        String strippedSubject = segments[0];
        for(int i = 1; i < numSegments && i < segments.length - 1; i++)
        {   
            strippedSubject += "." + segments[i];
        }
        return(strippedSubject);
    }

    public static void checkPermissionEval(int resourceTypeInt, int resourceOpInt, String resourceItem, Set<String> mockSubjectPaths) throws IOException, FileNotFoundException {
    
//        System.out.println("Checking permissions: " + ResourceType.getResourceType(resourceTypeInt) + " - " + ResourceOp.getResourceOp(resourceOpInt)  + " - " + resourceItem);

//        I would have first returned true if the permissionsObject is null, but I am assuming instrumentations are done
//        only if the permissions file is present

//        System.out.println("Permissions Object size: " + permissionObjectMap.size());

        ResourceType resourceType = ResourceType.getResourceType(resourceTypeInt);

        ResourceOp resourceOp = ResourceOp.getResourceOp(resourceOpInt);

        if (resourceType == null || resourceOp == null) {
            throw new SecurityException("Invalid Permission Request");
        }

        Set<String> subjectPaths = getSubjectPaths();

        subjectPaths = mockSubjectPaths.isEmpty() ? subjectPaths : mockSubjectPaths;

        if (subjectPaths.isEmpty()) {
            return;
        }

        int subjectPathSize = subjectPaths.size();

        if(monitorMode) {
            callback.onPermissionRequested(null, subjectPathSize, resourceType, resourceOp, resourceItem);
        }


//        Get the list of permission objects from the stack trace
        if(enforceMode) {
            Set<PermissionObject> permissionObjects = getPermissions(subjectPaths);

            if (permissionObjects.isEmpty()) {
                return;
            }

//        System.out.println("Permission count: " + permissionObjects.size());

//        We confirm each package in the stacktrace has the necessary permissions
            for (PermissionObject permissionObject: permissionObjects) {
                boolean permitted = performPermissionCheck(permissionObject, resourceType, resourceOp, resourceItem);

            if (!permitted) {
                callback.onPermissionFailure(subjectPaths, resourceType, resourceOp, resourceItem);
                if (ResourceType.NET.equals(resourceType) || ResourceType.RUNTIME.equals(resourceType)) {
                    throw new IOException("Permission not granted");
                } else if (ResourceType.FS.equals(resourceType)) {
                    throw new FileNotFoundException("Permission not granted");
                }
            }
        }

    }

    private static boolean performPermissionCheck(PermissionObject permissionObject, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

//        Permission checking starts from the lowest granularity
//        Replace with string matching, eg fs.read. If granted, check fs.read.deny; if denied, check fs.read.allow
        if (resourceType == ResourceType.FS) {
            if (permissionObject.getAllowedPaths().contains(resourceItem)) {
                return true;
            } else if (permissionObject.getDeniedPaths().contains(resourceItem)) {
                return false;
            } else if ((resourceOp == ResourceOp.READ && permissionObject.isFsRead()) || (resourceOp == ResourceOp.WRITE && permissionObject.isFsWrite())) {
                return true;
            } else {
                return permissionObject.isFs();
            }
        } else if (resourceType == ResourceType.NET) {
            if (permissionObject.getAllowedUrls().contains(resourceItem)) {
                return true;
            } else if (permissionObject.getDeniedUrls().contains(resourceItem)) {
                return false;
            } else if ((resourceOp == ResourceOp.CONNECT && permissionObject.isNetConnect()) || (resourceOp == ResourceOp.ACCEPT && permissionObject.isNetAccept())) {
                return true;
            } else {
                return permissionObject.isNet();
            }
        } else if (resourceType == ResourceType.RUNTIME) {
            if (permissionObject.getAllowedCommands().contains(resourceItem)) {
                return true;
            } else if (permissionObject.getDeniedCommands().contains(resourceItem)) {
                return false;
            } else if (resourceOp == ResourceOp.EXECUTE && permissionObject.isRuntimeExec()) {
                return true;
            } else {
                return permissionObject.isRuntime();
            }
        } else if (resourceType == ResourceType.THREAD) {
            if (resourceOp == ResourceOp.EXECUTE && permissionObject.isThreadStart()) {
                return true;
            } else {
                return permissionObject.isThread();
            }
        }

        return true;
    }

    private static String getPackageName(String className) {
        String[] segments = className.split("\\.");

        if (segments.length == 1) {
//            Taking care of a unique scenario in Dacapobench where the main class had no specified Package name
            return className;
        }
        int numSegments = Math.min(3, segments.length - 1);

        StringBuilder packageNameBuilder = new StringBuilder();
        for (int i = 0; i < numSegments - 1; i++) {
            packageNameBuilder.append(segments[i]).append(".");
        }
        packageNameBuilder.append(segments[numSegments - 1]);
//        if (segments.length < 4) {
//            System.out.println("Classname with <4 segments: " + className);
//
//            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//
//            for (StackTraceElement trace: stackTrace) {
//                System.out.println(trace.getClassName() + "." + trace.getMethodName());
//            }
//        }

        return packageNameBuilder.toString();
    }

//    private static String findClosestPackageName(String subjectPath) {
//        String closestPackageName = null;
//        int longestMatch = 0;
//
//        for (String packageName: permissionObjectMap.keySet()) {
//            if (subjectPath.startsWith(packageName) && packageName.length() > longestMatch) {
//                closestPackageName = packageName;
//                longestMatch = packageName.length();
//            }
//        }
//
//        return closestPackageName;
//
//    }

    private static Set<PermissionObject> getPermissions(Set<String> subjectPaths) {

        Set<PermissionObject> permissionObjects = new HashSet<>();

        for (String path: subjectPaths) {

            PermissionObject permObject = permissionResolver.getPermissionForClassName(path);

            if (permObject != null) {
                permissionObjects.add(permObject);
            }

        }

        return permissionObjects;

    }

    private static Boolean checkPermissionCache(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

        return null;
    }

}
