package delta.codecharacter.server.controller.request.Match;

import delta.codecharacter.server.util.enums.Status;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Score {
    @NotNull
    private Integer score;

    @NotNull
    private Status status;
}
