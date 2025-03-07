package com.taibiex.stakingservice.dto;


import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 单币质押/解质押事件记录
 */


@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTO {

    @Builder.Default
    private Integer pageSize = 10;

    @Builder.Default
    private Integer pageNumber = 1;

    @Hidden
    public Integer getPageNumber() {

        if (null == pageNumber) {
            pageNumber = 1;
        } else {
            pageNumber = pageNumber < 1 ? 1 : pageNumber;
        }
        return pageNumber;
    }

    @Hidden
    private Integer getPageSize() {
        if (null == pageSize || pageSize <= 0) {
            pageSize = 10;
        } else {
            pageSize = pageSize > 100 ? 10 : pageSize;
        }
        return pageSize;
    }

    @Hidden
    private int getOffset(){
        long offset = Math.max(0, (long) (getPageNumber() - 1) * getPageSize());
        return Math.toIntExact(offset);
    }

    @Hidden
    public Pageable getPageable()
    {
        return getPageableBySort(Sort.Direction.DESC,"lastUpdateTime");
    }

    @Hidden
    public Pageable getPageableBySort(Sort.Direction direction, String... orderProperties)
    {
        Sort sort = Sort.by(direction, orderProperties); //创建时间降序排序

        //PageRequest 中 pageNumber 从0开始， 传入 0 表示第一页，传入 1 表示第二页。
        //
        int pageIndex = getPageablePageIndex(); //getPageNumber() - 1;
        int pageSize  = getPageSize();
        return PageRequest.of(pageIndex, pageSize, sort);
    }

    @Hidden
    public int getPageablePageIndex()
    {   //PageRequest 中 pageNumber 从0开始， 传入 0 表示第一页，传入 1 表示第二页。
        //
        int pageIndex = getPageNumber() - 1;
        return pageIndex;
    }

    @Hidden
    public Integer getPageablePageSize() {
        return getPageSize();
    }
}

