package com.thena3ik.shopapi.mapper;

import com.thena3ik.shopapi.dto.product.ProductRequest;
import com.thena3ik.shopapi.dto.product.ProductResponse;
import com.thena3ik.shopapi.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponseDto(Product Product);

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest requestDto);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ProductRequest request, @MappingTarget Product existingProduct);
}
