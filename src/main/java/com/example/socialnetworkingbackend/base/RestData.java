package com.example.socialnetworkingbackend.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestData<T> {

    private RestStatus status;

    // Mã lỗi / mã thành công để FE xử lý logic
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    // Message hiển thị cho user
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    // Data trả về
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public RestData(T data) {
        this.status = RestStatus.SUCCESS;
        this.data = data;
    }

    // ===== SUCCESS =====
    public static <T> RestData<T> success(T data) {
        return RestData.<T>builder()
                .status(RestStatus.SUCCESS)
                .data(data)
                .build();
    }

    public static RestData<?> successWithMessage(String message) {
        return RestData.builder()
                .status(RestStatus.SUCCESS)
                .message(message)
                .build();
    }

    // ===== ERROR =====
    public static RestData<?> error(String code, String message) {
        return RestData.builder()
                .status(RestStatus.ERROR)
                .code(code)
                .message(message)
                .build();
    }

}

