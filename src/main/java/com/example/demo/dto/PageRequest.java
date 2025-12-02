package com.example.demo.dto;

/**
 * 分页请求DTO
 */
public class PageRequest {
    private Integer page = 1;  // 页码，从1开始
    private Integer size = 10;  // 每页大小，默认10

    public PageRequest() {
    }

    public PageRequest(Integer page, Integer size) {
        this.page = page != null && page > 0 ? page : 1;
        this.size = size != null && size > 0 ? size : 10;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size != null && size > 0 ? size : 10;
    }

    /**
     * 获取偏移量（用于SQL查询）
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }
}

