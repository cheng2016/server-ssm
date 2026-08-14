package com.cheng.game.common.error;

public enum ErrorCode {
    OK(0, "ok"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    CONFLICT(409, "conflict"),
    INTERNAL(500, "internal error"),
    INVALID_TOKEN(1001, "invalid token"),
    PLAYER_OFFLINE(1002, "player offline"),
    LOGIN_FAILED(1003, "login failed"),
    RATE_LIMITED(1004, "too many requests"),
    BAD_CONTENT(1005, "invalid chat content"),
    KICKED(1006, "kicked"),
    REPLACED(1007, "logged in elsewhere"),
    BUSY(1008, "server busy");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
