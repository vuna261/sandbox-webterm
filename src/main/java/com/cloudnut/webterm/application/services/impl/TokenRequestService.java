package com.cloudnut.webterm.application.services.impl;

import com.cloudnut.webterm.application.services.interfaces.ITokenRequestService;
import com.cloudnut.webterm.infrastructure.entity.TokenRequestEntity;
import com.cloudnut.webterm.infrastructure.repo.TokenRequestRepo;
import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TokenRequestService implements ITokenRequestService {

    @Autowired
    TokenRequestRepo tokenRequestRepo;

    /**
     * get token request from
     * @param token
     * @return
     */
    @Override
    public TokenRequest tokenToRequest(String token) {
        Optional<TokenRequestEntity> tokenRequestEntityOptional =
                tokenRequestRepo.findByToken(token);
        return tokenRequestEntityOptional.map(TokenRequest::from).orElse(null);
    }

    @Override
    public List<String> getChildToken(String token) {
        return null;
    }

}
