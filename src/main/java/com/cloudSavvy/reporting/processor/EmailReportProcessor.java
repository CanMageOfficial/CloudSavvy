package com.cloudSavvy.reporting.processor;

import com.cloudSavvy.email.EmailMessageClient;
import com.cloudSavvy.email.EmailMessageRequest;
import com.cloudSavvy.utils.TimeUtils;
import com.cloudSavvy.validator.EmailValidator;
import com.cloudSavvy.reporting.ReportLocationType;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.utils.EnvironmentUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class EmailReportProcessor implements ReportProcessor {

    private EmailMessageClient emailMessageClient;
    static final String TEXT_BODY = "Please view message as html";

    @Override
    public ProcessReportResult processReport(final @NonNull ProcessReportInput input) {
        String fromEMailAddress = EnvironmentUtils.getFromEMailAddress();
        if (fromEMailAddress == null) {
            log.error("No {} is set in environment variables.", EnvironmentUtils.FROM_EMAIL_ADDRESS);
            return ProcessReportResult.builder().build();
        }

        String toEMailAddressesText = EnvironmentUtils.getToEmailAddresses();
        String[] toEmailAddressTokens;
        if (StringUtils.isEmpty(toEMailAddressesText)) {
            log.info("No {} is set in environment variables.", EnvironmentUtils.TO_EMAIL_ADDRESSES);
            toEmailAddressTokens = new String[]{fromEMailAddress};
        } else {
            toEmailAddressTokens = toEMailAddressesText.split(EmailValidator.EMAIL_SEPARATOR);
        }

        List<String> toEmailAddresses = new ArrayList<>(List.of(toEmailAddressTokens));

        if (!input.getReportTypeDataMap().containsKey(ReportType.ISSUE_DATA_HTML)) {
            throw new RuntimeException("Issue data html is missing");
        }

        String issueDataHtml = input.getReportTypeDataMap().get(ReportType.ISSUE_DATA_HTML).getReport();
        String subject = String.format("CloudSavvy report for %s", TimeUtils.getReadableDayFormat());
        EmailMessageRequest request = EmailMessageRequest.builder()
                .htmlBody(issueDataHtml)
                .subject(subject)
                .from(fromEMailAddress)
                .to(toEmailAddresses)
                .textBody(TEXT_BODY).build();
        emailMessageClient.send(request);
        return ProcessReportResult.builder().reportLocationType(ReportLocationType.EMAIL)
                .emailAddresses(toEmailAddresses).build();
    }
}
