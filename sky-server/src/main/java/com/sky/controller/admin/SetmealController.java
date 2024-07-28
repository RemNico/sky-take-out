package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealService")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache",key="#setmealDTO.categoryId")
    public Result insert(@RequestBody SetmealDTO setmealDTO){
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult pageResult=setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        setmealService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> select(@PathVariable Long id){
        SetmealVO setmealDTO=setmealService.select(id);
        return Result.success(setmealDTO);
    }

    @PutMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping ("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result status(@PathVariable Integer status,Long id){
        log.info("参数:{},{}",status,id);
        setmealService.status(status,id);
        return Result.success();
    }
}
