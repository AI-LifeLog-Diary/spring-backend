package com.lifelog.diary.security.converter;

import com.amazonaws.util.IOUtils;
import com.lifelog.diary.security.dto.AppleUserInfoReqDto;
import com.lifelog.diary.security.util.OAuth2AuthorizationCodeGrantRequestEntityUtils;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

@Getter
@Component
@RequiredArgsConstructor
public class AppleTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final AppleUserInfoReqDto appleUserInfoReqDto;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {

        OAuth2AuthorizationCodeGrantRequestEntityUtils utils = new OAuth2AuthorizationCodeGrantRequestEntityUtils();
        RequestEntity<?> requestEntity;
        try {
            requestEntity = utils.buildRequestEntityWithClientSecret(request, createClientSecret());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RestTemplate restTemplate = new RestTemplate();

        // Apple 토큰 서버에 실제 HTTP 요청
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                requestEntity, new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new OAuth2AuthenticationException("Apple 토큰 응답에 access_token 없음");
        }

        // 추가 파라미터 처리
        Map<String, Object> additionalParams = new HashMap<>();
        if (responseBody.containsKey("id_token")) {
            additionalParams.put("id_token", responseBody.get("id_token"));
        }

        return OAuth2AccessTokenResponse.withToken((String) responseBody.get("access_token"))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(((Number) responseBody.get("expires_in")).longValue())
                .refreshToken((String) responseBody.get("refresh_token"))
                .additionalParameters(additionalParams)
                .build();
    }

    // Apple 전용 JWT 생성
    private String createClientSecret() throws Exception {

        return Jwts.builder()
                .header()
                    .add("alg", "ES256")
                    .add("kid", appleUserInfoReqDto.getKid())
                    .and()
                .issuer(appleUserInfoReqDto.getTid())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300_000))
                .claim("aud", appleUserInfoReqDto.getUrl())
                .subject(appleUserInfoReqDto.getCid())
                .signWith(getPrivatekey())
                .compact();
    }

    // Apple의 공개키를 동적으로 로드
    public static PublicKey getApplePublicKey(String kid) throws Exception {
        String jwksUrl = "https://appleid.apple.com/auth/keys";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> jwks = restTemplate.getForObject(jwksUrl, Map.class);

        List<Map<String, String>> keys = (List<Map<String, String>>) jwks.get("keys");

        for (Map<String, String> key : keys) {
            if (key.get("kid").equals(kid)) {
                String n = key.get("n");
                String e = key.get("e");

                byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
                byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

                BigInteger modulus = new BigInteger(1, modulusBytes);
                BigInteger exponent = new BigInteger(1, exponentBytes);

                RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(publicKeySpec);
            }
        }
        throw new IllegalArgumentException("Apple 공개키에서 해당 kid를 찾을 수 없음: " + kid);
    }


    // .p8 파일에서 개인키 로딩
    private PrivateKey getPrivatekey() throws Exception {
        ClassPathResource resource = new ClassPathResource(appleUserInfoReqDto.getPath());
        try (InputStream in = resource.getInputStream();
             PEMParser pemParser = new PEMParser(new StringReader(IOUtils.toString(in)))) {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        }
    }
}