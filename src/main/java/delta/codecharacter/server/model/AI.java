package delta.codecharacter.server.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

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
