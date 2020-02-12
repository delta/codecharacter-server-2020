package delta.codecharacter.server.model;

import delta.codecharacter.server.util.Enums.AuthMethod;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.*;

@Data
@Builder
public class User {

    @Id
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @NotBlank(message = "Username is mandatory")
    @Min(4)
    @Max(50)
    private String username;

    @Field("full_name")
    @Min(2)
    @Max(50)
    private String fullName;

    @NotNull
    @Email
    private String email;

    @Field("auth_method")
    @NotNull
    private AuthMethod authMethod;

    private String password;

    private String country;

    @Field("is_activated")
    @NotNull
    @Builder.Default
    private Boolean isActivated = false;

    private String college;

    @Field("is_admin")
    @NotNull
    @Builder.Default
    private Boolean isAdmin = false;

    @Field("avatar_id")
    @NotNull
    @Positive
    @Builder.Default
    private int avatarId = 1;
}
