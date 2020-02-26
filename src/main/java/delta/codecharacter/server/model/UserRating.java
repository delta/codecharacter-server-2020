package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class UserRating {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private Double rating;

    @NotNull
    private Double ratingDeviation;

    @Field("valid_from")
    @NotNull
    @Builder.Default
    private LocalDateTime validFrom = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
