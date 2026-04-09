package com.example.socialnetworkingbackend.domain.dto.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class FlagUserDateAuditingDto extends UserDateAuditingDto {

  private Boolean deleteFlag;

  private Boolean activeFlag;

}
