package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @hundune~
 * @version1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    //@Configuration
    //@ConfigurationProperties(prefix = "spring.redis")
    //会将 .yml 文件下同名的字段映射到这，可以使用下面的来获得 .yml 中配置的属性
    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
                //.setPassword(password);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
