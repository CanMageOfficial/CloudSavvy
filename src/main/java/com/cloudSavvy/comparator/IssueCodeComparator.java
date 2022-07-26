package com.cloudSavvy.comparator;

import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.IssueSeverity;

import java.util.Comparator;

public class IssueCodeComparator implements Comparator<IssueCode> {
    @Override
    public int compare(final IssueCode o1, final IssueCode o2) {
        IssueSeverity sev1 = o1.getIssueSeverity();
        IssueSeverity sev2 = o2.getIssueSeverity();
        if (sev1 == sev2) {
            return o1.toString().compareTo(o2.toString());
        }
        return Integer.compare(o2.getIssueSeverity().getValue(), o1.getIssueSeverity().getValue());
    }
}
