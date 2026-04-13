package com.financialmanagement.expense.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EqualSplitCalculatorTest {

    @Test
    void equalSplitDistributesRemainderToLast() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Map<UUID, BigDecimal> m = EqualSplitCalculator.equalSplit(List.of(a, b), new BigDecimal("10.00"));
        assertThat(m.get(a).add(m.get(b))).isEqualByComparingTo("10.00");
    }

    @Test
    void equalSplitSingleMemberGetsFullAmount() {
        UUID a = UUID.randomUUID();
        Map<UUID, BigDecimal> m = EqualSplitCalculator.equalSplit(List.of(a), new BigDecimal("7.50"));
        assertThat(m.get(a)).isEqualByComparingTo("7.50");
    }

    @Test
    void rejectsEmptyMembers() {
        assertThatThrownBy(() -> EqualSplitCalculator.equalSplit(List.of(), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
