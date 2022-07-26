package com.cloudSavvy.dagger;

import com.cloudSavvy.execution.NewIssueDetector;
import com.cloudSavvy.execution.ReportExecutor;
import com.cloudSavvy.reporting.builder.InternalDataJsonReport;
import com.cloudSavvy.reporting.builder.ReportBuilderFactory;
import com.cloudSavvy.reporting.processor.ReportProcessorFactory;
import com.cloudSavvy.email.EmailMessageClient;
import com.cloudSavvy.reporting.ReportType;
import com.cloudSavvy.reporting.builder.DailyChargesHtmlReport;
import com.cloudSavvy.reporting.builder.ErrorDataHtmlReport;
import com.cloudSavvy.reporting.builder.IssueDataHtmlReport;
import com.cloudSavvy.reporting.builder.ReportBuilder;
import com.cloudSavvy.reporting.builder.ServiceDataHtmlReport;
import com.cloudSavvy.reporting.builder.ShortMessageReport;
import com.cloudSavvy.reporting.processor.EmailReportProcessor;
import com.cloudSavvy.reporting.processor.FileReportProcessor;
import com.cloudSavvy.reporting.processor.S3ReportProcessor;
import com.cloudSavvy.reporting.processor.SNSReportProcessor;
import com.cloudSavvy.utils.EnvironmentUtils;
import com.cloudSavvy.utils.RegionUtils;
import com.cloudSavvy.utils.S3UrlBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;

@Module
@Slf4j
public class ReportingModule {

    @Provides
    @Singleton
    public Region getRegion() {
        String region = EnvironmentUtils.getLambdaRegion();
        if (region == null) {
            if (EnvironmentUtils.isRunningInLambda()) {
                throw new RuntimeException("AWS Region is not set in environment variables.");
            } else {
                return Region.US_EAST_1;
            }
        }

        log.info("Current region: {}", region);

        return RegionUtils.parseRegion(region);
    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    public S3Client getS3Client(final AwsCredentialsProvider credentialsProvider,
                                final Region region) {
        return S3Client.builder().credentialsProvider(credentialsProvider).region(region).build();
    }

    @Provides
    @Singleton
    public S3Presigner provideS3Presigner(final AwsCredentialsProvider credentialsProvider,
                                          final Region region) {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Provides
    @Singleton
    public S3UrlBuilder getS3UrlBuilder(final S3Presigner s3Presigner) {
        return new S3UrlBuilder(s3Presigner);
    }

    @Provides
    @Singleton
    public SnsClient getSnsClient(final AwsCredentialsProvider credentialsProvider,
                                  final Region region) {
        return SnsClient.builder().credentialsProvider(credentialsProvider).region(region).build();
    }

    @Provides
    @Singleton
    public S3ReportProcessor getS3ReportProcessor(final S3Client s3Client) {
        return new S3ReportProcessor(s3Client);
    }

    @Provides
    @Singleton
    public FileReportProcessor getFileReportProcessor() {
        return new FileReportProcessor();
    }

    @Provides
    @Singleton
    public EmailReportProcessor getEmailReportProcessor(final EmailMessageClient messageClient) {
        return new EmailReportProcessor(messageClient);
    }

    @Provides
    @Singleton
    public SNSReportProcessor getSNSReportProcessor(final SnsClient snsClient) {
        return new SNSReportProcessor(snsClient);
    }

    @Provides
    @Singleton
    public EmailMessageClient getEmailMessageClient(final SesV2Client sesV2Client) {
        return new EmailMessageClient(sesV2Client);
    }

    @Provides
    @Singleton
    public SesV2Client getSesV2Client(final AwsCredentialsProvider credentialsProvider,
                                      final Region region) {
        return SesV2Client.builder().credentialsProvider(credentialsProvider).region(region).build();
    }

    @Provides
    @Singleton
    public ReportProcessorFactory provideReportStorageFactory(final S3ReportProcessor s3ReportProcessor,
                                                              final FileReportProcessor fileReportProcessor,
                                                              final EmailReportProcessor emailReportProcessor,
                                                              final SNSReportProcessor snsReportProcessor) {
        return new ReportProcessorFactory(s3ReportProcessor, fileReportProcessor,
                emailReportProcessor, snsReportProcessor);
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getServiceDataHtmlReport() {
        return new ServiceDataHtmlReport();
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getIssueDataHtmlReport(final S3UrlBuilder s3UrlBuilder) {
        return new IssueDataHtmlReport(ReportType.ISSUE_DATA_HTML, s3UrlBuilder);
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getFullIssueDataHtmlReport(final S3UrlBuilder s3UrlBuilder) {
        return new IssueDataHtmlReport(ReportType.FULL_ISSUE_DATA_HTML, s3UrlBuilder);
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getErrorDataHtmlReport(final S3UrlBuilder s3UrlBuilder) {
        return new ErrorDataHtmlReport(s3UrlBuilder);
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getShortMessageReport() {
        return new ShortMessageReport();
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getDailyChargesCsvReport() {
        return new DailyChargesHtmlReport();
    }

    @Provides
    @Singleton
    @IntoSet
    public ReportBuilder getInternalDataJsonReport(final ObjectMapper objectMapper) {
        return new InternalDataJsonReport(objectMapper);
    }

    @Provides
    @Singleton
    public ReportBuilderFactory getHtmlReportFactory(final Set<ReportBuilder> reportBuilders) {
        List<ReportBuilder> reports = new ArrayList<>(reportBuilders);
        return new ReportBuilderFactory(reports);
    }

    @Provides
    @Singleton
    public ReportExecutor getReportExecutor(final ReportProcessorFactory reportProcessorFactory,
                                            final ReportBuilderFactory reportBuilderFactory) {
        return new ReportExecutor(reportProcessorFactory, reportBuilderFactory);
    }

    @Provides
    @Singleton
    public NewIssueDetector getNewIssueDetector(final ObjectMapper objectMapper,
                                                final S3Client s3Client) {
        return new NewIssueDetector(objectMapper, s3Client);
    }
}
