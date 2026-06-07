package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PoHistoryRepository extends JpaRepository<PoHistory, Long> {
    List<PoHistory> findByPoIdOrderByCreatedAtAscIdAsc(Long poId);
}
