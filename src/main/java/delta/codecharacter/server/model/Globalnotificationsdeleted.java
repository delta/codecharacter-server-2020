package delta.codecharacter.server.model;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Globalnotificationsdeleted {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Positive
    private Integer notificationId;
}
