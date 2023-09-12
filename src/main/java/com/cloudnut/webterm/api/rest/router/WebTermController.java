package com.cloudnut.webterm.api.rest.router;

import com.cloudnut.webterm.application.services.interfaces.ITokenRequestService;
import com.cloudnut.webterm.application.services.interfaces.ITokenService;
import com.cloudnut.webterm.application.services.interfaces.IWebTermService;
import com.cloudnut.webterm.thirdparty.connection.Connection;
import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import com.cloudnut.webterm.thirdparty.pojo.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cloudnut.webterm.application.services.impl.WebTermService.tokenToRootConnection;
import static com.cloudnut.webterm.utils.Constants.RESPONSE_STATUS_SUCCESS;

@Controller
@Slf4j
public class WebTermController {
    private static final Map<String, TokenRequest> TOKEN_MAP = new ConcurrentHashMap<>();
    private static final DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private IWebTermService webTermService;

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private ITokenRequestService tokenRequestService;

    private int TOKEN_TTL = 60;

    /**
     * go restricted
     * @param httpRequest
     * @return
     */
    @GetMapping("/restricted/go")
    public String go(HttpServletRequest httpRequest) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MINUTE, 10);

        TokenRequest tokenRequest = TokenRequest.builder()
                .webUserIp(httpRequest.getRemoteAddr())
                .host("localhost")
                .port(18022)
                .username("root")
                .password("1")
                .expiredTime(date.getTime())
                .build();

        TokenResponse tokenResponse = tokenService.initTokenResponse(tokenRequest);
        if (tokenResponse != null && tokenResponse.getStatus().equalsIgnoreCase(RESPONSE_STATUS_SUCCESS)) {
            return String.format("redirect:/session?token=" + tokenResponse.getPayload());
        } else {
            return null;
        }
    }

    @GetMapping("/session")
    public ModelAndView session(@RequestParam(name = "token", required = true) String token,
                                HttpServletRequest request) {
        TokenRequest tokenRequest = tokenRequestService.tokenToRequest(token);
        if (tokenRequest != null) {
            ModelAndView model = new ModelAndView("session");
            int rows = 24;
            int cols = 80;
            if (!StringUtils.isEmpty(tokenRequest.getParentToken())) {
                Connection connection = tokenToRootConnection(tokenRequest.getParentToken());
                if (connection != null) {
                    model.addObject("title", "hello" + "  ["
                            + "description" + "] "
                            + connection.getTerminalSessionInfo().getHost() + ":"
                            + connection.getTerminalSessionInfo().getPort().toString() + "/"
                            + connection.getTerminalSessionInfo().getConnectionType());
                } else {
                    model.addObject("title", "ERROR: Invalid Request (Session Not Found)");
                }
            } else {
                model.addObject("title", tokenRequest.getSessionType() + "  "
                        + tokenRequest.getHost() + ":"
                        + tokenRequest.getPort().toString());
            }
            model.addObject("canSuspend", true);

            model.addObject("token", token);
            model.addObject("fontSize", "14");

            model.addObject("resize", true);
            model.addObject("rows", rows);
            model.addObject("cols", cols);
            request.getSession().setMaxInactiveInterval(-1);
            return model;
        } else {
            ModelAndView error = new ModelAndView("tokenNotFound");
            error.addObject("token", token);
            return error;
        }
    }

    @GetMapping("/restricted/jumpbox")
    public String jumpbox(Model model) {
        TokenRequest request = new TokenRequest();
        model.addAttribute("request", request);
        model.addAttribute("connectionType", "");
        model.addAttribute("auditLogging", "");
        model.addAttribute("message", "Host required; Username required for ssh sessions");

        return "jumpbox";
    }
}
