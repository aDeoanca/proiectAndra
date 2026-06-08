package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class PurchaseOrderWorkflowService {

    private static final Map<Status, Role> STATUS_OWNER = Map.of(
            Status.PENDING_MANAGER_APPROVAL, Role.MANAGER,
            Status.PENDING_IT_VALIDATION,    Role.IT_REP,
            Status.PENDING_FINANCE_APPROVAL, Role.FINANCE
    );

    private final PoHistoryRecorder recorder;

    public PurchaseOrderWorkflowService(PoHistoryRecorder recorder) {
        this.recorder = recorder;
    }

    public void submit(PurchaseOrder po, User actor) {
        Status next = WorkflowRouter.entryState(po);
        po.setStatus(next);
        recorder.record(po, actor, HistoryAction.SUBMIT, null, next, null);
    }

    public void approve(PurchaseOrder po, User actor) {
        Status current = po.getStatus();
        guardPendingState(current);
        guardNoSelfApproval(po, actor);
        guardRoleOwns(current, actor);

        Stage completed = WorkflowRouter.stageOf(current);
        Status next = WorkflowRouter.nextStateAfter(po, completed);
        po.setStatus(next);
        recorder.record(po, actor, HistoryAction.APPROVE, current, next, null);
    }

    public void reject(PurchaseOrder po, User actor, String comment) {
        Status current = po.getStatus();
        guardPendingState(current);
        guardNoSelfApproval(po, actor);
        guardRoleOwns(current, actor);
        guardNonBlankComment(comment);

        po.setStatus(Status.NEEDS_REWORK);
        recorder.record(po, actor, HistoryAction.REJECT, current, Status.NEEDS_REWORK, comment);
    }

    public void resubmit(PurchaseOrder po, User actor) {
        if (po.getStatus() != Status.NEEDS_REWORK) {
            throw new IllegalTransitionException(
                    "resubmit requires NEEDS_REWORK, was: " + po.getStatus());
        }
        if (!po.getCreator().getId().equals(actor.getId())) {
            throw new ForbiddenActionException("only the creator may resubmit");
        }

        Status next = WorkflowRouter.entryState(po);
        po.setStatus(next);
        recorder.record(po, actor, HistoryAction.RESUBMIT, Status.NEEDS_REWORK, next, null);
    }

    private void guardPendingState(Status status) {
        if (!STATUS_OWNER.containsKey(status)) {
            throw new IllegalTransitionException("action not allowed in state: " + status);
        }
    }

    private void guardRoleOwns(Status status, User actor) {
        Role required = STATUS_OWNER.get(status);
        if (actor.getRole() != required) {
            throw new ForbiddenActionException(
                    "role " + actor.getRole() + " cannot act on state " + status);
        }
    }

    private void guardNoSelfApproval(PurchaseOrder po, User actor) {
        if (po.getCreator().getId().equals(actor.getId())) {
            throw new ForbiddenActionException("creator cannot approve or reject their own PO");
        }
    }

    private void guardNonBlankComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("reject comment must not be blank");
        }
    }
}
