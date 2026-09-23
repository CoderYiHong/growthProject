package com.yihong.growth.common;

import lombok.Data;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {
    private long total;
    private long pageSize;
    private long currentPage;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long pageSize, long currentPage, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.pageSize = pageSize;
        r.currentPage = currentPage;
        r.records = records;
        return r;
    }
}
