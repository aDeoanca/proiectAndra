package com.pomanagement.web.dto;

import com.pomanagement.domain.entity.PoHistory;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Status;

import java.time.LocalDateTime;

public record PoHistoryDto(
        Long id,
        HistoryAction action,
        Status fromStatus,
        Status toStatus,
        String comment,
        Long actorId,
        String actorName,
        LocalDateTime createdAt
) {
    public static PoHistoryDto from(PoHistory h) {
        return new PoHistoryDto(
                h.getId(),
                h.getAction(),
                h.getFromStatus(),
                h.getToStatus(),
                h.getComment(),
                h.getActor().getId(),
                h.getActor().getName(),
                h.getCreatedAt()
        );
    }
}
