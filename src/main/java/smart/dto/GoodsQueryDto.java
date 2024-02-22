package smart.dto;

public class GoodsQueryDto extends GeneralQueryDto{
    Boolean onSell;

    public Boolean getOnSell() {
        return onSell;
    }

    public void setOnSell(Boolean onSell) {
        this.onSell = onSell;
    }
}
