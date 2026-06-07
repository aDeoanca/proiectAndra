package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderWorkflowServiceTest {

    @Mock
    private PoHistoryRecorder recorder;

    private PurchaseOrderWorkflowService service;

    private User creator;
    private User manager;
    private User itRep;
    private User finance;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderWorkflowService(recorder);

        creator = User.builder().id(1L).name("Alice").email("alice@example.com").role(Role.CREATOR).build();
        manager = User.builder().id(2L).name("Bob").email("bob@example.com").role(Role.MANAGER).build();
        itRep   = User.builder().id(3L).name("Carol").email("carol@example.com").role(Role.IT_REP).build();
        finance = User.builder().id(4L).name("Dave").email("dave@example.com").role(Role.FINANCE).build();
    }

    private PurchaseOrder pendingManagerPo() {
        return PurchaseOrder.builder()
                .id(10L)
                .title("Test PO")
                .amount(new BigDecimal("150.00"))
                .category(Category.SERVICES)
                .status(Status.PENDING_MANAGER_APPROVAL)
                .creator(creator)
                .build();
    }

    private PurchaseOrder pendingFinancePo() {
        return PurchaseOrder.builder()
                .id(11L)
                .title("Test PO")
                .amount(new BigDecimal("50.00"))
                .category(Category.SERVICES)
                .status(Status.PENDING_FINANCE_APPROVAL)
                .creator(creator)
                .build();
    }

    private PurchaseOrder needsReworkPo() {
        return PurchaseOrder.builder()
                .id(12L)
                .title("Test PO")
                .amount(new BigDecimal("150.00"))
                .category(Category.SERVICES)
                .status(Status.NEEDS_REWORK)
                .creator(creator)
                .build();
    }

    private PurchaseOrder invoicedPo() {
        return PurchaseOrder.builder()
                .id(13L)
                .title("Test PO")
                .amount(new BigDecimal("50.00"))
                .category(Category.SERVICES)
                .status(Status.INVOICED)
                .creator(creator)
                .build();
    }

    // --- submit ---

    @Test
    void submit_setsEntryState() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(20L).title("T").amount(new BigDecimal("150.00"))
                .category(Category.SERVICES).creator(creator).build();
        service.submit(po, creator);
        assertThat(po.getStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    // --- approve: wrong role → ForbiddenActionException ---

    @Test
    void approve_wrongRole_forbidden() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.approve(po, finance))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void approve_itRepOnManagerState_forbidden() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.approve(po, itRep))
                .isInstanceOf(ForbiddenActionException.class);
    }

    // --- approve: self-approval → ForbiddenActionException ---

    @Test
    void approve_creatorApprovingOwnPo_forbidden() {
        // Creator has MANAGER role? No — creator has CREATOR role, so wrong-role fires first.
        // Build a scenario where actor role matches but actor == creator.
        User managerCreator = User.builder().id(1L).name("Alice").email("alice@example.com").role(Role.MANAGER).build();
        PurchaseOrder po = PurchaseOrder.builder()
                .id(10L).title("T").amount(new BigDecimal("150.00"))
                .category(Category.SERVICES).status(Status.PENDING_MANAGER_APPROVAL)
                .creator(managerCreator).build();

        assertThatThrownBy(() -> service.approve(po, managerCreator))
                .isInstanceOf(ForbiddenActionException.class);
    }

    // --- approve: illegal state → IllegalTransitionException ---

    @Test
    void approve_invoicedPo_illegalTransition() {
        assertThatThrownBy(() -> service.approve(invoicedPo(), manager))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void approve_needsReworkPo_illegalTransition() {
        assertThatThrownBy(() -> service.approve(needsReworkPo(), manager))
                .isInstanceOf(IllegalTransitionException.class);
    }

    // --- approve: happy path ---

    @Test
    void approve_manager_advancesToFinance() {
        PurchaseOrder po = pendingManagerPo();
        service.approve(po, manager);
        assertThat(po.getStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    @Test
    void approve_finance_invoices() {
        PurchaseOrder po = pendingFinancePo();
        service.approve(po, finance);
        assertThat(po.getStatus()).isEqualTo(Status.INVOICED);
    }

    // --- reject: wrong role → ForbiddenActionException ---

    @Test
    void reject_wrongRole_forbidden() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.reject(po, itRep, "no budget"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    // --- reject: self-approval → ForbiddenActionException ---

    @Test
    void reject_creatorRejectingOwnPo_forbidden() {
        User managerCreator = User.builder().id(1L).name("Alice").email("alice@example.com").role(Role.MANAGER).build();
        PurchaseOrder po = PurchaseOrder.builder()
                .id(10L).title("T").amount(new BigDecimal("150.00"))
                .category(Category.SERVICES).status(Status.PENDING_MANAGER_APPROVAL)
                .creator(managerCreator).build();

        assertThatThrownBy(() -> service.reject(po, managerCreator, "comment"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    // --- reject: blank comment → ValidationException ---

    @Test
    void reject_blankComment_validationException() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.reject(po, manager, ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reject_nullComment_validationException() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.reject(po, manager, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reject_whitespaceComment_validationException() {
        PurchaseOrder po = pendingManagerPo();
        assertThatThrownBy(() -> service.reject(po, manager, "   "))
                .isInstanceOf(ValidationException.class);
    }

    // --- reject: happy path ---

    @Test
    void reject_setsNeedsRework() {
        PurchaseOrder po = pendingManagerPo();
        service.reject(po, manager, "over budget");
        assertThat(po.getStatus()).isEqualTo(Status.NEEDS_REWORK);
    }

    // --- resubmit: non-NEEDS_REWORK → IllegalTransitionException ---

    @Test
    void resubmit_fromPendingManager_illegalTransition() {
        assertThatThrownBy(() -> service.resubmit(pendingManagerPo(), creator))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void resubmit_fromInvoiced_illegalTransition() {
        assertThatThrownBy(() -> service.resubmit(invoicedPo(), creator))
                .isInstanceOf(IllegalTransitionException.class);
    }

    // --- resubmit: non-creator → ForbiddenActionException ---

    @Test
    void resubmit_nonCreator_forbidden() {
        assertThatThrownBy(() -> service.resubmit(needsReworkPo(), manager))
                .isInstanceOf(ForbiddenActionException.class);
    }

    // --- resubmit: happy path ---

    @Test
    void resubmit_reEntersRouting() {
        PurchaseOrder po = needsReworkPo(); // amount=150, non-IT → manager
        service.resubmit(po, creator);
        assertThat(po.getStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }
}
