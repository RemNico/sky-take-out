package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into \n" +
            "    employee(name, username, password, phone, sex, id_number, status , create_time, update_time, create_user, update_user) \n" +
            "values \n" +
            "    (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);

    List<Employee> select(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    @Select("SELECT * from employee where id=#{id}")
    Employee selectById(long id);

    /*@Select("select * from employee where id=#{id}")
    Employee select(Long id);
*/

    //List<Employee> select(EmployeePageQueryDTO employeePageQueryDTO);
}
