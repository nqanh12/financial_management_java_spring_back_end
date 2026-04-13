package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.category.CategoryResponse;
import com.financialmanagement.expense.application.dto.category.CreateCategoryRequest;
import com.financialmanagement.expense.application.dto.category.UpdateCategoryRequest;
import com.financialmanagement.expense.application.service.CategoryService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return categoryService.list(principal.userId());
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        return categoryService.get(principal.userId(), id);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal JwtUserPrincipal principal, @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse c = categoryService.create(principal.userId(), request);
        return ResponseEntity.created(URI.create("/api/v1/categories/" + c.id())).body(c);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(principal.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID id) {
        categoryService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
