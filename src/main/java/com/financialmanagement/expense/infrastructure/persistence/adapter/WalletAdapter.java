package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.wallet.CreateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.UpdateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.WalletCurrencySummary;
import com.financialmanagement.expense.application.dto.wallet.WalletGroupOverview;
import com.financialmanagement.expense.application.dto.wallet.WalletOverviewItem;
import com.financialmanagement.expense.application.dto.wallet.WalletResponse;
import com.financialmanagement.expense.application.dto.wallet.WalletsOverviewResponse;
import com.financialmanagement.expense.application.port.out.WalletPort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.WalletEntity;
import com.financialmanagement.expense.infrastructure.persistence.mapper.WalletEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.repository.TransactionJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.UserJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.WalletJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WalletAdapter implements WalletPort {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final WalletJpaRepository walletJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final WalletEntityMapper walletEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public WalletsOverviewResponse overviewForUser(UUID userId) {
        List<WalletEntity> wallets = walletJpaRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        if (wallets.isEmpty()) {
            return new WalletsOverviewResponse(List.of(), List.of());
        }
        List<UUID> ids = wallets.stream().map(WalletEntity::getId).toList();
        Map<UUID, BigDecimal> netByWallet = new HashMap<>();
        for (TransactionJpaRepository.WalletNetRow row : transactionJpaRepository.sumSignedAmountGroupedByWallet(ids)) {
            netByWallet.put(row.getWalletId(), row.getNetAmount());
        }

        Map<String, List<WalletOverviewItem>> byGroup = new TreeMap<>();
        Map<String, Totals> globalByCurrency = new TreeMap<>();

        for (WalletEntity w : wallets) {
            BigDecimal net = netByWallet.getOrDefault(w.getId(), ZERO);
            BigDecimal opening = w.getOpeningBalance() != null ? w.getOpeningBalance() : ZERO;
            BigDecimal current = opening.add(net);
            String currency = w.getCurrency();
            String gk = walletGroupKey(w);

            globalByCurrency.computeIfAbsent(currency, c -> new Totals()).add(current);

            WalletOverviewItem item = new WalletOverviewItem(
                    w.getId(),
                    w.getName(),
                    currency,
                    current,
                    opening,
                    gk,
                    w.getIconKey(),
                    ONE,
                    w.getCreatedAt());

            byGroup.computeIfAbsent(gk, k -> new ArrayList<>()).add(item);
        }

        for (List<WalletOverviewItem> list : byGroup.values()) {
            list.sort(Comparator.comparing(WalletOverviewItem::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed());
        }

        List<WalletCurrencySummary> summary =
                globalByCurrency.entrySet().stream().map(e -> e.getValue().toSummary(e.getKey())).toList();

        List<WalletGroupOverview> groups = new ArrayList<>();
        for (Map.Entry<String, List<WalletOverviewItem>> e : byGroup.entrySet()) {
            Map<String, Totals> groupTotals = new TreeMap<>();
            for (WalletOverviewItem it : e.getValue()) {
                groupTotals.computeIfAbsent(it.currency(), c -> new Totals()).add(it.currentBalance());
            }
            List<WalletCurrencySummary> gs =
                    groupTotals.entrySet().stream().map(ent -> ent.getValue().toSummary(ent.getKey())).toList();
            groups.add(new WalletGroupOverview(e.getKey(), gs, List.copyOf(e.getValue())));
        }

        return new WalletsOverviewResponse(summary, groups);
    }

    private static String walletGroupKey(WalletEntity w) {
        String gk = w.getGroupKey();
        if (gk == null || gk.isBlank()) {
            return "CASH";
        }
        return gk;
    }

    private static String normalizeGroupKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "CASH";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static final class Totals {
        private BigDecimal assets = ZERO;
        private BigDecimal debts = ZERO;

        void add(BigDecimal balance) {
            int cmp = balance.signum();
            if (cmp > 0) {
                assets = assets.add(balance);
            } else if (cmp < 0) {
                debts = debts.add(balance.negate());
            }
        }

        WalletCurrencySummary toSummary(String currency) {
            return new WalletCurrencySummary(currency, assets, debts, assets.subtract(debts));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> listByUser(UUID userId) {
        List<WalletEntity> wallets = walletJpaRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        if (wallets.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = wallets.stream().map(WalletEntity::getId).toList();
        Map<UUID, BigDecimal> netByWallet = new HashMap<>();
        for (TransactionJpaRepository.WalletNetRow row : transactionJpaRepository.sumSignedAmountGroupedByWallet(ids)) {
            netByWallet.put(row.getWalletId(), row.getNetAmount());
        }
        return wallets.stream()
                .map(w -> walletEntityMapper.toResponse(
                        w, netByWallet.getOrDefault(w.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletResponse> findByUserAndId(UUID userId, UUID walletId) {
        return walletJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(walletId, userId)
                .map(w -> walletEntityMapper.toResponse(
                        w,
                        transactionJpaRepository.sumSignedAmountForWallet(w.getId())));
    }

    @Override
    @Transactional
    public WalletResponse create(UUID userId, CreateWalletRequest request) {
        UserEntity user = userJpaRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        WalletEntity e = new WalletEntity();
        e.setUser(user);
        e.setName(request.name());
        String c = request.currency();
        e.setCurrency(c != null && c.length() == 3 ? c.toUpperCase() : "USD");
        e.setDescription(request.description());
        BigDecimal opening = request.openingBalance() != null ? request.openingBalance() : BigDecimal.ZERO;
        e.setOpeningBalance(opening);
        e.setGroupKey(normalizeGroupKey(request.groupKey()));
        e.setIconKey(blankToNull(request.iconKey()));
        WalletEntity saved = walletJpaRepository.save(e);
        return walletEntityMapper.toResponse(saved, transactionJpaRepository.sumSignedAmountForWallet(saved.getId()));
    }

    @Override
    @Transactional
    public WalletResponse update(UUID userId, UUID walletId, UpdateWalletRequest request) {
        WalletEntity e = walletJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        walletEntityMapper.updateEntity(request, e);
        if (request.currency() != null && request.currency().length() == 3) {
            e.setCurrency(request.currency().toUpperCase());
        }
        if (request.groupKey() != null) {
            e.setGroupKey(normalizeGroupKey(request.groupKey()));
        }
        if (request.iconKey() != null) {
            e.setIconKey(blankToNull(request.iconKey()));
        }
        WalletEntity saved = walletJpaRepository.save(e);
        return walletEntityMapper.toResponse(saved, transactionJpaRepository.sumSignedAmountForWallet(saved.getId()));
    }

    @Override
    @Transactional
    public void softDelete(UUID userId, UUID walletId) {
        WalletEntity e = walletJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        e.softDelete();
        walletJpaRepository.save(e);
    }
}
