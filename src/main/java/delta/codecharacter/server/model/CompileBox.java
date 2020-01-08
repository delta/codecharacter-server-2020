package delta.codecharacter.server.model;

import javax.validation.constraints.NotNull;
import java.util.Enumeration;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Enumeration;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CompileBox {
    @NotNull
    private String url;

    @NotNull
    private Enumeration<String> status;
}
