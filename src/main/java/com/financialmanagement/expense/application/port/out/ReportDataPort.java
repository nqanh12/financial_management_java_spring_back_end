package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.report.CategoryBreakdownRow;
import com.financialmanagement.expense.application.dto.report.DailyAggregateRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportDataPort {

    BigDecimal sumByDirection(UUID userId, LocalDate fromInclusive, LocalDate toInclusive, boolean income);

    List<CategoryBreakdownRow> breakdownByCategory(UUID userId, LocalDate fromInclusive, LocalDate toInclusive);

    List<DailyAggregateRow> dailyAggregates(UUID userId, LocalDate fromInclusive, LocalDate toInclusive);
}
