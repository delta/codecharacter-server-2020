package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Avatar {
    @Field("avatar_id")
    @Id
    @NotNull
    @Positive
    private Integer avatarId;

    @Field("avatar_name")
    @NotNull
    private String avatarName;
}
