package com.cloudSavvy.localization;

import com.cloudSavvy.aws.common.IssueCode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalizationReaderTest {
    private final List<IssueCode> issueCodes = List.of(IssueCode.values());

    @Test
    public void test_getIssueDataShortDesc() {
        Map<IssueCode, String> descriptionsMap = LocalizationReader.getIssueCodeShortDesc(Locale.US, issueCodes);
        for (IssueCode issueCode : issueCodes) {
            assertTrue(descriptionsMap.containsKey(issueCode), issueCode.toString());
            assertFalse(descriptionsMap.get(issueCode).isEmpty(), issueCode.toString());
        }
    }

    @Test
    public void test_getIssueCodeLinks() {
        Map<IssueCode, String> linksMap = LocalizationReader.getIssueCodeLinks(Locale.US, issueCodes);
        for (IssueCode issueCode : issueCodes) {
            assertTrue(linksMap.containsKey(issueCode), issueCode.toString());
            assertFalse(linksMap.get(issueCode).isEmpty(), issueCode.toString());
        }
    }

    @Test
    public void test_getIssueCodeTitles() {
        Map<IssueCode, String> titlesMap =
                LocalizationReader.getIssueCodeLinkTitles(Locale.US, List.of(IssueCode.values()));
        for (IssueCode issueCode : issueCodes) {
            assertTrue(titlesMap.containsKey(issueCode), issueCode.toString());
            assertFalse(titlesMap.get(issueCode).isEmpty(), issueCode.toString());
        }
    }

    @Test
    public void test_getIssueCodeLinks_LinksAreSecure() {
        Map<IssueCode, String> linksMap =
                LocalizationReader.getIssueCodeLinks(Locale.US, List.of(IssueCode.values()));

        for (String link : linksMap.values()) {
            assertTrue(link.startsWith("https"), "Link starts with https:" + link);
        }
    }

    @Test
    public void test_IssueCodeShortDescFile() {
        ResourceBundle descriptions = ResourceBundle.getBundle("IssueCodeShortDesc", Locale.US);
        Set<String> issueCodeSet = getIssueCodeSet();
        for (String key : Collections.list(descriptions.getKeys())) {
            assertTrue(issueCodeSet.contains(key), key);
        }
    }

    @Test
    public void test_IssueCodeLinksFile() {
        ResourceBundle descriptions = ResourceBundle.getBundle("IssueCodeLinks", Locale.US);
        Set<String> issueCodeSet = getIssueCodeSet();
        for (String key : Collections.list(descriptions.getKeys())) {
            assertTrue(issueCodeSet.contains(key), key);
        }
    }

    @Test
    public void test_IssueCodeLinkTitlesFile() {
        ResourceBundle descriptions = ResourceBundle.getBundle("IssueCodeLinkTitles", Locale.US);
        Set<String> issueCodeSet = getIssueCodeSet();
        for (String key : Collections.list(descriptions.getKeys())) {
            assertTrue(issueCodeSet.contains(key), key);
        }
    }

    private Set<String> getIssueCodeSet() {
        return Arrays.stream(IssueCode.values()).map(IssueCode::name).collect(Collectors.toSet());
    }
}
