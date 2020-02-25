package delta.codecharacter.server.controller.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PragyanUserDetailsMessage {
    @SerializedName("full_name")
    private String fullName;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("user_name")
    private String username;

    @SerializedName("user_country")
    private String userCountry;
}
