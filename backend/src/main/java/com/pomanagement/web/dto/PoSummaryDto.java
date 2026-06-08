package com.pomanagement.web.dto;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PoSummaryDto(
        Long id,
        String title,
        BigDecimal amount,
        Category category,
        Status status,
        Long creatorId,
        String creatorName,
        LocalDateTime updatedAt
) {
    public static PoSummaryDto from(PurchaseOrder po) {
        return new PoSummaryDto(
                po.getId(),
                po.getTitle(),
                po.getAmount(),
                po.getCategory(),
                po.getStatus(),
                po.getCreator().getId(),
                po.getCreator().getName(),
                po.getUpdatedAt()
        );
    }
}
