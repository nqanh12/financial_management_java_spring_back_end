package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.wallet.CreateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.UpdateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.WalletResponse;
import com.financialmanagement.expense.application.dto.wallet.WalletsOverviewResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletPort {

    WalletsOverviewResponse overviewForUser(UUID userId);

    List<WalletResponse> listByUser(UUID userId);

    Optional<WalletResponse> findByUserAndId(UUID userId, UUID walletId);

    WalletResponse create(UUID userId, CreateWalletRequest request);

    WalletResponse update(UUID userId, UUID walletId, UpdateWalletRequest request);

    void softDelete(UUID userId, UUID walletId);
}
