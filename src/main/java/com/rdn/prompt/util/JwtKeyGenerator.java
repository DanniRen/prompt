package com.rdn.prompt.util;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class JwtKeyGenerator {
    public static Key generateSecretKey() {
        String secret = System.getenv("JWT_SECRET");
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }
}
