package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@Builder
public class TopMatch {
    @Id
    @Positive
    @NotNull
    private Integer id;

    @NotNull
    @Field("match_id")
    private Integer matchId;

    @NotNull
    @Field("created_at")
    @Builder.Default
    private Date createdAt = new Date();
}
