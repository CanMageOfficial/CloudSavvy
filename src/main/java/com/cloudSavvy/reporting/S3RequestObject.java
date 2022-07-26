package com.cloudSavvy.reporting;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.core.sync.RequestBody;

@Builder
@Getter
public class S3RequestObject {
    private static final String HTML_CONTENT = "text/html";
    private static final String JSON_CONTENT = "application/json";
    private String bucketName;
    private String key;
    private RequestBody requestBody;
    private String contentType;

    public static S3RequestObjectBuilder htmlDataBuilder(String htmlData) {
        return S3RequestObject.builder().contentType(HTML_CONTENT)
                .requestBody(RequestBody.fromString(htmlData));
    }

    public static S3RequestObjectBuilder jsonDataBuilder(String jsonData) {
        return S3RequestObject.builder().contentType(JSON_CONTENT)
                .requestBody(RequestBody.fromString(jsonData));
    }
}
