package com.rdn.prompt.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    // 当前页码
    private int pageNum;

    // 每页记录数
    private int pageSize;

    // 总记录数
    private long total;

    // 总页数
    private int totalPages;

    // 当前页数据
    private List<T> list;

    public PageResult() {
        this.pageNum = 0;
        this.pageSize = 0;
        this.total = 0;
        this.list = null;
        this.totalPages = 0;
    }

    public PageResult(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        this.totalPages = calculatePages();
    }

    // 计算总页数
    private int calculatePages() {
        if (pageSize == 0) {
            return 0;
        }
        return (int) ((total + pageSize - 1) / pageSize);
    }

    // 判断是否有上一页
    public boolean hasPreviousPage() {
        return pageNum > 1;
    }

    // 判断是否有下一页
    public boolean hasNextPage() {
        return pageNum < totalPages;
    }
}
