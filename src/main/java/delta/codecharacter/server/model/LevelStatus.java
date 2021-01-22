package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class LevelStatus {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    private List<Integer> stars;
}
