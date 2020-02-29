package delta.codecharacter.server.controller.response.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import delta.codecharacter.server.controller.response.User.PragyanUserDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PragyanApiResponse {
    @SerializedName("status_code")
    private Integer statusCode;

    private PragyanUserDetails message;
}
