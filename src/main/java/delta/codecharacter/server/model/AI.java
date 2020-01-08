package delta.codecharacter.server.model;

<<<<<<< HEAD
=======
import lombok.*;
>>>>>>> Add model AI
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class AI {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotNull
    private String dllPath;

    @NotNull
    private String name;
}
