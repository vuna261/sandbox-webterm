package com.cloudnut.webterm.application.services.interfaces;

import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;

import java.util.List;

public interface ITokenRequestService {
    TokenRequest tokenToRequest(String token);
    List<String> getChildToken(String token);
}
