package delta.codecharacter.server.model;

<<<<<<< HEAD
=======
import lombok.*;
>>>>>>> Add model Avatar
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Avatar {
    @Id
    @NotNull
    @Positive
    private Integer avatarId;

    @NotNull
    private String avatar;
}
