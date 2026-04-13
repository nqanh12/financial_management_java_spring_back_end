package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.wallet.CreateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.UpdateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.WalletResponse;
import com.financialmanagement.expense.application.dto.wallet.WalletsOverviewResponse;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import com.financialmanagement.expense.application.port.out.WalletPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletPort walletPort;
    private final ReportingCachePort reportingCachePort;

    @Transactional(readOnly = true)
    public WalletsOverviewResponse overview(UUID userId) {
        return walletPort.overviewForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> list(UUID userId) {
        return walletPort.listByUser(userId);
    }

    @Transactional(readOnly = true)
    public WalletResponse get(UUID userId, UUID walletId) {
        return walletPort
                .findByUserAndId(userId, walletId)
                .orElseThrow(() -> new com.financialmanagement.expense.domain.exception.ResourceNotFoundException(
                        "Wallet not found"));
    }

    @Transactional
    public WalletResponse create(UUID userId, CreateWalletRequest request) {
        WalletResponse w = walletPort.create(userId, request);
        reportingCachePort.evictUserReports(userId);
        return w;
    }

    @Transactional
    public WalletResponse update(UUID userId, UUID walletId, UpdateWalletRequest request) {
        WalletResponse w = walletPort.update(userId, walletId, request);
        reportingCachePort.evictUserReports(userId);
        return w;
    }

    @Transactional
    public void delete(UUID userId, UUID walletId) {
        walletPort.softDelete(userId, walletId);
        reportingCachePort.evictUserReports(userId);
    }
}
