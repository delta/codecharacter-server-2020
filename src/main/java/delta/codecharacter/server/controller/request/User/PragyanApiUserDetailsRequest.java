package delta.codecharacter.server.controller.request.User;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PragyanApiUserDetailsRequest {
    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("user_pass")
    private String userPassword;

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("event_secret")
    private String eventSecret;
}
