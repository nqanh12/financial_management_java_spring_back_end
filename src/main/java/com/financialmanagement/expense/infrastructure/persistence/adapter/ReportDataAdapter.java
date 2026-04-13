package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.report.CategoryBreakdownRow;
import com.financialmanagement.expense.application.dto.report.DailyAggregateRow;
import com.financialmanagement.expense.application.port.out.ReportDataPort;
import com.financialmanagement.expense.domain.model.TransactionDirection;
import com.financialmanagement.expense.infrastructure.persistence.repository.TransactionJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReportDataAdapter implements ReportDataPort {

    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumByDirection(UUID userId, LocalDate fromInclusive, LocalDate toInclusive, boolean income) {
        TransactionDirection d = income ? TransactionDirection.IN : TransactionDirection.OUT;
        return transactionJpaRepository.sumByUserAndDateRangeAndDirection(userId, fromInclusive, toInclusive, d);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryBreakdownRow> breakdownByCategory(UUID userId, LocalDate fromInclusive, LocalDate toInclusive) {
        return transactionJpaRepository.breakdownByCategory(userId, fromInclusive, toInclusive).stream()
                .map(p -> new CategoryBreakdownRow(
                        p.getCategoryId(), p.getCategoryName(), p.getTotalIncome(), p.getTotalExpense()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAggregateRow> dailyAggregates(UUID userId, LocalDate fromInclusive, LocalDate toInclusive) {
        return transactionJpaRepository.dailyAggregates(userId, fromInclusive, toInclusive).stream()
                .map(p -> new DailyAggregateRow(p.getDay(), p.getIncome(), p.getExpense()))
                .toList();
    }
}
