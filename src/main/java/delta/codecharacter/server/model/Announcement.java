package delta.codecharacter.server.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class Announcement {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotNull
    private String message;
}
