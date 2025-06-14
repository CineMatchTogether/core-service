package com.service.core.models.dto;

import com.service.core.models.entities.enums.ESessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SessionDto  {

    private String id;
    private ESessionStatus sessionStatus;
    private List<Long> moviesIds;
    private List<String> usernames;
}
