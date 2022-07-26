package com.cloudSavvy.comparator;

import com.cloudSavvy.common.ResourceMetadata;

import java.util.Comparator;

public class ResourceDateComparator implements Comparator<ResourceMetadata> {

    // Returns the newest date first
    @Override
    public int compare(final ResourceMetadata rm1, final ResourceMetadata rm2) {
        if (rm2 == null || rm2.getActivityDate() == null) {
            return 1;
        }

        if (rm1 == null || rm1.getActivityDate() == null) {
            return -1;
        }

        return rm2.getActivityDate().compareTo(rm1.getActivityDate());
    }
}
