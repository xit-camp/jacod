package camp.xit.kiwi.jacod.provider.gsheet.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.security.PrivateKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceAccount {

    private String type;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("private_key_id")
    private String privateKeyId;
    @JsonProperty("private_key")
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey privateKey;
    @JsonProperty("client_email")
    private String clientEmail;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("auth_uri")
    private String authUri;
    @JsonProperty("token_uri")
    private String tokenUri;
    @JsonProperty("auth_provider_x509_cert_url")
    private String authProviderUrl;
    @JsonProperty("client_x509_cert_url")
    private String clientCertUrl;
}
