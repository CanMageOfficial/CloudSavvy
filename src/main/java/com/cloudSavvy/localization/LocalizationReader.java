package com.cloudSavvy.localization;

import com.cloudSavvy.aws.common.IssueCode;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LocalizationReader {
    public static Map<IssueCode, String> getIssueCodeShortDesc(Locale curLoc, List<IssueCode> keys) {
        return getIssueCodeData(curLoc, keys, "IssueCodeShortDesc");
    }

    public static Map<IssueCode, String> getIssueCodeLinks(Locale curLoc, List<IssueCode> keys) {
        return getIssueCodeData(curLoc, keys, "IssueCodeLinks");
    }

    public static Map<IssueCode, String> getIssueCodeLinkTitles(Locale curLoc, List<IssueCode> keys) {
        return getIssueCodeData(curLoc, keys, "IssueCodeLinkTitles");
    }

    private static Map<IssueCode, String> getIssueCodeData(final Locale curLoc,
                                                           final List<IssueCode> keys,
                                                           final String bundleName) {

        Map<IssueCode, String> descriptionsMap = new HashMap<>();
        ResourceBundle descriptions
                = ResourceBundle.getBundle(bundleName, curLoc);

        for (IssueCode key : keys) {
            if (descriptions.containsKey(key.name())) {
                descriptionsMap.put(key, descriptions.getString(key.name()));
            }
        }
        return descriptionsMap;
    }

    public static ResourceBundle getMessageTexts(final Locale curLoc) {
        return ResourceBundle.getBundle("MessageTexts", curLoc);
    }
}
