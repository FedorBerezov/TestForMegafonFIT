package ru.megafon.b2b.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.megafon.b2b.response.ResponseCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T> {
    private T data;
    private StatusResponse status;

    public static <T> ResponseMessage<T> ok(T data) {
        return ResponseMessage.<T>builder()
                .data(data)
                .status(StatusResponse.builder()
                        .code(ResponseCode.ESB_OK.getCode())
                        .message(ResponseCode.ESB_OK.getText())
                        .build())
                .build();
    }

    public static ResponseMessage ok() {
        return ResponseMessage.builder()
                .status(StatusResponse.builder()
                        .code(ResponseCode.ESB_OK.getCode())
                        .message(ResponseCode.ESB_OK.getText())
                        .build())
                .build();
    }

    public static ResponseMessage error(ResponseCode code) {
        return ResponseMessage.builder()
                .status(StatusResponse.builder()
                        .code(code.getCode())
                        .message(code.getText())
                        .build())
                .build();
    }

    public static <T> ResponseMessage<T> error(ResponseCode code, T data) {
        return ResponseMessage.<T>builder()
                .data(data)
                .status(StatusResponse.builder()
                        .code(code.getCode())
                        .message(code.getText())
                        .build())
                .build();
    }
}
