package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PoHistory;
import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Status;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaPoHistoryRecorder implements PoHistoryRecorder {

    private final PoHistoryRepository repository;

    public JpaPoHistoryRecorder(PoHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(PurchaseOrder po, User actor, HistoryAction action,
                       Status fromStatus, Status toStatus, String comment) {
        repository.save(PoHistory.builder()
                .po(po)
                .actor(actor)
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .comment(comment)
                .build());
    }

    public List<PoHistory> historyFor(Long poId) {
        return repository.findByPoIdOrderByCreatedAtAscIdAsc(poId);
    }
}
