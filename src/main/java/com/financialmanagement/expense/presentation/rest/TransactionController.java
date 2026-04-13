package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.common.PageResponse;
import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.dto.transaction.TransactionResponse;
import com.financialmanagement.expense.application.dto.transaction.UpdateTransactionRequest;
import com.financialmanagement.expense.application.service.TransactionService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import com.financialmanagement.expense.presentation.support.ApiPaging;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Transactions")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public Object list(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        if (ApiPaging.isUnpaged(page, size)) {
            return transactionService.list(principal.userId());
        }
        return PageResponse.from(
                transactionService.listPaged(principal.userId(), ApiPaging.transactionPageable(page, size, sort)));
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        return transactionService.get(principal.userId(), id);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse t = transactionService.create(principal.userId(), request);
        return ResponseEntity.created(URI.create("/api/v1/transactions/" + t.id())).body(t);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.update(principal.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        transactionService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
