package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.HistoryAction;
import com.pomanagement.domain.enums.Status;

public interface PoHistoryRecorder {
    void record(PurchaseOrder po, User actor, HistoryAction action, Status fromStatus, Status toStatus, String comment);
}
