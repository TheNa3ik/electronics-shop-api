package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.common.PageResponse;
import com.thena3ik.shopapi.dto.product.ProductFilterRequest;
import com.thena3ik.shopapi.dto.product.ProductPatchRequest;
import com.thena3ik.shopapi.dto.product.ProductRequest;
import com.thena3ik.shopapi.dto.product.ProductResponse;
import com.thena3ik.shopapi.entity.Category;
import com.thena3ik.shopapi.entity.Product;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.mapper.ProductMapper;
import com.thena3ik.shopapi.repository.CategoryRepository;
import com.thena3ik.shopapi.repository.ProductRepository;
import com.thena3ik.shopapi.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> getAllProducts(ProductFilterRequest filter, Pageable pageable) {
        PredicateSpecification<Product> spec = PredicateSpecification.unrestricted();

        if (filter.brand() != null) spec = spec.and(ProductSpecification.hasBrand(filter.brand()));
        if (filter.categoryId() != null) spec = spec.and(ProductSpecification.hasCategory(filter.categoryId()));
        if (filter.minPrice() != null) spec = spec.and(ProductSpecification.hasPriceMin(filter.minPrice()));
        if (filter.maxPrice() != null) spec = spec.and(ProductSpecification.hasPriceMax(filter.maxPrice()));
        if (filter.inStock() != null && filter.inStock()) spec = spec.and(ProductSpecification.isInStock());
        if (filter.search() != null) spec = spec.and(ProductSpecification.hasSearch(filter.search()));

        Page<Product> page = productRepository.findBy(spec, q -> q.page(pageable));

        List<ProductResponse> content = page.getContent()
                .stream()
                .map(productMapper::toResponseDto)
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

    public ProductResponse getProductById(Long id) {
        return productRepository
                .findById(id)
                .map(productMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromDto(request, existingProduct);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        existingProduct.setCategory(category);

        return productMapper.toResponseDto(productRepository.save(existingProduct));
    }

    @Transactional
    public ProductResponse patchProduct(Long id, ProductPatchRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.name() != null) existingProduct.setName(request.name());
        if (request.brand() != null) existingProduct.setBrand(request.brand());
        if (request.description() != null) existingProduct.setDescription(request.description());
        if (request.price() != null) existingProduct.setPrice(request.price());
        if (request.stockQuantity() != null) existingProduct.setStockQuantity(request.stockQuantity());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

            existingProduct.setCategory(category);
        }

        return productMapper.toResponseDto(productRepository.save(existingProduct));
    }

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Product unsavedProduct = productMapper.toEntity(request);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        unsavedProduct.setCategory(category);

        Product savedProduct = productRepository.save(unsavedProduct);
        return productMapper.toResponseDto(savedProduct);
    }

    @Transactional
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
}
