package delta.codecharacter.server.model;

import javax.validation.constraints.NotNull;
import java.util.Enumeration;

public class CompileBox {
    @NotNull
    private String url;

    @NotNull
    private Enumeration<String> status;
}
