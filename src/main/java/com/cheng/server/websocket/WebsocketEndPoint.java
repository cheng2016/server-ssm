package com.cheng.server.websocket;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebsocketEndPoint extends TextWebSocketHandler {
    private static final Log logger = LogFactory.getLog(WebsocketEndPoint.class);

    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server");
        session.sendMessage(returnMessage);
        logger.info("message："+message);
        logger.info("returnMessage："+returnMessage.getPayload());
    }

}
