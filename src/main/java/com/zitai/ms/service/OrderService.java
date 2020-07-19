package com.zitai.ms.service;

public interface OrderService {
    //用来处理秒杀的下单方法 并返回订单id
    int kill(Integer id);

    int createVerifiedOrder(Integer sid, Integer userId, String verifyHash);
}
