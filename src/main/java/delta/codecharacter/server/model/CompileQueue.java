package delta.codecharacter.server.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CompileQueue {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private Integer codeVersionId;

    @NotNull
    private String codePath;

    @NotNull
    private String commitHash;
}
