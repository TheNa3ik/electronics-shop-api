package com.thena3ik.shopapi.rest;

import com.thena3ik.shopapi.dto.CategoryPatchRequest;
import com.thena3ik.shopapi.dto.CategoryRequest;
import com.thena3ik.shopapi.dto.CategoryResponse;
import com.thena3ik.shopapi.dto.PageResponse;
import com.thena3ik.shopapi.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(Pageable pageable) {
        PageResponse<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<CategoryResponse> addCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse savedCategory = categoryService.saveCategory(request);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long categoryId,
                                                           @Valid @RequestBody CategoryRequest request) {
        CategoryResponse updatedCategory = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> patchCategory (@PathVariable Long categoryId,
                                                           @RequestBody CategoryPatchRequest request) {
        CategoryResponse patchedCategory = categoryService.patchCategory(categoryId, request);
        return ResponseEntity.ok(patchedCategory);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }
}
