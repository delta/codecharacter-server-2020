package delta.codecharacter.server.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
public class PaginatedResponse<T> {
    @NotNull
    @PositiveOrZero
    @JsonProperty("count")
    private Integer count;

    @NotNull
    @Positive
    @JsonProperty("page")
    private Integer page;

    @NotNull
    @PositiveOrZero
    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("previous")
    private String previous;

    @JsonProperty("next")
    private String next;

    @NotNull
    @JsonProperty("results")
    private T results;
}