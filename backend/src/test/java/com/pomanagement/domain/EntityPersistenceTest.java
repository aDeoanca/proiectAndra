package com.pomanagement.domain;

import com.pomanagement.config.JpaConfig;
import com.pomanagement.domain.entity.PoHistory;
import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/pomanagement",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.flyway.enabled=true"
})
class EntityPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void persistsUserPurchaseOrderAndHistory() {
        User user = em.persist(User.builder()
                .name("Test User")
                .email("testuser@test.com")
                .role(Role.CREATOR)
                .build());

        PurchaseOrder po = em.persist(PurchaseOrder.builder()
                .title("Test PO")
                .amount(new BigDecimal("50.00"))
                .category(Category.SERVICES)
                .status(Status.PENDING_FINANCE_APPROVAL)
                .creator(user)
                .build());

        PoHistory history = em.persist(PoHistory.builder()
                .po(po)
                .actor(user)
                .action(HistoryAction.SUBMIT)
                .toStatus(Status.PENDING_FINANCE_APPROVAL)
                .build());

        em.flush();
        em.clear();

        assertThat(em.find(User.class, user.getId())).isNotNull();
        assertThat(em.find(PurchaseOrder.class, po.getId())).isNotNull();
        assertThat(em.find(PoHistory.class, history.getId())).isNotNull();
    }

    @Test
    void statusHasExactlyFiveValues() {
        assertThat(Status.values()).hasSize(5);
    }

    @Test
    void categoryAndRoleMatchDesign() {
        assertThat(Category.values()).containsExactlyInAnyOrder(
                Category.SERVICES, Category.OFFICE_SUPPLIES, Category.IT_EQUIPMENT);
        assertThat(Role.values()).containsExactlyInAnyOrder(
                Role.CREATOR, Role.MANAGER, Role.IT_REP, Role.FINANCE);
    }

    @Test
    void amountIsBigDecimal() throws NoSuchFieldException {
        var field = PurchaseOrder.class.getDeclaredField("amount");
        assertThat(field.getType()).isEqualTo(BigDecimal.class);
    }
}
