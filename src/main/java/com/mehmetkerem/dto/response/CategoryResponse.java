package com.mehmetkerem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private String id;
    private String name;
}
