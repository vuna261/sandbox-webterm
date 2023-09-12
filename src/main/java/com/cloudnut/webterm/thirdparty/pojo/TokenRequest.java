package com.cloudnut.webterm.thirdparty.pojo;

import com.cloudnut.webterm.infrastructure.entity.TokenRequestEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRequest {
    private String token;
    private String sessionType; // NEW,JOIN
    private String parentToken;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String webUserIp;
    private Date expiredTime;

    public TokenRequestEntity toEntity() {
        return TokenRequestEntity.builder()
                .token(token)
                .parentToken(parentToken)
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .webUserIp(webUserIp)
                .expiredTime(expiredTime)
                .build();
    }

    public static TokenRequest from(TokenRequestEntity entity) {
        return TokenRequest.builder()
                .token(entity.getToken())
                .parentToken(entity.getParentToken())
                .host(entity.getHost())
                .port(entity.getPort())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .webUserIp(entity.getWebUserIp())
                .expiredTime(entity.getExpiredTime())
                .build();
    }
}
