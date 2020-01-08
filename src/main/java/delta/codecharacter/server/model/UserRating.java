package delta.codecharacter.server.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class UserRating {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private String rating;

    @NotNull
    private String fromDateTime;
}
