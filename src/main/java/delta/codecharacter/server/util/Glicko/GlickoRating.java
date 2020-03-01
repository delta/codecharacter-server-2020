package delta.codecharacter.server.util.Glicko;

import lombok.Builder;
import lombok.Data;

/**
 * Rating object to represent single Glicko rating instance
 */
@Data
@Builder
public class GlickoRating {
    // Expected skill level of player
    private Double rating;

    // Standard deviation of skill level
    private Double ratingDeviation;
}
