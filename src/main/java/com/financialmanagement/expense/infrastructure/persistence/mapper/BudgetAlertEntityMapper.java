package com.financialmanagement.expense.infrastructure.persistence.mapper;

import com.financialmanagement.expense.application.dto.budget.BudgetAlertResponse;
import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetAlertEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BudgetAlertEntityMapper {

    @Mapping(target = "categoryId", source = "category.id")
    BudgetAlertResponse toResponse(BudgetAlertEntity entity);
}
