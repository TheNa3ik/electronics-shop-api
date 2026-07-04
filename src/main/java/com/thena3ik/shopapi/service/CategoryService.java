package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.*;
import com.thena3ik.shopapi.entity.Category;
import com.thena3ik.shopapi.exception.ResourceConflictException;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.mapper.CategoryMapper;
import com.thena3ik.shopapi.repository.CategoryRepository;
import com.thena3ik.shopapi.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public PageResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);

        List<CategoryResponse> content = page.getContent()
                .stream()
                .map(categoryMapper::toResponseDto)
                .toList();

        return new PageResponse<> (
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .map(categoryMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        existingCategory.setName(request.name());
        existingCategory.setDescription(request.description());

        return categoryMapper.toResponseDto(categoryRepository.save(existingCategory));
    }

    @Transactional
    public CategoryResponse patchCategory(Long id, CategoryPatchRequest request) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.name() != null) existingCategory.setName(request.name());
        if (request.description() != null) existingCategory.setDescription(request.description());

        return categoryMapper.toResponseDto(categoryRepository.save(existingCategory));
    }

    @Transactional
    public CategoryResponse saveCategory(CategoryRequest request) {
        Category unsavedEntity = categoryMapper.toEntity(request);
        Category savedEntity = categoryRepository.save(unsavedEntity);

        return categoryMapper.toResponseDto(savedEntity);
    }

    @Transactional
    public void deleteCategoryById(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new ResourceConflictException(
                    "Cannot delete category with id: " + id + " because it has existing products");
        }
        categoryRepository.deleteById(id);
    }
}
