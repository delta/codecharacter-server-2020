package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class ForgotPasswordDetails {
    @Field("password_change_token")
    @NotNull
    private String passwordChangeToken;

    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Field("token_expiry")
    @NotNull
    @Builder.Default
    private LocalDateTime tokenExpiry = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
