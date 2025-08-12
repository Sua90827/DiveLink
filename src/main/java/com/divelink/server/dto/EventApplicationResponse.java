package com.divelink.server.dto;

import com.divelink.server.domain.EventApplication.EventApplicationStatus;
import java.time.LocalDateTime;

public record EventApplicationResponse(
    Long id,
    String userId,
    String userName,
    EventApplicationStatus status,
    LocalDateTime createdAt
) {}
