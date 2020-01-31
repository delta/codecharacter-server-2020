package delta.codecharacter.server.controller.request.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import delta.codecharacter.server.util.enums.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserRequest {
    @Length(min = 1, max = 100)
    private String username;

    @Length(min = 1, max = 100)
    private String fullName;

    private String country;

    private String college;

    private String avatarId;

    private UserType userType;
}
