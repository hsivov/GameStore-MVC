package org.example.gamestoreapp.util;

import org.example.gamestoreapp.exception.CryptoProcessingException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HMACUtil {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public static String generateHMAC(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(), HMAC_ALGORITHM));
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            throw new CryptoProcessingException("Error generating signature", e);
        }
    }
}
