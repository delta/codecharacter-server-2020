package delta.codecharacter.server.service;

import com.sendgrid.*;
import delta.codecharacter.server.util.MailTemplate;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class MailService {

    private final Logger LOG = Logger.getLogger(MailService.class.getName());

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.mail}")
    private String sendGridSenderMail;

    @SneakyThrows
    public void sendActivationMail(String email, String username, String activationToken) {
        Email from = new Email(sendGridSenderMail);
        Email to = new Email(email);
        String contentString = MailTemplate.getActivationMessage(email, username, activationToken).toString();
        Content content = new Content("text/plain", contentString);
        String subject = "Code Character - Account Activation";
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            LOG.info("Activation mail status code to " + email + ": " + String.valueOf(response.getStatusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void sendPasswordResetMail(String email, String username, String activationToken) {
        Email from = new Email(sendGridSenderMail);
        Email to = new Email(email);
        String contentString = MailTemplate.getPasswordResetMessage(email, username, activationToken);
        Content content = new Content("text/plain", contentString);
        String subject = "Code Character - Account Activation";
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            LOG.info("Reset password mail status code to " + email + ": " + String.valueOf(response.getStatusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
