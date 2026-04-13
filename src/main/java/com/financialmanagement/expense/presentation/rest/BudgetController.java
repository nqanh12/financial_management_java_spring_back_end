package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.budget.BudgetAlertResponse;
import com.financialmanagement.expense.application.dto.budget.BudgetResponse;
import com.financialmanagement.expense.application.dto.budget.UpsertBudgetRequest;
import com.financialmanagement.expense.application.dto.common.PageResponse;
import com.financialmanagement.expense.application.service.BudgetService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import com.financialmanagement.expense.presentation.support.ApiPaging;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Budgets")
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public Object list(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        if (ApiPaging.isUnpaged(page, size)) {
            return budgetService.list(principal.userId());
        }
        return PageResponse.from(
                budgetService.listPaged(principal.userId(), ApiPaging.budgetPageable(page, size, sort)));
    }

    @PostMapping
    public BudgetResponse upsert(
            @AuthenticationPrincipal JwtUserPrincipal principal, @Valid @RequestBody UpsertBudgetRequest request) {
        return budgetService.upsert(principal.userId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        budgetService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public Object alerts(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        if (ApiPaging.isUnpaged(page, size)) {
            return budgetService.listAlerts(principal.userId());
        }
        return PageResponse.from(
                budgetService.listAlertsPaged(principal.userId(), ApiPaging.alertPageable(page, size, sort)));
    }
}
