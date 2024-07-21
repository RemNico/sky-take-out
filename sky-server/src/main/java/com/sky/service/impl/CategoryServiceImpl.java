package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        /*PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());*/
        Page<Category> page=categoryMapper.selectLimit(categoryPageQueryDTO);
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void status(Integer status, Long id) {
        Category category=new Category();
        category.setStatus(status);
        category.setId(id);
        categoryMapper.update(category);
    }

    @Override
    public Category select(Integer id) {
        Category category =categoryMapper.selectById(id);
        return category;
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category c=new Category();
        BeanUtils.copyProperties(categoryDTO,c);
        categoryMapper.update(c);
    }

    @Override
    public void insert(CategoryDTO categoryDTO) {
        Category c=new Category();
        BeanUtils.copyProperties(categoryDTO,c);
        categoryMapper.insert(c);
    }

    @Override
    public void delete(Long id) {
        categoryMapper.delete(id);
    }

    @Override
    public List<Category> selectBy(Integer type) {
        return categoryMapper.list(type);
    }


}
