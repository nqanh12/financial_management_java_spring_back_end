package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.category.CreateCategoryRequest;
import com.financialmanagement.expense.application.dto.category.UpdateCategoryRequest;
import com.financialmanagement.expense.application.port.out.CategoryPort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import com.financialmanagement.expense.infrastructure.persistence.entity.CategoryEntity;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import com.financialmanagement.expense.infrastructure.persistence.mapper.CategoryEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.repository.CategoryJpaRepository;
import com.financialmanagement.expense.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryPort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listByUser(UUID userId) {
        return categoryJpaRepository.findByUser_IdAndDeletedAtIsNullOrderByNameAsc(userId).stream()
                .map(categoryEntityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> findByUserAndId(UUID userId, UUID categoryId) {
        return categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .map(categoryEntityMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        UserEntity user = userJpaRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CategoryEntity e = categoryEntityMapper.toNewEntity(request, user);
        return categoryEntityMapper.toResponse(categoryJpaRepository.save(e));
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        CategoryEntity e = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryEntityMapper.updateEntity(request, e);
        return categoryEntityMapper.toResponse(categoryJpaRepository.save(e));
    }

    @Override
    @Transactional
    public void softDelete(UUID userId, UUID categoryId) {
        CategoryEntity e = categoryJpaRepository
                .findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        e.softDelete();
        categoryJpaRepository.save(e);
    }
}
