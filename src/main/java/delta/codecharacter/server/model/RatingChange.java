package delta.codecharacter.server.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.security.Timestamp;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class RatingChange {
    @NotNull
    @Positive
    private Integer userid;

    @Positive
    private Integer initialRating;

    @Positive
    private Integer finalRating;

    private Date time;
}
