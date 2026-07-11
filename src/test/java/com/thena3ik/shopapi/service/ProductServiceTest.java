package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.product.ProductRequest;
import com.thena3ik.shopapi.dto.product.ProductResponse;
import com.thena3ik.shopapi.entity.Category;
import com.thena3ik.shopapi.entity.Product;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.mapper.ProductMapper;
import com.thena3ik.shopapi.repository.CategoryRepository;
import com.thena3ik.shopapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void saveProduct_whenCategoryExists_savesAndReturnsResponse() {
        ProductRequest request = new ProductRequest(
                "RES-10K", "10K Resistor", "Yageo", "description",
                BigDecimal.valueOf(0.99), 100, 1L
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Resistors");

        Product unsavedProduct = new Product();
        unsavedProduct.setSku("RES-10K");
        unsavedProduct.setName("10K Resistor");

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setSku("RES-10K");
        savedProduct.setName("10K Resistor");
        savedProduct.setCategory(category);

        ProductResponse expectedResponse = new ProductResponse(
                1L, "RES-10K", "10K Resistor", "Yageo", "description",
                BigDecimal.valueOf(0.99), 100, 1L, "Resistors"
        );

        when(productMapper.toEntity(request)).thenReturn(unsavedProduct);
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
        when(productRepository.save(unsavedProduct)).thenReturn(savedProduct);
        when(productMapper.toResponseDto(savedProduct)).thenReturn(expectedResponse);

        ProductResponse result = productService.saveProduct(request);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void saveProduct_whenCategoryDoesNotExist_throwsResourceNotFoundException() {
        ProductRequest request = new ProductRequest(
                "RES-10K", "10K Resistor", "Yageo", "description",
                BigDecimal.valueOf(0.99), 100, 99L
        );

        Product unsavedProduct = new Product();

        when(productMapper.toEntity(request)).thenReturn(unsavedProduct);
        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 99");

        verify(productRepository, never()).save(any(Product.class));
    }
}
