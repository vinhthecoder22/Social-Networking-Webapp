package com.example.projectbase.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareMediaResponseDto {

    private Long id;
    private String title;
    private String singerName;
    private String downloadUrl;

}
