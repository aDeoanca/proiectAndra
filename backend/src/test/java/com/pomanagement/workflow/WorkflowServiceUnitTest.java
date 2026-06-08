package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests — no Spring context, no database. Uses a stub PoHistoryRecorder.
 */
class WorkflowServiceUnitTest {

    private RecordingStub stub;
    private PurchaseOrderWorkflowService svc;

    @BeforeEach
    void setUp() {
        stub = new RecordingStub();
        svc = new PurchaseOrderWorkflowService(stub);
    }

    // ---- helpers ----------------------------------------------------------------

    private User user(long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("User" + id);
        u.setEmail("u" + id + "@test.com");
        u.setRole(role);
        return u;
    }

    private PurchaseOrder po(User creator, Status status, String amount, Category category) {
        PurchaseOrder p = new PurchaseOrder();
        p.setId(1L);
        p.setTitle("Test PO");
        p.setAmount(new BigDecimal(amount));
        p.setCategory(category);
        p.setStatus(status);
        p.setCreator(creator);
        return p;
    }

    // ---- submit -----------------------------------------------------------------

    @Test
    void submit_belowThreshold_nonIT_setsFinanceState() {
        User creator = user(1, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "50.00", Category.SERVICES);

        svc.submit(p, creator);

        assertThat(p.getStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
        assertThat(stub.calls).hasSize(1);
        assertThat(stub.calls.get(0).action()).isEqualTo(HistoryAction.SUBMIT);
    }

    @Test
    void submit_exactlyAtThreshold_setsManagerState() {
        User creator = user(1, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "100.00", Category.SERVICES);

        svc.submit(p, creator);

        assertThat(p.getStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    @Test
    void submit_writesOneHistoryRow() {
        User creator = user(1, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        svc.submit(p, creator);

        assertThat(stub.calls).hasSize(1);
        RecordingStub.Entry entry = stub.calls.get(0);
        assertThat(entry.action()).isEqualTo(HistoryAction.SUBMIT);
        assertThat(entry.fromStatus()).isNull();
        assertThat(entry.toStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    // ---- approve ----------------------------------------------------------------

    @Test
    void approve_wrongRole_throwsForbidden() {
        User creator = user(1, Role.CREATOR);
        User itRep = user(2, Role.IT_REP);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.approve(p, itRep))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void approve_selfApproval_throwsForbidden() {
        User creator = user(1, Role.CREATOR);
        // make creator also have MANAGER role for the role check to not fire first
        User creatorAsManager = user(1, Role.MANAGER);
        PurchaseOrder p = po(creatorAsManager, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.approve(p, creatorAsManager))
                .isInstanceOf(ForbiddenActionException.class)
                .hasMessageContaining("cannot approve");
    }

    @Test
    void approve_invoicedState_throwsIllegalTransition() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.INVOICED, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.approve(p, manager))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void approve_needsReworkState_throwsIllegalTransition() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.NEEDS_REWORK, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.approve(p, manager))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void approve_writesHistoryRow() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        svc.approve(p, manager);

        assertThat(stub.calls).hasSize(1);
        RecordingStub.Entry entry = stub.calls.get(0);
        assertThat(entry.action()).isEqualTo(HistoryAction.APPROVE);
        assertThat(entry.fromStatus()).isEqualTo(Status.PENDING_MANAGER_APPROVAL);
        assertThat(entry.toStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    // ---- reject -----------------------------------------------------------------

    @Test
    void reject_blankComment_throwsValidation() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.reject(p, manager, "  "))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reject_nullComment_throwsValidation() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.reject(p, manager, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reject_wrongRole_throwsForbidden() {
        User creator = user(1, Role.CREATOR);
        User finance = user(2, Role.FINANCE);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.reject(p, finance, "reason"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void reject_selfApproval_throwsForbidden() {
        User creatorAsManager = user(1, Role.MANAGER);
        PurchaseOrder p = po(creatorAsManager, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.reject(p, creatorAsManager, "reason"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void reject_setsNeedsReworkAndWritesHistory() {
        User creator = user(1, Role.CREATOR);
        User manager = user(2, Role.MANAGER);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        svc.reject(p, manager, "over budget");

        assertThat(p.getStatus()).isEqualTo(Status.NEEDS_REWORK);
        assertThat(stub.calls).hasSize(1);
        RecordingStub.Entry entry = stub.calls.get(0);
        assertThat(entry.action()).isEqualTo(HistoryAction.REJECT);
        assertThat(entry.comment()).isEqualTo("over budget");
    }

    // ---- resubmit ---------------------------------------------------------------

    @Test
    void resubmit_nonReworkState_throwsIllegalTransition() {
        User creator = user(1, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.PENDING_MANAGER_APPROVAL, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.resubmit(p, creator))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void resubmit_nonCreator_throwsForbidden() {
        User creator = user(1, Role.CREATOR);
        User other = user(2, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.NEEDS_REWORK, "200.00", Category.SERVICES);

        assertThatThrownBy(() -> svc.resubmit(p, other))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void resubmit_writesHistoryRow() {
        User creator = user(1, Role.CREATOR);
        PurchaseOrder p = po(creator, Status.NEEDS_REWORK, "200.00", Category.SERVICES);

        svc.resubmit(p, creator);

        assertThat(stub.calls).hasSize(1);
        RecordingStub.Entry entry = stub.calls.get(0);
        assertThat(entry.action()).isEqualTo(HistoryAction.RESUBMIT);
        assertThat(entry.fromStatus()).isEqualTo(Status.NEEDS_REWORK);
    }

    @Test
    void resubmit_amountBelowThreshold_reroutes() {
        User creator = user(1, Role.CREATOR);
        // PO was originally manager-gated; after rework the amount dropped below threshold
        PurchaseOrder p = po(creator, Status.NEEDS_REWORK, "50.00", Category.SERVICES);

        svc.resubmit(p, creator);

        assertThat(p.getStatus()).isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    // ---- stub -------------------------------------------------------------------

    static class RecordingStub implements PoHistoryRecorder {

        record Entry(
                PurchaseOrder po, User actor, HistoryAction action,
                Status fromStatus, Status toStatus, String comment) {}

        final List<Entry> calls = new ArrayList<>();

        @Override
        public void record(PurchaseOrder po, User actor, HistoryAction action,
                           Status fromStatus, Status toStatus, String comment) {
            calls.add(new Entry(po, actor, action, fromStatus, toStatus, comment));
        }
    }
}
