package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    List<Long> SelectMealId(List<Long> ids);


    void insertBatch(List<SetmealDish> setmealDishes);

    @Delete("delete from setmeal_dish where setmeal_id=#{setmealId}")
    void deleteBySetmealId(Long setmealId);
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> seletBySetmealId(Long id);


    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /*@Select("select * from setmeal_dish where setmeal_id=#{id}")
    SetmealDish seletBySetmealId(Long id);*/
}
