package delta.codecharacter.server.controller.request.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePasswordRequest {

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String oldPassword;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String newPassword;

}
