package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Credit;

/**
* @author 86182
* @description 针对表【credit(积分表)】的数据库操作Service
* @createDate 2024-02-19 14:49:05
*/
public interface CreditService extends IService<Credit> {
    /**
     * 根据 当前用户ID 获取积分总数
     * @param userId
     * @return
     */
    Long getCreditTotal(Long userId);

    /**
     * 每日签到
     * @param userId
     * @return
     */
    Boolean signUser(Long userId);

    /**
     * 更新积分（内部方法） 正数为增加积分，负数为消耗积分
     * @param userId
     * @param credits
     * @return
     */
    Boolean updateCredits(Long userId,long credits);
}
