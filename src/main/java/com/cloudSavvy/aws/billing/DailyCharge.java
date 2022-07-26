package com.cloudSavvy.aws.billing;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Builder
@ToString
public class DailyCharge {
    private Instant date;
    private Double value;
}
