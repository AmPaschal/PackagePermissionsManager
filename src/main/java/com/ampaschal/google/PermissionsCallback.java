package com.ampaschal.google;

import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;

import java.util.Set;
import java.util.LinkedHashSet;

public interface PermissionsCallback {

    void onPermissionRequested(LinkedHashSet<String> subject, int subjectPathSize, ResourceType resourceType, ResourceOp resourceOp, String resourceItem);

    void onPermissionFailure(Set<String> subjectPaths, ResourceType resourceType, ResourceOp resourceOp, String resourceItem);

}
