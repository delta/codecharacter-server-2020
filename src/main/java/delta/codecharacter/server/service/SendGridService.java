package delta.codecharacter.server.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.sender}")
    private String sender;

    @Value("${client.request.origin}")
    private String BASE_URL;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send a authToken to user through sendGrid
     *
     * @param userId    userId of the user to whom activation mail is to be sent
     * @param authToken authToken to be sent back to activate account
     */
    public void sendActivationEmail(Integer userId, String authToken) {
        User user = userRepository.findByUserId(userId);

        String subject = "Code Character - Account Activation";
        String message = "Greetings " + user.getUsername() + "!,\nKindly use the link below " +
                "to activate your account for Code Character 2020\n" +
                BASE_URL + "/user-activate?authToken=" + authToken + "&userId=" + user.getUserId() + "\nThis link is valid only for 24 hours";

        sendMail(user.getEmail(), subject, message);
    }

    /**
     * Send a password reset token to user through sendGrid
     *
     * @param userId             userId of the user to whom mail is to be sent
     * @param passwordResetToken passwordResetToken to be sent back to reset password
     */
    public void sendPasswordResetEmail(Integer userId, String passwordResetToken) {
        User user = userRepository.findByUserId(userId);

        String subject = "Code Character - Password Reset Instructions";
        String message = "Greetings " + user.getUsername() + "!,\nKindly use the link below " +
                "to reset the password of your Code Character 2020 account\n" +
                BASE_URL + "/reset-password?passwordResetToken=" + passwordResetToken + "&userId=" + user.getUserId() + "\nThis link is valid only for 24 hours";

        sendMail(user.getEmail(), subject, message);
    }

    /**
     * Send email to the recipient
     *
     * @param email   Email of the user to whom mail is to be sent
     * @param subject Subject of the email
     * @param message Message body of the email
     */
    private void sendMail(String email, String subject, String message) {
        Email from = new Email(sender);
        Email to = new Email(email);
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
