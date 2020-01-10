package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class User {

    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotBlank(message = "Username is mandatory")
    @Min(4)
    @Max(50)
    private String username;

    @Field("full_name")
    @Min(2)
    @Max(50)
    private String fullName;

    @Email
    @Min(2)
    private String email;

    @NotNull
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
    private int avatarId;
}
