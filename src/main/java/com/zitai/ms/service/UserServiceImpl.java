package com.zitai.ms.service;

import com.zitai.ms.dao.StockDao;
import com.zitai.ms.dao.UserDao;
import com.zitai.ms.entity.CacheKey;
import com.zitai.ms.entity.Stock;
import com.zitai.ms.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

import static com.sun.javafx.font.FontResource.SALT;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StockDao stockDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public String getVerifyHash(Integer sid, Integer userId) {
        //校验redis商品是否在秒杀时间中

        if (!stringRedisTemplate.hasKey("kill"+sid)) {
            throw new RuntimeException("当前商品的抢购活动已经结束！");
        }

        // 检查用户合法性
        User user = userDao.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        LOGGER.info("用户信息：[{}]", user.toString());

        // 检查商品合法性
        Stock stock = stockDao.checkStock(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        LOGGER.info("商品信息：[{}]"+stock.toString());

        // 生成hash
        String verify = SALT + sid + userId+"";
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

        //将hash和用户商品信息存入redis
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        stringRedisTemplate.opsForValue().set(hashKey, verifyHash, 3600, TimeUnit.SECONDS);
        LOGGER.info("Redis写入：[{}] [{}]", hashKey, verifyHash);

        return verifyHash;

    }

    @Override
    public int addUserCount(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum == null) {
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            LOGGER.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > 10;
    }
}
