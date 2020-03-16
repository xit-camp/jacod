package camp.xit.kiwi.jacod.provider.gsheet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;

public class JsonDataReader {

    private static final JsonMapper JSON_MAPPER = getJsonMapper();


    private JsonDataReader() {}


    public static <T> T readResourceAs(String resource, Class<T> objClass) {
        try (InputStream in = JsonDataReader.class.getResourceAsStream(resource)) {
            return JSON_MAPPER.readValue(in, objClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static JsonMapper getJsonMapper() {
        JsonMapper jsonMapper = new JsonMapper();
        jsonMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return jsonMapper;
    }
}
