package com.lifelog.diary.security.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Collections;


public class OAuth2AuthorizationCodeGrantRequestEntityUtils {

    // Spring Security가 보내는 토큰 요청을 직접 만들어 Apple 서버에 전송
    public RequestEntity<?> buildRequestEntityWithClientSecret(OAuth2AuthorizationCodeGrantRequest request, String clientSecret) {

        MultiValueMap<String, String> formParams = getStringStringMultiValueMap(request, clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        URI tokenUri = URI.create(request.getClientRegistration().getProviderDetails().getTokenUri());

        return new RequestEntity<>(formParams, headers, HttpMethod.POST, tokenUri);
    }

    private static MultiValueMap<String, String> getStringStringMultiValueMap(OAuth2AuthorizationCodeGrantRequest request, String clientSecret) {
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        formParams.add("code", request.getAuthorizationExchange().getAuthorizationResponse().getCode());
        formParams.add("redirect_uri", request.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());
        formParams.add("client_id", request.getClientRegistration().getClientId());
        formParams.add("client_secret", clientSecret); // Apple용 JWT 삽입
        return formParams;
    }
}

