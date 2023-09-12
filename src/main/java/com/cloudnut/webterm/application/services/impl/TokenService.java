package com.cloudnut.webterm.application.services.impl;

import com.cloudnut.webterm.application.services.interfaces.ITokenService;
import com.cloudnut.webterm.application.services.interfaces.IWebTermService;
import com.cloudnut.webterm.infrastructure.entity.TokenRequestEntity;
import com.cloudnut.webterm.infrastructure.repo.TokenRequestRepo;
import com.cloudnut.webterm.thirdparty.connection.Connection;
import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import com.cloudnut.webterm.thirdparty.pojo.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.cloudnut.webterm.application.services.impl.WebTermService.tokenToRootConnection;
import static com.cloudnut.webterm.utils.Constants.*;

@Service
@Slf4j
public class TokenService implements ITokenService {

    @Autowired
    TokenRequestRepo tokenRequestRepo;

    @Autowired
    IWebTermService webTermService;

    /**
     * init Token response
     * @param tokenRequest
     * @return
     */
    @Override
    public TokenResponse initTokenResponse(TokenRequest tokenRequest) {
        TokenResponse resp = TokenResponse.builder().status(RESPONSE_STATUS_FAILURE).build();

        if (isTokenIssues(tokenRequest)) {
            String token = UUID.randomUUID().toString();
            TokenRequestEntity tokenRequestEntity = tokenRequest.toEntity();
            tokenRequestEntity.setToken(token);
            if (!StringUtils.isEmpty(tokenRequest.getParentToken())) {
                String parentToken = getParentToken(tokenRequest.getParentToken());
                tokenRequestEntity.setParentToken(parentToken);
            }
            tokenRequestRepo.save(tokenRequestEntity);
            resp.setStatus(RESPONSE_STATUS_SUCCESS);
            resp.setPayload(token);
        }
        return resp;
    }

    @Override
    @Transactional
    public void removeToken(String token) {
        tokenRequestRepo.deleteByToken(token);
        tokenRequestRepo.deleteByParentToken(token);
    }

    /**
     * check token is issues
     * @param request
     * @return
     */
    private boolean isTokenIssues(TokenRequest request) {
        boolean isVerified = false;
        if (request != null) {
            if (request.getParentToken() != null) {
                Connection connection = tokenToRootConnection(request.getParentToken());
                if (connection != null) {
                    isVerified = true;
                }
            } else {
                isVerified = true;
            }
        }
        return isVerified;
    }

    /**
     * get related parent token
     * @param parentToken
     * @return
     */
    private String getParentToken(String parentToken) {
        String token = null;
        Optional<TokenRequestEntity> tokenRequestEntityOptional = tokenRequestRepo.findByToken(parentToken);
        if (tokenRequestEntityOptional.isPresent()) {
            TokenRequestEntity tokenRequestEntity = tokenRequestEntityOptional.get();
            if (StringUtils.isEmpty(tokenRequestEntity.getParentToken())) {
                token = tokenRequestEntity.getToken();
            } else {
                token = tokenRequestEntity.getParentToken();
            }
        }
        return token;
    }

    /**
     * remove all expired token
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    @Transactional
    public void tokenHouseKeeping() {
        List<TokenRequestEntity> tokenRequestEntities =
                tokenRequestRepo.findByExpiredTimeBefore(new Date());
        for (TokenRequestEntity te: tokenRequestEntities) {
            removeToken(te.getToken());
        }
    }
}
