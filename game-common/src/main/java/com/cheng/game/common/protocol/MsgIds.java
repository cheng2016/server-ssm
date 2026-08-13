package com.cheng.game.common.protocol;

/**
 * Binary frame: int32 length + int16 msgId + protobuf payload.
 * length = 2 + payload.length
 */
public final class MsgIds {

    public static final short LOGIN_REQ = 1001;
    public static final short LOGIN_RESP = 1002;
    public static final short HEARTBEAT_REQ = 1003;
    public static final short HEARTBEAT_RESP = 1004;
    public static final short CHAT_REQ = 1005;
    public static final short CHAT_MSG = 1006;

    private MsgIds() {
    }
}
