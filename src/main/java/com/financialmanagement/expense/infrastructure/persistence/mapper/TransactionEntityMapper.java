package com.financialmanagement.expense.infrastructure.persistence.mapper;

import com.financialmanagement.expense.application.dto.transaction.CreateTransactionRequest;
import com.financialmanagement.expense.application.dto.transaction.TransactionResponse;
import com.financialmanagement.expense.application.dto.transaction.UpdateTransactionRequest;
import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.TransactionEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.WalletEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionEntityMapper {

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "categoryId", source = "category.id")
    TransactionResponse toResponse(TransactionEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallet", source = "wallet")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TransactionEntity toNewEntity(CreateTransactionRequest request, WalletEntity wallet, CategoryEntity category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateTransactionRequest request, @MappingTarget TransactionEntity entity);
}
