package com.thena3ik.shopapi.mapper;

import com.thena3ik.shopapi.dto.CategoryRequest;
import com.thena3ik.shopapi.dto.CategoryResponse;
import com.thena3ik.shopapi.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryResponse toResponseDto(Category category);

    Category toEntity(CategoryRequest requestDto);
}
