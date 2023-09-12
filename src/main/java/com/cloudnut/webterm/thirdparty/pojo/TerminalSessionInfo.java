package com.cloudnut.webterm.thirdparty.pojo;

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
public class TerminalSessionInfo {
    private boolean suspended = false;
    private String sessionType;
    private String token;
    private String parentToken;
    private String host;
    private Integer port;
    private String connectionType;
    private boolean usePty = true;
    private String username;
    private String password;
    private volatile boolean ready;
    private Integer maxIdleTime;
    private String webUserIp;
    private long lastTrafficTime = 0;   // to track idle timeout

    // trace expired
    private Date expiredTime;

    // session id
    private String sessionId;

    /**
     *
     */
    public void setTrafficTimeNow() {
        if (this.ready) {
            this.lastTrafficTime = System.currentTimeMillis() / 1000l;
        }
    }

    public static TerminalSessionInfo from(TokenRequest tr) {
        return TerminalSessionInfo.builder()
                .token(tr.getToken())
                .parentToken(tr.getParentToken())
                .host(tr.getHost())
                .port(tr.getPort())
                .username(tr.getUsername())
                .password(tr.getPassword())
                .webUserIp(tr.getWebUserIp())
                .expiredTime(tr.getExpiredTime())
                .ready(false)
                .build();
    }
}
