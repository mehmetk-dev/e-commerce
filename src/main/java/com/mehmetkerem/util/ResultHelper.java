package com.mehmetkerem.util;

import com.mehmetkerem.dto.response.CursorResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public class ResultHelper {

    public static <T> ResultData<T> success(T data){
        return new ResultData<>(true, "İşlem Başarılı","200",data);
    }

    public static <T> ResultData<CursorResponse<T>> cursor(Page<T> pageData){
        CursorResponse<T> cursor = new CursorResponse<>();
        cursor.setItems(pageData.getContent());
        cursor.setPageNumber(pageData.getNumber());
        cursor.setPageSize(pageData.getSize());
        cursor.setTotalElement(pageData.getTotalElements());

        return ResultHelper.success(cursor);
    }
}
