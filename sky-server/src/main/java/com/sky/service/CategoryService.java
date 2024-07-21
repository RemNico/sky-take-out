package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void status(Integer status, Long id);

    Category select(Integer id);

    void update(CategoryDTO categoryDTO);

    void insert(CategoryDTO categoryDTO);

    void delete(Long id);


    List<Category> selectBy(Integer type);
}
