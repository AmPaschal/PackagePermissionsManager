package com.ampaschal.google;

import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;
import com.ampaschal.google.utils.PackagePermissionResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PermissionsManager {

    private static PermissionsCallback callback;
    private static PackagePermissionResolver permissionResolver;

//    private static Map<String, PermissionObject> permissionObjectMap = new HashMap<>();

    public static void log() {
        System.out.println("Permissions check will be done here");

    }

    public static void mockTest(int resourceType, int resourceOp, String resourceItem) throws SecurityException {
        System.out.println("Substituting permission check " + resourceItem + resourceType + resourceOp);

        throw new SecurityException("Trying things out");
    }

    public static void setup() {

        String permissionsFilePath = "/home/pamusuo/research/permissions-manager/PackagePermissionsManager/src/main/" +
                "java/com/ampaschal/google/permfiles/sample-permissions.json";

        setup(permissionsFilePath, null);

    }

    private static PermissionsCallback getDefaultCallback() {
        return new PermissionsCallback() {
            @Override
            public void onPermissionRequested(String subject, int subjectPathSize, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

                 System.out.println("[PERMISSION] " + subject + " " + subjectPathSize + " " + resourceType + " " + resourceOp + " " + resourceItem);


            }

            @Override
            public void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

            }
        };
    }

    public static void setup(String permissionsFile, PermissionsCallback permCallback) {

        if (permissionsFile == null || permissionsFile.isEmpty() || !Files.exists(Paths.get(permissionsFile))) {
            System.out.println("Permissions File not found");
            return;
        }
//        Set the permissions object
        try {
            permissionResolver = new PackagePermissionResolver();
            permissionResolver.generatePermissionsContext(permissionsFile);
            callback = permCallback != null ? permCallback : getDefaultCallback();
        } catch (IOException e) {
            System.out.println("Exception thrown");
            throw new RuntimeException(e);
        }
    }

//    private static void parseAndSetPermissionsObject(String permissionsFilePath) throws IOException {
//
//        File permissionsFile = new File(permissionsFilePath);
//
//        TypeReference<Map<String, PermissionObject>> typeRef = new TypeReference<Map<String, PermissionObject>>() {};
//
//        Map<String, PermissionObject> permMap = new ObjectMapper().readValue(permissionsFile, typeRef);
//
//        if (permMap != null && !permMap.isEmpty()) {
//            permissionObjectMap.putAll(permMap);
//        }
//    }

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

    private static Set<String> getSubjectPaths() {
//        I used a set to avoid repeated entries. This will reduce the overhead when walking the list
        Set<String> subjectPaths = new LinkedHashSet<>();
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

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, String resourceItem) {

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

        if (subjectPaths.isEmpty()) {
            return;
        }

        int subjectPathSize = subjectPaths.size();

        callback.onPermissionRequested(null, subjectPathSize, resourceType, resourceOp, resourceItem);

//        Check the Permissions cache if access is permitted
//        Boolean cachedPermission = checkPermissionCache(subjectPaths, resourceType, resourceOp, resourceItem);
//
//        if (cachedPermission != null) {
//            if (cachedPermission) {
//                return;
//            } else {
//                throw new SecurityException("File read " + resourceItem + " not permitted");
//            }
//        }


//        Get the list of permission objects from the stack trace
        Set<PermissionObject> permissionObjects = getPermissions(subjectPaths);

        if (permissionObjects.isEmpty()) {
            return;
        }

        // System.out.println("Permission count: " + permissionObjects.size());

//        We confirm each package in the stacktrace has the necessary permissions
        for (PermissionObject permissionObject: permissionObjects) {
            boolean permitted = performPermissionCheck(permissionObject, resourceType, resourceOp, resourceItem);

            if (!permitted) {
                callback.onPermissionFailure(subjectPaths, resourceType, resourceOp, resourceItem);
                throw new SecurityException("Access to " + resourceItem + " not permitted");
            }
        }

    }

    public static void checkPermissionEval(int resourceTypeInt, int resourceOpInt, String resourceItem, Set<String> mockSubjectPaths) {

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

        callback.onPermissionRequested(null, subjectPathSize, resourceType, resourceOp, resourceItem);

//        Check the Permissions cache if access is permitted
//        Boolean cachedPermission = checkPermissionCache(subjectPaths, resourceType, resourceOp, resourceItem);
//
//        if (cachedPermission != null) {
//            if (cachedPermission) {
//                return;
//            } else {
//                throw new SecurityException("File read " + resourceItem + " not permitted");
//            }
//        }


//        Get the list of permission objects from the stack trace
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
                throw new SecurityException("Access to " + resourceItem + " not permitted");
            }
        }

    }

    private static boolean performPermissionCheck(PermissionObject permissionObject, ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

//        Permission checking starts from the lowest granularity
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
