package delta.codecharacter.server.model;

import delta.codecharacter.server.util.enums.Division;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class Leaderboard {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Positive
    private Double rating;

    @NotNull
    @Builder.Default
    private Division division = Division.DIV_2;
}
