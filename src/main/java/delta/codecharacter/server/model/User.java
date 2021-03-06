package delta.codecharacter.server.model;

import delta.codecharacter.server.util.enums.AuthMethod;
import delta.codecharacter.server.util.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Field("user_type")
    @NotNull
    @Builder.Default
    private UserType userType = UserType.PROFESSIONAL;

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

    @Field("created_at")
    @NotNull
    @Builder.Default
    private Date createdAt = new Date();

    @Field("is_first_login")
    @NotNull
    @Builder.Default
    private Boolean isFirstLogin = true;

    @Field("current_level")
    @NotNull
    @Builder.Default
    private Integer currentLevel = 1;


}
