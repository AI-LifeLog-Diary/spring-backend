package com.lifelog.diary.security.dto;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "apple")
public class AppleUserInfoReqDto {
    private final String path;
    private final String url;
    private final String cid;
    private final String tid;
    private final String kid;

    public AppleUserInfoReqDto(String path, String url, String cid, String tid, String kid) {
        this.path = path;
        this.url = url;
        this.cid = cid;
        this.tid = tid;
        this.kid = kid;
    }
}
