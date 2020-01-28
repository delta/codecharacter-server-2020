package delta.codecharacter.server.util;

public class MailTemplate {
    private static final String BASE_URL = "https://localhost:8080";

    /**
     * Creates a Message to be sent as Text for Activation email
     *
     * @param username  Username of the recipient
     * @param authToken AuthToken for account activation
     * @return Message to be sent
     */
    public static String getActivationMessage(String username, String authToken) {
        return "Greetings " + username + "!,\nKindly use the link below " +
                "to activate your account for Code Character 2020\n" +
                BASE_URL + "/user/activate/" + authToken + "\nThis link is valid only for 24 hours";
    }

    /**
     * Creates a Message to be sent as Text for Activation email
     *
     * @param username           Username of the recipient
     * @param passwordResetToken PasswordResetToken for resetting password
     * @return Message to be sent
     */
    public static String getPasswordResetMessage(String username, String passwordResetToken) {
        return "Greeting " + username + "!\nKindly use the link below " +
                "to reset your account password for Code Character 2020\n" +
                BASE_URL + "/user/password/" + passwordResetToken + "\nThink link is valid only for 24 hours";
    }
}
