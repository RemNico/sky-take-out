package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.impl.CategoryServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCategoryController")
@Slf4j
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询参数：{}",categoryPageQueryDTO);
        PageResult pageResult=categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "变更启用状态")
    public Result status(@PathVariable Integer status,Long id){
        log.info("启用状态变更参数:{},{}",status,id);
        categoryService.status(status,id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询对应的ID")
    public Result select(@PathVariable Integer id){
        log.info("查询的id参数:{}",id);
        Category c= categoryService.select(id);
        return Result.success(c);
    }

    @PutMapping
    @ApiOperation(value = "根据对应的ID修改数据")
    public Result select(@RequestBody CategoryDTO categoryDTO){
        log.info("需要修改的参数:{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping
    @ApiOperation(value = "添加数据")
    public Result insert(@RequestBody CategoryDTO categoryDTO){
        log.info("添加数据的参数:{}",categoryDTO);
        categoryService.insert(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation(value = "删除数据")
    public Result delete(Long id){
        log.info("删除的id参数:{}",id);
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类数据")
    public Result<List<Category>> selectBy(Integer type){
        List<Category> list= categoryService.selectBy(type);
        return Result.success(list);
    }


}
