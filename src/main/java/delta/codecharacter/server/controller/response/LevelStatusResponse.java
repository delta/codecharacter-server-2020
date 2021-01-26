package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LevelStatusResponse {
    private Integer level;
    private Integer stars;
}
