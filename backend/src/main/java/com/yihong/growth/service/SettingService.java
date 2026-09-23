package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.entity.SiteSetting;
import com.yihong.growth.mapper.SiteSettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SiteSettingMapper mapper;

    public Map<String, String> getAll() {
        List<SiteSetting> list = mapper.selectList(null);
        Map<String, String> map = new LinkedHashMap<>();
        for (SiteSetting s : list) map.put(s.getSettingKey(), s.getSettingValue());
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAll(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            SiteSetting exist = mapper.selectOne(
                new LambdaQueryWrapper<SiteSetting>().eq(SiteSetting::getSettingKey, entry.getKey()));
            if (exist != null) {
                exist.setSettingValue(entry.getValue());
                mapper.updateById(exist);
            } else {
                SiteSetting s = new SiteSetting();
                s.setSettingKey(entry.getKey());
                s.setSettingValue(entry.getValue());
                mapper.insert(s);
            }
        }
    }

    /** 仅当 value 不为 null 且不为空时写入，用于部分更新 */
    public void putIfPresent(String key, String value) {
        if (value == null || value.isBlank()) return;
        SiteSetting exist = mapper.selectOne(
            new LambdaQueryWrapper<SiteSetting>().eq(SiteSetting::getSettingKey, key));
        if (exist != null) {
            exist.setSettingValue(value);
            mapper.updateById(exist);
        } else {
            SiteSetting s = new SiteSetting();
            s.setSettingKey(key);
            s.setSettingValue(value);
            mapper.insert(s);
        }
    }
}
