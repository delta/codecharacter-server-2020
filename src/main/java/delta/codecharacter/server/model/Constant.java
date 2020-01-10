package delta.codecharacter.server.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Constant {
    @NotNull
    private String key;

    @NotNull
    private String value;
}
