package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.Status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class WorkflowRouter {

    private static final BigDecimal MANAGER_THRESHOLD = new BigDecimal("100");
    private static final List<Stage> STAGES = List.of(Stage.MANAGER, Stage.IT, Stage.FINANCE);

    private static final Map<Stage, Status> STAGE_TO_STATUS = Map.of(
            Stage.MANAGER, Status.PENDING_MANAGER_APPROVAL,
            Stage.IT,      Status.PENDING_IT_VALIDATION,
            Stage.FINANCE, Status.PENDING_FINANCE_APPROVAL
    );

    private static final Map<Status, Stage> STATUS_TO_STAGE = Map.of(
            Status.PENDING_MANAGER_APPROVAL, Stage.MANAGER,
            Status.PENDING_IT_VALIDATION,    Stage.IT,
            Status.PENDING_FINANCE_APPROVAL, Stage.FINANCE
    );

    private WorkflowRouter() {}

    /**
     * Returns the next pending state after {@code completedStage}.
     * Pass {@code null} for {@code completedStage} to get the entry state on submit/resubmit.
     */
    public static Status nextStateAfter(PurchaseOrder po, Stage completedStage) {
        boolean scanning = (completedStage == null);
        for (Stage stage : STAGES) {
            if (!scanning) {
                if (stage == completedStage) {
                    scanning = true;
                }
                continue;
            }
            if (appliesTo(stage, po)) {
                return STAGE_TO_STATUS.get(stage);
            }
        }
        return Status.INVOICED;
    }

    /** Entry state on initial submit or resubmit after rework. */
    public static Status entryState(PurchaseOrder po) {
        return nextStateAfter(po, null);
    }

    /**
     * Maps a {@code PENDING_*} status back to the {@link Stage} that owns it.
     * Used by the approve path to determine which stage just completed.
     */
    public static Stage stageOf(Status status) {
        Stage stage = STATUS_TO_STAGE.get(status);
        if (stage == null) {
            throw new IllegalArgumentException("No stage owns status: " + status);
        }
        return stage;
    }

    private static boolean appliesTo(Stage stage, PurchaseOrder po) {
        return switch (stage) {
            case MANAGER -> po.getAmount().compareTo(MANAGER_THRESHOLD) >= 0;
            case IT      -> po.getCategory() == Category.IT_EQUIPMENT;
            case FINANCE -> true;
        };
    }
}
