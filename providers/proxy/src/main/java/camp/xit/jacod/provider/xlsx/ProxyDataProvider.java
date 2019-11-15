package camp.xit.jacod.provider.xlsx;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hlavki
 */
public class ProxyDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyDataProvider.class);
    private final String baseUrl;
    private final HttpClient httpClient;
    private JsonMapper jsonMapper;


    public ProxyDataProvider(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.jsonMapper = new JsonMapper();
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Optional<List<EntryData>> result = empty();
        try {
            JsonNode node = readData(getCodelistUrl(codelist));
            List<EntryData> data = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode entryNode : node) {
                    EntryData entryData = new EntryData();
                    Iterator<Map.Entry<String, JsonNode>> it = entryNode.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> attrEntry = it.next();
                        String attr = attrEntry.getKey();
                        JsonNode attrNode = attrEntry.getValue();
                        entryData.addField(attr, getValue(attrNode));
                    }
                    data.add(entryData);
                }
                result = Optional.of(data);
            }
        } catch (NotFoundException e) {
            LOG.warn("Codelist {} not found", codelist);
        }
        return result;
    }


    private Collection<String> getValue(JsonNode valueNode) {
        List<String> result = emptyList();
        if (valueNode.isValueNode() && !valueNode.isNull()) {
            result = singletonList(valueNode.asText());
        } else if (valueNode.isArray()) {
            result = new ArrayList<>();
            for (JsonNode arrNode : valueNode) {
                result.add(arrNode.asText());
            }
        }
        return result;
    }


    protected String getCodelistUrl(String codelist) {
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/") + codelist;
    }


    @Override
    public Set<String> readAllNames() {
        Set<String> result = emptySet();
        try {
            JsonNode node = readData(baseUrl);
            if (node.isArray()) {
                result = new HashSet<>();
                for (JsonNode nameNode : node) {
                    result.add(nameNode.asText());
                }
            }
        } catch (NotFoundException e) {
            LOG.debug("Cannot find codelist list");
        }
        return result;
    }


    private JsonNode readData(String url) throws NotFoundException {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<InputStream> response = httpClient.send(req, BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                try (InputStream in = response.body()) {
                    return jsonMapper.readTree(in);
                }
            } else if (response.statusCode() == 404) {
                throw new NotFoundException("Data not found for " + url);
            } else {
                throw new RuntimeException("Error " + response.statusCode() + " while getting " + url);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
