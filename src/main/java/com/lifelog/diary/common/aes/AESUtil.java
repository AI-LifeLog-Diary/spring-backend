package com.lifelog.diary.common.aes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AESUtil {

    private final AESKeyManager keyManager;

    // 암호화: 환경변수에서 가져온 고정 키 + 랜덤 IV
    public String encrypt(String plainText) throws Exception {
        byte[] key = keyManager.getSecretKey(); // ⬅ 여기서 환경변수 키 사용

        // 16바이트 랜덤 IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + 암호문을 합쳐서 Base64 인코딩 (KEY는 따로 관리하므로 저장하지 않음)
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // 복호화: 환경변수에서 키를 불러오고, combined에서 IV + 암호문 분리
    public String decrypt(String base64CipherText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(base64CipherText);
        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
        byte[] cipherText = Arrays.copyOfRange(combined, 16, combined.length);

        byte[] key = keyManager.getSecretKey(); // ⬅ 여기서 환경변수 키 사용
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(cipherText);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}

