package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.Mode;
import delta.codecharacter.server.util.Verdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AggregateResponse {
    private Long total;

    private Verdict verdict;

    private Mode matchMode;
}
