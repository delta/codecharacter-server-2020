package delta.codecharacter.server.model;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder

public class Leaderboard {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Positive
    private Integer rating;

    @NotNull
    private String dll1;

    @NotNull
    private String dll2;

    @NotNull
    private Boolean isAi;

    private String division;

    enum mode{
        MANUAL,
        AUTO;
    }
    private mode matchMode;
}
