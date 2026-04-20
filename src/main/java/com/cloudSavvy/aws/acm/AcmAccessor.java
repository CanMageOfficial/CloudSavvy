package com.cloudSavvy.aws.acm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateStatus;
import software.amazon.awssdk.services.acm.model.CertificateSummary;
import software.amazon.awssdk.services.acm.model.ListCertificatesRequest;
import software.amazon.awssdk.services.acm.model.ListCertificatesResponse;
import software.amazon.awssdk.services.acm.paginators.ListCertificatesIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class AcmAccessor {

    private AcmClient acmClient;

    public List<CertificateSummary> listIssuedCertificates() {
        ListCertificatesRequest request = ListCertificatesRequest.builder()
                .certificateStatuses(CertificateStatus.ISSUED)
                .build();
        ListCertificatesIterable iterable = acmClient.listCertificatesPaginator(request);
        List<CertificateSummary> certificates = new ArrayList<>();
        for (ListCertificatesResponse response : iterable) {
            certificates.addAll(response.certificateSummaryList());
            if (certificates.size() > 1000) {
                break;
            }
        }
        return certificates;
    }
}
