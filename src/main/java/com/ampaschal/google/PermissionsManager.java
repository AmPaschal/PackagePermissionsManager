package com.ampaschal.google;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;
import com.ampaschal.google.utils.PackagePermissionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PermissionsManager {

    private static PermissionsCallback callback;
    private static PackagePermissionResolver permissionResolver;

    private static PermissionArgs permissionArgs;
    private static Map<String, PermissionObject> monitorObjectMapDirect = new HashMap<>();
    private static Map<String, PermissionObject> monitorObjectMapTransitive = new HashMap<>();

    private static Map<Thread, LinkedHashSet<PermissionObject>> threadParentPermissions = new HashMap<>();

    public static void log() {
        System.out.println("Permissions check will be done here");

    }

    public static void mockTest(int resourceType, int resourceOp, Object resourceItem) throws SecurityException {
        System.out.println("Substituting permission check for " + resourceItem);
        System.out.println("Type and Op: " + resourceType + resourceOp);

        // throw new SecurityException("Trying things out");
    }

    public static void setup(PermissionArgs permissionArgs) {

        PermissionsCallback callback = permissionArgs.isMonitorModeEnabled() ? getMonitorModeCallback() : null;

        setup(permissionArgs, callback);

        if (permissionArgs.isMonitorModeEnabled()) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    writeJsonFile(permissionArgs.getOutputPath());
                }
            });
        }

    }

    private static PermissionsCallback getMonitorModeCallback() {
        return new PermissionsCallback() {
            @Override
            public void onPermissionRequested(LinkedHashSet<String> subjectPaths, int subjectPathSize,
                    ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

                Iterator<String> subjectPathsIterator = subjectPaths.iterator();

                String subject = subjectPathsIterator.next();
                String packageName = getPackageName(subject);
                // System.out.println("[PERMISSION] " + packageName + " " + subjectPathSize + "
                // " + resourceType + " " + resourceOp + " " + resourceItem);
                updateMonitorMapDirect(packageName, subjectPathSize, resourceType, resourceOp, resourceItem);
                updateMonitorMapTransitive(packageName, subjectPathSize, resourceType, resourceOp, resourceItem);

                for (int i = 1; i < subjectPathSize; i++) {
                    subject = subjectPathsIterator.next();
                    packageName = getPackageName(subject);
                    updateMonitorMapTransitive(packageName, subjectPathSize, resourceType, resourceOp, resourceItem);
                }

            }

            @Override
            public void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp,
                    String resourceItem) {

            }
        };
    }

    private static void writeJsonFile(String outputPath) {

        try {
            String directFileName = "direct-dependencies.json";
            String transitiveFileName = "transitive-dependencies.json";

            // Use ObjectMapper to write monitorObjectMapDirect and monitorObjectMapTransitive to files
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Paths.get(outputPath, directFileName).toFile(), monitorObjectMapDirect);
            mapper.writeValue(Paths.get(outputPath, transitiveFileName).toFile(), monitorObjectMapTransitive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateMonitorMapDirect(String subject, int subjectPathSize, ResourceType resourceType,
            ResourceOp resourceOp, String resourceItem) {

        if (!monitorObjectMapDirect.containsKey(subject)) {
            monitorObjectMapDirect.put(subject, new PermissionObject());
        }

        PermissionObject curObjectDirect = monitorObjectMapDirect.get(subject);

        if (resourceType == ResourceType.FS) {
            if (resourceOp == ResourceOp.READ) {
                curObjectDirect.setFsRead(true);

                curObjectDirect.addAllowedPath(resourceItem);

            } else if (resourceOp == ResourceOp.WRITE) {

                curObjectDirect.setFsWrite(true);

                curObjectDirect.addAllowedPath(resourceItem);

            }

        } else if (resourceType == ResourceType.NET) {
            if (resourceOp == ResourceOp.ACCEPT) {
                curObjectDirect.setNetAccept(true);

                curObjectDirect.addAllowedUrl(resourceItem);

            } else if (resourceOp == ResourceOp.CONNECT) {
                curObjectDirect.setNetConnect(true);

                curObjectDirect.addAllowedUrl(resourceItem);

            }

        } else {
            curObjectDirect.setRuntimeExec(true);
            curObjectDirect.addAllowedCommand(resourceItem);

        }
    }

    private static void updateMonitorMapTransitive(String subject, int subjectPathSize, ResourceType resourceType,
            ResourceOp resourceOp, String resourceItem) {

        if (!monitorObjectMapTransitive.containsKey(subject)) {
            monitorObjectMapTransitive.put(subject, new PermissionObject());
        }

        PermissionObject curObjectTransitive = monitorObjectMapTransitive.get(subject);

        if (resourceType == ResourceType.FS) {
            if (resourceOp == ResourceOp.READ) {
                curObjectTransitive.setFsRead(true);

                curObjectTransitive.addAllowedPath(resourceItem);

            } else if (resourceOp == ResourceOp.WRITE) {

                curObjectTransitive.setFsWrite(true);

                curObjectTransitive.addAllowedPath(resourceItem);

            }

        } else if (resourceType == ResourceType.NET) {
            if (resourceOp == ResourceOp.ACCEPT) {
                curObjectTransitive.setNetAccept(true);

                curObjectTransitive.addAllowedUrl(resourceItem);

            } else if (resourceOp == ResourceOp.CONNECT) {
                curObjectTransitive.setNetConnect(true);

                curObjectTransitive.addAllowedUrl(resourceItem);

            }

        } else {
            curObjectTransitive.setRuntimeExec(true);
            curObjectTransitive.addAllowedCommand(resourceItem);

        }
    }

    private static PermissionsCallback getDefaultCallback() {
        return new PermissionsCallback() {

            @Override
            public void onPermissionRequested(LinkedHashSet<String> subject, int subjectPathSize,
                    ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {
            }

            @Override
            public void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp,
                    String resourceItem) {
            }

        };
    }

    public static void setup(PermissionArgs permArgs, PermissionsCallback permCallback) {

        permissionArgs = permArgs;

        if (permissionArgs.isEnforceModeEnabled()) {

            String permissionsFile = permissionArgs.getPermissionFilePath();

            if (permissionsFile == null || permissionsFile.isEmpty() || !Files.exists(Paths.get(permissionsFile))) {
                System.out.println("Permissions File not found");
                return;
            }

            // Set the permissions object
            try {
                permissionResolver = new PackagePermissionResolver();
                permissionResolver.generatePermissionsContext(permissionsFile);

            } catch (IOException e) {
                System.out.println("Exception thrown");
                throw new RuntimeException(e);
            }

        }

        callback = permCallback != null ? permCallback : getDefaultCallback();

    }

    private static LinkedHashSet<String> getSubjectPaths() {
        // I used a set to avoid repeated entries. This will reduce the overhead when
        // walking the list
        LinkedHashSet<String> subjectPaths = new LinkedHashSet<>();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String currentClass = stackTrace[1].getClassName();

        // Return the first non-java class in the stackstrace
        // I want to skip the containing class of this method as it clearly can't be the
        // subject
        for (StackTraceElement element : stackTrace) {
            String elementClassName = element.getClassName();
            if (elementClassName.startsWith("java") || elementClassName.startsWith("jdk")
                    || elementClassName.startsWith("sun") || elementClassName.equals(currentClass)) {
                continue;
            }
            subjectPaths.add(elementClassName);
        }
        return subjectPaths;
    }

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, Object resourceItem)
            throws IOException, FileNotFoundException {

        LinkedHashSet<String> subjectPaths = getSubjectPaths();

        checkPermission(resourceTypeInt, resourceOpInt, (String) resourceItem, subjectPaths);

    }

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, Object resourceItem,
            LinkedHashSet<String> subjectPaths) throws IOException, FileNotFoundException {

        ResourceType resourceType = ResourceType.getResourceType(resourceTypeInt); // Change to string

        ResourceOp resourceOp = ResourceOp.getResourceOp(resourceOpInt); // Change to string

        if (resourceType == null || resourceOp == null) {
            throw new SecurityException("Invalid Permission Request");
        }

        int subjectPathSize = subjectPaths.size();

        callback.onPermissionRequested(subjectPaths, subjectPathSize, resourceType, resourceOp, resourceItem.toString());

        if (!permissionArgs.isEnforceModeEnabled()) {
            return;
        }

        // Get the list of permission objects from the stack trace

        LinkedHashSet<PermissionObject> permissionObjects = getPermissions(subjectPaths);

        if (ResourceType.THREAD.getId() == resourceTypeInt) {
            saveThreadParentPermission((Thread)resourceItem, permissionObjects);
            return;
        }

        LinkedHashSet<PermissionObject> parentPermissions = threadParentPermissions.getOrDefault(Thread.currentThread(), new LinkedHashSet<>());

        parentPermissions.addAll(permissionObjects);

        if (parentPermissions.isEmpty()) {
            return;
        }

        String resourceItemString = (String)resourceItem;

        // We confirm each package in the stacktrace has the necessary permissions
        // Evaluate what is faster - checking permission on each item or creating a compressed item first
        for (PermissionObject permissionObject : parentPermissions) {
            boolean permitted = performPermissionCheck(permissionObject, resourceType, resourceOp, resourceItemString);

            if (!permitted) {
                callback.onPermissionFailure(subjectPaths, resourceType, resourceOp, resourceItemString);
                if (ResourceType.NET.equals(resourceType) || ResourceType.RUNTIME.equals(resourceType)) {
                    throw new IOException("Permission not granted");
                } else if (ResourceType.FS.equals(resourceType)) {
                    throw new FileNotFoundException("Permission not granted");
                }
            }
        }

    }

    private static void saveThreadParentPermission(Thread thread, LinkedHashSet<PermissionObject> permissionObjects) {
        
        threadParentPermissions.put(thread, permissionObjects);
    }

    private static boolean performPermissionCheck(PermissionObject permissionObject, ResourceType resourceType,
            ResourceOp resourceOp, String resourceItem) {

        // Permission checking starts from the lowest granularity
        // Replace with string matching, eg fs.read. If granted, check fs.read.deny; if
        // denied, check fs.read.allow
        if (resourceType == ResourceType.FS) {
            if (permissionObject.getAllowedPaths().contains(resourceItem)) {
                return true;
            } else if (permissionObject.getDeniedPaths().contains(resourceItem)) {
                return false;
            } else if ((resourceOp == ResourceOp.READ && permissionObject.isFsRead())
                    || (resourceOp == ResourceOp.WRITE && permissionObject.isFsWrite())) {
                return true;
            } else {
                return permissionObject.isFs();
            }
        } else if (resourceType == ResourceType.NET) {
            if (permissionObject.getAllowedUrls().contains(resourceItem)) {
                return true;
            } else if (permissionObject.getDeniedUrls().contains(resourceItem)) {
                return false;
            } else if ((resourceOp == ResourceOp.CONNECT && permissionObject.isNetConnect())
                    || (resourceOp == ResourceOp.ACCEPT && permissionObject.isNetAccept())) {
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
            // Taking care of a unique scenario in Dacapobench where the main class had no
            // specified Package name
            return className;
        }
        int numSegments = Math.min(3, segments.length - 1);

        StringBuilder packageNameBuilder = new StringBuilder();
        for (int i = 0; i < numSegments - 1; i++) {
            packageNameBuilder.append(segments[i]).append(".");
        }
        packageNameBuilder.append(segments[numSegments - 1]);

        return packageNameBuilder.toString();
    }

    private static LinkedHashSet<PermissionObject> getPermissions(Set<String> subjectPaths) {

        LinkedHashSet<PermissionObject> permissionObjects = new LinkedHashSet<PermissionObject>();

        for (String path : subjectPaths) {

            PermissionObject permObject = permissionResolver.getPermissionForClassName(path);

            if (permObject != null) {
                permissionObjects.add(permObject);
            }

        }

        return permissionObjects;

    }

}
