package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CommonResponseDto {

  private Boolean status;

  private String message;

  public CommonResponseDto(boolean status, String message) {
    this.status = status;
    this.message = message;
  }
}
