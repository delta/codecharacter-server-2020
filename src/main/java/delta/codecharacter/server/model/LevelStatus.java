package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class LevelStatus {
    @Id
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    private List<Integer> stars;
}
