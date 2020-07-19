package com.zitai.ms.service;

import com.zitai.ms.controller.OrderController;
import com.zitai.ms.dao.StockDao;
import com.zitai.ms.entity.CacheKey;
import com.zitai.ms.entity.Stock;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class StockServiceImpl implements  StockService{

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Override
    public int getCountByDB(int sid) {
        Stock stock = stockDao.checkStock(sid);

        return stock.getCount()-stock.getSale();
    }

    @Override
    public Integer getCountByRedis(int sid) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + sid;
        String countStr = stringRedisTemplate.opsForValue().get(hashKey);
        int count = 0;
        if(countStr == null){
            LOGGER.info("缓存未命中，查询数据库，并写入缓存");
            count = getCountByDB(sid);
            countStr = String.valueOf(count);
            LOGGER.info("写入商品库存缓存: [{}] [{}]", hashKey, countStr);
            stringRedisTemplate.opsForValue().set(hashKey, countStr, 3600, TimeUnit.SECONDS);
            return count;
        }
        count = Integer.parseInt(countStr);
        return count;
    }

    @Override
    public void delStockCountCache(int sid) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + sid;
        stringRedisTemplate.delete(hashKey);
        LOGGER.info("删除商品id：[{}] 缓存", sid);
    }
}
