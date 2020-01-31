package delta.codecharacter.server.util;

import org.springframework.mail.SimpleMailMessage;

public class MailTemplate {
    private static final String BASE_URL = "https://localhost:8080";

    private static SimpleMailMessage mailMessage;

    /**
     * Creates a Message to be sent as Text for Activation email
     *
     * @param email     Email of the recipient
     * @param username  Username of the recipient
     * @param authToken AuthToken for account activation
     * @return Message to be sent
     */
    public static SimpleMailMessage getActivationMessage(String email, String username, String authToken) {
        mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Code Character - Account Activation");
        mailMessage.setText("Greetings " + username + "!,\nKindly use the link below " +
                "to activate your account for Code Character 2020\n" +
                BASE_URL + "/user/activate/" + authToken + "\nThis link is valid only for 24 hours");

        return mailMessage;
    }

    /**
     * Creates a Message to be sent as Text for Activation email
     *
     * @param email              Email of the recipient
     * @param username           Username of the recipient
     * @param passwordResetToken PasswordResetToken for resetting password
     * @return Message to be sent
     */
    public static SimpleMailMessage getPasswordResetMessage(String email, String username, String passwordResetToken) {
        mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Code Character - Account Activation");
        mailMessage.setText("Greeting " + username + "!\nKindly use the link below " +
                "to reset your account password for Code Character 2020\n" +
                BASE_URL + "/user/password/" + passwordResetToken + "\nThink link is valid only for 24 hours");

        return mailMessage;
    }
}
