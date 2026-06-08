package com.pomanagement;

import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.domain.repository.UserRepository;
import com.pomanagement.service.PurchaseOrderService;
import com.pomanagement.web.dto.CreatePoRequest;
import com.pomanagement.web.dto.PoDetailResponse;
import com.pomanagement.web.dto.UpdatePoRequest;
import com.pomanagement.workflow.JpaPoHistoryRecorder;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @SpringBootTest integration tests on a real Testcontainers PostgreSQL instance.
 * Flyway migrations run on startup; each test is rolled back via @Transactional.
 */
@Transactional
class WorkflowIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private JpaPoHistoryRecorder historyRecorder;

    private User creator;
    private User manager;
    private User itRep;
    private User finance;

    @BeforeEach
    void setUpUsers() {
        creator = userRepository.save(User.builder()
                .name("Creator").email("creator@int.test").role(Role.CREATOR).build());
        manager = userRepository.save(User.builder()
                .name("Manager").email("manager@int.test").role(Role.MANAGER).build());
        itRep = userRepository.save(User.builder()
                .name("IT Rep").email("itrep@int.test").role(Role.IT_REP).build());
        finance = userRepository.save(User.builder()
                .name("Finance").email("finance@int.test").role(Role.FINANCE).build());
    }

    // ---- routing happy paths ----------------------------------------------------

    @Test
    void happyPath_belowThreshold_nonIT_financeApprovesDirectly() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Pens", null, new BigDecimal("50.00"), Category.SERVICES),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(1);

        PoDetailResponse invoiced = poService.approve(po.id(), finance);
        assertThat(invoiced.status()).isEqualTo(Status.INVOICED);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(2);
    }

    @Test
    void happyPath_belowThreshold_IT_routesThroughIT() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Mouse", null, new BigDecimal("50.00"), Category.IT_EQUIPMENT),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_IT_VALIDATION);

        PoDetailResponse afterIT = poService.approve(po.id(), itRep);
        assertThat(afterIT.status()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);

        PoDetailResponse invoiced = poService.approve(po.id(), finance);
        assertThat(invoiced.status()).isEqualTo(Status.INVOICED);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(3);
    }

    @Test
    void happyPath_aboveThreshold_nonIT_routesThroughManagerThenFinance() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Furniture", null, new BigDecimal("500.00"), Category.SERVICES),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);

        PoDetailResponse afterManager = poService.approve(po.id(), manager);
        assertThat(afterManager.status()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);

        PoDetailResponse invoiced = poService.approve(po.id(), finance);
        assertThat(invoiced.status()).isEqualTo(Status.INVOICED);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(3);
    }

    @Test
    void happyPath_aboveThreshold_IT_routesThroughAllThreeStages() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Server", null, new BigDecimal("5000.00"), Category.IT_EQUIPMENT),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);

        PoDetailResponse afterManager = poService.approve(po.id(), manager);
        assertThat(afterManager.status()).isEqualTo(Status.PENDING_IT_VALIDATION);

        PoDetailResponse afterIT = poService.approve(po.id(), itRep);
        assertThat(afterIT.status()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);

        PoDetailResponse invoiced = poService.approve(po.id(), finance);
        assertThat(invoiced.status()).isEqualTo(Status.INVOICED);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(4);
    }

    @Test
    void boundaryAmount_exactlyOneHundred_requiresManagerApproval() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Exact100", null, new BigDecimal("100.00"), Category.SERVICES),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    // ---- reject → rework → edit → resubmit loop --------------------------------

    @Test
    void reworkLoop_rejectEditResubmitApprovesToInvoiced() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Laptop", null, new BigDecimal("1500.00"), Category.IT_EQUIPMENT),
                creator);

        assertThat(po.status()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(1);

        // manager rejects
        PoDetailResponse rejected = poService.reject(po.id(), "needs justification", manager);
        assertThat(rejected.status()).isEqualTo(Status.NEEDS_REWORK);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(2);
        assertThat(historyRecorder.historyFor(po.id()).get(1).getAction())
                .isEqualTo(HistoryAction.REJECT);
        assertThat(historyRecorder.historyFor(po.id()).get(1).getComment())
                .isEqualTo("needs justification");

        // creator edits (changes to non-IT, below threshold → will bypass manager and IT on resubmit)
        poService.update(po.id(),
                new UpdatePoRequest(null, "updated description", new BigDecimal("50.00"), Category.SERVICES),
                creator);

        // creator resubmits — routing re-reads the updated amount/category
        PoDetailResponse resubmitted = poService.resubmit(po.id(), creator);
        assertThat(resubmitted.status()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(3);
        assertThat(historyRecorder.historyFor(po.id()).get(2).getAction())
                .isEqualTo(HistoryAction.RESUBMIT);

        // finance approves
        PoDetailResponse invoiced = poService.approve(po.id(), finance);
        assertThat(invoiced.status()).isEqualTo(Status.INVOICED);
        assertThat(historyRecorder.historyFor(po.id())).hasSize(4);
    }

    @Test
    void reworkLoop_historyIsAppendOnly_allRowsPersisted() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Widget", null, new BigDecimal("200.00"), Category.SERVICES),
                creator);

        poService.reject(po.id(), "incomplete", manager);
        poService.resubmit(po.id(), creator);
        poService.approve(po.id(), manager);
        poService.approve(po.id(), finance);

        var history = historyRecorder.historyFor(po.id());
        assertThat(history).hasSize(5);
        assertThat(history.stream().map(h -> h.getAction()).toList()).containsExactly(
                HistoryAction.SUBMIT, HistoryAction.REJECT, HistoryAction.RESUBMIT,
                HistoryAction.APPROVE, HistoryAction.APPROVE);
    }

    // ---- guard violations assert typed exceptions / correct status -------------

    @Test
    void guardViolation_selfApproval_throwsForbidden() {
        // Manager creates a PO — then tries to approve their own
        User managerAsCreator = userRepository.save(User.builder()
                .name("Mgr Creator").email("mgr.creator@int.test").role(Role.MANAGER).build());

        PoDetailResponse po = poService.create(
                new CreatePoRequest("Self PO", null, new BigDecimal("200.00"), Category.SERVICES),
                managerAsCreator);

        assertThatThrownBy(() -> poService.approve(po.id(), managerAsCreator))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void guardViolation_wrongRole_throwsForbidden() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Chairs", null, new BigDecimal("200.00"), Category.SERVICES),
                creator);

        assertThatThrownBy(() -> poService.approve(po.id(), itRep))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void guardViolation_rejectWithoutComment_throwsValidation() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Tables", null, new BigDecimal("200.00"), Category.SERVICES),
                creator);

        assertThatThrownBy(() -> poService.reject(po.id(), "", manager))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void guardViolation_approveInvoiced_throwsIllegalTransition() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Ink", null, new BigDecimal("50.00"), Category.SERVICES),
                creator);

        poService.approve(po.id(), finance);

        assertThatThrownBy(() -> poService.approve(po.id(), finance))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void guardViolation_resubmitNonRework_throwsIllegalTransition() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Paper", null, new BigDecimal("200.00"), Category.SERVICES),
                creator);

        assertThatThrownBy(() -> poService.resubmit(po.id(), creator))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void guardViolation_editWhileNotNeedsRework_throwsIllegalTransition() {
        PoDetailResponse po = poService.create(
                new CreatePoRequest("Stapler", null, new BigDecimal("200.00"), Category.SERVICES),
                creator);

        assertThatThrownBy(() -> poService.update(po.id(),
                new UpdatePoRequest("New title", null, null, null), creator))
                .isInstanceOf(IllegalTransitionException.class);
    }
}
