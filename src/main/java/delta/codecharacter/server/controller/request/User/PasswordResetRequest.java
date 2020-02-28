package delta.codecharacter.server.controller.request.User;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PasswordResetRequest {
    @NotNull
    private Integer userId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String passwordResetToken;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String newPassword;
}
