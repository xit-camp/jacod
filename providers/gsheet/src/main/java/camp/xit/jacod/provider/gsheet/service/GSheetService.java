package camp.xit.jacod.provider.gsheet.service;

import static java.lang.String.join;
import static java.net.URLEncoder.encode;
import static java.util.stream.Collectors.toList;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import camp.xit.google.api.credentials.GoogleCredentials;

public class GSheetService {

    private static final String API_URI_PREFIX = "https://sheets.googleapis.com/v4/spreadsheets/";
    private static final String GSHEET_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

    protected final HttpClient httpClient;
    protected final JsonMapper jsonMapper;
    protected GoogleCredentials credentials;


    protected GSheetService() {
        this.httpClient = HttpClient.newBuilder()
            .executor(new ThreadPoolExecutor(0, 20, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100)))
            .build();
        this.jsonMapper = new JsonMapper();
        this.jsonMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        this.jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    public GSheetService(File serviceAccountFile) {
        this();
        this.credentials = new GoogleCredentials(serviceAccountFile, GSHEET_SCOPE);
    }


    public GSheetService(InputStream serviceAccount) {
        this();
        this.credentials = new GoogleCredentials(serviceAccount, GSHEET_SCOPE);
    }


    public SpreadSheet getSpreadSheet(String spreadSheetId) throws NotFoundException, GoogleApiException {
        URI sheetUri = URI.create(API_URI_PREFIX + spreadSheetId
                + "?fields=properties(title%2Clocale%2CtimeZone)%2Csheets(properties(sheetId%2Ctitle))");
        return readAs(sheetUri, SpreadSheet.class);
    }


    public RangeValue getSheetValues(String spreadSheetId, String range) throws NotFoundException, GoogleApiException {
        return getSheetValues(spreadSheetId, range, null, null, null);
    }


    public ValueRanges getValuesBatch(String spreadSheetId, Collection<String> ranges) throws NotFoundException, GoogleApiException {
        return getValuesBatch(spreadSheetId, ranges, null, null, null);
    }


    public ValueRanges getValuesBatch(String spreadSheetId, Collection<String> ranges,
            MajorDimension dimension, ValueRenderOption valueRenderOption, DateTimeRenderOption dateTimeRenderOption)
            throws NotFoundException, GoogleApiException {

        Charset charset = Charset.defaultCharset();
        List<String> encodedRanges = ranges.stream().map(r -> "ranges=" + encode(r, charset)).collect(toList());
        String uriStr = MessageFormat.format(API_URI_PREFIX + "{0}/values:batchGet", spreadSheetId);
        List<String> params = new ArrayList<>();
        if (dimension != null) {
            params.add("majorDimension=" + dimension.toString());
        }
        if (valueRenderOption != null) {
            params.add("valueRenderOption=" + valueRenderOption.toString());
        }
        if (dateTimeRenderOption != null) {
            params.add("dateTimeRenderOption=" + dateTimeRenderOption.toString());
        }
        if (encodedRanges != null && !encodedRanges.isEmpty()) params.addAll(encodedRanges);
        if (!params.isEmpty()) uriStr += "?" + join("&", params);
        URI sheetUri = URI.create(uriStr);
        return readAs(sheetUri, ValueRanges.class);
    }


    public RangeValue getSheetValues(String spreadSheetId, String range, MajorDimension dimension,
            ValueRenderOption valueRenderOption, DateTimeRenderOption dateTimeRenderOption)
            throws NotFoundException, GoogleApiException {

        String encodedRange = URLEncoder.encode(range, Charset.defaultCharset());
        String uriStr = MessageFormat.format(API_URI_PREFIX + "{0}/values/{1}", spreadSheetId, encodedRange);
        List<String> params = new ArrayList<>();
        if (dimension != null) {
            params.add("majorDimension=" + dimension.toString());
        }
        if (valueRenderOption != null) {
            params.add("valueRenderOption=" + valueRenderOption.toString());
        }
        if (dateTimeRenderOption != null) {
            params.add("dateTimeRenderOption=" + dateTimeRenderOption.toString());
        }
        if (!params.isEmpty()) uriStr += "?" + join("&", params);
        URI sheetUri = URI.create(uriStr);
        return readAs(sheetUri, RangeValue.class);
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
            try (InputStream in = response.body()) {
                int status = response.statusCode();
                switch (status) {
                case 200:
                    return jsonMapper.readValue(in, objClass);
                case 404:
                    throw new NotFoundException("Requested data not found! URI: " + request.uri());
                default:
                    String content = consumeContent(in);
                    throw new GoogleApiException(content, response.statusCode());
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("unable to read from gsheet", e);
        }
    }


    static String consumeContent(java.io.InputStream is) {
        try (java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
}
