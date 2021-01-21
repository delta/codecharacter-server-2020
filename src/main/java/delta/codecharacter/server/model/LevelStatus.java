package delta.codecharacter.server.model;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

public class LevelStatus {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Builder.Default
    private List<Integer> stars = new ArrayList<>();
}
