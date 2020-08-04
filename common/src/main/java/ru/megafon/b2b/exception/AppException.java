package ru.megafon.b2b.exception;

import lombok.Getter;
import ru.megafon.b2b.response.ResponseCode;

@Getter
public class AppException extends RuntimeException {
    private ResponseCode code;

    public AppException(String message, ResponseCode code) {
        super(message);
        this.code = code;
    }

    public AppException(ResponseCode code) {
        super("Application exception");
        this.code = code;
    }
}
