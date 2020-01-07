package delta.codecharacter.server.model;

import lombok.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class UserActivation {
    @NotNull
    private String activationToken;

    @NotNull
    @Future
    private Date tokenExpiry;

    @NotNull
    @Positive
    private Integer userId;
}
