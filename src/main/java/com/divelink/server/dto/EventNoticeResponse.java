package com.divelink.server.dto;

import java.time.LocalDateTime;

public record EventNoticeResponse(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {}
