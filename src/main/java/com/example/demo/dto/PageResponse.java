package com.example.demo.dto;

import java.util.List;

/**
 * 分页响应DTO
 */
public class PageResponse<T> {
    private List<T> content;      // 数据列表
    private Integer page;          // 当前页码
    private Integer size;          // 每页大小
    private Long total;            // 总记录数
    private Integer totalPages;    // 总页数

    public PageResponse() {
    }

    public PageResponse(List<T> content, Integer page, Integer size, Long total) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}

