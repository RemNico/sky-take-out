package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult pageQuery(int page, int pageSize, Integer status);

    void cancelOrder(Long id) throws Exception;

    OrderVO select(Long id);

    void repetion(Long id);


    PageResult search(OrdersPageQueryDTO ordersPageQueryDTO);


    OrderStatisticsVO selectByStatus();

    OrderVO selectById(Long id);

    void confirm(OrdersDTO ordersDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    void delivery(Long id);

    /**
     * 完成订单
     *
     * @param id
     */
    void complete(Long id);

    void reminder(Long id);
}
