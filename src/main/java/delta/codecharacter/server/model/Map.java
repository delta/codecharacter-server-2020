package delta.codecharacter.server.model;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Map {
    @NotNull
    @Positive
    private Integer MapId;

    @NotNull
    private String path;

    private Boolean isHidden;
}
