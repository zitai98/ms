package com.zitai.ms.dao;


import com.zitai.ms.entity.Stock;

public interface  StockDao {

    //根据商品id查询库存信息的方法
    Stock checkStock(Integer id);

    //根据商品id扣除库存
    int updateSale(Stock stock);

}