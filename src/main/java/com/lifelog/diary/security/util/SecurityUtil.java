package com.lifelog.diary.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static String getCurrentAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof String) {
            return (String) credentials;
        }
        return null;
    }
}
