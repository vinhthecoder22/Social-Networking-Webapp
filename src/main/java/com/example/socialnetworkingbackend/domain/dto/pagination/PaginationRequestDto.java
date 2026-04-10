package com.example.socialnetworkingbackend.domain.dto.pagination;

import com.example.socialnetworkingbackend.constant.CommonConstant;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PaginationRequestDto {

  @Parameter(description = "Page you want to retrieve (1..N)")
  private Integer pageNum = CommonConstant.ONE_INT_VALUE;

  @Parameter(description = "Number of records per page")
  private Integer pageSize = CommonConstant.PAGE_SIZE_DEFAULT;

  public int getPageNum() {
    if (pageNum == null || pageNum < 1) {
        return 0; // JPA tính page từ 0
    }
    return pageNum - 1;
  }

  public int getPageSize() {
    if (pageSize == null || pageSize < 1) {
        return CommonConstant.PAGE_SIZE_DEFAULT;
    }
    return pageSize;
  }

}
