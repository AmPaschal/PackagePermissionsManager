package com.ampaschal.google.enums;

public enum ResourceType {
    FS (0),
    NET(1),
    RUNTIME(2),
    THREAD(3),
    ;

    private final int id;

    public int getId() {
        return id;
    }

    ResourceType(int id) {
        this.id = id;
    }
    public static ResourceType getResourceType(int id) {
        for (ResourceType res: values()) {
            if (res.id == id) {
                return res;
            }
        }

        return null;
    }

    public static int getResourceTypeId(ResourceType resourceType) {
        for (ResourceType res: values()) {
            if (res == resourceType) {
                return res.id;
            }
        }

        return -1;
    }
}
