package com.pomanagement.domain.repository;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByStatusAndCreatorIdNot(Status status, Long creatorId, Pageable pageable);

    List<PurchaseOrder> findByCreatorId(Long creatorId, Pageable pageable);

    List<PurchaseOrder> findByStatus(Status status, Pageable pageable);
}
