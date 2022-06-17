package swcnoops.server.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.FileInputStream;
import java.io.InputStream;

public class JacksonJsonParser implements JsonParser {
    static final private ObjectMapper mapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    @Override
    public <T> T fromJsonObject(Object object, Class<T> aClass) {
        return mapper.convertValue(object, aClass);
    }

    @Override
    public String toJson(Object object) {
        String json;
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            json = null;
        }

        return json;
    }

    @Override
    public <T> T toObjectFromResource(String resource, Class<T> clazz) throws Exception {
        T jsonObject;
        try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)){
            jsonObject = mapper.readValue(in, clazz);
        }
        return jsonObject;
    }

    @Override
    public <T> T fromJsonString(String json, Class<T> aClass) throws Exception {
        return mapper.readValue(json, aClass);
    }

    @Override
    public Object fromJsonFile(String absolutePath) throws Exception {
        return fromJsonFile(absolutePath, Object.class);
    }

    @Override
    public <T> T fromJsonFile(String absolutePath, Class<T> aClass) throws Exception {
        T jsonObject;
        try (InputStream in = new FileInputStream(absolutePath)){
            jsonObject = mapper.readValue(in, aClass);
        }
        return jsonObject;
    }
}
