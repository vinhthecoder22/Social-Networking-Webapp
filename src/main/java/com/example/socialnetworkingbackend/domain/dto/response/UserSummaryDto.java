package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {

    private String id;
    private String firstName;
    private String lastName;
    private String imageUrl;

}
