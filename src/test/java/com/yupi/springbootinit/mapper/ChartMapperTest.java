package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.manager.GenTable;
import com.yupi.springbootinit.model.vo.Col;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @hundune~
 * @version1.0
 */
@SpringBootTest
class ChartMapperTest {
    @Resource
    private  ChartMapper chartMapper;

    @Test
    void createTable() {
//        chartMapper.createTable(GenTable.HG());
    }

    @Test
    void insertTable() {
//        Col.insertCol(chartMapper,GenTable.HG(),222);
    }
}