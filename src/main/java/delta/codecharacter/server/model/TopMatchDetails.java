package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class TopMatchDetails {
    @Id
    @Positive
    @NotNull
    private Integer id;

    @NotNull
    private Integer matchId;
}
