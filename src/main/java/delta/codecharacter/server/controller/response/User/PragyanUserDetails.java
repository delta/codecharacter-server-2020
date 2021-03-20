package delta.codecharacter.server.controller.response.User;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PragyanUserDetails {
    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("user_name")
    private String username;

    @SerializedName("user_fullname")
    private String fullName;

    @SerializedName("user_country")
    private String userCountry;
}
