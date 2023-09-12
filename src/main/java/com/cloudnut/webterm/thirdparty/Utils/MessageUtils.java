package com.cloudnut.webterm.thirdparty.Utils;

import com.cloudnut.webterm.thirdparty.pojo.TwoWayMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;

public class MessageUtils {
    private MessageUtils() {}
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * send message from byte to client
     * @param message
     * @return
     */
    public static TextMessage toClientBytes(byte[] message) {
        TwoWayMessage toClient = new TwoWayMessage("n", new String(message));
        try {
            String json = objectMapper.writeValueAsString(toClient);
            return new TextMessage(json);
        } catch (Exception e) {
            return new TextMessage(e.getMessage());
        }

    }

    /**
     * write client
     * @param message
     * @param type
     * @return
     */
    private static TextMessage toClient(String message, String type) {
        TwoWayMessage toClient = new TwoWayMessage(type, message);
        try {
            String json = objectMapper.writeValueAsString(toClient);
            return new TextMessage(json);
        } catch (Exception e) {
            return new TextMessage(e.getMessage());
        }
    }

    /**
     * send string to client
     * @param message
     * @return
     */
    public static TextMessage toClientString(String message) {
        return toClient(message, "n");
    }
}
