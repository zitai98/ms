package com.zitai.ms.service;


import com.zitai.ms.dao.OrderDao;
import com.zitai.ms.dao.StockDao;
import com.zitai.ms.dao.UserDao;
import com.zitai.ms.entity.CacheKey;
import com.zitai.ms.entity.Order;
import com.zitai.ms.entity.Stock;
import com.zitai.ms.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{
    @Autowired
    private StockDao stockDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public  int kill(Integer id) {
        //校验redis商品是否在秒杀时间中
        if (!stringRedisTemplate.hasKey("kill"+id)) {
            throw new RuntimeException("当前商品的抢购活动已经结束！");
        }
        //校验库存
        Stock stock = checkStock(id);
        //更新库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) {
        //校验redis商品是否在秒杀时间中
        if (!stringRedisTemplate.hasKey("kill"+sid)) {
            throw new RuntimeException("当前商品的抢购活动已经结束！");
        }

        // 验证hash值合法性
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        String verifyHashInRedis = stringRedisTemplate.opsForValue().get(hashKey);
        if (!verifyHash.equals(verifyHashInRedis)) {
            throw new RuntimeException("hash值与Redis中不符合");
        }
        log.info("验证hash值合法性成功");

        // 检查用户合法性
        User user = userDao.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        log.info("用户信息验证成功：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockDao.checkStock(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        log.info("商品信息验证成功：[{}]", stock.toString());

        //更新库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);

    }



    //校验库存
    private Stock checkStock(Integer id){
        Stock stock = stockDao.checkStock(id);
        if(stock.getSale().equals(stock.getCount())){
            throw  new RuntimeException("库存不足!!!");
        }
        return stock;
    }

    //扣除库存
    private void updateSale(Stock stock){
        //在sql层面完成销量的+1  和 版本号的+  并且根据商品id和版本号同时查询更新的商品
        int updateRows = stockDao.updateSale(stock);
        if (updateRows==0){
            throw new RuntimeException("请购失败,请重试!!!");
        }
    }

    //创建订单
    private Integer createOrder(Stock stock){
        Order order = new Order();
        order.setSid(stock.getId()).setName(stock.getName()).setCreateDate(new Date());
        orderDao.createOrder(order);
        return order.getId();
    }
}
