package camp.xit.kiwi.jacod.provider.gsheet.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientAccessToken {

    @JsonProperty("access_token")
    private String tokenKey;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private long expiresIn = -1;


    @Override
    public String toString() {
        if (OAuthConstants.BEARER_AUTHORIZATION_SCHEME.equalsIgnoreCase(getTokenType())) {
            return OAuthConstants.BEARER_AUTHORIZATION_SCHEME + " " + getTokenKey();
        }
        return super.toString();
    }
}
