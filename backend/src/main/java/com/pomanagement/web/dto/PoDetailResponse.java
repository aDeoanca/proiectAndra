package com.pomanagement.web.dto;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PoDetailResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        String currency,
        Category category,
        Status status,
        Long creatorId,
        String creatorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PoHistoryDto> history
) {
    public static PoDetailResponse from(PurchaseOrder po, List<PoHistoryDto> history) {
        return new PoDetailResponse(
                po.getId(),
                po.getTitle(),
                po.getDescription(),
                po.getAmount(),
                po.getCurrency(),
                po.getCategory(),
                po.getStatus(),
                po.getCreator().getId(),
                po.getCreator().getName(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                history
        );
    }
}
