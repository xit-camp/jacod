package camp.xit.kiwi.jacod.provider.gsheet.service;

import camp.xit.kiwi.jacod.provider.gsheet.jwt.GoogleCredentials;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.text.MessageFormat;

public class GSheetService {

    private static final String API_URI_PREFIX = "https://sheets.googleapis.com/v4/spreadsheets/";

    protected final HttpClient httpClient;
    protected final JsonMapper jsonMapper;
    protected final GoogleCredentials credentials;


    public GSheetService(String serviceAccountFile) {
        this(new File(serviceAccountFile));
    }


    public GSheetService(File serviceAccountFile) {
        this.httpClient = HttpClient.newHttpClient();
        this.jsonMapper = new JsonMapper();
        this.jsonMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        this.jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.credentials = new GoogleCredentials(serviceAccountFile);
    }


    public SpreadSheet getSpreadSheet(String spreadSheetId) throws NotFoundException, GoogleApiException {
        URI sheetUri = URI.create(API_URI_PREFIX + spreadSheetId
                + "?fields=properties(title%2Clocale%2CtimeZone)%2Csheets(properties(sheetId%2Ctitle))");
        return readAs(sheetUri, SpreadSheet.class);
    }


    public RangeValue readSheetValues(String spreadSheetId, String range) throws NotFoundException, GoogleApiException {
        return readSheetValues(spreadSheetId, range, MajorDimension.DIMENSION_UNSPECIFIED);
    }


    public RangeValue readSheetValues(String spreadSheetId, String range, MajorDimension dimension)
            throws NotFoundException, GoogleApiException {

        try {
            String encodedRange = URLEncoder.encode(range, Charset.defaultCharset());
            String uriStr = MessageFormat.format(API_URI_PREFIX + "{0}/values/{1}", spreadSheetId, encodedRange);
            if (dimension != null) uriStr += "?majorDimension=" + dimension.toString();
            URI sheetUri = URI.create(uriStr);
            return readAs(sheetUri, RangeValue.class);
        } catch (GoogleApiException e) {
            if (e.getStatus() == 400) {
                throw new NotFoundException("Invalid range " + range);
            } else throw e;
        }
    }


    protected String readAsString(URI uri) throws NotFoundException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", credentials.getAccessToken().toString())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 200) {
                return response.body();
            } else if (status == 404) {
                throw new NotFoundException("Requested data not found! URI: " + request.uri());
            } else {
                throw new GoogleApiException(response.body(), response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unexpected error!", e);
        }
    }


    protected <T> T readAs(URI uri, Class<T> objClass) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", credentials.getAccessToken().toString())
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (status == 200) {
                try (InputStream in = response.body()) {
                    return jsonMapper.readValue(in, objClass);
                }
            } else if (status == 404) {
                throw new NotFoundException("Requested data not found! URI: " + request.uri());
            } else {
                String content = "";
                try (InputStream in = response.body()) {
                    content = consumeContent(in);
                }
                throw new GoogleApiException(content, response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unexpected error!", e);
        }
    }


    static String consumeContent(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
