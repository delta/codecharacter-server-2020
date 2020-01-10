package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class UserPragyanDetails {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Field("pragyan_id")
    @NotNull
    @Positive
    private Integer pragyanId;
}
