package com.financialmanagement.expense.presentation.support;

import com.financialmanagement.expense.domain.exception.BusinessRuleException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class ApiPaging {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private ApiPaging() {}

    public static boolean isUnpaged(Integer page, Integer size) {
        return page == null && size == null;
    }

    public static Pageable transactionPageable(Integer page, Integer size, String sortParam) {
        int p = page != null ? page : 0;
        int s = size != null ? size : DEFAULT_SIZE;
        if (p < 0) {
            throw new BusinessRuleException("page must be >= 0");
        }
        if (s < 1 || s > MAX_SIZE) {
            throw new BusinessRuleException("size must be between 1 and " + MAX_SIZE);
        }
        Sort sort = parseTransactionSort(sortParam);
        return PageRequest.of(p, s, sort);
    }

    public static Pageable budgetPageable(Integer page, Integer size, String sortParam) {
        int p = page != null ? page : 0;
        int s = size != null ? size : DEFAULT_SIZE;
        if (p < 0) {
            throw new BusinessRuleException("page must be >= 0");
        }
        if (s < 1 || s > MAX_SIZE) {
            throw new BusinessRuleException("size must be between 1 and " + MAX_SIZE);
        }
        Sort sort = parseBudgetSort(sortParam);
        return PageRequest.of(p, s, sort);
    }

    public static Pageable alertPageable(Integer page, Integer size, String sortParam) {
        int p = page != null ? page : 0;
        int s = size != null ? size : DEFAULT_SIZE;
        if (p < 0) {
            throw new BusinessRuleException("page must be >= 0");
        }
        if (s < 1 || s > MAX_SIZE) {
            throw new BusinessRuleException("size must be between 1 and " + MAX_SIZE);
        }
        Sort sort = parseAlertSort(sortParam);
        return PageRequest.of(p, s, sort);
    }

    private static Sort parseTransactionSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "transactionDate");
        }
        String[] parts = sortParam.split(",");
        if (parts.length > 2) {
            throw new BusinessRuleException("sort must be like field or field,direction");
        }
        String field = parts[0].trim();
        if (!field.equals("transactionDate") && !field.equals("amount")) {
            throw new BusinessRuleException("sort field must be transactionDate or amount");
        }
        Sort.Direction dir = Sort.Direction.DESC;
        if (parts.length == 2) {
            try {
                dir = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("sort direction must be ASC or DESC");
            }
        }
        return Sort.by(dir, field);
    }

    private static Sort parseBudgetSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "yearMonth");
        }
        String[] parts = sortParam.split(",");
        if (parts.length > 2) {
            throw new BusinessRuleException("sort must be like field or field,direction");
        }
        String field = parts[0].trim();
        if (!field.equals("yearMonth") && !field.equals("limitAmount")) {
            throw new BusinessRuleException("sort field must be yearMonth or limitAmount");
        }
        Sort.Direction dir = Sort.Direction.DESC;
        if (parts.length == 2) {
            try {
                dir = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("sort direction must be ASC or DESC");
            }
        }
        return Sort.by(dir, field);
    }

    private static Sort parseAlertSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",");
        if (parts.length > 2) {
            throw new BusinessRuleException("sort must be like field or field,direction");
        }
        String field = parts[0].trim();
        if (!field.equals("createdAt") && !field.equals("yearMonth")) {
            throw new BusinessRuleException("sort field must be createdAt or yearMonth");
        }
        Sort.Direction dir = Sort.Direction.DESC;
        if (parts.length == 2) {
            try {
                dir = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("sort direction must be ASC or DESC");
            }
        }
        return Sort.by(dir, field);
    }
}
