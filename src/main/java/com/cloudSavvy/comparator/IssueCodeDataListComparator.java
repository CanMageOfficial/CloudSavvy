package com.cloudSavvy.comparator;

import com.cloudSavvy.aws.common.IssueCode;
import com.cloudSavvy.aws.common.IssueSeverity;
import com.cloudSavvy.common.IssueData;
import com.cloudSavvy.utils.IssueDataUtils;
import software.amazon.awssdk.regions.Region;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

public class IssueCodeDataListComparator implements
        Comparator<Map.Entry<Region, SortedMap<IssueCode, List<IssueData>>>> {

    @Override
    public int compare(Map.Entry<Region, SortedMap<IssueCode, List<IssueData>>> o1,
                       Map.Entry<Region, SortedMap<IssueCode, List<IssueData>>> o2) {
        Map<IssueSeverity, Integer> issueSevCountMap1 = buildIssueCountMap(o1.getValue());
        Map<IssueSeverity, Integer> issueSevCountMap2 = buildIssueCountMap(o2.getValue());
        List<IssueSeverity> sortedSeverities = IssueDataUtils.getSortedSeverities();
        for (IssueSeverity sev : sortedSeverities) {
            if (!Objects.equals(issueSevCountMap1.get(sev), issueSevCountMap2.get(sev))) {
                return Integer.compare(issueSevCountMap2.get(sev), issueSevCountMap1.get(sev));
            }
        }

        return o1.getKey().id().compareTo(o2.getKey().id());
    }

    private Map<IssueSeverity, Integer> buildIssueCountMap(final Map<IssueCode, List<IssueData>> issueCodeListMap) {
        Map<IssueSeverity, Integer> issueSevCountMap = new HashMap<>();
        Arrays.stream(IssueSeverity.values()).forEach(sev -> issueSevCountMap.put(sev, 0));
        for (Map.Entry<IssueCode, List<IssueData>> entry : issueCodeListMap.entrySet()) {
            IssueSeverity issueSev = entry.getKey().getIssueSeverity();
            Integer count = issueSevCountMap.get(issueSev);
            issueSevCountMap.put(issueSev, count + entry.getValue().size());
        }
        return issueSevCountMap;
    }
}
