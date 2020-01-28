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

    private Integer totalMatches;

    private Integer totalWins;

    private Integer totalLoses;

    private Integer totalTies;

    private Date lastMatchAt;
}
