package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Builder
public class UserStatsResponse {

    private Integer userId;

    private Long numMatches;

    private Long initiatedWins;
    private Long facedWins;
    private Long autoWins;

    private Long initiatedLosses;
    private Long facedLosses;
    private Long autoLosses;

    private Long initiatedTies;
    private Long facedTies;
    private Long autoTies;

    private Date lastMatchAt;
}
