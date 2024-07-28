package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation(value = "添加菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("添加的参数",dishDTO);
        String key="dish_"+dishDTO.getCategoryId();
        cleanCache(key);
        dishService.insert(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页展示")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页的参数:{}",dishPageQueryDTO);
        PageResult pageResult=dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation(value = "删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除所需要的参数:{}",ids);
        dishService.delete(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "通过ID查询菜品")
    public Result<DishVO> select(@PathVariable Long id){
        DishVO dishVO= dishService.select(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation(value = "修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        dishService.update(dishDTO);
        cleanCache("dish_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改菜品起售状态")
    public Result status(@PathVariable Integer status,Long id){
        dishService.status(status,id);
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void cleanCache(String patten){
        Set keys=redisTemplate.keys(patten);
        redisTemplate.delete(keys);
    }
}
