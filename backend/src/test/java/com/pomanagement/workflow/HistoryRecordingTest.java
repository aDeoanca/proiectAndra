package com.pomanagement.workflow;

import com.pomanagement.config.JpaConfig;
import com.pomanagement.domain.entity.PoHistory;
import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, JpaPoHistoryRecorder.class, PurchaseOrderWorkflowService.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/pomanagement",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.flyway.enabled=true"
})
class HistoryRecordingTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaPoHistoryRecorder recorder;

    @Autowired
    private PurchaseOrderWorkflowService workflowService;

    private User creator;
    private User manager;
    private User finance;
    private PurchaseOrder po;

    @BeforeEach
    void setUp() {
        creator = em.persist(User.builder()
                .name("Hist Creator")
                .email("hist.creator@test.com")
                .role(Role.CREATOR)
                .build());

        manager = em.persist(User.builder()
                .name("Hist Manager")
                .email("hist.manager@test.com")
                .role(Role.MANAGER)
                .build());

        finance = em.persist(User.builder()
                .name("Hist Finance")
                .email("hist.finance@test.com")
                .role(Role.FINANCE)
                .build());

        // amount >= 100, SERVICES (not IT) → routes MANAGER → FINANCE
        po = em.persist(PurchaseOrder.builder()
                .title("History Test PO")
                .amount(new BigDecimal("500.00"))
                .category(Category.SERVICES)
                .status(Status.PENDING_MANAGER_APPROVAL)
                .creator(creator)
                .build());

        em.flush();
    }

    @Test
    void submitApproveRejectResubmitProducesFourChronologicalRows() {
        workflowService.submit(po, creator);
        workflowService.approve(po, manager);
        workflowService.reject(po, finance, "budget exceeded");
        workflowService.resubmit(po, creator);

        List<PoHistory> rows = recorder.historyFor(po.getId());

        assertThat(rows).hasSize(4);

        PoHistory submit = rows.get(0);
        assertThat(submit.getAction()).isEqualTo(HistoryAction.SUBMIT);
        assertThat(submit.getFromStatus()).isNull();
        assertThat(submit.getToStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
        assertThat(submit.getActor().getId()).isEqualTo(creator.getId());
        assertThat(submit.getComment()).isNull();

        PoHistory approve = rows.get(1);
        assertThat(approve.getAction()).isEqualTo(HistoryAction.APPROVE);
        assertThat(approve.getFromStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
        assertThat(approve.getToStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
        assertThat(approve.getActor().getId()).isEqualTo(manager.getId());
        assertThat(approve.getComment()).isNull();

        PoHistory reject = rows.get(2);
        assertThat(reject.getAction()).isEqualTo(HistoryAction.REJECT);
        assertThat(reject.getFromStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
        assertThat(reject.getToStatus()).isEqualTo(Status.NEEDS_REWORK);
        assertThat(reject.getActor().getId()).isEqualTo(finance.getId());
        assertThat(reject.getComment()).isEqualTo("budget exceeded");

        PoHistory resubmit = rows.get(3);
        assertThat(resubmit.getAction()).isEqualTo(HistoryAction.RESUBMIT);
        assertThat(resubmit.getFromStatus()).isEqualTo(Status.NEEDS_REWORK);
        assertThat(resubmit.getToStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
        assertThat(resubmit.getActor().getId()).isEqualTo(creator.getId());
        assertThat(resubmit.getComment()).isNull();

        // rows are in chronological order (id tiebreaks equal timestamps)
        for (int i = 0; i < rows.size() - 1; i++) {
            assertThat(rows.get(i).getCreatedAt()).isBeforeOrEqualTo(rows.get(i + 1).getCreatedAt());
        }
    }

    @Test
    void failedActionLeavesNoOrphanHistoryRow() {
        workflowService.submit(po, creator);

        // creator has CREATOR role; PENDING_MANAGER_APPROVAL requires MANAGER → guard fires before record()
        assertThatThrownBy(() -> workflowService.approve(po, creator))
                .isInstanceOf(ForbiddenActionException.class);

        assertThat(recorder.historyFor(po.getId())).hasSize(1);
    }
}
