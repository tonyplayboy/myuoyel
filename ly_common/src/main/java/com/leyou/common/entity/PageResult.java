package com.leyou.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageResult<T> {
    private Long total;
    private Long totalPage;
    private List<T> items;
    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

}
