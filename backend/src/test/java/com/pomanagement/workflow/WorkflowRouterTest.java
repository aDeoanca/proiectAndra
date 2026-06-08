package com.pomanagement.workflow;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.enums.Category;
import com.pomanagement.domain.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowRouterTest {

    private PurchaseOrder po(String amount, Category category) {
        return PurchaseOrder.builder()
                .title("Test")
                .amount(new BigDecimal(amount))
                .category(category)
                .status(Status.PENDING_MANAGER_APPROVAL)
                .build();
    }

    // --- Parameterised entry-state matrix: {amount <100, =100, >100} × {IT_EQUIPMENT, non-IT} ---

    static Stream<Arguments> entryStateMatrix() {
        return Stream.of(
                Arguments.of("99.99",  Category.SERVICES,     Status.PENDING_FINANCE_APPROVAL),
                Arguments.of("99.99",  Category.IT_EQUIPMENT, Status.PENDING_IT_VALIDATION),
                Arguments.of("100.00", Category.SERVICES,     Status.PENDING_MANAGER_APPROVAL),
                Arguments.of("100.00", Category.IT_EQUIPMENT, Status.PENDING_MANAGER_APPROVAL),
                Arguments.of("150.00", Category.SERVICES,     Status.PENDING_MANAGER_APPROVAL),
                Arguments.of("150.00", Category.IT_EQUIPMENT, Status.PENDING_MANAGER_APPROVAL)
        );
    }

    @ParameterizedTest(name = "amount={0} cat={1} → {2}")
    @MethodSource("entryStateMatrix")
    void entryState_matrix(String amount, Category category, Status expectedEntry) {
        assertThat(WorkflowRouter.entryState(po(amount, category)))
                .isEqualTo(expectedEntry);
    }

    // --- Entry-state truth table (individual boundary checks) ---

    @Test
    void entryState_belowThreshold_nonIT_goesToFinance() {
        assertThat(WorkflowRouter.entryState(po("99.99", Category.SERVICES)))
                .isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    @Test
    void entryState_belowThreshold_officeSupplies_goesToFinance() {
        assertThat(WorkflowRouter.entryState(po("0.01", Category.OFFICE_SUPPLIES)))
                .isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    @Test
    void entryState_belowThreshold_IT_goesToITValidation() {
        assertThat(WorkflowRouter.entryState(po("50.00", Category.IT_EQUIPMENT)))
                .isEqualTo(Status.PENDING_IT_VALIDATION);
    }

    @Test
    void entryState_exactlyAtThreshold_nonIT_goesToManager() {
        assertThat(WorkflowRouter.entryState(po("100.00", Category.SERVICES)))
                .isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    @Test
    void entryState_exactlyAtThreshold_IT_goesToManager() {
        assertThat(WorkflowRouter.entryState(po("100.00", Category.IT_EQUIPMENT)))
                .isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    @Test
    void entryState_aboveThreshold_IT_goesToManager() {
        assertThat(WorkflowRouter.entryState(po("200.00", Category.IT_EQUIPMENT)))
                .isEqualTo(Status.PENDING_MANAGER_APPROVAL);
    }

    // --- Approve progression ---

    @Test
    void afterManager_nonIT_goesToFinance() {
        assertThat(WorkflowRouter.nextStateAfter(po("150.00", Category.SERVICES), Stage.MANAGER))
                .isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    @Test
    void afterManager_IT_goesToITValidation() {
        assertThat(WorkflowRouter.nextStateAfter(po("150.00", Category.IT_EQUIPMENT), Stage.MANAGER))
                .isEqualTo(Status.PENDING_IT_VALIDATION);
    }

    @Test
    void afterIT_goesToFinance() {
        assertThat(WorkflowRouter.nextStateAfter(po("50.00", Category.IT_EQUIPMENT), Stage.IT))
                .isEqualTo(Status.PENDING_FINANCE_APPROVAL);
    }

    @Test
    void afterFinance_invoiced() {
        assertThat(WorkflowRouter.nextStateAfter(po("100.00", Category.SERVICES), Stage.FINANCE))
                .isEqualTo(Status.INVOICED);
    }

    @Test
    void afterFinance_IT_invoiced() {
        assertThat(WorkflowRouter.nextStateAfter(po("50.00", Category.IT_EQUIPMENT), Stage.FINANCE))
                .isEqualTo(Status.INVOICED);
    }

    // --- stageOf mapping ---

    @Test
    void stageOf_pendingManager() {
        assertThat(WorkflowRouter.stageOf(Status.PENDING_MANAGER_APPROVAL)).isEqualTo(Stage.MANAGER);
    }

    @Test
    void stageOf_pendingIT() {
        assertThat(WorkflowRouter.stageOf(Status.PENDING_IT_VALIDATION)).isEqualTo(Stage.IT);
    }

    @Test
    void stageOf_pendingFinance() {
        assertThat(WorkflowRouter.stageOf(Status.PENDING_FINANCE_APPROVAL)).isEqualTo(Stage.FINANCE);
    }

    @Test
    void stageOf_nonPendingStatus_throws() {
        assertThatThrownBy(() -> WorkflowRouter.stageOf(Status.INVOICED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
