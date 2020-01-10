package delta.codecharacter.server.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Constant {
    @NotNull
    private String key;

    @NotNull
    private String value;
}
