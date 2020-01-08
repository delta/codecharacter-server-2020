package delta.codecharacter.server.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class Avatar {
    @Id
    @NotNull
    @Positive
    private Integer avatarId;

    @NotNull
    private String avatar;
}
