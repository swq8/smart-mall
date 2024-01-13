package smart.dto;

public class OrderQueryDto extends GeneralQueryDto{
    Long status;

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
}
