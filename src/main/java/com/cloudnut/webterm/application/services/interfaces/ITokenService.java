package com.cloudnut.webterm.application.services.interfaces;

import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import com.cloudnut.webterm.thirdparty.pojo.TokenResponse;

public interface ITokenService {
    TokenResponse initTokenResponse(TokenRequest tokenRequest);
    void removeToken(String token);
}
