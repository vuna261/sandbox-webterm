package com.cloudnut.webterm.application.services.interfaces;


public interface IAuthService {
    boolean checkAuthorization(String token, String[] roles);
    String getUserName(String token);
    String getEmail(String token) ;
    Long getUserId(String token);
}
