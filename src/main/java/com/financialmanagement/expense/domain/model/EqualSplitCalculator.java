package com.financialmanagement.expense.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EqualSplitCalculator {

    private EqualSplitCalculator() {}

    /**
     * Splits {@code total} equally across {@code memberIds} (stable order). Per-share amount uses rounding down; the
     * last member receives the remainder so the parts sum to {@code total}.
     */
    public static Map<UUID, BigDecimal> equalSplit(List<UUID> memberIds, BigDecimal total) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new IllegalArgumentException("Members required");
        }
        int n = memberIds.size();
        BigDecimal each = total.divide(BigDecimal.valueOf(n), 4, RoundingMode.DOWN);
        Map<UUID, BigDecimal> map = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            UUID id = memberIds.get(i);
            BigDecimal share = each;
            if (i == n - 1) {
                share = total.subtract(allocated);
            }
            map.put(id, share);
            allocated = allocated.add(share);
        }
        return map;
    }

    public static List<UUID> mergeUnique(List<UUID> ordered) {
        List<UUID> out = new ArrayList<>();
        for (UUID id : ordered) {
            if (!out.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }
}
