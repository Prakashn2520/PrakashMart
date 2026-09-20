package com.prakash.prakashmart.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic container for paginated query results.
 * Contains page information, total counts, and payload data.
 *
 * @param <T> Item type
 */
public class PaginatedResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int currentPage;
    private int pageSize;
    private long totalResults;
    private int totalPages;
    private List<T> data;

    public PaginatedResult() {
        this.data = Collections.emptyList();
    }

    public PaginatedResult(int currentPage, int pageSize, long totalResults, int totalPages, List<T> data) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
        this.data = data != null ? data : Collections.emptyList();
    }

    public static <T> PaginatedResult<T> of(List<T> data, int page, int size, long total) {
        int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PaginatedResult<>(page, size, total, pages, data);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public long getTotalElements() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data != null ? data : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "PaginatedResult{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalResults=" + totalResults +
                ", totalPages=" + totalPages +
                ", dataCount=" + (data != null ? data.size() : 0) +
                '}';
    }
}
