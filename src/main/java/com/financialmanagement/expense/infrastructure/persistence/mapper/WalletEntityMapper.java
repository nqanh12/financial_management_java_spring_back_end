package com.financialmanagement.expense.infrastructure.persistence.mapper;

import com.financialmanagement.expense.application.dto.wallet.UpdateWalletRequest;
import com.financialmanagement.expense.application.dto.wallet.WalletResponse;
import com.financialmanagement.expense.infrastructure.persistence.entity.WalletEntity;
import java.math.BigDecimal;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletEntityMapper {

    @Mapping(
            target = "currentBalance",
            expression = "java(entity.getOpeningBalance().add(netFromTransactions))")
    WalletResponse toResponse(WalletEntity entity, @Context BigDecimal netFromTransactions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateWalletRequest request, @MappingTarget WalletEntity entity);
}
