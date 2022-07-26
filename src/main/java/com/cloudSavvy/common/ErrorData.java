package com.cloudSavvy.common;

import com.cloudSavvy.aws.common.AWSService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class ErrorData {
    private AWSService awsService;
    private String errorMessage;
}
