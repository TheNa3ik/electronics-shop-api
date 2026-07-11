package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.category.CategoryResponse;
import com.thena3ik.shopapi.entity.Category;
import com.thena3ik.shopapi.exception.ResourceConflictException;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.mapper.CategoryMapper;
import com.thena3ik.shopapi.repository.CategoryRepository;
import com.thena3ik.shopapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getCategoryById_whenCategoryExists_returnsResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Resistors");
        category.setDescription("Resistor components");

        CategoryResponse expectedResponse = new CategoryResponse(1L, "Resistors", "Resistor components");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(expectedResponse);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getCategoryById_whenCategoryDoesNotExist_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 99");
    }

    @Test
    void deleteCategoryById_whenNoProductsExists_deletesSuccessfully() {
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategoryById(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategoryById_whenProductsExist_throwsResourceConflictException() {
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategoryById(1L))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Cannot delete category with id: 1");

        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
