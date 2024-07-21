package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper

public interface CategoryMapper {


    Page<Category> selectLimit(CategoryPageQueryDTO categoryPageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    @Select("select * from category where id=#{id}")
    Category selectById(Integer id);

    @AutoFill(OperationType.INSERT)
    @Insert("insert into" +
            "    category(type, name, sort, status, create_time, update_time, create_user, update_user) \n" +
            "values" +
            "    (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser});")
    void insert(Category c);

    @Delete("delete from category where id={id}")
    void delete(Long id);


    List<Category> list(Integer type);
}
