package com.ampaschal.google;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

    private static Map<String, PermissionObject> monitorMap = new HashMap<>();

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
                    writeJsonFile(permissionArgs.getOutputFile());
                }
            });
        }

    }

    private static LinkedHashSet<String> getForSubjectPackageNames(LinkedHashSet<String> subjectPaths) {
        LinkedHashSet<String> packageNames = new LinkedHashSet<>();
        for (String subject : subjectPaths) {
            packageNames.add(getPackageName(subject));
        }
        return packageNames;
    }

    private static void updateMonitorMap(String packageName, ResourceType resourceType, ResourceOp resourceOp,
            String resourceItem, boolean direct) {

        PermissionObject permissionObject = monitorMap.get(packageName);

        if (permissionObject == null) {
            permissionObject = new PermissionObject();
            monitorMap.put(packageName, permissionObject);
        }

        if (resourceType == ResourceType.FS) {
            permissionObject.setFs(true);
            if (resourceOp == ResourceOp.READ) {
                permissionObject.setFsRead(true);

                
                if (direct) {
                    permissionObject.addFsReadAllowed(resourceItem);
                } else {
                    permissionObject.addFsReadTransitive(resourceItem);
                }
            } 
            else if (resourceOp == ResourceOp.WRITE) {
                permissionObject.setFsWrite(true);
                
                if (direct) {
                    permissionObject.addFsWriteAllowed(resourceItem);
                } else {
                    permissionObject.addFsWriteTransitive(resourceItem);
                }
            }
        } else if (resourceType == ResourceType.NET) {
            permissionObject.setNet(true);
            if (resourceOp == ResourceOp.CONNECT) {
                permissionObject.setNetConnect(true);
                
                if (direct) {
                    permissionObject.addNetConnectAllowed(resourceItem);
                } else {
                    permissionObject.addNetConnectTransitive(resourceItem);
                }
            } else if (resourceOp == ResourceOp.ACCEPT) {
                permissionObject.setNetAccept(true);
                
                if (direct) {
                    permissionObject.addNetAcceptAllowed(resourceItem);
                } else {
                    permissionObject.addNetAcceptTransitive(resourceItem);
                }
            }
        }
        else if (resourceType == ResourceType.RUNTIME) {
            permissionObject.setRuntime(true);
            if (resourceOp == ResourceOp.EXECUTE) {
                permissionObject.setRuntimeExec(true);
                
                if (direct) {
                    permissionObject.addRuntimeExecAllowed(resourceItem);
                } else {
                    permissionObject.addRuntimeExecTransitive(resourceItem);
                }
            }
        }
    }

    private static PermissionsCallback getMonitorModeCallback() {
        return new PermissionsCallback() {
            @Override
            public void onPermissionRequested(LinkedHashSet<String> subjectPaths, int subjectPathSize,
                    ResourceType resourceType, ResourceOp resourceOp, String resourceItem) {

                LinkedHashSet<String> packageNames = getForSubjectPackageNames(subjectPaths);

                // System.out.println("Permission requested: " + resourceItem + " " + resourceType + " " + resourceOp + " " + subjectPathSize + " " + packageNames.size());

                // We want to identify the parent package making this request, which should be the package at the top of the stack trace
                boolean direct = true; 

                for (String packageName : packageNames) {
                    updateMonitorMap(packageName, resourceType, resourceOp, resourceItem, direct);
                    direct = false;
                }

            }

            @Override
            public void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp,
                    String resourceItem) {

            }
        };
    }

    private static void writeJsonFile(String outputFile) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new FileOutputStream(outputFile), monitorMap);
        } catch (IOException e) {
            e.printStackTrace();
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
                try {
                    String logFile = "policy-failures.log";
                    FileWriter writer = new FileWriter(logFile, true);
                    writer.write("Permission not granted: \n");
                    writer.write("Accessor package: " + subjectPaths.iterator().next() + "\n");
                    writer.write("Resource Type: " + resourceType + "\n");
                    writer.write("Resource Op: " + resourceOp + "\n");
                    writer.write("Resource: " + resourceItem + "\n");
                    writer.write("------------------------------------------------\n\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                        
                        
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

        String permissionPackage = stackTrace[1].getClassName();

        // Return the first non-java class in the stackstrace
        // I want to skip the containing class of this method as it clearly can't be the
        // subject
        for (StackTraceElement element : stackTrace) {
            String elementClassName = element.getClassName();
            if (elementClassName.startsWith("java") || elementClassName.startsWith("jdk")
                    || elementClassName.startsWith("sun") || elementClassName.startsWith(permissionPackage)) {
                continue;
            }
            subjectPaths.add(elementClassName);
        }
        return subjectPaths;
    }

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, Object resourceItem)
            throws IOException, FileNotFoundException {

        LinkedHashSet<String> subjectPaths = getSubjectPaths();

        if (resourceTypeInt == ResourceType.RUNTIME.getId()) {
            List<String> commandList = (List<String>) resourceItem;
            resourceItem = String.join(" ", commandList);
        }

        checkPermission(resourceTypeInt, resourceOpInt, (String) resourceItem, subjectPaths);

    }

    public static void checkPermission(int resourceTypeInt, int resourceOpInt, Object resourceItem,
            LinkedHashSet<String> subjectPaths) throws IOException, FileNotFoundException {

        ResourceType resourceType = ResourceType.getResourceType(resourceTypeInt); // Change to string

        ResourceOp resourceOp = ResourceOp.getResourceOp(resourceOpInt); // Change to string

        if (resourceType == null || resourceOp == null) {
            throw new SecurityException("Invalid Permission Request");
        }

        if (((String)resourceItem).contains("policy-failures.log")) {
            return;
        }

        int subjectPathSize = subjectPaths.size();

        callback.onPermissionRequested(subjectPaths, subjectPathSize, resourceType, resourceOp,
                resourceItem.toString());

        if (!permissionArgs.isEnforceModeEnabled()) {
            return;
        }

        PermissionObject permissionObject = null;

        if (permissionArgs.isEnableCache() && ResourceType.FS == resourceType) {
            String cacheKey = getCacheKey(subjectPaths);
            permissionObject = permissionResolver.getPermissionFromCache(cacheKey);
        }

        // Get the list of permission objects from the stack trace

        if (permissionObject == null) {
            LinkedHashSet<PermissionObject> permissionObjects = getPermissions(subjectPaths);

            if (ResourceType.THREAD.getId() == resourceTypeInt) {
                saveThreadParentPermission((Thread) resourceItem, permissionObjects);
                return;
            }

            LinkedHashSet<PermissionObject> parentPermissions = threadParentPermissions.getOrDefault(Thread.currentThread(),
                new LinkedHashSet<>());

            parentPermissions.addAll(permissionObjects);

            if (parentPermissions.isEmpty()) {
                return;
            }

            permissionObject = compressPermissionObjects(permissionObjects, resourceType, resourceOp, permissionArgs.getFineGranularity());

            if (permissionArgs.isEnableCache() && ResourceType.FS == resourceType) {
                String cacheKey = getCacheKey(subjectPaths);
                permissionResolver.savePermissionToCache(cacheKey, permissionObject);
            }
        }

        // LinkedHashSet<PermissionObject> permissionObjects = getPermissions(subjectPaths);

        // if (ResourceType.THREAD.getId() == resourceTypeInt) {
        //     saveThreadParentPermission((Thread) resourceItem, permissionObjects);
        //     return;
        // }

        // LinkedHashSet<PermissionObject> parentPermissions = threadParentPermissions.getOrDefault(Thread.currentThread(),
        //         new LinkedHashSet<>());

        // parentPermissions.addAll(permissionObjects);

        // if (parentPermissions.isEmpty()) {
        //     return;
        // }

        String resourceItemString = (String) resourceItem;

        // PermissionObject permissionObject = compressPermissionObjects(parentPermissions, resourceType, resourceOp, permissionArgs.getFineGranularity());

        // We confirm each package in the stacktrace has the necessary permissions
        // Evaluate what is faster - checking permission on each item or creating a
        // compressed item first
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

    private static String getCacheKey(LinkedHashSet<String> subjectPaths) {
        StringBuilder cacheKey = new StringBuilder();
        int count = 0;
        for (String path : subjectPaths) {
            cacheKey.append(path);
            if (count >= 5) {
                break;
            }
            count++;
        }
        return cacheKey.toString();
    }

    private static PermissionObject compressPermissionObjects(LinkedHashSet<PermissionObject> permissionObjects, ResourceType resourceType, ResourceOp resourceOp, boolean fineGranularity) {

        PermissionObject compObject = new PermissionObject(true);
        for (PermissionObject permObject: permissionObjects) {

            if (resourceType == ResourceType.FS) {
                compObject.setFs(compObject.isFs() && permObject.isFs());
                if (resourceOp == ResourceOp.READ) {
                    compObject.setFsRead(compObject.isFsRead() && permObject.isFsRead());
                    if (fineGranularity) {
                        compObject.getFsReadAllowed().addAll(permObject.getFsReadAllowed());
                        compObject.getFsReadDenied().addAll(permObject.getFsReadDenied());
                    }
                } else if (resourceOp == ResourceOp.WRITE) {
                    compObject.setFsWrite(compObject.isFsWrite() && permObject.isFsWrite());
                    if (fineGranularity) {
                        compObject.getFsWriteAllowed().addAll(permObject.getFsWriteAllowed());
                        compObject.getFsWriteDenied().addAll(permObject.getFsWriteDenied());
                    }
                }
            } else if (resourceType == ResourceType.NET) {
                compObject.setNet(compObject.isNet() && permObject.isNet());
                if (resourceOp == ResourceOp.CONNECT) {
                    compObject.setNetConnect(compObject.isNetConnect() && permObject.isNetConnect());
                    if (fineGranularity) {
                        compObject.getNetConnectAllowed().addAll(permObject.getNetConnectAllowed());
                        compObject.getNetConnectDenied().addAll(permObject.getNetConnectDenied());
                    }
                } else if (resourceOp == ResourceOp.ACCEPT) {
                    compObject.setNetAccept(compObject.isNetAccept() && permObject.isNetAccept());
                    if (fineGranularity) {
                        compObject.getNetAcceptAllowed().addAll(permObject.getNetAcceptAllowed());
                        compObject.getNetAcceptDenied().addAll(permObject.getNetAcceptDenied());
                    }
                }
            } else if (resourceType == ResourceType.RUNTIME) {
                compObject.setRuntime(compObject.isRuntime() && permObject.isRuntime());
                compObject.setRuntimeExec(compObject.isRuntimeExec() && permObject.isRuntimeExec());
                if (fineGranularity) {
                    compObject.getRuntimeExecAllowed().addAll(permObject.getRuntimeExecAllowed());
                    compObject.getRuntimeExecDenied().addAll(permObject.getRuntimeExecDenied());
                }
            }
        }

        return compObject;
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

            if (!permissionObject.isFs()) {
                return false;
            }

            if (resourceOp == ResourceOp.READ) {
                if (!permissionObject.isFsRead()) {
                    return false;
                }

                if (permissionArgs.getFineGranularity()) {
                    if (permissionObject.getFsReadDenied().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return false;
                    } else if (permissionObject.getFsReadAllowed().isEmpty() || permissionObject.getFsReadAllowed().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return true;
                    } 
                } else {
                    return true;
                }
            } else if (resourceOp == ResourceOp.WRITE) {
                if (!permissionObject.isFsWrite()) {
                    return false;
                }

                if (permissionArgs.getFineGranularity()) {
                    if (permissionObject.getFsWriteDenied().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return false;
                    } else if (permissionObject.getFsWriteAllowed().isEmpty() || permissionObject.getFsWriteAllowed().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return true;
                    } 
                } else {
                    return true;
                }
            }

        } else if (resourceType == ResourceType.NET) {

            if (!permissionObject.isNet()) {
                return false;
            }

            if (resourceOp == ResourceOp.CONNECT) {
                if (!permissionObject.isNetConnect()) {
                    return false;
                }

                if (permissionArgs.getFineGranularity()) {
                    if (permissionObject.getNetConnectDenied().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return false;
                    } else if (permissionObject.getNetConnectAllowed().isEmpty() || permissionObject.getNetConnectAllowed().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return true;
                    } 
                } else {
                    return true;
                }

            } else if (resourceOp == ResourceOp.ACCEPT) {
                if (!permissionObject.isNetAccept()) {
                    return false;
                }

                if (permissionArgs.getFineGranularity()) {
                    if (permissionObject.getNetAcceptDenied().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return false;
                    } else if (permissionObject.getNetAcceptAllowed().isEmpty() || permissionObject.getNetAcceptAllowed().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return true;
                    } 
                } else {
                    return true;
                }
            }

        } else if (resourceType == ResourceType.RUNTIME) {

            if (!permissionObject.isRuntime()) {
                return false;
            }

            if (resourceOp == ResourceOp.EXECUTE) {
                if (!permissionObject.isRuntimeExec()) {
                    return false;
                }

                if (permissionArgs.getFineGranularity()) {
                    if (permissionObject.getRuntimeExecDenied().stream().anyMatch(item -> resourceItem.contains(item))) {
                        return false;
                    } else if (permissionObject.getRuntimeExecAllowed().isEmpty() || permissionObject.getRuntimeExecAllowed().stream().anyMatch(item -> resourceItem.startsWith(item))) {
                        return true;
                    } 
                } else {
                    return true;
                }
            }

        }

        return false;
    }

    private static String getPackageName(String className) {
        String[] segments = className.split("\\.");

        if (segments.length == 1) {
            // Taking care of a unique scenario in Dacapobench where the main class had no
            // specified Package name
            return className;
        }
        int numSegments = Math.min(3, segments.length - 1);

        StringBuilder packageNameBuilder = new StringBuilder(segments[0]);
        for (int i = 1; i < numSegments; i++) {
            packageNameBuilder.append(".").append(segments[i]);
        }

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
