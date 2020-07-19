package com.zitai.ms.dao;

import com.zitai.ms.entity.User;

public interface UserDao {
    User selectByPrimaryKey(Integer id);
}
