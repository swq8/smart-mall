package smart.dto;

public class OrderQueryDto extends GeneralQueryDto{
    Long deleted;
    Long status;

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
}
