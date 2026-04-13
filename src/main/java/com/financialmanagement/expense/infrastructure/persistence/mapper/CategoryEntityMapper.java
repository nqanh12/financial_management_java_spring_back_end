package com.financialmanagement.expense.infrastructure.persistence.mapper;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.category.CreateCategoryRequest;
import com.financialmanagement.expense.application.dto.category.UpdateCategoryRequest;
import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryEntityMapper {

    CategoryResponse toResponse(CategoryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    CategoryEntity toNewEntity(CreateCategoryRequest request, UserEntity user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget CategoryEntity entity);
}
