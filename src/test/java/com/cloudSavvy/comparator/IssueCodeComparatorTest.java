package com.cloudSavvy.comparator;

import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.IssueSeverity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class IssueCodeComparatorTest {

    @Test
    public void test_compare() {
        List<IssueCode> issueCodes = Arrays.stream(IssueCode.values()).
                sorted(new IssueCodeComparator()).collect(Collectors.toList());
        assertEquals(IssueSeverity.LOW, issueCodes.get(issueCodes.size() - 1).getIssueSeverity());
        SortedSet<IssueCode> sortedSet = new TreeSet<>(new IssueCodeComparator());
        sortedSet.addAll(List.of(IssueCode.values()));
        assertEquals(IssueSeverity.HIGH, sortedSet.iterator().next().getIssueSeverity());

        Set<String> issueCodeTexts = new HashSet<>();
        for (IssueCode issueCode : IssueCode.values()) {
            assertFalse(issueCodeTexts.contains(issueCode.toString()), issueCode.toString());
            issueCodeTexts.add(issueCode.toString());
        }

        assertEquals(IssueCode.values().length, sortedSet.size());
    }
}
