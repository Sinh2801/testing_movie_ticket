package com.movieticketbooking.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedResponse<T> {
    private List<T> content;      // Danh sách dữ liệu của trang hiện tại
    private int pageNumber;       // Số trang hiện tại
    private int pageSize;         // Số lượng phần tử trên mỗi trang
    private long totalElements;   // Tổng số phần tử dưới Database
    private int totalPages;       // Tổng số trang
    private boolean isFirst;      // Có phải trang đầu tiên không
    private boolean isLast;       // Có phải trang cuối cùng không

    public PagedResponse() {}

    public PagedResponse(Page<T> page) {
        this.content = page.getContent();
        // Cộng thêm 1 để hiển thị số trang từ 1 giống cấu hình PageableConfig
        this.pageNumber = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
    }

    public PagedResponse(List<T> content, Page<T> page) {
        this.content = content;
        // Cộng thêm 1 để hiển thị số trang từ 1 giống cấu hình PageableConfig
        this.pageNumber = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
    }
}
