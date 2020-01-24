package delta.codecharacter.server.controller.request.User;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class PasswordResetRequest {
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String passwordResetToken;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String newPassword;
}
