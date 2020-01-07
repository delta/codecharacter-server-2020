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
    private String username;

    @Field("full_name")
    @Min(2)
    @Max(50)
    private String fullName;

    @Email
    private String email;

    @Min(2)
    @Max(50)
    @NotNull
    private String password;

    private String country;
    private Boolean isActivated;
    private String college;
    private Boolean isAdmin;

    @NotNull
    @Positive
    private int avatarId;
}
