package camp.xit.kiwi.jacod.provider.gsheet.jwt;

import camp.xit.jacod.ExpirationSupplier;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GoogleCredentials {

    private static final int TOKEN_EXPIRATION = 3600;
    private static final String GSHEET_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

    private final ExpirationSupplier<ClientAccessToken> tokenCache;
    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;
    private final ServiceAccount serviceAccount;


    public GoogleCredentials(String serviceAccountFile) {
        this(new File(serviceAccountFile));
    }


    public GoogleCredentials(File serviceAccountFile) {
        this.jsonMapper = getJsonMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.serviceAccount = readServiceAccount(serviceAccountFile);
        this.tokenCache = new ExpirationSupplier<>(this::readToken, TOKEN_EXPIRATION - 3, TimeUnit.SECONDS);
    }


    public ClientAccessToken getAccessToken() {
        return tokenCache.get();
    }


    private JsonMapper getJsonMapper() {
        JsonMapper mapper = new JsonMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        return mapper;
    }


    private ServiceAccount readServiceAccount(File serviceAccountFile) {
        try {
            return jsonMapper.readValue(serviceAccountFile, ServiceAccount.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read service account file", e);
        }
    }


    private ClientAccessToken readToken(ClientAccessToken previousValue, long lastModification) {
        log.info("Refreshing access token");

        Instant now = Instant.now();
        String encodedToken = Jwts.builder()
                .setIssuer(serviceAccount.getClientEmail())
                .setAudience(serviceAccount.getTokenUri())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(TOKEN_EXPIRATION)))
                .signWith(serviceAccount.getPrivateKey(), SignatureAlgorithm.RS256)
                .claim("scope", GSHEET_SCOPE)
                .compact();

        String type = URLEncoder.encode(OAuthConstants.JWT_BEARER_GRANT, Charset.defaultCharset());
        String assertion = URLEncoder.encode(encodedToken, Charset.defaultCharset());
        String query = "grant_type=" + type + "&assertion=" + assertion;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceAccount.getTokenUri()))
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body()) {
                return jsonMapper.readValue(in, ClientAccessToken.class);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Cannot read access token", e);
        }
    }
}
