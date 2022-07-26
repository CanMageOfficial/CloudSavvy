package com.cloudSavvy.email;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@Slf4j
@AllArgsConstructor
public class EmailMessageClient {

    private SesV2Client sesV2Client;

    private static final String UTF_8 = "UTF-8";

    public void send(EmailMessageRequest emailMessageRequest) {
        Content htmlContent = getUtf8Charset().data(emailMessageRequest.getHtmlBody()).build();
        Content textContent = getUtf8Charset().data(emailMessageRequest.getTextBody()).build();
        Body body = Body.builder().html(htmlContent).text(textContent).build();
        Content subject = getUtf8Charset().data(emailMessageRequest.getSubject()).build();

        EmailContent emailContent = EmailContent.builder()
                .simple(Message.builder().body(body).subject(subject).build()).build();

        emailMessageRequest.getTo().stream().parallel()
                .forEach(toEmail -> sendMail(emailContent, emailMessageRequest.getFrom(), toEmail));
    }

    private void sendMail(EmailContent emailContent, String fromAddress, String toAddress) {
        try {
            Destination destination = Destination.builder()
                    .toAddresses(toAddress).build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(destination)
                    .content(emailContent)
                    .fromEmailAddress(fromAddress).build();

            sesV2Client.sendEmail(request);
            log.info("Email sent to {}", toAddress);
        } catch (Exception ex) {
            log.error("Failure while sending email. Error message: ", ex);
        }
    }

    private Content.Builder getUtf8Charset() {
        return Content.builder().charset(UTF_8);
    }
}
