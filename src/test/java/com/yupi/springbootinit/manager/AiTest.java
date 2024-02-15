package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;

/**
 * @hundune~
 * @version1.0
 */
@SpringBootTest
class AiTest {
    @Resource
    private AiManager aiManager;
    @Test
    void doChat() {
        aiManager.doAnalysis("分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30");
    }
}
