package com.mehmetkerem.util;

import com.mehmetkerem.dto.response.CursorResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

public class ResultHelper {

    public static <T> ResultData<T> success(T data) {
        return new ResultData<>(true, "İşlem Başarılı", "200", data);
    }

    public static Result ok() {
        return new Result(true, "İşlem Başarılı", "200");
    }

    public static Result error(String message, HttpStatus status) {
        return new Result(false, message, String.valueOf(status.value()));
    }

    public static Result notFoundError(String message) {
        return new Result(false, message, "404");
    }

    public static <T> ResultData<T> validateError(T data) {
        return new ResultData<>(false, "Validasyon Hatası", "400", data);
    }

    public static <T> ResultData<CursorResponse<T>> cursor(Page<T> pageData) {
        CursorResponse<T> cursor = new CursorResponse<>();
        cursor.setItems(pageData.getContent());
        cursor.setPageNumber(pageData.getNumber());
        cursor.setPageSize(pageData.getSize());
        cursor.setTotalElement(pageData.getTotalElements());

        return ResultHelper.success(cursor);
    }
}
