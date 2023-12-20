package com.ampaschal.google.utils;

import com.ampaschal.google.PermissionObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Credits to ChatGPT for this implementation

public class PackagePermissionResolver {

    private TrieNode root = new TrieNode();

    public void generatePermissionsContext(String permissionsFilePath) throws IOException {

        File permissionsFile = new File(permissionsFilePath);

        TypeReference<Map<String, PermissionObject>> typeRef = new TypeReference<Map<String, PermissionObject>>() {};

        Map<String, PermissionObject> permMap = new ObjectMapper().readValue(permissionsFile, typeRef);

        for (Map.Entry<String, PermissionObject> entry: permMap.entrySet()) {
            addPackagePermission(entry.getKey(), entry.getValue());
        }

    }

    private void addPackagePermission(String packageName, PermissionObject permission) {
        TrieNode node = root;
        String[] segments = packageName.split("\\.");

        for (String segment : segments) {
            node = node.getChildren().computeIfAbsent(segment, s -> new TrieNode());
        }

        // Store the permission at the leaf node
        node.setPermission(new PackagePermission(packageName, permission));
    }

    public PermissionObject getPermissionForClassName(String className) {
        String[] segments = className.split("\\.");
        TrieNode node = root;
        PackagePermission result = null;

        for (String segment : segments) {
            TrieNode child = node.getChildren().get(segment);
            if (child == null) {
                break;
            }

            if (child.getPermission() != null) {
                // Store the permission if found but continue searching for longer matches
                result = child.getPermission();
            }

            node = child;
        }

        return result == null ? null : result.getPermission();
    }

    static class PackagePermission {
        private final String packageName;
        private final PermissionObject permission;

        public PackagePermission(String packageName, PermissionObject permission) {
            this.packageName = packageName;
            this.permission = permission;
        }

        public String getPackageName() {
            return packageName;
        }

        public PermissionObject getPermission() {
            return permission;
        }
    }

    static class TrieNode {
        private final Map<String, TrieNode> children = new HashMap<>();
        private PackagePermission permission;

        public Map<String, TrieNode> getChildren() {
            return children;
        }

        public PackagePermission getPermission() {
            return permission;
        }

        public void setPermission(PackagePermission permission) {
            this.permission = permission;
        }
    }
}