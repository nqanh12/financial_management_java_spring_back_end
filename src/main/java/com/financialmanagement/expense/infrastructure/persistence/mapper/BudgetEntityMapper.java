package com.financialmanagement.expense.infrastructure.persistence.mapper;

import com.financialmanagement.expense.application.dto.budget.BudgetResponse;
import com.financialmanagement.expense.infrastructure.persistence.entity.BudgetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BudgetEntityMapper {

    @Mapping(target = "categoryId", source = "category.id")
    BudgetResponse toResponse(BudgetEntity entity);
}
