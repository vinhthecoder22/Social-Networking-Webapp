package com.example.socialnetworkingbackend.domain.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {

    private List<T> data;
    private Long nextCursor;
    private boolean hasNext;

}