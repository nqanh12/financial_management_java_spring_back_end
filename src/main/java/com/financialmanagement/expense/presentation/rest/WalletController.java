package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.wallet.CreateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.UpdateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.WalletResponse;
import com.financialmanagement.expense.application.dto.wallet.WalletsOverviewResponse;
import com.financialmanagement.expense.application.service.WalletService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wallets")
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Wallet screen: totals by currency, groups, and per-wallet balances")
    @GetMapping("/overview")
    public WalletsOverviewResponse overview(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return walletService.overview(principal.userId());
    }

    @GetMapping
    public List<WalletResponse> list(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return walletService.list(principal.userId());
    }

    @GetMapping("/{id}")
    public WalletResponse get(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        return walletService.get(principal.userId(), id);
    }

    @Operation(summary = "Create wallet")
    @PostMapping
    public ResponseEntity<WalletResponse> create(
            @AuthenticationPrincipal JwtUserPrincipal principal, @Valid @RequestBody CreateWalletRequest request) {
        WalletResponse w = walletService.create(principal.userId(), request);
        return ResponseEntity.created(URI.create("/api/v1/wallets/" + w.id())).body(w);
    }

    @PutMapping("/{id}")
    public WalletResponse update(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWalletRequest request) {
        return walletService.update(principal.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        walletService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
