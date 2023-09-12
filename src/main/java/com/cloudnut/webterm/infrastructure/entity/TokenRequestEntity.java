package com.cloudnut.webterm.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "token_request")
public class TokenRequestEntity {
    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "parent_token")
    private String parentToken;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "web_user_ip")
    private String webUserIp;

    @Column(name = "expired_time")
    private Date expiredTime;
}
