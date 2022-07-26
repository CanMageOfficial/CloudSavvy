package com.cloudSavvy.model;

import com.yworks.util.annotation.Obfuscation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Obfuscation()
public class StartRunRequest {
    List<String> regions;
    List<String> toEmailAddresses;
}
