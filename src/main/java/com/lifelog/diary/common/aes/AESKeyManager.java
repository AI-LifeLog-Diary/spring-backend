package com.lifelog.diary.common.aes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class AESKeyManager {
    @Value("${aes.key}")
    private String base64Key;

    public byte[] getSecretKey() {
        return Base64.getDecoder().decode(base64Key);
    }
}
