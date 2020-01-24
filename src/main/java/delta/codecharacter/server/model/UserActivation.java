package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@Builder
public class UserActivation {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Field("activation_token")
    @NotNull
    private String activationToken;

    @Field("token_expiry")
    @NotNull
    private LocalDateTime tokenExpiry;
}
