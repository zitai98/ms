package com.zitai.ms.service;

public interface UserService {
    String getVerifyHash(Integer sid, Integer userId);

    int addUserCount(Integer userId);

    boolean getUserIsBanned(Integer userId);
}
