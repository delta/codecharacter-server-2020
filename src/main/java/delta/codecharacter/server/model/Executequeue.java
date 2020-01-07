package delta.codecharacter.server.model;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Executequeue {
    @NotNull
    @Positive
    private Integer userid1;

    @NotNull
    @Positive
    private Integer userid2;

    @NotNull
    @Positive
    private Integer gameId;

    @NotNull
    private String dll1Path;

    @NotNull
    private String dll2Path;

    private String status;
    private String type;

    @NotNull
    @Positive
    private Integer aiId;

}
