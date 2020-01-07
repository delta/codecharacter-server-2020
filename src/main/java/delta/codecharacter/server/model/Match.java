package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Match {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotNull
    @Positive
    private Integer userid1;

    @NotNull
    @Positive
    private Integer userid2;

    private String verdict;
    private String status;
    private Integer score1;
    private Integer score2;



}
