package delta.codecharacter.server.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PragyanUserDetailsResponse {
    @SerializedName("status_code")
    private Integer statusCode;

    private PragyanUserDetailsMessage message;
}
