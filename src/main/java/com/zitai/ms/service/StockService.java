package com.zitai.ms.service;

public interface StockService {
    int getCountByDB(int sid);

    Integer getCountByRedis(int sid);

    /**
     * 删除库存缓存
     * @param id
     */
    void delStockCountCache(int sid);


}
