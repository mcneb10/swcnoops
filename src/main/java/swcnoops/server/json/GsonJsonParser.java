package swcnoops.server.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GsonJsonParser implements JsonParser {
    final static private Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();
    @Override
    public <T> T fromJsonObject(Object object, Class<T> aClass) {
        return gson.fromJson(gson.toJsonTree(object), aClass);
    }
    @Override
    public String toJson(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T toObjectFromResource(String resource, Class<T> aClass) throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        T response;
        try (InputStream is = classloader.getResourceAsStream(resource)) {
            response = gson.fromJson(new InputStreamReader(is), aClass);
        }
        return response;
    }

    @Override
    public <T> T fromJsonString(String json, Class<T> aClass) throws Exception {
        return gson.fromJson(json, aClass);
    }

    @Override
    public Object fromJsonFile(String absolutePath) throws Exception {
        return fromJsonFile(absolutePath, Object.class);
    }

    @Override
    public <T> T fromJsonFile(String absolutePath, Class<T> aClass) throws Exception {
        T jsonObject;
        try (InputStream is = new FileInputStream(absolutePath)) {
            jsonObject = gson.fromJson(new InputStreamReader(is), aClass);
        }
        return jsonObject;
    }
}
