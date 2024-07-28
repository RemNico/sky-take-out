package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/admin/order")
@RequestMapping("adminOrderController")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @GetMapping("/conditionSearch")
    public Result<PageResult> search(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult=orderService.search(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> selectByStatus(){
        OrderStatisticsVO orderStatisticsVO=orderService.selectByStatus();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> select(@PathVariable("id") Long id){
        OrderVO orderVO=orderService.selectById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersDTO ordersDTO){
        orderService.confirm(ordersDTO);
        return  Result.success();
    }

    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return  Result.success();
    }

    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO OrdersCancelDTO) throws Exception {
        orderService.cancel(OrdersCancelDTO);
        return  Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable("id") Long id) throws Exception {
        orderService.delivery(id);
        return  Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
