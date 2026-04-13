package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.dto.transaction.TransactionResponse;
import com.financialmanagement.expense.application.dto.transaction.UpdateTransactionRequest;
import com.financialmanagement.expense.application.port.out.TransactionPort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.TransactionEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.WalletEntity;
import com.financialmanagement.expense.infrastructure.persistence.mapper.TransactionEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.repository.CategoryJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.TransactionJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.WalletJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TransactionAdapter implements TransactionPort {

    private final TransactionJpaRepository transactionJpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final TransactionEntityMapper transactionEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> listByUser(UUID userId) {
        return transactionJpaRepository
                .findByWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNullOrderByTransactionDateDesc(userId)
                .stream()
                .map(transactionEntityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> listByUserPaged(UUID userId, Pageable pageable) {
        return transactionJpaRepository
                .findByWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(userId, pageable)
                .map(transactionEntityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findByUserAndId(UUID userId, UUID transactionId) {
        return transactionJpaRepository
                .findByIdAndWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(transactionId, userId)
                .map(transactionEntityMapper::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        WalletEntity wallet = walletJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.walletId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        CategoryEntity category = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        TransactionEntity e = transactionEntityMapper.toNewEntity(request, wallet, category);
        return transactionEntityMapper.toResponse(transactionJpaRepository.save(e));
    }

    @Override
    @Transactional
    public TransactionResponse update(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        TransactionEntity e = transactionJpaRepository
                .findByIdAndWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        WalletEntity wallet = walletJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.walletId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        CategoryEntity category = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        transactionEntityMapper.updateEntity(request, e);
        e.setWallet(wallet);
        e.setCategory(category);
        return transactionEntityMapper.toResponse(transactionJpaRepository.save(e));
    }

    @Override
    @Transactional
    public void softDelete(UUID userId, UUID transactionId) {
        TransactionEntity e = transactionJpaRepository
                .findByIdAndWallet_User_IdAndDeletedAtIsNullAndWallet_DeletedAtIsNull(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        e.softDelete();
        transactionJpaRepository.save(e);
    }
}
