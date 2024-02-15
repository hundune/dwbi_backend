package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @hundune~
 * @version1.0
 */
@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimiter() {
        String userId = "1";
        for(int i = 0; i < 5;i++){
            redisLimiterManager.doRateLimiter(userId);
            System.out.println("成功");
        }
    }
}