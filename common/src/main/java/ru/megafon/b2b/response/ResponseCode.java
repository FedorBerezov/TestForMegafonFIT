package ru.megafon.b2b.response;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ResponseCode {
    ESB_OK(200, "OK"),
    ESB_400(400, "Invalid input"),
    ESB_404(404, "Not found"),
    ESB_429(429, "Too many requests"),
    ESB_500(500, "Internal error"),
    ESB_503(503, "503 message");

    private final int code;
    private final String text;

    ResponseCode(int code, String text) {
        this.code = code;
        this.text = text;
    }

    private static final Map<Integer, ResponseCode> map;

    public static ResponseCode of(int code) {
        return map.get(code);
    }

    static {
        Map<Integer, ResponseCode> otMap = new HashMap<>();
        for (ResponseCode responseCode : ResponseCode.values()) {
            otMap.put(responseCode.code, responseCode);
        }
        map = Collections.unmodifiableMap(otMap);
    }
}
