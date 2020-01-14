package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    @Builder.Default
    private LocalDateTime tokenExpiry = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
