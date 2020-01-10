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
public class Announcement {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @Field("admin_user_id")
    @NotNull
    @Positive
    private Integer adminUserId;

    @NotNull
    private String message;
}
