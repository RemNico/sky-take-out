package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private WebSocketServer webSocketServer;
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }



        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list==null||list.size()<=0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setAddressBookId(addressBookId);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        List<OrderDetail> details=new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            details.add(orderDetail);
        }

        orderDetailMapper.insertBatch(details);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);


        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map=new HashMap<>();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+outTradeNo);

        String json= JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult pageQuery(int page, int pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(page);
        ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> p=orderMapper.selectpageQuery(ordersPageQueryDTO);

        List<OrderVO> list=new ArrayList<>();
        if(p!=null&&p.size()>0){
            for (Orders orders : p) {
                Long orderId = orders.getId();
                List<OrderDetail> orderDetailList=orderDetailMapper.selectByOderId(orderId);
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(p.getTotal(),list);
    }

    @Override
    public void cancelOrder(Long id) throws Exception {
        Orders ordersDB=orderMapper.selectById(id);

        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(ordersDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal("0.01"),
                    new BigDecimal("0.01"));
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);
    }

    @Override
    public OrderVO select(Long id) {
        Orders orders = orderMapper.selectById(id);
        List<OrderDetail> details = orderDetailMapper.selectByOderId(orders.getId());
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    @Override
    public void repetion(Long id) {
        Orders orders =orderMapper.selectById(id);
        List<OrderDetail> details = orderDetailMapper.selectByOderId(orders.getId());
        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        for (OrderDetail detail : details) {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(detail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    @Override
    public PageResult search(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page=orderMapper.selectpageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList=getOrderVOList(page);
        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public OrderStatisticsVO selectByStatus() {
        Integer confirmedNumber=orderMapper.selectStatus(Orders.CONFIRMED);
        Integer deliveryInProgressNumber=orderMapper.selectStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer  toBeConfirmedNumber=orderMapper.selectStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedNumber);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedNumber);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressNumber);

        return orderStatisticsVO;
    }

    @Override
    public OrderVO selectById(Long id) {
        Orders orders=orderMapper.selectById(id);
        List<OrderDetail> orderDetailList=orderDetailMapper.selectByOderId(orders.getId());
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void confirm(OrdersDTO ordersDTO) {
        Orders orders = Orders.builder()
                .id(ordersDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {

        Orders ordersDB=orderMapper.selectById(ordersRejectionDTO.getId());
        if(ordersDB==null||!Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if(Orders.PAID==ordersDB.getPayStatus()){
            weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
        }


        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders ordersDB=orderMapper.selectById(ordersCancelDTO.getId());
        if(ordersDB==null||!Orders.CANCELLED.equals(ordersDB.getStatus())){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if(Orders.PAID==ordersDB.getPayStatus()){
            weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
        }


        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders ordersDB=orderMapper.selectById(id);
        if(ordersDB==null||ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders= new Orders();
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setId(ordersDB.getId());
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.selectById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders ordersDB = orderMapper.selectById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null ) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map=new HashMap<>();
        map.put("type",2);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+ordersDB.getNumber());

        String json= JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList=new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            String orderDishes=getorderDishes(orders);
            orderVO.setOrderDishes(orderDishes);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    private String getorderDishes(Orders orders) {

        List<OrderDetail> details = orderDetailMapper.selectByOderId(orders.getId());
        List<String> collect = details.stream().map(s -> {
            String orderDish = s.getName() + "*" + s.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("",collect);
    }


}
