package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * A map is location where matches are held.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Map {
    @NotNull
    @Positive
    private Integer id;

    @Field("map_name")
    @NotNull
    private String mapName;

    @Field("is_hidden")
    @NotNull
    @Builder.Default
    private Boolean isHidden = false;
}
