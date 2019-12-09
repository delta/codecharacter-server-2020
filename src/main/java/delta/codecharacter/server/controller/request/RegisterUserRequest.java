package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserRequest {
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String username;

    @Email
    private String email;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 1, max = 100)
    private String fullName;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String password;
}
