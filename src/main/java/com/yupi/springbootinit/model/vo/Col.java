package com.yupi.springbootinit.model.vo;

import com.yupi.springbootinit.mapper.ChartMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @hundune~
 * @version1.0
 */
@Service
public class Col {
    public static Map<String, Object> genColum(List<Map<Integer, String>> list, Long id) {
        //获得列名
        Map<Integer, String> integerStringMap = list.get(0);
        Map<String, Object> map = new HashMap<>();
        String chart_name = "chart_" + id;
        List<String> tempList = new ArrayList<>();
        for (Map.Entry entry : integerStringMap.entrySet()) {
            String value = null;
            if (entry.getValue() != null) {
                value = (String) entry.getValue();
                tempList.add(value);
            }
        }
        map.put("tablecode", chart_name);
        map.put("tableParams", tempList);
        return map;
    }
    public static void insertCol(ChartMapper chartMapper,List<Map<Integer, String>> list, Long id) {
        Col col = new Col();
        Map<String, Object> map = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        String chart_name = "chart_" + id;
        map.put("tablecode", chart_name);
        int size = list.size();

        Map<Integer, String> integerStringMap = list.get(0);
        for (Map.Entry entry : integerStringMap.entrySet()) {
            String value = null;
            if (entry.getValue() != null) {
                value = (String) entry.getValue();
                list1.add(value);
            }
        }
        map.put("tableParams", list1);
        for (int i = 1; i < size; i++) {
            list2.clear();
            Map<Integer, String> integerMap = list.get(i);
            for (Map.Entry entry : integerMap.entrySet()) {
                String value = null;
                if (entry.getValue() != null) {
                    value = (String) entry.getValue();
                    list2.add(value);
                }
            }
            map.put("tableParamsValue", list2);
            chartMapper.insertTable(map);
        }
    }
}
