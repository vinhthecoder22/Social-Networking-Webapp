package com.example.socialnetworkingbackend.domain.dto.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class UserAuditingDto {

  private CreatedByDto createdBy;

  private LastModifiedByDto lastModifiedBy;

}
