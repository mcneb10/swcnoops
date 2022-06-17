package swcnoops.server.commands;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TokenHelper {

    static public String generateToken(long timestamp, String playerId, String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        String str = String.format("{\"userId\":\"%s\",\"expires\":%s}", playerId, timestamp);
        String token = generateHmac256(str, secret.getBytes(StandardCharsets.UTF_8));
        String allToken = String.format("%s.%s", token, str);
        return Base64.getEncoder().encodeToString(allToken.getBytes());
    }

    static public String generateHmac256(String message, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] bytes = hmac("HmacSHA256", key, message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bytes);
    }

    static public byte[] hmac(String algorithm, byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(message);
    }

    static public String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
