package com.cloudnut.webterm.utils;

public class Constants {
    private Constants() {}

    public static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String AUTHORITY = "authorities";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String SYSTEM = "system";
    public static final String ATTRIBUTES = "attributes";
    public static final String CLOUD_NUT_SALT = "CLOUDNUTSANDBOX";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final String START_LAB = "Pay [[amount]] coins for start lab [[status]]";
    public static final String REFUND = "Refund [[amount]] coins [[status]]";
    public static final String RECHARGE = "Recharge [[amount]] coins [[status]]";
    public static final String RECHARGE_COMMON_FAILURE = "Recharge for [[email]] failure";


    public static final String PAY_DESCRIPTION = "Thanh toán gói [[transactionId]]";

    /**
     *
     */
    public static final String CR_LF = "\r\n";

    /**
     *
     */
    public static final String LF = "\n";

    /**
     *
     */
    public static final String SSH_LC = "ssh";

    /**
     *
     */
    public static final String TELNET_LC = "telnet";

    /**
     *
     */
    public static final String TN3270_LC = "tn3270";

    /**
     *
     */
    public static final String NEW = "NEW";

    /**
     *
     */
    public static final String JOIN = "JOIN";

    /**
     *
     */
    public static final String WATCH = "WATCH";

    /**
     *
     */
    public static final String TAKE = "TAKE";

    /**
     *
     */
    public static final int FONTSIZE = 15;

    /**
     * for pty telnet on Linux at least, won't work on Windows
     */
    public static final String TELNET_CMD = "/usr/bin/telnet";

    /**
     * for pty c3270 on Linux at least, won't work on Windows
     */
    public static final String C3270_CMD = "/usr/bin/c3270";

    /**
     * for pty ssh on Linux at least, won't work on Windows
     */
    public static final String SSH_CMD = "/usr/bin/ssh";

    public static final int ROW_SIZE = 24;
    public static final int COL_SIZE = 80;

    public static final String[] PASSWORD_PROMPTS = {"password: "};

    public static final String[] LOGIN_PROMPTS = {"login: ", "user: ", "name: ", "id: "};

    public static final String CLIENT_DATA = "d";  // save a few bytes for client

    public static final String SESSION_UUID = "SessionId";

    public static final String CLIENT_CONNECT = "connect";

    public static final String CLIENT_DISCONNECT = "disconnect";

    public static final String RESPONSE_STATUS_SUCCESS = "success";

    public static final String RESPONSE_STATUS_FAILURE = "failure";

}
