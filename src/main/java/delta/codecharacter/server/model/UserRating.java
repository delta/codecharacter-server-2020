package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

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
    private Date validFrom = new Date();
}
