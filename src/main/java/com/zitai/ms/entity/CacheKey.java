package com.zitai.ms.entity;

public enum CacheKey {
    HASH_KEY("miaosha_hash"),
    STOCK_COUNT("miaosha_v1_stock_count"),
    LIMIT_KEY("miaosha_limit");

    private String key;


    private CacheKey(String key){
        this.key = key;
    }
    public String getKey() {
        return key;
    }
}
