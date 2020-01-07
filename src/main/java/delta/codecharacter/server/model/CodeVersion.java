package delta.codecharacter.server.model;

import org.springframework.data.annotation.Id;

import lombok.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CodeVersion {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private String commitHash;
}
