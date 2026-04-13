package com.financialmanagement.expense.application.port.out;

import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.dto.transaction.TransactionResponse;
import com.financialmanagement.expense.application.dto.transaction.UpdateTransactionRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionPort {

    List<TransactionResponse> listByUser(UUID userId);

    Page<TransactionResponse> listByUserPaged(UUID userId, Pageable pageable);

    Optional<TransactionResponse> findByUserAndId(UUID userId, UUID transactionId);

    TransactionResponse create(UUID userId, CreateTransactionRequest request);

    TransactionResponse update(UUID userId, UUID transactionId, UpdateTransactionRequest request);

    void softDelete(UUID userId, UUID transactionId);
}
