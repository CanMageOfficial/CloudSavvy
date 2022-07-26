package com.cloudSavvy.email;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class EmailMessageRequest {
    private String from;
    private List<String> to;
    private String htmlBody;
    private String textBody;
    private String subject;
}
