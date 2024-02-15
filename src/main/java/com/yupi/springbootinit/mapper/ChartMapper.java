package com.yupi.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author 86182
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-12-23 20:41:15
* @Entity com.yupi.springbootinit.model.entity.Chart
*/

public interface ChartMapper extends BaseMapper<Chart> {
    boolean createTable(Map<String, Object> map);

    Integer insertTable(Map<String, Object> map);
}




