package swcnoops.server.json;

public interface JsonParser {
    String toJson(Object object);
    <T extends Object> T toObjectFromResource(String s, Class<T> aClass) throws Exception;
    <T extends Object> T fromJsonObject(Object object, Class<T> aClass);
    <T extends Object> T fromJsonString(String json, Class<T> aClass) throws Exception;
    Object fromJsonFile(String absolutePath) throws Exception ;
    <T extends Object> T fromJsonFile(String absolutePath, Class<T> aClass) throws Exception ;
}
