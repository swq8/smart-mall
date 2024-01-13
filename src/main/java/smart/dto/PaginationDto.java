package smart.dto;

import jakarta.validation.constraints.Max;

public class PaginationDto extends BaseDto {
    Long page;

    @Max(200)
    Long pageSize;

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
