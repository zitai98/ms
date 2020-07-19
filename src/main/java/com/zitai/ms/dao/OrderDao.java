package com.zitai.ms.dao;


import com.zitai.ms.entity.Order;

public interface OrderDao {
    //创建订单方法
    void createOrder(Order order);
}
